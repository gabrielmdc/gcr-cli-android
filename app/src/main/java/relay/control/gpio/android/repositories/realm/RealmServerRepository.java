package relay.control.gpio.android.repositories.realm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmResults;
import relay.control.gpio.android.models.IServerModel;
import relay.control.gpio.android.models.realm.Server;
import relay.control.gpio.android.repositories.IServerRepository;

public class RealmServerRepository implements IServerRepository {

    private Realm realm;

    public RealmServerRepository(Realm realm) {
        this.realm = realm;
    }

    public IServerModel create(String name, String address) {
        realm.beginTransaction();
        int id = getNextId();
        Server server = new Server(id, name, address);
        IServerModel realmObject = realm.copyToRealm(server);
        realm.commitTransaction();
        return realmObject;
    }

    public List<IServerModel> getAll() {
        RealmResults<Server> ss = realm.where(Server.class).findAll();
        List<IServerModel> servers = new ArrayList<>();
        servers.addAll(ss);
        return servers;
    }

    @Override
    public void delete(IServerModel server) {
        realm.beginTransaction();
        ((Server)server).deleteFromRealm();
        //realm.where(Server.class).equalTo("id", data.getId()).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    @Override
    public IServerModel edit(IServerModel server, String name, String address) {
        realm.beginTransaction();
        server.setName(name);
        server.setAddress(address);
        IServerModel realmObject = realm.copyToRealmOrUpdate((Server)server);
        realm.commitTransaction();
        return realmObject;
    }

    @Override
    public IServerModel findById(int id) {
        return realm.where(Server.class).equalTo("id", id).findFirst();
    }

    @Override
    public IServerModel findByAddress(String address) {
        return realm.where(Server.class).equalTo("address", address).findFirst();
    }

    private int getNextId() {
        RealmResults<Server> results = realm.where(Server.class).findAll();
        return (results.size() > 0)?
                new AtomicInteger(results.max("id").intValue()).incrementAndGet()
                : new AtomicInteger().intValue();
    }
}
