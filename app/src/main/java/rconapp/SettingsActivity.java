package rconapp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.example.rconapp.R;

public class SettingsActivity extends AppCompatActivity {

    public static SettingsActivity Instance;

    private boolean isAnotherLayoutOpened = false;

    @Override
    public void onBackPressed() {
        if (isAnotherLayoutOpened){
            reCreate();
            isAnotherLayoutOpened = false;
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
                if (isAnotherLayoutOpened){
                    reCreate();
                    isAnotherLayoutOpened = false;
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

        final Config config = Config.getConfig();

        eSteamAPI.setText(config.SteamAPIKey);
        eFilter.setText(config.FilteredMessages);
        ePrefixes.setText(config.ChatPrefixes);

        Button save = findViewById(R.id.activity_settings_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                config.SteamAPIKey = eSteamAPI.getText().toString();
                config.FilteredMessages = eFilter.getText().toString();
                config.ChatPrefixes = ePrefixes.getText().toString();

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
                isAnotherLayoutOpened = true;
                setContentView(R.layout.layout_serverlist);
                SettingsServersList.UpdateServersList();
            }
        });

        Button notifications = findViewById(R.id.activity_settings_notifications);
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setTitle("Notifications");
                isAnotherLayoutOpened = true;
                setContentView(R.layout.layout_notifications);
                SettingsNotificationsList.HandleButtonAddNotification();
                SettingsNotificationsList.UpdateNotificationsList();
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
