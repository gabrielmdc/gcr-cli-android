package gcr.cli.android.sockets;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.models.Relay;
import gcr.cli.android.services.ReceiverService;

public class ServerConnection {

    private Context context;
    private Socket senderSocket;
    private ServerObservable connectionObservable;
    private ServerObservable receiverObservable;
    private ServiceReceiver serviceReceiver;

    private String address;
    private int port;

    public ServerConnection(Context context, String address, int port) {
        this.context = context;
        Handler contextHandler = new Handler(this.context.getMainLooper());
        serviceReceiver = new ServiceReceiver(contextHandler);
        connectionObservable = new ServerObservable();
        receiverObservable = new ServerObservable();
        this.address = address;
        this.port = port;
    }

    public void connect() throws IOException {
        // Once the receiver is connected, the sender will try to connect by the ServiceReceiver
        ReceiverService.start(port, context, serviceReceiver);
    }

    public void closeConnection() {
        if(senderSocket != null && !senderSocket.isClosed()) {
            SenderDisconnectTask t = new SenderDisconnectTask();
            t.execute();
        }
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

    private class ServiceReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        ServiceReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if (resultData.containsKey(Receiver.KEY_CONNECTION)){
                ConnectionStatus status = ConnectionStatus.values()[resultCode];
                connectionObservable.setChangedAndNotify(status);
                switch(status) {
                    case RECEIVER_WAITING_FOR_SENDER:
                        SenderConnectionTask t = new SenderConnectionTask(address, port);
                        t.execute();
                        break;
                    case RECEIVER_CONNECTED:
                        System.out.println("Receiver connected (action received)...");
                }
                return;
            }

            if (resultData.containsKey(ReceiverService.KEY_RECEIVED)) {
                String relaysJson = resultData.getString(Receiver.EXTRA_RELAYS_JSON);
                SparseArray<IRelay> relays = getRelaysFromJsonMsg(relaysJson);
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
                    System.out.println("------> 1 " + context);
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
            ConnectionStatus status = result ? ConnectionStatus.SENDER_CONNECTED :
                    ConnectionStatus.SENDER_REFUSED;
            if(status == ConnectionStatus.SENDER_REFUSED) {
                closeConnection();
            }
            connectionObservable.setChangedAndNotify(status);
        }

    }

    private class SenderDisconnectTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Sender sender = getSenderReady();
                sender.sendEndConnection();
                senderSocket.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private class ServerObservable extends Observable {
        private void setChangedAndNotify(Object arg) {
            setChanged();
            notifyObservers(arg);
        }
    }

    static private SparseArray<IRelay> getRelaysFromJsonMsg(String msg) {
        SparseArray<IRelay> relays = new SparseArray<>();
        JSONArray arr;
        try {
            arr = new JSONArray(msg);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int id = obj.getInt("id");
                String name = obj.getString("name");
                int gpio = obj.getInt("port");
                boolean status = obj.getString("status").equals("1");
                boolean toDelete = Boolean.parseBoolean(obj.getString("deleted"));
                boolean inverted = Boolean.parseBoolean(obj.getString("inverted"));
                Relay relay = new Relay(id, name, gpio, status, inverted, toDelete);
                relays.put(id, relay);
            }
        } catch (JSONException e) {
            e.getStackTrace();
        }
        return relays;
    }
}
