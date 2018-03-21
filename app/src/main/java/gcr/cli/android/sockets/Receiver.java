package gcr.cli.android.sockets;

import android.os.Bundle;
import android.os.ResultReceiver;

import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import gcr.cli.android.services.ReceiverService;

public class Receiver extends Observable implements Runnable {

    public static final String KEY_CONNECTION = "KEY_CONNECTION";
    public static final String EXTRA_RELAYS_JSON = "EXTRA_RELAYS_JSON";

    private Socket socket;
    private int port;
    private ResultReceiver resultReceiver;

    public Receiver(int port, ResultReceiver resultReceiver){
        this.port = port;
        this.resultReceiver = resultReceiver;
        this.socket = null;
    }

    @Override
    public void run() {
        DataInputStream in;
        socket = getNewSocket();
        if(socket == null) {
            return;
        }
        try {
            in = new DataInputStream(socket.getInputStream());
            while (socket != null && !socket.isClosed()){
                System.out.println("Receiver: a la escucha ..." + this);
                readAndSendResponse(in);
                System.out.println("Receiver: Leído from ..." + this);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            closeSocket();
        }

        System.out.println("Receiver finished... " + this);
    }

    private Socket getNewSocket() {
        Socket newSocket = null;
        ServerSocket serverSocket = null;
        if(port < 0 || resultReceiver == null) {
            sendConnectionStatus(ConnectionStatus.RECEIVER_REFUSED);
            return null;
        }
        try {
            sendConnectionStatus(ConnectionStatus.RECEIVER_START);
            serverSocket = new ServerSocket(port);
            sendConnectionStatus(ConnectionStatus.RECEIVER_WAITING_FOR_SENDER);
            System.out.println("------> 2 " + this);
            serverSocket.setSoTimeout(2000); // milliseconds
            newSocket = serverSocket.accept();
            System.out.println("Receiver connected..." + newSocket);
            sendConnectionStatus(ConnectionStatus.RECEIVER_CONNECTED);
        } catch (SocketTimeoutException e) {
            sendConnectionStatus(ConnectionStatus.RECEIVER_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
            sendConnectionStatus(ConnectionStatus.RECEIVER_REFUSED);
        } finally {
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return newSocket;
    }

    /**
     * Require: the socket is connected
     * @param in
     * @throws IOException
     */
    private void readAndSendResponse(DataInputStream in) throws IOException {
        byte[] buff = new byte[200];
        System.out.println("Esperando respuesta...");
        if(in.read(buff) > 0){
            String msgBack = new String(buff,"UTF-8").trim();
            System.out.println("Recibido Mensaje: " + msgBack);
            sendRelaysReceived(msgBack);
            return;
        }
        closeSocket();
    }

    private void closeSocket() {
        if(socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                socket = null;
            }
            sendConnectionStatus(ConnectionStatus.RECEIVER_DISCONNECTED);
        }
    }

    private void sendRelaysReceived(String relaysJson){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ReceiverService.KEY_RECEIVED, true);
        bundle.putString(EXTRA_RELAYS_JSON, relaysJson);
        resultReceiver.send(0, bundle);
    }

    private void sendConnectionStatus(ConnectionStatus resultCode){
        Bundle bundle = new Bundle();
        bundle.putBoolean(Receiver.KEY_CONNECTION, true);
        resultReceiver.send(resultCode.ordinal(), bundle);
    }

}
