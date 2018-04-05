package gcr.cli.android.models;

public interface IServer extends IModel {
    int getId();
    String getName();
    String getAddress();
    int getSocketPort();
    void setName(String name);
    void setAddress(String address);
    void setSocketPort(int socketPort);
}