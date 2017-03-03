package org.near.kodirelayremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final String ADDRESS = "192.168.1.50";
    private final int PORT = 10000;
    private Sender sender;
    private ReceiverObserver receiverObs;
    private Receiver receiver;

    private ImageButton ibtnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ibtnAction = (ImageButton) findViewById(R.id.ibtnAction);
        setComponentsEvents();
        sender = new Sender(ADDRESS, PORT);
        receiverObs = new ReceiverObserver(ibtnAction, this);
    }

    private void setComponentsEvents(){
        ibtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {// TODO use AsyncTask
                    public void run() {
                        if(sender != null && receiverObs != null) {
                            try {
                                sender.sendMessage(receiverObs.getNextStatus());
                                //Toast.makeText(v.getContext(), "Command sended", Toast.LENGTH_LONG).show();
                            }catch(IOException e){
                                //Toast.makeText(v.getContext(), "Command not sended: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).start();
            }
        });

    }

    private void connectReceiver(){
        if(receiver == null) {
            receiver = new Receiver(PORT);
            receiver.addObserver(receiverObs);
            new Thread(receiver).start();
        }
    }

    private void connectSender() {
        if (sender != null) {
            new Thread(new Runnable() {// TODO use AsynkTask
                public void run() {
                    try {
                        sender.connect();
                    } catch (IOException e) {
                        //Toast.makeText(MainActivity.this, "Connection fail: "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }).start();
        }
    }

    private void connect() {
        connectSender();
        connectReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connect();
    }

}
