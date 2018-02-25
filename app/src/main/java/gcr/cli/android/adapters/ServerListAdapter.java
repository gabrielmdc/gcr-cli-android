package gcr.cli.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import gcr.cli.android.models.IServerModel;
import gcr.cli.android.R;

public class ServerListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private List<IServerModel> servers;

    public ServerListAdapter(Context context, int layout, List<IServerModel> servers) {
        this.context = context;
        this.layout = layout;
        this.servers = servers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // View Holder Pattern
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_server, null);

            viewHolder = new ViewHolder();
            viewHolder.serverNameTextView = convertView.findViewById(R.id.serverNameTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        IServerModel currentServer = getItem(position);
        viewHolder.serverNameTextView.setText(currentServer.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return servers.size();
    }

    @Override
    public IServerModel getItem(int position) {
        return servers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        private TextView serverNameTextView;
    }
}
