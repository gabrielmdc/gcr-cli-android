package relay.control.gpio.android.app;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import relay.control.gpio.android.R;
import relay.control.gpio.android.activities.RelayActivity;
import relay.control.gpio.android.adapters.ServerListAdapter;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.models.realm.Server;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;
import relay.control.gpio.android.repositories.realm.RealmRepositories;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView serverListView;
    private List<IServerModel> servers;
    private ServerListAdapter serverListAdapter;
    private FloatingActionButton createServerFloatingButton;
    private IRepositories repositories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        final EditText serverNameEditText = viewInflated.findViewById(R.id.serverNameEditText);
        final EditText serverAddressEditText = viewInflated.findViewById(R.id.serverAddressEditText);
        final EditText socketPortEditText = viewInflated.findViewById(R.id.socketPortEditText);

        serverNameEditText.setText(server.getName());
        serverAddressEditText.setText(server.getAddress());
        socketPortEditText.setText(server.getSocketPort()+"");

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String serverName = serverNameEditText.getText().toString().trim();
                String serverAddress = serverAddressEditText.getText().toString().trim();
                int socketPort = Integer.parseInt(socketPortEditText.getText().toString().trim());
                if(serverName.length() > 0 && serverAddress.length() > 0) {
                    editServer(server, serverName, serverAddress, socketPort);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCreateServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new server");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_server, null);
        builder.setView(viewInflated);

        final EditText serverNameEditText = viewInflated.findViewById(R.id.serverNameEditText);
        final EditText serverAddressEditText = viewInflated.findViewById(R.id.serverAddressEditText);
        final EditText socketPortEditText = viewInflated.findViewById(R.id.socketPortEditText);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String serverName = serverNameEditText.getText().toString().trim();
                String serverAddress = serverAddressEditText.getText().toString().trim();
                int socketPort = Integer.parseInt(socketPortEditText.getText().toString().trim());
                if(serverName.length() > 0 && serverAddress.length() > 0) {
                    createNewServer(serverName, serverAddress, socketPort);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
