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
            while (socket.isConnected() && !status.startsWith("END")){
                readResponse(in);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Require: the socket is connected
     * @param in
     * @throws IOException
     */
    private void readResponse(DataInputStream in) throws IOException {
        byte[] buff = new byte[1];
        System.out.println("Esperando respuesta...");
        if(in.read(buff) > 0){
            setStatus(new String(buff,"UTF-8").trim());
            System.out.println("Recibido STATUS: " + status);
        }
    }

    private void setStatus(String status) {
        this.status = status.compareTo("1") == 0? Sender.ON : Sender.OFF;
        setChanged();
        notifyObservers(this.status);
    }

}
