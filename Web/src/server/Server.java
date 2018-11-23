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
import grid.HttpGrid;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {

    public static int port = 9090;
    public static String data = "";
    public static int contadorRequisicoesAtendidas = 0;
    public static int contadorRequisicoesAtendendo = 0;
    public static String virtualPath = "/virtual/";

    public static synchronized void incrementAtendidas() {
        Server.contadorRequisicoesAtendidas++;
    }

    public static synchronized void incrementAtendendo() {
        Server.contadorRequisicoesAtendendo++;
    }

    public static synchronized void decrementAtendendo() {
        Server.contadorRequisicoesAtendendo--;
    }

    public static void main(String args[]) throws Exception {
        ServerSocket server = new ServerSocket(Server.port);
        DateFormat df = new SimpleDateFormat("dd/M/yyyy HH:mm");
        Server.data = df.format(new Date());
        System.out.println("Servidor ouvindo na porta " + Server.port);
        HttpGrid.getInstance().start();
        while (server.isBound()) {
            Socket s = server.accept();
            Server.incrementAtendendo();
            Server.incrementAtendidas();
            System.out.println("Cliente conectado! Criando Thread Worker");
            new Thread(new ServerWorker(s)).start();
        }
        server.close();
    }
}
