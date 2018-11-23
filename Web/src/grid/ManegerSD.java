/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;

/**
 *
 * @author guilherme
 */
public class ManegerSD implements Runnable {


    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(5554);
            byte[] buffer = new byte[15];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                if (msg.matches("SD\\d\\d\\d\\d \\d\\d\\d\\d\n")) {
                    String[] portas = msg.replaceAll("SD|\n", "").split(" ");
                    String portaUnicast = portas[0];
                    String portahttp = portas[1];
                    String ip = packet.getAddress().getHostAddress();

                    sendUnicast(ip + ":" + portaUnicast);

                    if (!HttpGrid.getInstance().existsIp(ip + ":" + portahttp)) {
                        System.out.println("Manager SD - IP nao existe, adicionando na lista");
                        HttpGrid.getInstance().insertIpPorta(ip + ":" + portahttp);
                    } else {
                        System.out.println("Já existe esse ip e porta ->" + ip + ":" + portahttp);
                    }
                } else {
                    System.out.println("Mensagem de SD não corresponde ao formato: "
                            + "SD\\d\\d\\d\\d \\d\\d\\d\\d\\\n");
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(ManegerSD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManegerSD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void sendUnicast(String ipPorta) {
        String parts[] = ipPorta.split(":");
        System.out.println("Manager SD - Sending Packet Unicast: Ip:porta-> " + ipPorta);
        try {
            Socket s = new Socket(InetAddress.getByName(parts[0]), Integer.valueOf(parts[1]));
            String sendAD = "AD" + Server.port + "\n";
            System.out.println("enviando: "+sendAD);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF(sendAD);

            s.close();
            out.close();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ManegerSD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManegerSD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
