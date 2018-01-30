package relay.control.gpio.android.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import relay.control.gpio.android.R;
import relay.control.gpio.android.models.IRelay;
import relay.control.gpio.android.sockets.Sender;

public class RelayListAdapter extends BaseAdapter implements Observer {

    private Context context;
    private int layout;
    private SparseArray<IRelay> relays;
    private Sender sender;

    public RelayListAdapter(Context context, int layout, Sender sender) {
        this.context = context;
        this.layout = layout;
        this.sender = sender;
        this.relays = new SparseArray<>();
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
        viewHolder.relayStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String status = isChecked? Sender.STATUS_ON : Sender.STATUS_OFF;
                sendAction(currentRelay.getId(), status);
            }
        });
        return convertView;
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
        this.notifyDataSetChanged();
    }

    private void sendAction(int relayId, String status) {
        final String msg = "ACTION:" + relayId + ":" + status;
        System.out.println(msg);
        new Thread(new Runnable() {// TODO use AsynkTask
            public void run() {
                try {
                    sender.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }

    private static class ViewHolder {
        private TextView relayNameTextView;
        private Switch relayStatusSwitch;
    }
}
