package gcr.cli.android.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

import gcr.cli.android.sockets.ServerConnection;
import gcr.cli.android.R;
import gcr.cli.android.models.IRelay;

public class RelayListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private SparseArray<IRelay> relays;
    private ServerConnection serverConnection;

    public RelayListAdapter(Context context, int layout, SparseArray<IRelay> relays,
                            ServerConnection serverConnection) {
        this.context = context;
        this.layout = layout;
        this.relays = relays;
        this.serverConnection = serverConnection;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_relay, null);

            viewHolder = new ViewHolder();
            viewHolder.relayNameTextView = convertView.findViewById(R.id.relayNameTextView);
            viewHolder.relayStateBtn = convertView.findViewById(R.id.relayStateBtn);
            viewHolder.relayOptionsBtn = convertView.findViewById(R.id.relayOptionsBtn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final IRelay currentRelay = getItem(position);
        viewHolder.relayNameTextView.setText(currentRelay.getName());
        int stateBtnImage = currentRelay.isOn()? R.drawable.ic_power_on : R.drawable.ic_power_off;
        viewHolder.relayStateBtn.setImageResource(stateBtnImage);
        viewHolder.relayStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean nextStatus = !currentRelay.isOn();
                ActionSenderTask actionSenderTask = new ActionSenderTask(currentRelay.getId(), nextStatus);
                actionSenderTask.execute();
                viewHolder.relayStateBtn.setImageResource(R.drawable.ic_power_waiting);
            }
        });
        viewHolder.relayOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.showContextMenuForChild(v);
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
        int key = relays.keyAt(position);
        return key;
    }

    private static class ViewHolder {
        private TextView relayNameTextView;
        private ImageButton relayStateBtn;
        private ImageButton relayOptionsBtn;
    }

    private class ActionSenderTask extends AsyncTask<Void, Void, Boolean> {

        private int relayId;
        private boolean status;

        ActionSenderTask(int relayId, boolean status) {
            this.relayId = relayId;
            this.status = status;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if(status) {
                    serverConnection.turnOnRelay(relayId);
                    return true;
                }
                serverConnection.turnOffRelay(relayId);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
