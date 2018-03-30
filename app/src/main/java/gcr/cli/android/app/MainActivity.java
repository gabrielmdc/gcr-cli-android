package gcr.cli.android.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import gcr.cli.android.activities.RelayActivity;
import gcr.cli.android.models.IServer;
import gcr.cli.android.repositories.IRepositories;
import gcr.cli.android.repositories.IServerRepository;
import gcr.cli.android.repositories.realm.RealmRepositories;
import gcr.cli.android.R;
import gcr.cli.android.adapters.ServerListAdapter;
import gcr.cli.android.validatiors.ServerValidator;
import gcr.cli.android.validatiors.errorkeys.ServerErrorKeys;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView serverListView;
    private List<IServer> servers;
    private ServerListAdapter serverListAdapter;
    private FloatingActionButton createServerFloatingButton;
    private IRepositories repositories;
    private ServerValidator serverValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverValidator = new ServerValidator();
        setContentView(R.layout.activity_main);

        // Get servers from data base
        dataBaseSetUp();
        IServerRepository serverRepo = repositories.getServerRepository();
        servers = serverRepo.getAll();

        serverListAdapter = new ServerListAdapter(this, R.layout.list_server, servers);

        serverListView = findViewById(R.id.serverListView);
        serverListView.setAdapter(serverListAdapter);
        registerForContextMenu(serverListView);
        serverListView.setOnItemClickListener(this);

        createServerFloatingButton = findViewById(R.id.createServerFloatingButton);
        createServerFloatingButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateServerDialog();
            }
        });

        if(servers.size() == 0) {
            showCreateServerDialog();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        IServer serverSelected = servers.get(position);
        openRelayList(serverSelected);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IServer serverSelected = servers.get(info.position);
        menu.setHeaderTitle(serverSelected.getName());
        inflater.inflate(R.menu.server_context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.edit_server:
                showEditServerDialog(servers.get(info.position));
                serverListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete_server:
                deleteServer(info.position);
                serverListAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Connect to the server and open a new activity with a list of its relays
     * @param server
     */
    private void openRelayList(IServer server) {
        Intent intent = new Intent(MainActivity.this, RelayActivity.class);
        intent.putExtra("serverId", server.getId());
        startActivity(intent);
    }

    private void showEditServerDialog(final IServer server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.server_edit));
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_server, null);
        builder.setView(viewInflated);
        builder.setPositiveButton(getString(R.string.edit), null);
        AlertDialog dialog = builder.create();
        final EditText serverNameEditText = viewInflated.findViewById(R.id.serverNameEditText);
        final EditText serverAddressEditText = viewInflated.findViewById(R.id.serverAddressEditText);
        final EditText socketPortEditText = viewInflated.findViewById(R.id.socketPortEditText);

        serverNameEditText.setText(server.getName());
        serverAddressEditText.setText(server.getAddress());
        socketPortEditText.setText(String.format("%d",server.getSocketPort()));

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(validateAndEditServer(serverNameEditText, serverAddressEditText,
                                socketPortEditText, server)) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void showCreateServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.server_add));
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_server, null);
        builder.setView(viewInflated);
        builder.setPositiveButton(getString(R.string.add), null);
        AlertDialog dialog = builder.create();
        if(servers.size() == 0) {
            dialog.setCanceledOnTouchOutside(false);
        }
        final EditText serverNameEditText = viewInflated.findViewById(R.id.serverNameEditText);
        final EditText serverAddressEditText = viewInflated.findViewById(R.id.serverAddressEditText);
        final EditText socketPortEditText = viewInflated.findViewById(R.id.socketPortEditText);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(validateAndCreateServer(serverNameEditText, serverAddressEditText,
                                socketPortEditText)) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private boolean validateAndEditServer(EditText serverNameEditText,
                                          EditText serverAddressEditText,
                                          EditText socketPortEditText,
                                          IServer server) {

        String name = serverNameEditText.getText().toString().trim();
        String address = serverAddressEditText.getText().toString().trim();
        String socketPortStr = socketPortEditText.getText().toString().trim();
        int socketPort = socketPortStr.isEmpty()? 10000 : Integer.parseInt(socketPortStr);

        ServerErrorKeys nameErrorKey = serverValidator.validateName(name);
        ServerErrorKeys addressErrorKey = serverValidator.validateAddress(address);
        ServerErrorKeys socketPortErrorKey = serverValidator.validateSocketPort(socketPort);
        if(nameErrorKey != null) {
            String errorMsg = serverValidator.getErrorMessage(nameErrorKey);
            serverNameEditText.setError(errorMsg);
        }
        if(addressErrorKey != null) {
            String errorMsg = serverValidator.getErrorMessage(addressErrorKey);
            serverAddressEditText.setError(errorMsg);
        }
        if(socketPortErrorKey != null) {
            String errorMsg = serverValidator.getErrorMessage(socketPortErrorKey);
            socketPortEditText.setError(errorMsg);
        }
        boolean isValidData = nameErrorKey == null &&
                addressErrorKey == null &&
                socketPortErrorKey == null;
        if(isValidData) {
            editServer(server, name, address, socketPort);
            return true;
        }
        String msg = getString(R.string.invalid_data);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean validateAndCreateServer(EditText serverNameEditText,
        EditText serverAddressEditText, EditText socketPortEditText) {

        String name = serverNameEditText.getText().toString().trim();
        String address = serverAddressEditText.getText().toString().trim();
        String socketPortStr = socketPortEditText.getText().toString().trim();
        int socketPort = socketPortStr.isEmpty()? 10000 : Integer.parseInt(socketPortStr);

        ServerErrorKeys nameErrorKey = serverValidator.validateName(name);
        ServerErrorKeys addressErrorKey = serverValidator.validateAddress(address);
        ServerErrorKeys socketPortErrorKey = serverValidator.validateSocketPort(socketPort);
        if(nameErrorKey != null) {
            String errorMsg = serverValidator.getErrorMessage(nameErrorKey);
            serverNameEditText.setError(errorMsg);
        }
        if(addressErrorKey != null) {
            String errorMsg = serverValidator.getErrorMessage(addressErrorKey);
            serverAddressEditText.setError(errorMsg);
        }
        if(socketPortErrorKey != null) {
            String errorMsg = serverValidator.getErrorMessage(socketPortErrorKey);
            socketPortEditText.setError(errorMsg);
        }
        boolean isValidData = nameErrorKey == null &&
                addressErrorKey == null &&
                socketPortErrorKey == null;
        if(isValidData) {
            IServer server = createNewServer(name, address, socketPort);
            openRelayList(server);
            return true;
        }
        String msg = getString(R.string.invalid_data);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void editServer(IServer server, String name, String address, int socketPort) {
        IServerRepository serverRepo = repositories.getServerRepository();
        server = serverRepo.edit(server, name, address, socketPort);
        String msg = getString(R.string.server_modified);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void deleteServer(int position) {
        IServer server = servers.get(position);
        IServerRepository serverRepo = repositories.getServerRepository();
        String serverDescription = server.toString();
        servers.remove(server);
        serverRepo.delete(server);
        String msg = getString(R.string.server_deleted);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if(servers.size() == 0) {
            showCreateServerDialog();
        }
    }

    private IServer createNewServer(String name, String address, int socketPort) {
        IServerRepository serverRepo = repositories.getServerRepository();
        IServer server = serverRepo.create(name, address, socketPort);
        servers.add(server);
        serverListAdapter.notifyDataSetChanged();
        String msg = getString(R.string.server_added);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return server;
    }

    private void dataBaseSetUp() {
        repositories = new RealmRepositories();
        repositories.openConnection(this);
    }
}
