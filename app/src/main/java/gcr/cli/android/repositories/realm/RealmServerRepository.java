package gcr.cli.android.repositories.realm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import gcr.cli.android.models.IServer;
import gcr.cli.android.models.realm.Server;
import gcr.cli.android.repositories.IServerRepository;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmServerRepository implements IServerRepository {

    private Realm realm;

    public RealmServerRepository(Realm realm) {
        this.realm = realm;
    }

    public IServer create(String name, String address, int socketPort) {
        realm.beginTransaction();
        int id = getNextId();
        Server server = new Server(id, name, address, socketPort);
        IServer realmObject = realm.copyToRealm(server);
        realm.commitTransaction();
        return realmObject;
    }

    public List<IServer> getAll() {
        RealmResults<Server> ss = realm.where(Server.class).findAll();
        List<IServer> servers = new ArrayList<>();
        servers.addAll(ss);
        return servers;
    }

    @Override
    public void delete(IServer server) {
        realm.beginTransaction();
        ((Server)server).deleteFromRealm();
        //realm.where(Server.class).equalTo("id", data.getId()).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    @Override
    public IServer edit(IServer server, String name, String address, int socketPort) {
        realm.beginTransaction();
        server.setName(name);
        server.setAddress(address);
        server.setSocketPort(socketPort);
        IServer realmObject = realm.copyToRealmOrUpdate((Server)server);
        realm.commitTransaction();
        return realmObject;
    }

    @Override
    public IServer findById(int id) {
        return realm.where(Server.class).equalTo("id", id).findFirst();
    }

    @Override
    public IServer findByAddressAndSocketPort(String address, int socketPort) {
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
