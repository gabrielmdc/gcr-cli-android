package relay.control.gpio.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import relay.control.gpio.android.R;
import relay.control.gpio.android.models.IRelay;

public class RelayListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private List<IRelay> relays;

    public RelayListAdapter(Context context, int layout, List<IRelay> relays) {
        this.context = context;
        this.layout = layout;
        this.relays = relays;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // View Holder Pattern
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_server, null);

            viewHolder = new ViewHolder();
            viewHolder.relayNameTextView = convertView.findViewById(R.id.relayNameTextView);
            viewHolder.relayStatusSwitch = convertView.findViewById(R.id.relayStateSwitch);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        IRelay currentRelay = getItem(position);
        viewHolder.relayNameTextView.setText(currentRelay.getName());
        viewHolder.relayStatusSwitch.setChecked(currentRelay.isOn());
        return convertView;
    }

    @Override
    public int getCount() {
        return relays.size();
    }

    @Override
    public IRelay getItem(int position) {
        return relays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        private TextView relayNameTextView;
        private Switch relayStatusSwitch;
    }
}
