package relay.control.gpio.android.models;

public class Relay implements IRelay {

    private int id;
    private String name;
    private int gpio;
    private boolean status;
    private boolean inverted;
    private boolean toDelete;

    public Relay(int id, String name, int gpio, boolean status, boolean inverted, boolean toDelete) {
        this.id = id;
        this.name = name;
        this.gpio = gpio;
        this.status = status;
        this.inverted = inverted;
        this.toDelete = toDelete;
    }

    public boolean toDelete() {
        return toDelete;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGpio(int gpio) {
        this.gpio = gpio;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getGpio(){
        return gpio;
    }

    public boolean isOn(){
        return status;
    }
}
