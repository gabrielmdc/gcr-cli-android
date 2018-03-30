package gcr.cli.android.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import gcr.cli.android.models.IServer;
import gcr.cli.android.repositories.IRepositories;
import gcr.cli.android.R;
import gcr.cli.android.adapters.RelayListAdapter;
import gcr.cli.android.models.IRelay;
import gcr.cli.android.repositories.IServerRepository;
import gcr.cli.android.repositories.realm.RealmRepositories;
import gcr.cli.android.sockets.ConnectionStatus;
import gcr.cli.android.sockets.ServerConnection;
import gcr.cli.android.validatiors.RelayValidator;
import gcr.cli.android.validatiors.errorkeys.RelayErrorKeys;

public class RelayActivity extends AppCompatActivity implements Observer {

    private ListView relayListView;
    private IServer server;
    private IRepositories repositories;
    private RelayListAdapter relayListAdapter;
    private ServerConnection serverConnection;
    private SparseArray<IRelay> relays;
    private ProgressDialog progressDialog;
    private RelayValidator relayValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        relays = new SparseArray<>();
        relayValidator = new RelayValidator();

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
                Toast.makeText(this, getString(R.string.connected), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, getString(R.string.connection_refused), Toast.LENGTH_SHORT).show();
                this.finish();
                break;
            case DISCONNECTED:
                Toast.makeText(this, getString(R.string.connection_closed), Toast.LENGTH_SHORT).show();
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
        builder.setTitle(getString(R.string.relay_edit));
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_relay, null);
        builder.setView(viewInflated);
        builder.setPositiveButton(getString(R.string.edit), null);
        AlertDialog dialog = builder.create();
        final EditText relayNameEditText = viewInflated.findViewById(R.id.relayNameEditText);
        final EditText relayGpioEditText = viewInflated.findViewById(R.id.relayGpioEditText);
        final CheckBox relayInvertedCheckBox = viewInflated.findViewById(R.id.relayInvertedCheckBox);

        String gpio = relay.getGpio() + "";
        relayNameEditText.setText(relay.getName());
        relayGpioEditText.setText(gpio);
        relayInvertedCheckBox.setChecked(relay.isInverted());

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(validateAndEditRelay(relayNameEditText, relayGpioEditText,
                                relayInvertedCheckBox, relay)) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void showCreateRelayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.relay_add));
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_relay, null);
        builder.setView(viewInflated);
        builder.setPositiveButton(getString(R.string.add), null);
        AlertDialog dialog = builder.create();
        final EditText relayNameEditText = viewInflated.findViewById(R.id.relayNameEditText);
        final EditText relayGpioEditText = viewInflated.findViewById(R.id.relayGpioEditText);
        final CheckBox relayInvertedCheckBox = viewInflated.findViewById(R.id.relayInvertedCheckBox);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(validateAndCreateRelay(relayNameEditText, relayGpioEditText,
                                relayInvertedCheckBox)) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private boolean validateAndCreateRelay(EditText relayNameEditText,
                                         EditText relayGpioEditText,
                                         CheckBox relayInvertedCheckBox) {

        String name = relayNameEditText.getText().toString().trim();
        String gpioStr = relayGpioEditText.getText().toString().trim();
        boolean relayIsInverted = relayInvertedCheckBox.isChecked();

        RelayErrorKeys nameErrorKey = relayValidator.validateName(name);
        RelayErrorKeys gpioErrorKey = relayValidator.validateGpio(gpioStr);
        if(nameErrorKey != null) {
            String errorMsg = relayValidator.getErrorMessage(nameErrorKey);
            relayNameEditText.setError(errorMsg);
        }
        if(gpioErrorKey != null) {
            String errorMsg = relayValidator.getErrorMessage(nameErrorKey);// TODO
            relayGpioEditText.setError(errorMsg);
        }
        boolean isValidData = nameErrorKey == null && gpioErrorKey == null;
        if(isValidData) {
            int gpio = Integer.parseInt(gpioStr);
            createRelay(name, gpio, relayIsInverted);
            return true;
        }
        String msg = getString(R.string.invalid_data);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean validateAndEditRelay(EditText relayNameEditText,
                                          EditText relayGpioEditText,
                                          CheckBox relayInvertedCheckBox,
                                          IRelay relay) {

        String name = relayNameEditText.getText().toString().trim();
        String gpioStr = relayGpioEditText.getText().toString().trim();
        boolean relayIsInverted = relayInvertedCheckBox.isChecked();

        RelayErrorKeys nameErrorKey = relayValidator.validateName(name);
        RelayErrorKeys gpioErrorKey = relayValidator.validateGpio(gpioStr);
        if(nameErrorKey != null) {
            String errorMsg = relayValidator.getErrorMessage(nameErrorKey);
            relayNameEditText.setError(errorMsg);
        }
        if(gpioErrorKey != null) {
            String errorMsg = relayValidator.getErrorMessage(gpioErrorKey);
            relayGpioEditText.setError(errorMsg);
        }
        boolean isValidData = nameErrorKey == null && gpioErrorKey == null;
        if(isValidData) {
            int gpio = Integer.parseInt(gpioStr);
            editRelay(relay, name, relayIsInverted, gpio);
            return true;
        }
        String msg = getString(R.string.invalid_data);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
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
            if(relays.size() == 0) {
                showCreateRelayDialog();
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
