package relay.control.gpio.android.models;

public interface IServerModel {
    int getId();
    String getName();
    String getAddress();
    void setName(String name);
    void setAddress(String address);
}