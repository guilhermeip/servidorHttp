/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;

/**
 *
 * @author guilherme
 */
public class HttpGrid {

    private static HttpGrid grid = null;
    private LinkedList<String> listaIpPortaHttp;
    private final int portaUnicast = 9091;
    private final int portaHttp = Server.port;

    public HttpGrid() {
        listaIpPortaHttp = new LinkedList<>();
    }

    public synchronized static HttpGrid getInstance() {
        if (grid == null) {
            grid = new HttpGrid();
        }
        return grid;
    }

    public synchronized void start() {
        new Thread(new ServerUnicast(portaUnicast)).start();
        try {
            BroadCast.broadcast("SD" + portaUnicast + " " + portaHttp + "\n", InetAddress.getByName("255.255.255.255"));
        } catch (UnknownHostException ex) {
            Logger.getLogger(HttpGrid.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HttpGrid.class.getName()).log(Level.SEVERE, null, ex);
        }
        new Thread(new ManegerSD()).start();
    }
    
    public synchronized boolean existsIp(String ipPorta){
        return grid.listaIpPortaHttp.contains(ipPorta);
    }
    public synchronized void insertIpPorta(String ipPorta) {
        grid.listaIpPortaHttp.add(ipPorta);
    }

    public synchronized boolean removerIpPorta(String ipPorta) {
        return grid.listaIpPortaHttp.remove(ipPorta);
    }
    

    public synchronized LinkedList<String> getListaIpPortaHttp() {
        return listaIpPortaHttp;
    }

    public void setListaIpPortaHttp(LinkedList<String> listaIpPortaHttp) {
        this.listaIpPortaHttp = listaIpPortaHttp;
    }
    
}
