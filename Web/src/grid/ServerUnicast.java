/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class ServerUnicast implements Runnable {

    private int port;

    public ServerUnicast(int UnicastPort) {
        this.port = UnicastPort;
    }

    @Override
    public void run() {
        try {
            System.out.println("Servidor Unicast ouvindo na porta - "+port);
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket cliente = server.accept();
                new Thread(new Unicast(cliente)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerUnicast.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
