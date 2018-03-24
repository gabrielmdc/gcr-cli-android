package gcr.cli.android.repositories;

import java.util.List;

import gcr.cli.android.models.IServer;

public interface IServerRepository {
    IServer findById(int id);
    IServer findByAddressAndSocketPort(String address, int socketPort);
    IServer create(String name, String address, int socketPort);
    List<IServer> getAll();
    void delete(IServer server);
    IServer edit(IServer server, String name, String address, int socketPort);
}
