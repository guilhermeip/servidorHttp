package body;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author a1764543
 */
public class HtmlBody {

    private StringBuilder body;
    private StringBuilder header;

    public HtmlBody() {
        this.body = new StringBuilder();
        this.header = new StringBuilder();
        this.header.append("<meta charset=\"UTF-8\">");
    }

    public void adicionarTagBody(String tag) {
        this.body.append(tag);
    }

    public void adicionarTagHeader(String tag) {
        this.header.append(tag);
    }

    public String retornarHtmlCompleto() {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + header.toString()
                + "<title>Servidor Http</title>"
                + "</head>"
                + "<body>"
                + body.toString()
                + "<script src='/guilherme/Documentos/Web/sort.js'></script>"
                + "</body>"
                + "</html>";
    }
}
