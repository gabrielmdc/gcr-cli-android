package relay.control.gpio.android.activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

        Toolbar relaysToolbar = findViewById(R.id.relays_toolbar);
        setSupportActionBar(relaysToolbar);

        repositories = new RealmRepositories();
        setServerFromIntent();

        relayListAdapter = new RelayListAdapter(this, R.layout.list_relay);
        relayListView = findViewById(R.id.relayListView);


        relayListView.setAdapter(relayListAdapter);
        registerForContextMenu(relayListView);
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.edit_relay:
                IRelay relaySelected = relayListAdapter.getItem(info.position);
//                showEditRelayDialog(servers.get(info.position));
                showEditRelayDialog(relaySelected);
                //relayListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete_relay:
                deleteRelay(info.position);
                //relayListAdapter.notifyDataSetChanged();
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
        if(server != null){
            relayListAdapter.startConnection(PORT, server.getAddress());
        }
    }

    @Override
    public void onDestroy() {
        relayListAdapter.closeConnection();
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
            CreateRelayTask createRelayTask = new CreateRelayTask(name, gpio, inverted);
            createRelayTask.execute();
        }
    }

    private void editRelay(IRelay relay, String name, boolean inverted, int gpio) {
        if(name.length() > 0 && gpio > 0) {
            EditRelayTask editRelayTask = new EditRelayTask(relay.getId(), name, gpio, inverted);
            editRelayTask.execute();
        }
    }

    private void deleteRelay(int position) {
        IRelay relay = relayListAdapter.getItem(position);
        DeleteRelayTask deleteRelayTask = new DeleteRelayTask(relay.getId());
        deleteRelayTask.execute();
    }

    private class CreateRelayTask extends AsyncTask<Void, Void, Boolean> {

        private String name;
        private int gpio;
        private boolean inverted;

        CreateRelayTask(String name, int gpio, boolean inverted) {
            this.name = name;
            this.gpio = gpio;
            this.inverted = inverted;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ServerConnection serverConnection = relayListAdapter.getServerConnection();
            try {
                serverConnection.createRelay(name, gpio, inverted);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private class DeleteRelayTask extends AsyncTask<Void, Void, Boolean> {

        private int id;

        DeleteRelayTask(int id) {
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ServerConnection serverConnection = relayListAdapter.getServerConnection();
            try {
                serverConnection.deleteRelay(id);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private class EditRelayTask extends AsyncTask<Void, Void, Boolean> {

        private String name;
        private int gpio;
        private boolean inverted;
        private int id;

        EditRelayTask(int id, String name, int gpio, boolean inverted) {
            this.name = name;
            this.gpio = gpio;
            this.inverted = inverted;
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ServerConnection serverConnection = relayListAdapter.getServerConnection();
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
