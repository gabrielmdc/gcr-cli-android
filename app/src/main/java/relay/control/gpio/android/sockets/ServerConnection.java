package relay.control.gpio.android.sockets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import java.io.IOException;
import java.net.Socket;
import java.util.Observable;

import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.services.ReceiverService;

public class ServerConnection extends Observable {

    private Context context;
    private Socket senderSocket;

    public ServerConnection(Context context) {
        this.context = context;
        mountBroadCastReceiver(this.context);
    }

    public void connect(String address, int port) throws IOException {
        ReceiverService.start(port, context);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        senderConnection(address, port);
    }

    public void sendMessage(String msg) throws IOException {
        Sender sender = new Sender();
        sender.connect(senderSocket);
        sender.sendMessage(msg);
    }

    public boolean senderIsConnected() {
        return senderSocket.isConnected();
    }

    private void senderConnection(String address, int port) throws IOException {
        if(senderSocket == null || !senderSocket.isConnected()) {
            Sender sender = new Sender();
            senderSocket = sender.connect(address, port);
        }
    }

    private void mountBroadCastReceiver(Context context) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter(ReceiverService.EVENT_FINISHED);
        filter.addAction(ReceiverService.EXTRA_TARGET);
        ReceiverBroadCastReceiver broadCastReceiver = new ReceiverBroadCastReceiver();
        broadcastManager.registerReceiver(broadCastReceiver, filter);
    }

    private class ReceiverBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String relaysJson = intent.getStringExtra(ReceiverService.EXTRA_TARGET);
            SparseArray<IRelay> relays = Receiver.getRelaysFromJsonMsg(relaysJson);
            setChanged();
            notifyObservers(relays);
        }
    }
}
