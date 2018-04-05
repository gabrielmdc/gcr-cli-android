package gcr.cli.android.sockets;

import android.text.TextUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Protocol:
 * ------------------
 * :END
 * :STATUS:id_gpio,...:'ON' | 'OFF'
 * :EDIT:id_gpio:name:port:inverted
 * :ADD:name:port:inverted
 * :DELETE:id_gpio
 * ------------------
 */
public class Sender {

    private static final String STATUS_ON = "ON";
    private static final String STATUS_OFF = "OFF";

    private static final String ACTION_END = "END";
    private static final String ACTION_STATUS = "STATUS";
    private static final String ACTION_EDIT = "EDIT";
    private static final String ACTION_ADD = "ADD";
    private static final String ACTION_DELETE = "DELETE";

    private Socket socket;
    private DataOutputStream out;

    public void openConnection(String address, int port) throws IOException {
        socket = new Socket(address, port);
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void closeConnection() throws IOException {
        if(isConnected()) {
            try {
                sendMessage(ACTION_END);
            } finally {
                socket.close();
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void sendStatusOn(int... relayIds) throws IOException {
        if(relayIds.length < 1) {
            return;
        }
        String strRelayIds = getRelayIdsString(relayIds);
        sendMessage(ACTION_STATUS, strRelayIds, STATUS_ON);
    }

    public void sendStatusOff(int... relayIds) throws IOException {
        if(relayIds.length < 1) {
            return;
        }
        String strRelayIds = getRelayIdsString(relayIds);
        sendMessage(ACTION_STATUS, strRelayIds, STATUS_OFF);
    }

    public void sendEdit(int relayId, String name, int port, boolean inverted) throws IOException {
        if(name.isEmpty()) {
            return;
        }
        String invertedStr = inverted? "1" : "0";
        sendMessage(ACTION_EDIT, relayId+"", name, port+"", invertedStr);
    }

    public void sendAdd(String name, int port, boolean inverted) throws IOException {
        if(name.isEmpty()) {
            return;
        }
        String invertedStr = inverted? "1" : "0";
        sendMessage(ACTION_ADD, name, port+"", invertedStr);
    }

    public void sendDelete(int relayId) throws IOException {
        String relayIdStr = relayId + "";
        sendMessage(ACTION_DELETE, relayIdStr);
    }

    private static String getRelayIdsString(int[] relayIds) {
        List<String> relayIdsStr = new ArrayList<>();
        for(int relayId : relayIds) {
            relayIdsStr.add(relayId+"");
        }
        String strRelayIds = TextUtils.join(",", relayIdsStr);
        return strRelayIds;
    }

    private void sendMessage(String... tokens) throws IOException {
        final String PRE_MSG = ":";
        if(socket.isClosed() || tokens.length < 1) {
            return;
        }
        String msg = PRE_MSG;
        msg += TextUtils.join(":", tokens);
        out.writeUTF(msg);
    }
}
