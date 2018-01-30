package relay.control.gpio.android.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ListView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import relay.control.gpio.android.R;
import relay.control.gpio.android.adapters.RelayListAdapter;
import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.models.Relay;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;
import relay.control.gpio.android.repositories.realm.RealmRepositories;
import relay.control.gpio.android.sockets.Receiver;
import relay.control.gpio.android.sockets.Sender;
import relay.control.gpio.android.sockets.ServerConnection;

public class RelayActivity extends AppCompatActivity {

    private ListView relayListView;
    private IServerModel server;
    private IRepositories repositories;
    private RelayListAdapter relayListAdapter;

    private ServerConnection connection;
    private final String ADDRESS = "10.0.0.3";
    private final int PORT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        repositories = new RealmRepositories();
        setServerFromIntent();

        connection = new ServerConnection(ADDRESS, PORT);
        Sender sender = connection.getSender();

        relayListAdapter = new RelayListAdapter(this, R.layout.list_relay, sender);
        connection.addReceiverObserver(relayListAdapter);
        relayListView = findViewById(R.id.relayListView);
        relayListView.setAdapter(relayListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connection.connect();
    }

    private void setServerFromIntent() {
        if(getIntent().getExtras() != null){
            int serverId = getIntent().getExtras().getInt("serverId");
            IServerRepository serverRepository = repositories.getServerRepository();
            server = serverRepository.findById(serverId);
        }
    }

    private void connect(){
        //ServerSocket serverSocket = null;

//            sender.connect();
//            serverSocket = new ServerSocket(PORT);
//            Socket socket = serverSocket.accept();
//
//            Receiver receiver  = new Receiver(socket);
//
////            receiverObs = new ReceiverObserver(hBox, sender);
////            receiver.addObserver(receiverObs);
//
//            Thread t = new Thread(receiver);
//            t.start();
    }

    /*private final String ADDRESS = "10.0.0.3";
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
    }*/
}
