package relay.control.gpio.android.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import relay.control.gpio.android.R;
import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.sockets.Sender;
import relay.control.gpio.android.sockets.ServerConnection;

public class RelayListAdapter extends BaseAdapter implements Observer{

    private Context context;
    private int layout;

    private SparseArray<IRelay> relays;
    private ServerConnection serverConnection;

    public RelayListAdapter(Context context, int layout) {
        this.context = context;
        this.layout = layout;

        this.relays = new SparseArray<>();
        serverConnection = new ServerConnection(context);
        serverConnection.addObserver(this);
    }

    public void startConnection(int port, String address) {
        ConnectionTask connectionTask = new ConnectionTask(port, address);
        connectionTask.execute();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // View Holder Pattern
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_relay, null);

            viewHolder = new ViewHolder();
            viewHolder.relayNameTextView = convertView.findViewById(R.id.relayNameTextView);
            viewHolder.relayStatusSwitch = convertView.findViewById(R.id.relayStateSwitch);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final IRelay currentRelay = getItem(position);
        viewHolder.relayNameTextView.setText(currentRelay.getName());
        viewHolder.relayStatusSwitch.setChecked(currentRelay.isOn());
        viewHolder.relayStatusSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = currentRelay.isOn()? Sender.STATUS_OFF : Sender.STATUS_ON;
                ActionSenderTask actionSenderTask = new ActionSenderTask(currentRelay.getId(), status);
                actionSenderTask.execute();
            }
        });
        return convertView;
    }

    @Override
    public void update(Observable o, Object arg) {
        SparseArray<IRelay> relaysFromReceiver = (SparseArray<IRelay>)arg;
        for(int i = 0; i < relaysFromReceiver.size(); i++) {
            int key = relaysFromReceiver.keyAt(i);
            IRelay relayFromReceiver = relaysFromReceiver.get(key);
            // If there is Not any button for that relay
            if(relays.get(key) == null) {
                relays.put(key, relayFromReceiver);
                continue;
            }
            if(relayFromReceiver.toDelete()) {
                relays.remove(key);
                continue;
            }
            relays.put(key, relayFromReceiver);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return relays.size();
    }

    @Override
    public IRelay getItem(int position) {
        int key = relays.keyAt(position);
        return relays.get(key);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        private TextView relayNameTextView;
        private Switch relayStatusSwitch;
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {

        private String address;
        private int port;

        ConnectionTask(int port, String address) {
            this.address = address;
            this.port = port;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                serverConnection.connect(address, port);
                if(serverConnection.senderIsConnected()){
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private class ActionSenderTask extends AsyncTask<Void, Void, Boolean> {

        private int relayId;
        private String status;

        ActionSenderTask(int relayId, String status) {
            this.relayId = relayId;
            this.status = status;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String msg = "STATUS:" + relayId + ":" + status;
                serverConnection.sendMessage(msg);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
