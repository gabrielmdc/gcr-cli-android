package org.near.kodirelayremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import java.net.ServerSocket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private final String ADDRESS = "192.168.1.50";
    private final int PORT = 10001;
    private Sender sender;
    private ReceiverObserver receiverObs;

    private ImageButton ibtnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ibtnAction = (ImageButton) findViewById(R.id.ibtnAction);
        ibtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {
                    public void run() {
                        if(sender != null && receiverObs != null) {
                            try {
                                sender.sendMessage(receiverObs.getNextStatus());
                            }catch(IOException e){
                                Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).start();
            }
        });
        sender = new Sender(ADDRESS, PORT);
    }

    private void senderConnect() {
        if(sender != null){
            try {
                sender.connect();
            }catch(IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            Socket socket = serverSocket.accept();

            Receiver receiver  = new Receiver(socket);

            receiverObs = new ReceiverObserver(ibtnAction);
            receiver.addObserver(receiverObs);

            Thread t = new Thread(receiver);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        senderConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        senderConnect();
    }

    /*public void refreshControls() {
        new Thread(new Runnable() {
            public void run() {
                device.connect();
                final String STATUS = device.sendAction("INFO");
                ibtnAction.post(new Runnable() {
                    public void run() {
                        changeBtnActionImage(STATUS);
                    }
                });
            }
        }).start();
    }*/

    public void changeBtnActionImage(String status) {
        if(status.compareTo("0") == 0) {
            ibtnAction.setImageResource(R.mipmap.button_off);
            Toast.makeText(this, "Tv is OFF", Toast.LENGTH_SHORT).show();
        } else {
            ibtnAction.setImageResource(R.mipmap.button_on);
            Toast.makeText(this, "TV is ON", Toast.LENGTH_SHORT).show();
        }
    }
}
