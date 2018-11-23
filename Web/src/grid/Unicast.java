/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class Unicast implements Runnable{
    Socket cliente;
    
    public Unicast(Socket cliente){
        this.cliente = cliente;
    }
    
    @Override
    public void run(){
        try {
            DataInputStream in = new DataInputStream(cliente.getInputStream());
            String buffer = in.readUTF();
            if(buffer.matches("AD\\d\\d\\d\\d\n")){
                String port = buffer.replaceAll("AD|\n", "");
                String ip = cliente.getInetAddress().getHostAddress();
                System.out.println("AD recebido, inserindo IP:porta-> "+ip+":"+port);
                HttpGrid.getInstance().insertIpPorta(ip+":"+port);
            }else{
                System.out.println("Resposta não foi AD\\d\\d\\d\\d\\\n");
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Unicast.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.cliente.close();
        } catch (IOException ex) {
            Logger.getLogger(Unicast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
