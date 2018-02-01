package relay.control.gpio.android.sockets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

    public Socket connect(String address, int port) throws IOException {
        Socket socket = new Socket(address, port);
        return connect(socket);
    }

    public Socket connect(Socket socket) throws IOException {
        if(socket != null && socket.isConnected()){
            this.socket = socket;
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Sender connected");
        }
        return socket;
    }

    public void sendMessage(String msg) throws IOException{
        final String PRE_MSG = ":";
        if(socket.isConnected()) {
            String msgToSend = PRE_MSG + msg;
            out.writeUTF(msgToSend);
            System.out.println("MSG: " + msgToSend);
        }
    }
}
