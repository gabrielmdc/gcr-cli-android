package gcr.cli.android.repositories.realm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import gcr.cli.android.models.IServerModel;
import gcr.cli.android.models.realm.Server;
import gcr.cli.android.repositories.IServerRepository;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmServerRepository implements IServerRepository {

    private Realm realm;

    public RealmServerRepository(Realm realm) {
        this.realm = realm;
    }

    public IServerModel create(String name, String address, int socketPort) {
        realm.beginTransaction();
        int id = getNextId();
        Server server = new Server(id, name, address, socketPort);
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
    public IServerModel edit(IServerModel server, String name, String address, int socketPort) {
        realm.beginTransaction();
        server.setName(name);
        server.setAddress(address);
        server.setSocketPort(socketPort);
        IServerModel realmObject = realm.copyToRealmOrUpdate((Server)server);
        realm.commitTransaction();
        return realmObject;
    }

    @Override
    public IServerModel findById(int id) {
        return realm.where(Server.class).equalTo("id", id).findFirst();
    }

    @Override
    public IServerModel findByAddressAndSocketPort(String address, int socketPort) {
        return realm.where(Server.class)
                .equalTo("address", address)
                .and()
                .equalTo("socketPort", socketPort)
                .findFirst();
    }

    private int getNextId() {
        RealmResults<Server> results = realm.where(Server.class).findAll();
        return (results.size() > 0)?
                new AtomicInteger(results.max("id").intValue()).incrementAndGet()
                : new AtomicInteger().intValue();
    }
}
