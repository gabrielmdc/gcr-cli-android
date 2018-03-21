package gcr.cli.android.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import gcr.cli.android.models.IServerModel;
import gcr.cli.android.repositories.IRepositories;
import gcr.cli.android.R;
import gcr.cli.android.adapters.RelayListAdapter;
import gcr.cli.android.models.IRelay;
import gcr.cli.android.repositories.IServerRepository;
import gcr.cli.android.repositories.realm.RealmRepositories;
import gcr.cli.android.services.ReceiverService;
import gcr.cli.android.sockets.ConnectionStatus;
import gcr.cli.android.sockets.ServerConnection;

public class RelayActivity extends AppCompatActivity implements Observer {

    private ListView relayListView;
    private IServerModel server;
    private IRepositories repositories;
    private RelayListAdapter relayListAdapter;
    private ServerConnection serverConnection;
    private SparseArray<IRelay> relays;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        relays = new SparseArray<>();

        Toolbar relaysToolbar = findViewById(R.id.relays_toolbar);
        setSupportActionBar(relaysToolbar);

        repositories = new RealmRepositories();
        setServerFromIntent();
        serverConnection = new ServerConnection(this, server.getAddress(), server.getSocketPort());
        serverConnection.addReceiverObserver(new ReceiverObserver());
        serverConnection.addConnectionObserver(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IRelay relaySelected = relayListAdapter.getItem(info.position);
        menu.setHeaderTitle(relaySelected.getName());
        inflater.inflate(R.menu.relay_context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        IRelay relaySelected = relayListAdapter.getItem(info.position);
        switch(item.getItemId()){
            case R.id.edit_relay:
                showEditRelayDialog(relaySelected);
                return true;
            case R.id.delete_relay:
                deleteRelay(relaySelected);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.relays_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_relay:
                showCreateRelayDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(serverConnection.isConnected()) {
            return;
        }
        try {
            progressDialog = ProgressDialog.show(this, "Connecting",
                    "Stabilising connection...");
            serverConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg == null) {
            return;
        }
        ConnectionStatus status = (ConnectionStatus) arg;

        switch(status) {
            case CONNECTED:
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                relayListAdapter = new RelayListAdapter(this, R.layout.list_relay, relays,
                        serverConnection);
                relayListView = findViewById(R.id.relayListView);
                relayListView.setAdapter(relayListAdapter);
                registerForContextMenu(relayListView);
                if(progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case REFUSED:
                if(progressDialog != null) {
                    progressDialog.dismiss();
                }
                Toast.makeText(this, "Connection refused", Toast.LENGTH_SHORT).show();
                this.finish();
                break;
            case DISCONNECTED:
                Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
                this.finish();
        }
    }

    @Override
    public void onDestroy() {
        serverConnection.closeConnection();
        super.onDestroy();
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

        String gpio = relay.getGpio() + "";
        relayNameEditText.setText(relay.getName());
        relayGpioEditText.setText(gpio);
        relayInvertedCheckBox.setChecked(relay.isInverted());

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String relayName = relayNameEditText.getText().toString().trim();
                int relayGpio = Integer.parseInt(relayGpioEditText.getText().toString().trim());
                boolean relayIsInverted = relayInvertedCheckBox.isChecked();
                editRelay(relay, relayName, relayIsInverted, relayGpio);
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
                String name = relayNameEditText.getText().toString().trim();
                int gpio = Integer.parseInt(relayGpioEditText.getText().toString().trim());
                boolean inverted = relayInvertedCheckBox.isChecked();
                createRelay(name, gpio, inverted);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createRelay(String name, int gpio, boolean inverted) {
        if(name.length() > 0 && gpio > 0) {
            CreateRelayTask createRelayTask = new CreateRelayTask(name, gpio, inverted, serverConnection);
            createRelayTask.execute();
        }
    }

    private void editRelay(IRelay relay, String name, boolean inverted, int gpio) {
        if(name.length() > 0 && gpio > 0) {
            EditRelayTask editRelayTask = new EditRelayTask(relay.getId(), name, gpio, inverted, serverConnection);
            editRelayTask.execute();
        }
    }

    private void deleteRelay(IRelay relay) {
        DeleteRelayTask deleteRelayTask = new DeleteRelayTask(relay.getId(), serverConnection);
        deleteRelayTask.execute();
    }

    private class ReceiverObserver implements Observer{

        @Override
        public void update(Observable o, Object arg) {
            SparseArray<IRelay> relaysFromReceiver = (SparseArray<IRelay>)arg;
            for(int i = 0; i < relaysFromReceiver.size(); i++) {
                int key = relaysFromReceiver.keyAt(i);
                IRelay relayFromReceiver = relaysFromReceiver.get(key);
                // If there is Not any button for that relay
                if(relayFromReceiver.toDelete()) {
                    relays.remove(key);
                    continue;
                }
                if(relays.get(key) == null) {
                    relays.put(key, relayFromReceiver);
                    continue;
                }
                relays.put(key, relayFromReceiver);
            }
            relayListAdapter.notifyDataSetChanged();
        }
    }

    private static class CreateRelayTask extends AsyncTask<Void, Void, Boolean> {

        private String name;
        private int gpio;
        private boolean inverted;
        private ServerConnection serverConnection;

        CreateRelayTask(String name, int gpio, boolean inverted, ServerConnection serverConnection) {
            this.name = name;
            this.gpio = gpio;
            this.inverted = inverted;
            this.serverConnection = serverConnection;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                serverConnection.createRelay(name, gpio, inverted);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static class DeleteRelayTask extends AsyncTask<Void, Void, Boolean> {

        private int id;
        private ServerConnection serverConnection;

        DeleteRelayTask(int id, ServerConnection serverConnection) {
            this.id = id;
            this.serverConnection = serverConnection;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                serverConnection.deleteRelay(id);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static class EditRelayTask extends AsyncTask<Void, Void, Boolean> {

        private String name;
        private int gpio;
        private boolean inverted;
        private int id;
        private ServerConnection serverConnection;

        EditRelayTask(int id, String name, int gpio, boolean inverted,
                      ServerConnection serverConnection) {
            this.name = name;
            this.gpio = gpio;
            this.inverted = inverted;
            this.id = id;
            this.serverConnection = serverConnection;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                serverConnection.editRelay(id, name, gpio, inverted);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
