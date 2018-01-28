package relay.control.gpio.android.models.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import relay.control.gpio.android.models.IServerModel;

public class Server extends RealmObject implements IServerModel {

    @PrimaryKey
    private int id;
    @Required
    private String name;
    @Required
    private String address;

    public Server(){}

    public Server(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }
}
