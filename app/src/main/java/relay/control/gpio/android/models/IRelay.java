package relay.control.gpio.android.models;

public interface IRelay {
    int getId();
    boolean toDelete();
    boolean isInverted();
    void setName(String name);
    void setGpio(int gpio);
    void setStatus(boolean status);
    void setInverted(boolean inverted);
    String getName();
    int getGpio();
    boolean isOn();
}
