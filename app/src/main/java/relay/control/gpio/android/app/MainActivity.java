package relay.control.gpio.android.app;

import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import relay.control.gpio.android.R;
import relay.control.gpio.android.adapters.ServerListAdapter;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.models.realm.Server;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;
import relay.control.gpio.android.repositories.realm.RealmRepositories;

public class MainActivity extends AppCompatActivity {

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
        repositories = new RealmRepositories(this);
        IServerRepository serverRepo = repositories.getServerRepository();
        servers = serverRepo.getAll();

        serverListAdapter = new ServerListAdapter(this, R.layout.list_server, servers);

        serverListView = findViewById(R.id.serverListView);
        serverListView.setAdapter(serverListAdapter);
        registerForContextMenu(serverListView);

        createServerFloatingButton = findViewById(R.id.createServerFloatingButton);
        createServerFloatingButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateServerDialog();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IServerModel serverSelected = servers.get(info.position);
        menu.setHeaderTitle(serverSelected.getName());
        inflater.inflate(R.menu.server_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.edit_server:
                break;
            case R.id.delete_server:
                deleteServer(servers.get(info.position));
                servers.remove(info.position);
                serverListAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showCreateServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new server");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.create_server_dialog, null);
        builder.setView(viewInflated);

        final EditText serverNameEditText = viewInflated.findViewById(R.id.serverNameEditText);
        final EditText serverAddressEditText = viewInflated.findViewById(R.id.serverAddressEditText);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String serverName = serverNameEditText.getText().toString().trim();
                String serverAddress = serverAddressEditText.getText().toString().trim();
                if(serverName.length() > 0 && serverAddress.length() > 0) {
                    createNewServer(serverName, serverAddress);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteServer(IServerModel server) {
        IServerRepository serverRepo = repositories.getServerRepository();
        serverRepo.delete(server);
        Toast.makeText(this, "Server '" + server + "' deleted", Toast.LENGTH_SHORT).show();
    }

    private void createNewServer(String name, String address) {
        IServerRepository serverRepo = repositories.getServerRepository();
        IServerModel server = serverRepo.create(name, address);
        servers.add(server);
        serverListAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Server '" + server + "' added", Toast.LENGTH_SHORT).show();
    }
}
