package org.near.kodirelayremote;

import java.util.Observable;
import java.util.Observer;
import android.app.Activity;
import android.widget.ImageButton;

class ReceiverObserver implements Observer {

    private String status;
    private ImageButton btn;
    private Activity activity;

    ReceiverObserver(ImageButton btn, Activity activity) {
        status = "";
        this.btn = btn;
        this.activity = activity;
    }

    @Override
    public void update(Observable o, Object arg) {
        status = (String)arg;
        activity.runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
            switch (status)
            {
                case Sender.OFF:
                    btn.setImageResource(R.mipmap.button_off);
                    break;
                case Sender.ON:
                    btn.setImageResource(R.mipmap.button_on);
                    break;
            }
            }
        });
    }

    public String getStatus() {
        return status;
    }

    String getNextStatus() {
        return status.compareTo(Sender.ON) == 0? Sender.OFF : Sender.ON;
    }

}
