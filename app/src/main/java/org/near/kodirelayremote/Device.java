package org.near.kodirelayremote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Device {
    private String address;
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Device(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void connect(String address, int port){
        this.address = address;
        this.port = port;
        connect();
    }

    /**
     *
     * @return String status
     */
    public void connect(){
        int attempsCount = 10;
        while(socket == null || !socket.isConnected()){
            try{
                if(attempsCount < 10){
                    Thread.sleep(2000);
                }
                socket = new Socket(address, port);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

            }catch(Exception e){
//				System.err.println("[" + address + "]" + e.getMessage());
                if(attempsCount-- == 0){
                    System.exit(1);
                }
            }
        }
    }

    private String getStatus() {
        String status = null;
        try {
            if(socket.isConnected()){
                byte[] buff = new byte[1];
                in.read(buff);
                status = new String(buff,"UTF-8").trim();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            socket = null;
            in = null;
            out = null;
        }
        return status;
    }

    public String sendAction(String action){
        String status = null;
        try {
//			System.out.println("Enviando action: " + action);
            out.writeUTF("MSG:" + action);
            status = getStatus();
//			System.out.println("Status: " + status);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return status;
    }
}
