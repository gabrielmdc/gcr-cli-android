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
import gcr.cli.android.models.IServerModel;
import gcr.cli.android.repositories.IRepositories;
import gcr.cli.android.repositories.IServerRepository;
import gcr.cli.android.repositories.realm.RealmRepositories;
import gcr.cli.android.R;
import gcr.cli.android.adapters.ServerListAdapter;
import gcr.cli.android.validatiors.IModelValidator;
import gcr.cli.android.validatiors.ServerModelValidator;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView serverListView;
    private List<IServerModel> servers;
    private ServerListAdapter serverListAdapter;
    private FloatingActionButton createServerFloatingButton;
    private IRepositories repositories;
    private IModelValidator<IServerModel> serverValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverValidator = new ServerModelValidator();
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
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, RelayActivity.class);
        IServerModel serverSelected = servers.get(position);
        intent.putExtra("serverId", serverSelected.getId());
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IServerModel serverSelected = servers.get(info.position);
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

    private void showEditServerDialog(final IServerModel server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + server + " server");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_server, null);
        builder.setView(viewInflated);
        builder.setPositiveButton("Edit", null);
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
        builder.setTitle("Add new server");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_server, null);
        builder.setView(viewInflated);
        builder.setPositiveButton("Add", null);
        AlertDialog dialog = builder.create();
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
                                          IServerModel server) {

        String name = serverNameEditText.getText().toString().trim();
        String address = serverAddressEditText.getText().toString().trim();
        String socketPortStr = socketPortEditText.getText().toString().trim();
        int socketPort = socketPortStr.isEmpty()? 10000 : Integer.parseInt(socketPortStr);

        String nameErrorMsg = ((ServerModelValidator)serverValidator).validateName(name);
        String addressErrorMsg = ((ServerModelValidator)serverValidator).validateAddress(address);
        String socketPortErrorMsg = ((ServerModelValidator)serverValidator).validateSocketPort(socketPort);
        if(nameErrorMsg != null) {
            serverNameEditText.setError(nameErrorMsg);
        }
        if(addressErrorMsg != null) {
            serverAddressEditText.setError(addressErrorMsg);
        }
        if(socketPortErrorMsg != null) {
            socketPortEditText.setError(socketPortErrorMsg);
        }
        boolean isValidData = nameErrorMsg == null &&
                addressErrorMsg == null &&
                socketPortErrorMsg == null;
        if(isValidData) {
            editServer(server, name, address, socketPort);
            return true;
        }
        Toast.makeText(this, "Invalid data form", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean validateAndCreateServer(EditText serverNameEditText,
        EditText serverAddressEditText, EditText socketPortEditText) {

        String name = serverNameEditText.getText().toString().trim();
        String address = serverAddressEditText.getText().toString().trim();
        String socketPortStr = socketPortEditText.getText().toString().trim();
        int socketPort = socketPortStr.isEmpty()? 10000 : Integer.parseInt(socketPortStr);

        String nameErrorMsg = ((ServerModelValidator)serverValidator).validateName(name);
        String addressErrorMsg = ((ServerModelValidator)serverValidator).validateAddress(address);
        String socketPortErrorMsg = ((ServerModelValidator)serverValidator).validateSocketPort(socketPort);
        if(nameErrorMsg != null) {
            serverNameEditText.setError(nameErrorMsg);
        }
        if(addressErrorMsg != null) {
            serverAddressEditText.setError(addressErrorMsg);
        }
        if(socketPortErrorMsg != null) {
            socketPortEditText.setError(socketPortErrorMsg);
        }
        boolean isValidData = nameErrorMsg == null &&
                addressErrorMsg == null &&
                socketPortErrorMsg == null;
        if(isValidData) {
            createNewServer(name, address, socketPort);
            return true;
        }
        Toast.makeText(this, "Invalid data form", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void editServer(IServerModel server, String name, String address, int socketPort) {
        IServerRepository serverRepo = repositories.getServerRepository();
        server = serverRepo.edit(server, name, address, socketPort);
        Toast.makeText(this, "Server '" + server + "' modified", Toast.LENGTH_SHORT).show();
    }

    private void deleteServer(int position) {
        IServerModel server = servers.get(position);
        IServerRepository serverRepo = repositories.getServerRepository();
        String serverDescription = server.toString();
        servers.remove(server);
        serverRepo.delete(server);
        Toast.makeText(this, "Server '" + serverDescription + "' deleted", Toast.LENGTH_SHORT).show();
    }

    private void createNewServer(String name, String address, int socketPort) {
        IServerRepository serverRepo = repositories.getServerRepository();
        IServerModel server = serverRepo.create(name, address, socketPort);
        servers.add(server);
        serverListAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Server '" + server + "' added", Toast.LENGTH_SHORT).show();
    }

    private void dataBaseSetUp() {
        repositories = new RealmRepositories();
        repositories.openConnection(this);
    }
}
