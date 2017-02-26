package org.near.kodirelayremote;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class Sender{
    static final String ON = "ON";
    static final String OFF = "OFF";
    static final String END = "END";
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
        final String PRE_MSG = "MSG:";
        connect();
        out.writeUTF(PRE_MSG + msg);
    }

}
