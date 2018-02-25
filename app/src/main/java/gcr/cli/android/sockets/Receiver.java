package gcr.cli.android.sockets;

import android.util.SparseArray;

import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.models.Relay;

public class Receiver extends Observable implements Runnable {

    private Socket socket;

    public Receiver(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        DataInputStream in;
        try {
            in = new DataInputStream(socket.getInputStream());
            while (socket != null && !socket.isClosed()){
                readResponse(in);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Receiver termina...");
    }

    static public SparseArray<IRelay> getRelaysFromJsonMsg(String msg) {
        SparseArray<IRelay> relays = new SparseArray<>();
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
                Relay relay = new Relay(id, name, gpio, status, inverted, toDelete);
                relays.put(id, relay);
            }
        } catch (JSONException e) {
            e.getStackTrace();
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

            //SparseArray<IRelay> relays = getRelaysFromJsonMsg(msgBack);
            setChanged();
            notifyObservers(msgBack);
            return;
        }
        socket.close();
    }
}
