package org.near.kodirelayremote;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;

class Receiver extends Observable implements Runnable{

    private Socket socket;
    private String status;

    Receiver(Socket socket){
        this.socket = socket;
        status = "";
    }

    @Override
    public void run() {
        DataInputStream in;
        try {
            in = new DataInputStream(socket.getInputStream());
            do {
                readResponse(in);
            } while (!status.startsWith("END"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void readResponse(DataInputStream in) throws IOException {
        if(socket.isConnected()){
            byte[] buff = new byte[1];
            if(in.read(buff) > 0){
                setStatus(new String(buff,"UTF-8").trim());
            }
        }
    }

    private void setStatus(String status) {
        this.status = status.compareTo("1") == 0? Sender.ON : Sender.OFF;
        setChanged();
        notifyObservers(this.status);
    }

}
