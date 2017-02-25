package org.near.kodirelayremote;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

class Sender{

    static final String ON = "ON";
    static final String OFF = "OFF";
    private final String PRE_MSG = "MSG:";
    private Socket socket;
    private DataOutputStream out;
    private String address;
    private int port;

    Sender(String address, int port) {
        this.address = address;
        this.port = port;
    }

    void connect() throws IOException{
        if(socket == null || !socket.isConnected()){
            socket = new Socket(address, port);
            out = new DataOutputStream(socket.getOutputStream());
        }
    }

    void sendMessage(String msg) throws IOException{
        connect();
        out.writeUTF(PRE_MSG + msg);
    }

}
