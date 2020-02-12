package com.example.rconapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class SettingsActivity extends Activity {

    public static SettingsActivity Instance;

    public class ListAdapter extends ArrayAdapter<Config.Server> {

        private int resourceLayout;
        private Context mContext;

        public ListAdapter(Context context, int resource, List<Config.Server> items) {
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

            final Config.Server server = getItem(position);

            if (server != null) {
                TextView name = (TextView) v.findViewById(R.id.listview_text);

                if (name != null) {
                    name.setText(server.Name);
                }
                TextView adress = (TextView) v.findViewById(R.id.listview_textAdress);

                if (adress != null) {
                    adress.setText(server.IP + ":" + server.Port);
                }
                Button delete = (Button) v.findViewById(R.id.listview_delete);
                delete.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Config.getConfig().ServerList.remove(server);
                        Config.saveConfig();
                        RconManager.remove(server);
                        MainActivity.Instance.UpdateMenu();
                        UpdateServersList();
                        return false;
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Instance, "Long tap to delete...",
                                Toast.LENGTH_LONG).show();
                    }
                });
                Button edit = (Button) v.findViewById(R.id.listview_edit);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditServerActivity.serverToEdit = server;
                        startActivity(new Intent(Instance, EditServerActivity.class));
                    }
                });
            }

            return v;
        }

    }

    @Override
    protected  void onResume(){
        super.onResume();
        UpdateServersList();
    }

    private void UpdateServersList(){
        ListView listView = (ListView)findViewById(R.id.server_add_listview);
        ListAdapter customAdapter = new ListAdapter(this,
                R.layout.listview_item, Config.getConfig().ServerList);
        listView.setAdapter(customAdapter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final EditText eSteamAPI = (EditText)findViewById(R.id.server_add_steamapi);
        final EditText eFilter = (EditText)findViewById(R.id.server_add_wordsfilter);
        final EditText ePrefixes = (EditText)findViewById(R.id.server_add_chatprefixes);
        final EditText eNotify = (EditText)findViewById(R.id.server_add_notifymessages);

        final Config config = Config.getConfig();

        eSteamAPI.setText(config.SteamAPIKey);
        eFilter.setText(config.FilteredMessages);
        ePrefixes.setText(config.ChatPrefixes);
        eNotify.setText(config.NotificationMessages);

        UpdateServersList();

        Button save = (Button) findViewById(R.id.activity_settings_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                config.SteamAPIKey = eSteamAPI.getText().toString();
                config.FilteredMessages = eFilter.getText().toString();
                config.ChatPrefixes = ePrefixes.getText().toString();
                config.NotificationMessages = eNotify.getText().toString();

                Config.saveConfig();

                // Hide a keyboard
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
            }
        });

        Button addServer = (Button) findViewById(R.id.activity_settings_addserver);
        addServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Instance, NewServerActivity.class));
            }
        });

    }

}
