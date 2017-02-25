package org.near.kodirelayremote;

import java.util.Observable;
import java.util.Observer;
import android.widget.ImageButton;

class ReceiverObserver implements Observer {

    private String status;
    private ImageButton btn;

    ReceiverObserver(ImageButton btn) {
        status = "";
        this.btn = btn;
    }

    @Override
    public void update(Observable o, Object arg) {
        status = (String)arg;
        switch(status) {
            case Sender.OFF:
                btn.setImageResource(R.mipmap.button_on);
                break;
            case Sender.ON:
                btn.setImageResource(R.mipmap.button_off);
                break;
        }
    }

    public String getStatus() {
        return status;
    }

    String getNextStatus() {
        return status.compareTo(Sender.ON) == 0? Sender.OFF : Sender.ON;
    }

}
