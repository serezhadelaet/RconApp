package rconapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.rconapp.R;
import java.util.ArrayList;
import java.util.List;

public class SettingsServersList {

    public static ListView listViewServers;

    public static class ServersItemAdapter {
        private String name;
        private String address;

        public ServersItemAdapter(String name, String address){
            this.name = name;
            this.address = address;
        }

        public String getName(){
            return name;
        }

        public String getAddress(){
            return address;
        }
    }

    public static class ServersListAdapter extends ArrayAdapter<ServersItemAdapter> {

        private int resourceLayout;
        private Context mContext;

        public ServersListAdapter(Context context, int resource, List<ServersItemAdapter> items) {
            super(context, resource, items);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            final ServersItemAdapter item = getItem(position);
            TextView name = v.findViewById(R.id.listview_text);
            TextView adress = v.findViewById(R.id.listview_textAdress);

            if (item.address != null) {
                name.setText(item.getName());
                adress.setText(item.getAddress());
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < Config.getConfig().getServerList().size(); i++){
                            Server server = Config.getConfig().getServerList().get(i);
                            String address = server.IP + ":" + server.Port;
                            if (server.Name.equals(item.getName()) &&
                                    address.equals(item.getAddress())){
                                EditServer.EditServer(SettingsActivity.Instance, server);
                                break;
                            }
                        }
                    }
                });
            }
            else {
                TextView label = v.findViewById(R.id.listview_label);
                label.setText("Add new server");
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditServer.EditServer(SettingsActivity.Instance, null);
                    }
                });
            }
            return v;
        }

    }

    public static void UpdateServersList() {
        listViewServers = SettingsActivity.Instance.findViewById(R.id.activity_settings_listview);
        List<ServersItemAdapter> list = new ArrayList<>();
        for (int i = 0; i < Config.getConfig().getServerList().size(); i++){
            Server server = Config.getConfig().getServerList().get(i);
            list.add(new ServersItemAdapter(server.Name, server.IP + ":" + server.Port));
        }
        list.add(new ServersItemAdapter(null, null));
        ServersListAdapter customAdapter = new ServersListAdapter(SettingsActivity.Instance.getApplicationContext(), R.layout.listview_item, list);
        listViewServers.setAdapter(customAdapter);
    }
}
