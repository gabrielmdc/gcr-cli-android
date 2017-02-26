package org.near.kodirelayremote;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

class Receiver extends Observable implements Runnable{

    private String status;
    private int port;

    Receiver(int port){
        this.port = port;
        status = "";
    }

    @Override
    public void run() {
        DataInputStream in;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();

            in = new DataInputStream(socket.getInputStream());
            while (socket.isConnected() && !status.startsWith(Sender.END)){
                readResponse(in);
            }
        } catch (IOException e) {
            e.printStackTrace();// TODO Manage this
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();// TODO Manage this
                }
            }
        }
    }

    /**
     * Require: the socket is connected
     * @param in DataInputStream
     * @throws IOException
     */
    private void readResponse(DataInputStream in) throws IOException {
        byte[] buff = new byte[1];
        if(in.read(buff) > 0){
            setStatus(new String(buff,"UTF-8").trim());
        }
    }

    private void setStatus(String status) {
        this.status = status.compareTo("1") == 0? Sender.ON : Sender.OFF;
        setChanged();
        notifyObservers(this.status);
    }

}
