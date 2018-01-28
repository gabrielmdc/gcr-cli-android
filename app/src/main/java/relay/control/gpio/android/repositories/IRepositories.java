package relay.control.gpio.android.repositories;

public interface IRepositories {
    IServerRepository getServerRepository();
    void closeConnection();
}
