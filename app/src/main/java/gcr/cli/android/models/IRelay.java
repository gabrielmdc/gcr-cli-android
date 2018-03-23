package gcr.cli.android.models;

public interface IRelay extends IModel {
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
