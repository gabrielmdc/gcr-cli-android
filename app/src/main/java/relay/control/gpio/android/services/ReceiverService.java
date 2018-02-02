package relay.control.gpio.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Observable;
import java.util.Observer;

import relay.control.gpio.android.sockets.Receiver;

public class ReceiverService extends IntentService implements Observer {

    public static final String CANONICAL_NAME = ReceiverService.class.getCanonicalName();
    public static final String ACTION_TO = CANONICAL_NAME + ".ACTION_TO";
    public static final String EXTRA_TARGET = CANONICAL_NAME + ".EXTRA_TARGET";
    public static final String EVENT_FINISHED = CANONICAL_NAME + ".FINISHED";

    private Socket receiverSocket;

    public ReceiverService() {
        super("ReceiverService");
    }

    public static void start(int port, Context clientContext) {
        Intent requestIntent = new Intent(clientContext, ReceiverService.class);
        requestIntent.setAction(ACTION_TO);
        requestIntent.putExtra(EXTRA_TARGET, port);
        clientContext.startService(requestIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int port = intent.getExtras().getInt(EXTRA_TARGET);
        try {
            final ServerSocket serverSocket = new ServerSocket();
            // In case that the connection was closed but still bounded (Very rare case)
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));

            receiverSocket = serverSocket.accept();
            Receiver receiver = new Receiver(receiverSocket);
            receiver.addObserver(this);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        String relaysJson = (String)arg;
        sendBroadCast(relaysJson);
    }

    @Override
    public boolean stopService(Intent intent) {
        if(receiverSocket.isConnected()) {
            try {
                receiverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.stopService(intent);
    }

    private void sendBroadCast(String relaysJson){
        LocalBroadcastManager broadcastManager = LocalBroadcastManager
                .getInstance(ReceiverService.this);
        Intent resultIntent = new Intent(EVENT_FINISHED);
        resultIntent.putExtra(EXTRA_TARGET, relaysJson);
        broadcastManager.sendBroadcast(resultIntent);
    }
}
