package gcr.cli.android.models.realm;

import gcr.cli.android.models.IServerModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Server extends RealmObject implements IServerModel {

    @PrimaryKey
    private int id;
    @Required
    private String name;
    @Required
    private String address;
    private int socketPort;

    public Server(){}

    public Server(int id, String name, String address, int socketPort) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.socketPort = socketPort;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getSocketPort() {
        return socketPort;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }
}
