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

public class ServerConnection {

    public static final String SENDER_CONNECTED = "sender_connected";
    public static final String SENDER_REFUSED = "sender_refused";

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
    }

    public void closeConnection() {
        try {
            if(senderSocket != null && !senderSocket.isClosed()){
                senderSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent requestIntent = new Intent(context, ReceiverService.class);
        context.stopService(requestIntent);
    }

    public void createRelay(String name, int port, boolean inverted) throws IOException {
        if(name.isEmpty()) {
            return;
        }
        Sender sender = getSenderReady();
        sender.sendAdd(name, port, inverted);
    }

    public void editRelay(int relayId, String name, int port, boolean inverted) throws IOException {
        if(name.isEmpty()) {
            return;
        }
        Sender sender = getSenderReady();
        sender.sendEdit(relayId, name, port, inverted);
    }

    public void deleteRelay(int relayId) throws IOException {
        Sender sender = getSenderReady();
        sender.sendDelete(relayId);
    }

    public void turnOnRelay(int... relayIds) throws IOException {
        Sender sender = getSenderReady();
        sender.sendStatusOn(relayIds);
    }

    public void turnOffRelay(int... relayIds) throws IOException {
        Sender sender = getSenderReady();
        sender.sendStatusOff(relayIds);
    }

    public void addReceiverObserver(Observer o) {
        receiverObservable.addObserver(o);
    }

    public void addConnectionObserver(Observer o) {
        connectionObservable.addObserver(o);
    }

    private Sender getSenderReady() throws IOException {
        Sender sender = new Sender();
        sender.connect(senderSocket);
        return sender;
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
                connectionObservable.setChangedAndNotify(ReceiverService.ACTION_CONNECTION_WAITING);
                SenderConnectionTask t = new SenderConnectionTask(address, port);
                t.execute();
            } else if( action == ReceiverService.ACTION_CONNECTED) {
                System.out.println("Receiver connected...");
                connectionObservable.setChangedAndNotify(ReceiverService.ACTION_CONNECTED);
            } else if(action == ReceiverService.ACTION_RECEIVED) {
                String relaysJson = intent.getStringExtra(ReceiverService.EXTRA);
                SparseArray<IRelay> relays = Receiver.getRelaysFromJsonMsg(relaysJson);
                receiverObservable.setChangedAndNotify(relays);
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
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Intent intentService = new Intent(context, ReceiverService.class);
                    context.stopService(intentService);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                connectionObservable.setChangedAndNotify(SENDER_CONNECTED);
                return;
            }
            connectionObservable.setChangedAndNotify(SENDER_REFUSED);
        }

    }

    private class ServerObservable extends Observable {
        private void setChangedAndNotify(Object arg) {
            setChanged();
            notifyObservers(arg);
        }
    }
}
