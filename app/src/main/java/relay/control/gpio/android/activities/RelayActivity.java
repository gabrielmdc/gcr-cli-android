package relay.control.gpio.android.activities;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

import relay.control.gpio.android.R;
import relay.control.gpio.android.adapters.RelayListAdapter;
import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;
import relay.control.gpio.android.repositories.realm.RealmRepositories;
import relay.control.gpio.android.sockets.ServerConnection;

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
        registerForContextMenu(relayListView);
        relayListView.setAdapter(relayListAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(server != null){
            relayListAdapter.startConnection(PORT, server.getAddress());
        }
    }

    @Override
    public void onDestroy() {
        relayListAdapter.closeConnection();
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IRelay relaySelected = relayListAdapter.getItem(info.position);
        menu.setHeaderTitle(relaySelected.getName());
        inflater.inflate(R.menu.relay_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.edit_relay:
                IRelay relaySelected = relayListAdapter.getItem(info.position);
//                showEditRelayDialog(servers.get(info.position));
                showEditRelayDialog(relaySelected);
                relayListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete_relay:
                deleteRelay(info.position);
                relayListAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void setServerFromIntent() {
        if(getIntent().getExtras() != null){
            int serverId = getIntent().getExtras().getInt("serverId");
            IServerRepository serverRepository = repositories.getServerRepository();
            server = serverRepository.findById(serverId);
        }
    }

    private void showEditRelayDialog(final IRelay relay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + relay.getName() + " relay");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_relay, null);
        builder.setView(viewInflated);

        final EditText relayNameEditText = viewInflated.findViewById(R.id.relayNameEditText);
        final EditText relayGpioEditText = viewInflated.findViewById(R.id.relayGpioEditText);
        final CheckBox relayInvertedCheckBox = viewInflated.findViewById(R.id.relayInvertedCheckBox);

        relayNameEditText.setText(relay.getName());
        relayGpioEditText.setText(relay.getGpio());
        relayInvertedCheckBox.setChecked(relay.isInverted());

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String relayName = relayNameEditText.getText().toString().trim();
                int relayGpio = Integer.parseInt(relayGpioEditText.getText().toString().trim());
                boolean relayIsInverted = relayInvertedCheckBox.isChecked();
                if(relayName.length() > 0 && relayGpio > 0) {
                    ServerConnection serverConnection = relayListAdapter.getServerConnection();
                    try {
                        serverConnection.createRelay(relayName, relayGpio, relayIsInverted);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCreateRelayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new relay");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_relay, null);
        builder.setView(viewInflated);

        final EditText relayNameEditText = viewInflated.findViewById(R.id.relayNameEditText);
        final EditText relayGpioEditText = viewInflated.findViewById(R.id.relayGpioEditText);
        final CheckBox relayInvertedCheckBox = viewInflated.findViewById(R.id.relayInvertedCheckBox);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String relayName = relayNameEditText.getText().toString().trim();
                int relayGpio = Integer.parseInt(relayGpioEditText.getText().toString().trim());
                boolean relayIsInverted = relayInvertedCheckBox.isChecked();
                if(relayName.length() > 0 && relayGpio > 0) {
                    ServerConnection serverConnection = relayListAdapter.getServerConnection();
                    try {
                        serverConnection.createRelay(relayName, relayGpio, relayIsInverted);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editRelay(IRelay relay, String name, boolean inverted, int gpio) {
        ServerConnection serverConnection = relayListAdapter.getServerConnection();
//        String msg = "EDIT:" + relay.getId();
        try {
            serverConnection.editRelay(relay.getId(), relay.getName(), relay.getGpio(), relay.isInverted());
            Toast.makeText(this, "Relay '" + relay + "' deleted", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Relay '" + relay + "' not deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRelay(int position) {
        IRelay relay = relayListAdapter.getItem(position);
        if(relay == null) {
            return;
        }
        ServerConnection serverConnection = relayListAdapter.getServerConnection();
//        String msg = "DELETE:" + relay.getId();
        try {
            serverConnection.deleteRelay(relay.getId());
            Toast.makeText(this, "Relay '" + relay + "' deleted", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Relay '" + relay + "' not deleted", Toast.LENGTH_SHORT).show();
        }
    }

}
