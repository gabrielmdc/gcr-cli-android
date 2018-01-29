package relay.control.gpio.android.sockets;

import android.widget.Toast;

import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import relay.control.gpio.android.models.Relay;

public class Receiver extends Observable implements Runnable {

    private Socket socket;

    Receiver(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream in;
        try {
            in = new DataInputStream(socket.getInputStream());
            while (socket.isConnected()){
                readResponse(in);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Relay> getRelaysFromJsonMsg(String msg) {
        List<Relay> relays = new ArrayList<>();
        JSONArray arr;
        try {
            arr = new JSONArray(msg);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int id = obj.getInt("id");
                String name = obj.getString("name");
                int gpio = obj.getInt("port");
                boolean status = obj.getString("status").equals("1");
                boolean toDelete = Boolean.parseBoolean(obj.getString("deleted"));
                boolean inverted = Boolean.parseBoolean(obj.getString("inverted"));
                System.out.println(inverted);
                Relay relay = new Relay(id, name, gpio, status, inverted, toDelete);
                relays.add(relay);
            }
        } catch (JSONException e) {
            //Toast.makeText()
        }
        return relays;
    }

    /**
     * Require: the socket is connected
     * @param in
     * @throws IOException
     */
    private void readResponse(DataInputStream in) throws IOException {
        byte[] buff = new byte[200];
        System.out.println("Esperando respuesta...");
        if(in.read(buff) > 0){
            String msgBack = new String(buff,"UTF-8").trim();
            System.out.println("Recibido Mensaje: " + msgBack);

            List<Relay> relays = getRelaysFromJsonMsg(msgBack);
            setChanged();
            notifyObservers(relays);
        }
    }
}
