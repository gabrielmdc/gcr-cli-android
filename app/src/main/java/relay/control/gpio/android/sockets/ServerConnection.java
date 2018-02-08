package relay.control.gpio.android.sockets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.services.ReceiverService;

public class ServerConnection extends Observable {

    private Context context;
    private Socket senderSocket;
    private ServerObservable connectionObservable;
    private ServerObservable receiverObservable;

    private String address;
    private int port;

    public ServerConnection(Context context, String address, int port) {
        this.context = context;
        mountBroadCastReceiver(this.context);
        connectionObservable = new ServerObservable();
        receiverObservable = new ServerObservable();
        this.address = address;
        this.port = port;
    }

    public void connect() throws IOException {
        ReceiverService.start(port, context);
        // TODO
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        senderConnection(address, port);
    }

    public void closeConnection() {
        try {
            senderSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent requestIntent = new Intent(context, ReceiverService.class);
        context.stopService(requestIntent);
    }

    public void sendMessage(String msg) throws IOException {
        Sender sender = new Sender();
        sender.connect(senderSocket);
        sender.sendMessage(msg);
    }

    public void addReceiverObserver(Observer o) {
        receiverObservable.addObserver(o);
    }

    public void addConnectionObserver(Observer o) {
        connectionObservable.addObserver(o);
    }

    private void mountBroadCastReceiver(Context context) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiverService.ACTION_RECEIVED);
        filter.addAction(ReceiverService.ACTION_CONNECTED);
        filter.addAction(ReceiverService.ACTION_CONNECTION_WAITING);
        ReceiverBroadCastReceiver broadCastReceiver = new ReceiverBroadCastReceiver();
        broadcastManager.registerReceiver(broadCastReceiver, filter);
    }

    private class ReceiverBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( action == ReceiverService.ACTION_CONNECTION_WAITING) {
                SenderConnectionTask t = new SenderConnectionTask(address, port);
                t.execute();
            } else if( action == ReceiverService.ACTION_CONNECTED) {
                System.out.println("Receiver connected...");
                connectionObservable.changed();
                connectionObservable.notifyObservers(ReceiverService.ACTION_CONNECTED);
            } else if(action == ReceiverService.ACTION_RECEIVED) {
                String relaysJson = intent.getStringExtra(ReceiverService.EXTRA);
                SparseArray<IRelay> relays = Receiver.getRelaysFromJsonMsg(relaysJson);
                setChanged();
                notifyObservers(relays);
//                receiverObservable.changed();
//                receiverObservable.notifyObservers(relays);
            }
        }
    }

    private class SenderConnectionTask extends AsyncTask<Void, Void, Boolean> {

        private String address;
        private int port;

        SenderConnectionTask(String address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(senderSocket == null || senderSocket.isClosed()) {
                Sender sender = new Sender();
                try {
                    senderSocket = sender.connect(address, port);
                    connectionObservable.notifyObservers(ReceiverService.ACTION_CONNECTION_WAITING);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Intent intentService = new Intent(context, ReceiverService.class);
                    context.stopService(intentService);
                }
            }
            return false;
        }
    }

    private class ServerObservable extends Observable {
        public void changed() {
            setChanged();
        }
    }
}
