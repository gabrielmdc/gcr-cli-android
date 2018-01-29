package relay.control.gpio.android.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import relay.control.gpio.android.R;
import relay.control.gpio.android.adapters.RelayListAdapter;
import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;
import relay.control.gpio.android.repositories.realm.RealmRepositories;

public class RelayActivity extends AppCompatActivity {

    private ListView relayListView;
    private List<IRelay> relays;
    private IServerModel server;
    private IRepositories repositories;
    private RelayListAdapter relayListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        repositories = new RealmRepositories();
        setServerFromIntent();

        relays = new ArrayList<>();

        relayListAdapter = new RelayListAdapter(this, R.layout.list_server, relays);
        relayListView = findViewById(R.id.relayListView);
        relayListView.setAdapter(relayListAdapter);
    }

    private void setServerFromIntent() {
        if(getIntent().getExtras() != null){
            int serverId = getIntent().getExtras().getInt("serverId");
            IServerRepository serverRepository = repositories.getServerRepository();
            server = serverRepository.findById(serverId);
        }
    }
}
