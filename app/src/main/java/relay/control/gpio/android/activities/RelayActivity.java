package relay.control.gpio.android.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import relay.control.gpio.android.R;
import relay.control.gpio.android.adapters.RelayListAdapter;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;
import relay.control.gpio.android.repositories.realm.RealmRepositories;

public class RelayActivity extends AppCompatActivity {

    private ListView relayListView;
    private IServerModel server;
    private IRepositories repositories;
    private RelayListAdapter relayListAdapter;

    private final int PORT = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        repositories = new RealmRepositories();
        setServerFromIntent();

        relayListAdapter = new RelayListAdapter(this, R.layout.list_relay);
        relayListView = findViewById(R.id.relayListView);
        relayListView.setAdapter(relayListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(server != null){
            relayListAdapter.startConnection(PORT, server.getAddress());
        }
    }

    private void setServerFromIntent() {
        if(getIntent().getExtras() != null){
            int serverId = getIntent().getExtras().getInt("serverId");
            IServerRepository serverRepository = repositories.getServerRepository();
            server = serverRepository.findById(serverId);
        }
    }
}
