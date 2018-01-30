package relay.control.gpio.android.sockets;

import android.util.SparseArray;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.models.Relay;

public class ReceiverObserver implements Observer {

    private Sender sender;
    private SparseArray<IRelay> relays;

    public ReceiverObserver(SparseArray<IRelay> relays, Sender sender) {
        this.sender = sender;
        this.relays = relays;
    }

    @Override
    public void update(Observable o, Object arg) {
        SparseArray<IRelay> relays = (SparseArray<IRelay>)arg;

    }
}
