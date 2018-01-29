package relay.control.gpio.android.sockets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Protocol:
 * ------------------
 * :END
 * :STATUS:id_gpio,...:'ON' | 'OFF'
 * :GET:ALL | id_gpio
 * :EDIT:id_gpio:name:port
 * :ADD:name:port
 * :DELETE:id_gpio
 * ------------------
 */
public class Sender {

    public static final String STATUS_ON = "ON";
    public static final String STATUS_OFF = "OFF";

    private Socket socket;
    private DataOutputStream out;
    private String address;
    private int port;

    Sender(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void connect() throws UnknownHostException, IOException{
        if(socket == null || !socket.isConnected()){
            socket = new Socket(address, port);
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Sender connected");
        }
    }

    public void sendMessage(String msg) throws UnknownHostException, IOException{
        final String PRE_MSG = ":";
        connect();
        String msgToSend = PRE_MSG + msg;
        out.writeUTF(msgToSend);
        System.out.println("MSG: " + msgToSend);
    }
}
