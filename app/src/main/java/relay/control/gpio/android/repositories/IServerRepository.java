package relay.control.gpio.android.repositories;

import java.util.List;

import relay.control.gpio.android.models.IServerModel;

public interface IServerRepository {
    IServerModel findById(int id);
    IServerModel findByAddressAndSocketPort(String address, int socketPort);
    IServerModel create(String name, String address, int socketPort);
    List<IServerModel> getAll();
    void delete(IServerModel server);
    IServerModel edit(IServerModel server, String name, String address, int socketPort);
}
