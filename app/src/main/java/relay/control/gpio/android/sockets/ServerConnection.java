package relay.control.gpio.android.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import relay.control.gpio.android.models.IServerModel;

public class ServerConnection {

    private Receiver receiver;
    private Sender sender;
    private int port;
    private String address;
    private List<Observer> observers;

    public ServerConnection(String address, int port) {
        sender = new Sender(address, port);
        this.port = port;
        this.address = address;
        observers = new ArrayList<>();
    }

    public void connect() {
        new Thread(new Runnable() {// TODO use AsynkTask
            public void run() {
                try {
                    sender.connect();
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket socket = serverSocket.accept();

                    receiver = new Receiver(socket);

                    for (Observer o : observers) {
                        receiver.addObserver(o);
                    }

                    Thread t = new Thread(receiver);
                    t.start();
                } catch (IOException e) {
                    //Toast.makeText(MainActivity.this, "Connection fail: "+e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    System.out.println("ERROR CONNECTION.");
                }
            }
        }).start();
    }

    public void addReceiverObserver(Observer o) {
        observers.add(o);
    }

    public Sender getSender() {
        return sender;
    }
}
