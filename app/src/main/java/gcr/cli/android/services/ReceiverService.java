package gcr.cli.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import gcr.cli.android.sockets.Receiver;

public class ReceiverService extends IntentService implements Observer {

    public static final String CANONICAL_NAME = ReceiverService.class.getCanonicalName();
    public static final String ACTION_START = CANONICAL_NAME + ".ACTION_START";
    public static final String ACTION_CONNECTED = CANONICAL_NAME + ".ACTION_CONNECTED";
    public static final String ACTION_CONNECTION_WAITING = CANONICAL_NAME + ".ACTION_CONNECTION_WAITING";
    public static final String EXTRA = CANONICAL_NAME + ".EXTRA";
    public static final String ACTION_RECEIVED = CANONICAL_NAME + ".ACTION_RECEIVED";

    private Socket receiverSocket;

    public ReceiverService() {
        super("ReceiverService");
    }

    public static void start(int port, Context clientContext) {
        Intent requestIntent = new Intent(clientContext, ReceiverService.class);
        requestIntent.setAction(ACTION_START);
        requestIntent.putExtra(EXTRA, port);
        clientContext.startService(requestIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int port = intent.getExtras().getInt(EXTRA);
        try {
            final ServerSocket serverSocket = new ServerSocket();
            // In case that the connection was closed but still bounded (Very rare case)
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));

            sendConnectionWaitingBroadCast();
            receiverSocket = serverSocket.accept();
            System.out.println("Receiver connected...");

            sendConnectedBroadCast();
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
        sendReceivedBroadCast(relaysJson);
    }

    @Override
    public boolean stopService(Intent intent) {
        if(!receiverSocket.isClosed()) {
            try {
                receiverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.stopService(intent);
    }

    private void sendConnectionWaitingBroadCast(){
        LocalBroadcastManager broadcastManager = LocalBroadcastManager
                .getInstance(ReceiverService.this);
        Intent resultIntent = new Intent(ACTION_CONNECTION_WAITING);
        broadcastManager.sendBroadcast(resultIntent);
    }

    private void sendConnectedBroadCast(){
        LocalBroadcastManager broadcastManager = LocalBroadcastManager
                .getInstance(ReceiverService.this);
        Intent resultIntent = new Intent(ACTION_CONNECTED);
        broadcastManager.sendBroadcast(resultIntent);
    }

    private void sendReceivedBroadCast(String relaysJson){
        LocalBroadcastManager broadcastManager = LocalBroadcastManager
                .getInstance(ReceiverService.this);
        Intent resultIntent = new Intent(ACTION_RECEIVED);
        resultIntent.putExtra(EXTRA, relaysJson);
        broadcastManager.sendBroadcast(resultIntent);
    }
}
