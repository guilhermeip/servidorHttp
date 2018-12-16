/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package http;

import body.HtmlBody;
import grid.HttpGrid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import server.Server;

public class HttpProtocol {

    private String baseDir = "/var/www/";
    private OutputStream clienteOut;

    private boolean isAuthenticate(HttpRequest msg) {

        String valueAuthenticate = msg.headerFields.get("Authorization");

        if (valueAuthenticate != null) {
            String[] parts = valueAuthenticate.split(" ");
            String senha = new String(Base64.getDecoder().decode(parts[1]));
            if (senha.equals("admin:admin")) {
                return true;
            }
        }
        return false;
    }

    public HttpResponse processMsg(OutputStream out, HttpRequest msg) {
        HttpResponse response = new HttpResponse();
        this.clienteOut = out;

        switch (msg.httpMethod) {
            case GET:
                if (!isAuthenticate(msg)) {
                    return response401(msg);
                }
                response = processGET(msg);
                break;
            case OPTIONS:
                response = processOPTIONS(msg);
                break;
            default:
                response.statusCode = "501";
                response.reasonPhrase = "Not Implemented";
        }

        return response;
    }

    private HttpResponse processGET(HttpRequest msg) {
        HttpResponse response = new HttpResponse();
        System.out.println(msg.path);
        File file = new File(baseDir + msg.path);

        if (msg.path.startsWith(Server.virtualPath)) {
            return processVirtual(msg);
        }
        if (file.exists()) {
            response.headerFields.put("Access-Control-Allow-Origin", "*");
            response.statusCode = "200";
            response.reasonPhrase = "OK";
            /*process Cookie*/
            processCookie(msg, response);

            if (file.isDirectory()) {
                if (!msg.path.endsWith("/")) {
                    return response301(msg);
                }
                byte[] retorno = null;
                if (msg.headerFields.containsKey("X-Requested-With")) {
                    retorno = returnJsonDirectory(msg, file).getBytes();
                    response.headerFields.put("Content-Type", "application/json");
                } else {
                    try {
                        retorno = Files.readAllBytes(Paths.get(baseDir + "index.html"));
                        response.headerFields.put("Content-Type", "text/html");
                    } catch (IOException ex) {
                        Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                response.body = retorno;
                response.headerFields.put("Content-Length", "" + response.body.length);
            } else {
                if (file.canExecute()) {
                    try {
                        Runtime rt = Runtime.getRuntime();
                        Process processo = rt.exec(file.getAbsolutePath());
                        byte[] taxa = new byte[1024];
                        InputStream in = processo.getInputStream();
                        int read = 0;
                        while ((read = in.read(taxa)) > 0) {
                            clienteOut.write(taxa);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (file.getName().endsWith(".dyn")) {
                    try {
                        String dyn = new String(Files.readAllBytes(Paths.get(baseDir + msg.path)));
                        dyn = processDyn(dyn, msg);
                        response.body = dyn.getBytes();
                        response.headerFields.put("Content-Length", "" + response.body.length);
                        response.headerFields.put("Content-Type", "text/html");
                    } catch (IOException ex) {
                        Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        response.headerFields.put("Content-Length", "" + file.length());
                        if (msg.params.containsKey("download")) {
                            response.headerFields.put("Content-Disposition", "attachment;");
                        }
                        response.headerFields.put("Content-Type", Files.probeContentType(file.toPath()));
                        response.body = Files.readAllBytes(Paths.get(baseDir + msg.path));
                    } catch (IOException e) {
                        return response500(msg, e);
                    }
                }
            }
            return response;
        } else {
            return response404(msg);
        }
    }

    private HttpResponse response404(HttpRequest msg) {
        HttpResponse response = new HttpResponse();

        if (!msg.headerFields.containsKey("FromServer")) {
            msg.headerFields.put("FromServer", "True");
            System.out.println("Quantidade de pessoas na listaHTTP:" + HttpGrid.getInstance().getListaIpPortaHttp().size());
            for (String ipPorta : HttpGrid.getInstance().getListaIpPortaHttp()) {
                String[] parts = ipPorta.split(":");
                String ip = parts[0];
                String porta = parts[1];
                String request = msg.toString();
                try {
                    Socket s = new Socket(InetAddress.getByName(ip), Integer.valueOf(porta));
                    System.out.println("Enviando requisicao para amigo GRID\nRequisição a ser enviada:\n" + request);
                    s.getOutputStream().write(request.getBytes());

                    InputStream in = s.getInputStream();
                    String firstLine = readLine(in);
                    System.out.println("firstLine: " + firstLine);
                    int status = Integer.valueOf(firstLine.split(" ")[1]);

                    if (status < 400) {
                        clienteOut.write(firstLine.getBytes());

                        byte taxa[] = new byte[1024];
                        int read = 0;
                        while ((read = in.read(taxa)) > 0) {
                            clienteOut.write(taxa, 0, read);
                        }
                        clienteOut.close();
                        break;
                    }
                    s.close();

                } catch (UnknownHostException ex) {
                    HttpGrid.getInstance().removerIpPorta(ip + ":" + porta);
                    Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        response.statusCode = "404";
        response.reasonPhrase = "Resource not found!";

        try {
            response.body = Files.readAllBytes(Paths.get(baseDir + "guilherme/Documentos/Web/angular/dist/web/index.html"));
        } catch (IOException ex) {
            Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        response.headerFields.put("Content-Lenght", "" + response.body.length);
        return response;
    }

    private HttpResponse response500(HttpRequest msg, Exception e) {
        HttpResponse response = new HttpResponse();
        response.statusCode = "500";
        response.reasonPhrase = "Internal Error";

        /* source from http://google.com/notFound.html*/
        String pageSource = "<!DOCTYPE html>\n<html lang=en>\n  <meta charset=utf-8>\n  <meta name=viewport content=\"initial-scale=1, minimum-scale=1, width=device-width\">\n  <title>Internal Error 500!</title>\n  <style>\n    *{margin:0;padding:0}html,code{font:15px/22px arial,sans-serif}html{background:#fff;color:#222;padding:15px}body{margin:7% auto 0;max-width:390px;min-height:180px;padding:30px 0 15px}* > body{background:url(//www.google.com/images/errors/robot.png) 100% 5px no-repeat;padding-right:205px}p{margin:11px 0 22px;overflow:hidden}ins{color:#777;text-decoration:none}a img{border:0}@media screen and (max-width:772px){body{background:none;margin-top:0;max-width:none;padding-right:0}}#logo{background:url(//www.google.com/images/branding/googlelogo/1x/googlelogo_color_150x54dp.png) no-repeat;margin-left:-5px}@media only screen and (min-resolution:192dpi){#logo{background:url(//www.google.com/images/branding/googlelogo/2x/googlelogo_color_150x54dp.png) no-repeat 0% 0%/100% 100%;-moz-border-image:url(//www.google.com/images/branding/googlelogo/2x/googlelogo_color_150x54dp.png) 0}}@media only screen and (-webkit-min-device-pixel-ratio:2){#logo{background:url(//www.google.com/images/branding/googlelogo/2x/googlelogo_color_150x54dp.png) no-repeat;-webkit-background-size:100% 100%}}#logo{display:inline-block;height:54px;width:150px}\n  </style>\n  <a href=//www.google.com/><span id=logo aria-label=Google></span></a>\n  <p><b>404.</b> <ins>That’s an internal error.</ins>\n  <p>The requested URL <code>"
                + msg.path
                + "</code> generate the following error:<p>"
                + e.getMessage()
                + ".  <ins>That’s all we know.</ins>";
        response.body = pageSource.getBytes();
        return response;
    }

    private String returnJsonDirectory(HttpRequest msg, File file) {
        File[] listFiles = file.listFiles();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"nameDir\":\"" + file.getName() + "\"");
        json.append(",\n");
        json.append("\"lengthDir\":\"" + listFiles.length + "\"");
        json.append(",\n");
        json.append("\"listDir\":[");

        String name = "";
        String tam = "";
        String last = "";
        String ex = "";
        int i = 0;
        for (File f : listFiles) {
            json.append("{");
            json.append("\"name\":\"" + f.getName() + "\"");
            json.append(",\n");
            tam = "";
            ex = "no";
            if (f.isFile()) {

                /*Tamanho*/
                tam = String.valueOf(f.length());
                /*é executavel?*/
                if (f.canExecute()) {
                    ex = "yes";
                }
            }
            json.append("\"length\":\"" + tam + "\"");
            json.append(",\n");
            json.append("\"canExecute\":\"" + ex + "\"");
            json.append(",\n");
            json.append("\"lastModified\":\"" + f.lastModified() + "\"");

            json.append("}");
            if (++i < listFiles.length) {
                json.append(",\n");
            }
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String retornarHtmlDirectory(HttpRequest msg, File file) {
        HtmlBody html = new HtmlBody();
        File[] listFiles = file.listFiles();
        html.adicionarTagBody("<h1> " + file.getName() + " - " + listFiles.length + " Item(s)</h1>");
        html.adicionarTagBody("<table id=\"myTable\">");
        html.adicionarTagBody("<tr>");
        html.adicionarTagBody("<th>Nome"
                + "<button onclick='sortName()'>Sort</button>"
                + "</th>");
        html.adicionarTagBody("<th>Tamanho"
                + "<button onclick='sortTam()'>Sort</button>"
                + "</th>");
        html.adicionarTagBody("<th>Última Modificação</th>");
        html.adicionarTagBody("<th>Executável</th>");
        html.adicionarTagBody("</tr>");

        if (!msg.path.equals("/")) {
            html.adicionarTagBody("<tr>");
            html.adicionarTagBody("<td>");
            html.adicionarTagBody("<a href='..'>Diretório Anterior</a>");
            html.adicionarTagBody("</td>");
            html.adicionarTagBody("<td></td><td></td><td></td>");
            html.adicionarTagBody("</tr>");
        }
        String name = "";
        String tam = "";
        String last = "";
        String ex = "";
        for (File f : listFiles) {
            html.adicionarTagBody("<tr>");
            /*Nome*/
            try {
                name = "<a href=" + URLEncoder.encode(f.getName(), "UTF-8") + ">" + f.getName() + "</a>";
            } catch (UnsupportedEncodingException ex1) {
                Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex1);
            }
            /*Last Modficad*/
            DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, new Locale("pt", "BR"));
            last = df.format(new Date(f.lastModified()));

            /*é executavel?*/
            ex = "";
            /*Tamanho*/
            tam = "";
            if (f.isFile()) {

                /*Tamanho*/
                tam = getTam(String.valueOf(f.length()));
                /*é executavel?*/
                if (f.canExecute()) {
                    ex = "X";
                }
            }
            html.adicionarTagBody("<td>" + name + "</td>" + "<td>" + tam + "</td>" + "<td>" + last + "</td>" + "<td>" + ex + "</td>");
            html.adicionarTagBody("</tr>");
        }
        html.adicionarTagBody("</table>");
        return html.retornarHtmlCompleto();
    }

    private HttpResponse response301(HttpRequest msg) {
        HttpResponse response = new HttpResponse();
        response.statusCode = "301";
        response.reasonPhrase = "Moved Permanently";
        response.headerFields.put("Location", msg.path + "/");
        return response;
    }

    private void processCookie(HttpRequest msg, HttpResponse response) {
        if (msg.cookies.get("count") != null) {
            int count = Integer.valueOf(msg.cookies.get("count"));
            response.cookies.put("count", String.valueOf((count + 1)));
        } else {
            response.cookies.put("count", "1");
        }
    }

    private HttpResponse response401(HttpRequest msg) {
        HttpResponse response = new HttpResponse();
        HtmlBody html = new HtmlBody();
        html.adicionarTagBody("<h1>Erro 401</h1>");
        html.adicionarTagBody("<p>Está pagina necessita de autentificação.</p>");
        response.statusCode = "401";
        response.reasonPhrase = "Need Authenticate";
        response.body = html.retornarHtmlCompleto().getBytes();
        response.headerFields.put("WWW-Authenticate", "Basic realm=\"Necessario autenticacao!\"");
        response.headerFields.put("Access-Control-Allow-Origin", "*");
        response.headerFields.put("Content-Type", "text/html");
        response.headerFields.put("Content-Length", "" + response.body.length);

        return response;
    }

    private String getTam(String bytes) {
        double dBytes = Double.valueOf(bytes);
        int m = 1000;
        DecimalFormat df = new DecimalFormat("0.##");
        String byt = "Byte(s)";
        String GB = df.format(dBytes / (m * m * m)).replace(",", ".");
        String mB = df.format(dBytes / (m * m)).replace(",", ".");
        String kB = df.format(dBytes / (m)).replace(",", ".");
        return Double.valueOf(GB) > 1 ? GB + " G" + byt : Double.valueOf(mB) > 1 ? mB + " M" + byt : Double.valueOf(kB) > 1 ? kB + " k" + byt : bytes + " " + byt;
    }

    private HttpResponse processVirtual(HttpRequest msg) {
        HttpResponse response = new HttpResponse();
        response.headerFields.put("Access-Control-Allow-Origin", "*");
        String pathVirtual = msg.path.replace(Server.virtualPath, "");
        StringBuilder json = new StringBuilder();
        switch (pathVirtual) {
            case "telemetria": {
                json.append("{");
                json.append("\"porta\": \"" + Server.port + "\",");
                json.append("\"uptime\": \"" + Server.data + "\",");
                json.append("\"atendendo\": \"" + Server.contadorRequisicoesAtendendo + "\",");
                json.append("\"atendidas\": \"" + Server.contadorRequisicoesAtendidas + "\"");
                json.append("}");
                break;
            }
            default: {
                json.append("{\"error\": \"Não possui esse recurso virtual\"}");
            }
        }

        response.headerFields.put("Content-Type", "text/html");
        response.body = json.toString().getBytes();
        response.headerFields.put("Content-Length", String.valueOf(response.body.length));
        return response;
    }

    private String processDyn(String dyn, HttpRequest msg) {
        Pattern pattern = Pattern.compile("<%[^%>]*%>");
        Matcher matcher = pattern.matcher(dyn);
        while (matcher.find()) {
            String dados = matcher.group().replaceAll("<|>|%", "").trim();
            String[] funcoes = dados.split(";");
            String retorno = "";
            for (String s : funcoes) {
                s = s.trim();
                if (s.startsWith("getParam")) {
                    retorno += "<h2>Params</h2>\n";
                    retorno += "<p>";
                    System.out.println("coutn: " + msg.params.size());
                    for (Entry<String, String> hmap : msg.params.entrySet()) {
                        retorno += hmap.getKey();
                        retorno += ": " + hmap.getValue() + ", ";
                    }
                    retorno += "</p>";
                    retorno += "\n";

                } else if (s.startsWith("date(")) {
                    String formato = s.replaceAll("date|\"|\\)|\\(", "");
                    retorno += "<h2> Data </h2>\n";
                    retorno += "<p>";
                    try {
                        SimpleDateFormat df = new SimpleDateFormat(formato, new Locale("pt", "BR"));
                        retorno += df.format(new Date());
                    } catch (NullPointerException np) {
                        retorno += "Nenhum parametro foi passado";
                    }
                    retorno += "</p>";
                    retorno += "\n";

                } else if (s.startsWith("getHeaderField(")) {
                    String argumento = s.replaceAll("getHeaderField|\\(|\\)|\"", "");
                    retorno += "<h2>HeaderField</h2>\n";
                    retorno += "<p>";
                    retorno += argumento + ": " + msg.headerFields.get(argumento);
                    retorno += "</p>";
                    retorno += "\n";
                }

            }
            dyn = dyn.replaceFirst("<%[^%>]*%>", retorno);
        }
        return dyn;
    }

    private String readLine(InputStream in) {
        String firstLine = "";
        char c = 0;
        do {
            try {
                c = (char) in.read();
                firstLine += c;
            } catch (IOException ex) {
                Logger.getLogger(HttpProtocol.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (c != '\n');
        return firstLine;
    }

    private HttpResponse processOPTIONS(HttpRequest msg) {
        HttpResponse res = new HttpResponse();
        //res.headerFields.put("Allow", "OPTIONS, GET");
        res.headerFields.put("Access-Control-Allow-Origin", "*");
        res.headerFields.put("Access-Control-Allow-Headers", "*");
        res.headerFields.put("Content-Lenght", "0");

        return res;
    }
}
