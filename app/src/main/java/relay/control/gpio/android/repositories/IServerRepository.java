package relay.control.gpio.android.repositories;

import java.util.List;

import relay.control.gpio.android.models.IServerModel;

public interface IServerRepository {
    IServerModel findById(int id);
    IServerModel findByAddress(String address);
    IServerModel create(String name, String address);
    List<IServerModel> getAll();
    void delete(IServerModel server);
}
