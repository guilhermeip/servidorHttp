/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author a1764543
 */
import http.HttpProtocol;
import http.HttpRequest;
import http.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ServerWorker implements Runnable {

    private Socket socket;

    public ServerWorker(Socket s) {
        this.socket = s;
    }

    public void run() {
        try {
            HttpProtocol httpProtocol = new HttpProtocol();

            HttpRequest request = new HttpRequest(socket.getInputStream());
            OutputStream out = socket.getOutputStream();
            HttpResponse response = httpProtocol.processMsg(out, request);
            response.write(out);
            Server.decrementAtendendo();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
