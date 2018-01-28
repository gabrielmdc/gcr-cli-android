package relay.control.gpio.android.repositories;

import relay.control.gpio.android.models.IServerModel;

public interface IRepositories {
    IServerRepository getServerRepository();
    void closeConnection();
}
