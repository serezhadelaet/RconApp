package com.example.rconapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    public static SettingsActivity Instance;

    private boolean isServerListOpened = false;

    private ListView listViewServers;

    class AdapterItem {
        private String name;
        private String address;

        public AdapterItem(String name, String address){
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

    public class ListAdapter extends ArrayAdapter<AdapterItem> {

        private int resourceLayout;
        private Context mContext;

        public ListAdapter(Context context, int resource, List<AdapterItem> items) {
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

            final AdapterItem item = getItem(position);
            TextView name = v.findViewById(R.id.listview_text);
            TextView adress = v.findViewById(R.id.listview_textAdress);

            if (item.address != null) {
                name.setText(item.getName());
                adress.setText(item.getAddress());
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < Config.getConfig().ServerList.size(); i++){
                            Config.Server server = Config.getConfig().ServerList.get(i);
                            String address = server.IP + ":" + server.Port;
                            if (server.Name.equals(item.getName()) &&
                                    address.equals(item.getAddress())){
                                EditServer.EditServer(Instance, server);
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
                        EditServer.EditServer(Instance, null);
                    }
                });
            }
            return v;
        }

    }

    public void UpdateServersList() {
        List<AdapterItem> list = new ArrayList<>();
        for (int i = 0; i < Config.getConfig().ServerList.size(); i++){
            Config.Server server = Config.getConfig().ServerList.get(i);
            list.add(new AdapterItem(server.Name, server.IP + ":" + server.Port));
        }
        list.add(new AdapterItem(null, null));
        ListAdapter customAdapter = new ListAdapter(this, R.layout.listview_item, list);
        listViewServers.setAdapter(customAdapter);
    }

    @Override
    public void onBackPressed() {
        if (isServerListOpened){
            reCreate();
            isServerListOpened = false;
        }
        else{
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isServerListOpened){
                    reCreate();
                    isServerListOpened = false;
                }
                else{
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reCreate(){
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText eSteamAPI = findViewById(R.id.server_add_steamapi);
        final EditText eFilter = findViewById(R.id.server_add_wordsfilter);
        final EditText ePrefixes = findViewById(R.id.server_add_chatprefixes);
        final EditText eNotify = findViewById(R.id.server_add_notifymessages);

        final Config config = Config.getConfig();

        eSteamAPI.setText(config.SteamAPIKey);
        eFilter.setText(config.FilteredMessages);
        ePrefixes.setText(config.ChatPrefixes);
        eNotify.setText(config.NotificationMessages);

        Button save = findViewById(R.id.activity_settings_save);
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

        Button servers = findViewById(R.id.activity_settings_servers);
        servers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setTitle("Manage servers");
                isServerListOpened = true;
                setContentView(R.layout.layout_serverlist);
                listViewServers = findViewById(R.id.activity_settings_listview);
                UpdateServersList();
            }
        });

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Instance = this;
        super.onCreate(savedInstanceState);
        reCreate();
    }

}
