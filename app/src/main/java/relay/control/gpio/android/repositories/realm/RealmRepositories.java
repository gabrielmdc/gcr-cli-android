package relay.control.gpio.android.repositories.realm;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import relay.control.gpio.android.models.realm.Server;
import relay.control.gpio.android.repositories.IRepositories;
import relay.control.gpio.android.repositories.IServerRepository;

public class RealmRepositories implements IRepositories {

    private Realm realm;
    private IServerRepository serverRepository;

    public RealmRepositories(Context context) {
        Realm.init(context);
        setUpConfiguration();
        realm = Realm.getDefaultInstance();
        serverRepository = new RealmServerRepository(realm);
    }

    public IServerRepository getServerRepository() {
        return serverRepository;
    }

    public void closeConnection() {
        realm.close();
    }

    private static void setUpConfiguration() {
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
