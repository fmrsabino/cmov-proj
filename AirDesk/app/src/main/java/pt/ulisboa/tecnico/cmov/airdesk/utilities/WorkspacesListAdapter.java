package pt.ulisboa.tecnico.cmov.airdesk.utilities;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import pt.ulisboa.tecnico.cmov.airdesk.R;


public class WorkspacesListAdapter extends BaseAdapter
{
    public static class Content {
        private String ws_name;
        private String quota;

        public Content(String ws_name, String quota) {
            this.ws_name = ws_name;
            this.quota = quota + " Mb";
        }

        public String getQuota() {
            return quota;
        }

        public void setQuota(String quota) {
            this.quota = quota;
        }

        public String getWs_name() {
            return ws_name;
        }

        public void setWs_name(String ws_name) {
            this.ws_name = ws_name;
        }
    }

    private Activity activity;
    private List<Content> list;

    public WorkspacesListAdapter(Activity activity, List<Content> list) {
        super();
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Content getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView First;
        TextView Second;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater =  activity.getLayoutInflater();

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listview_multicol, null);
            holder = new ViewHolder();
            holder.First = (TextView) convertView.findViewById(R.id.workspace);
            holder.Second = (TextView) convertView.findViewById(R.id.quota);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.First.setText(list.get(position).getWs_name());
        holder.Second.setText(list.get(position).getQuota());


        return convertView;
    }

}