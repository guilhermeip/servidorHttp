/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package http;

/**
 *
 * @author a1764543
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {

    static enum HttpMethods {
        GET, POST, HEAD, DELETE, TRACE, CONNECT, OPTIONS
    };

    public HttpMethods httpMethod;
    public String path;
    public byte httpMajorVersion;
    public byte httpMinorVersion;

    public Map<String, String> params = new HashMap<String, String>();
    public Map<String, String> cookies = new HashMap<String, String>();
    public Map<String, String> headerFields = new HashMap<String, String>();

    public HttpRequest(InputStream input) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        readFirstLine(br);
        readHeader(br);
    }

    private void readFirstLine(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        if (firstLine != null) {
            System.out.println("DEBUG: First line of request: " + firstLine);

            String[] parts = firstLine.split(" ");
            httpMethod = HttpMethods.valueOf(parts[0]);
            path = URLDecoder.decode(parts[1], "UTF-8");
            Pattern pattern = Pattern.compile("(\\?|\\&)([^=]+)=([^&]+)");
            Matcher matcher = pattern.matcher(path);
            while (matcher.find()) {
                path = path.replaceAll("(\\?|\\&).*", "");
                params.put(matcher.group(2), matcher.group(3));
            }
            // HTTP/1.1
            httpMajorVersion = Byte.parseByte("" + parts[2].charAt(5));
            httpMinorVersion = Byte.parseByte("" + parts[2].charAt(7));
        }
    }

    private String getHeaderFieldsToString() {
        String _headerFields = "";

        for (Map.Entry<String, String> entry : headerFields.entrySet()) {
            _headerFields += entry.getKey();
            _headerFields += ": ";
            _headerFields += entry.getValue();
            _headerFields += "\r\n";

        }
        return _headerFields;
    }

    private String getParamsToString() {
        String _params = "";
        int size = params.size(), i = 0;
        if (size > 0) {
            _params += "?";
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            _params += entry.getKey();
            _params += "=";
            _params += entry.getValue();
            if (i + 1 < size) {
                _params += "&";
            }
            i++;

        }
        return _params;
    }

    @Override
    public String toString() {
        String request = "";
        String parametros = getParamsToString();
        String HttpVersion = "HTTP/" + Byte.toString(httpMajorVersion) + "." + Byte.toString(httpMinorVersion);

        String firstLine = httpMethod.name() + " " + path + parametros + " " + HttpVersion + "\r\n";
        String headerFields = getHeaderFieldsToString() + "\r\n";

        request = firstLine + headerFields;

        return request;
    }

    private void readHeader(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.equals("")) {
            System.out.println("DEBUG: Header line: " + line);
            String[] parts = line.split(":");
            String key = parts[0];
            String value = parts[1].trim(); // Remove SP. LWS not supported yet!
            if (key.equals("Cookie")) {
                String[] partsCookie1 = value.split(";");
                for (String s : partsCookie1) {
                    String[] partsCookie2 = s.split("=");
                    String keyCookie = partsCookie2[0];
                    String valueCookie = partsCookie2[1];
                    cookies.put(keyCookie, valueCookie);
                }
            } else {
                headerFields.put(key, value);
            }
        }
    }
}
