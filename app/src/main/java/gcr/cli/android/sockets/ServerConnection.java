package gcr.cli.android.sockets;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.models.Relay;
import gcr.cli.android.services.ReceiverService;

public class ServerConnection {

    private Context context;
    private Sender sender;
    private ServerObservable connectionObservable;
    private ServerObservable receiverObservable;
    private ServiceReceiver serviceReceiver;
    private boolean isConnected;
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
        sender = new Sender();
        isConnected = false;
    }

    public void connect() throws IOException {
        // Once the receiver is connected, the sender will try to connect by the ServiceReceiver
        ReceiverService.start(port, context, serviceReceiver);
    }

    public void closeConnection() {
        if(sender.isConnected()) {
            SenderDisconnectTask t = new SenderDisconnectTask();
            t.execute();
        }
    }

    public void createRelay(String name, int port, boolean inverted) throws IOException {
        if(name.isEmpty()) {
            return;
        }
        if(sender.isConnected()) {
            sender.sendAdd(name, port, inverted);
        }
    }

    public void editRelay(int relayId, String name, int port, boolean inverted) throws IOException {
        if(name.isEmpty()) {
            return;
        }
        if(sender.isConnected()) {
            sender.sendEdit(relayId, name, port, inverted);
        }
    }

    public void deleteRelay(int relayId) throws IOException {
        if(sender.isConnected()) {
            sender.sendDelete(relayId);
        }
    }

    public void turnOnRelay(int... relayIds) throws IOException {
        if(sender.isConnected()) {
            sender.sendStatusOn(relayIds);
        }
    }

    public void turnOffRelay(int... relayIds) throws IOException {
        if(sender.isConnected()) {
            sender.sendStatusOff(relayIds);
        }
    }

    public void addReceiverObserver(Observer o) {
        receiverObservable.addObserver(o);
    }

    public void addConnectionObserver(Observer o) {
        connectionObservable.addObserver(o);
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void statusConnected() {
        isConnected = true;
        connectionObservable.setChangedAndNotify(ConnectionStatus.CONNECTED);
    }

    private void statusRefused() {
        isConnected = false;
        connectionObservable.setChangedAndNotify(ConnectionStatus.REFUSED);
    }

    private void statusDisconnected() {
        isConnected = false;
        connectionObservable.setChangedAndNotify(ConnectionStatus.DISCONNECTED);
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
                switch(status) {
                    case RECEIVER_WAITING_FOR_SENDER:
                        SenderConnectionTask t = new SenderConnectionTask(address, port);
                        t.execute();
                        break;
                    case RECEIVER_CONNECTED:
                        statusConnected();
                        break;
                    case RECEIVER_REFUSED:
                    case RECEIVER_TIMEOUT:
                        if(!sender.isConnected()) {
                            statusRefused();
                            return;
                        }
                    case RECEIVER_DISCONNECTED:
                        closeConnection();
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
            if(!sender.isConnected()) {
                try {
                    sender.openConnection(address, port);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result) {
                statusRefused();
            }
        }

    }

    private class SenderDisconnectTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                sender.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            statusDisconnected();
        }
    }

    private class ServerObservable extends Observable {
        private void setChangedAndNotify(Object arg) {
            setChanged();
            notifyObservers(arg);
        }
    }
}
