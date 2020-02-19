package com.example.rconapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.customview.widget.ViewDragHelper;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.os.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.neovisionaries.ws.client.WebSocketState;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static EditText consoleInput;

    public static MainActivity Instance;

    public static LinearLayout linearLayoutConsoleInput;

    private LinearLayout layoutPlayers;

    private DrawerLayout drawer;

    private Intent serviceIntent;

    public static Boolean isPlayersOpened = false;


    public static boolean isPlayersTabOpen;

    public static boolean isNavBarOpen(){
        return Instance.drawer.isDrawerOpen(GravityCompat.START);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_console:

                    if (consoleInput.getText().toString().length() == 0 ||
                            consoleInput.getText().toString().equals("Type a message")) {
                        consoleInput.setText("Type a command");
                        consoleInput.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }

                    isPlayersTabOpen = false;
                    messagesView.setVisibility(View.VISIBLE);
                    layoutPlayers.setVisibility(View.INVISIBLE);
                    chatMessagesView.setVisibility(View.INVISIBLE);
                    linearLayoutConsoleInput.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_chat:

                    if (consoleInput.getText().length() == 0 ||
                            consoleInput.getText().toString().equals("Type a command")) {
                        consoleInput.setText("Type a message");
                        consoleInput.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }

                    isPlayersTabOpen = false;
                    chatMessagesView.setVisibility(View.VISIBLE);
                    layoutPlayers.setVisibility(View.INVISIBLE);
                    messagesView.setVisibility(View.INVISIBLE);
                    linearLayoutConsoleInput.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_players:
                    isPlayersTabOpen = true;
                    if (!isPlayersOpened){
                        if (PlayersAdapter.getAdapter().playersWithoutAvatar.size() > 0){
                            new PlayerInfo.DownLoadAvatars(new ArrayList<>(PlayersAdapter.getAdapter().playersWithoutAvatar)).execute();
                        }
                        PlayersAdapter.getAdapter().playersWithoutAvatar.clear();
                        isPlayersOpened = true;
                    }
                    View view = MainActivity.Instance.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    layoutPlayers.setVisibility(View.VISIBLE);
                    chatMessagesView.setVisibility(View.INVISIBLE);
                    messagesView.setVisibility(View.INVISIBLE);
                    linearLayoutConsoleInput.setVisibility(View.INVISIBLE);
                    return true;
            }
            return false;
        }
    };

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        //If we click on a settings button
        if (id == Config.getConfig().ServerList.size()) {
            isPlayersTabOpen = false;
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            drawer.closeDrawer(GravityCompat.START);
        }
        else if (id == Config.getConfig().ServerList.size()+1) {
            getApplicationContext().stopService(serviceIntent);
            finishAffinity();
            System.exit(0);
        }
        else {
            Config.Server server = Config.getConfig().ServerList.get(id);
            server.Enabled = !server.Enabled;
            RconManager.Rcons.get(server).OnActivityChange();
            UpdateServers();
            if (server.Enabled)
                item.setIcon(R.drawable.ic_radio_button_checked_accent_24dp);
            else {
                item.setIcon(R.drawable.ic_radio_button_unchecked_accent_24dp);
                Output(new Message(server, "Disconnected", false, false));
            }
            Config.saveConfig();
        }
        return true;
    }

    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private MessageAdapter chatMessageAdapter;
    private ListView chatMessagesView;

    private void InitMessageAdapters(){
        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        chatMessageAdapter = new MessageAdapter(this);
        chatMessagesView = findViewById(R.id.chatmessages_view);
        chatMessagesView.setAdapter(chatMessageAdapter);
    }

    public static void Output(final Message message) {
        if (message.isChatMessage())
            OutputChat(message);
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final boolean canScrollNow = Instance.messagesView.canScrollVertically(1);

                Instance.messageAdapter.add(message);
                if (!canScrollNow) {
                    Instance.messagesView.setSelection(Instance.messageAdapter.getCount() - 1);
                }
            }
        });
    }

    public static void OutputChat(String prefix, String text) {
        final Message message = new Message("[" + prefix + "] " + text, false, true);
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final boolean canScrollNow = Instance.chatMessagesView.canScrollVertically(1);

                Instance.chatMessageAdapter.add(message);

                if (!canScrollNow) {
                    Instance.chatMessagesView.setSelection(Instance.chatMessageAdapter.getCount() - 1);
                }
            }
        });
    }

    public static void OutputChat(final Message message) {
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final boolean canScrollNow = Instance.chatMessagesView.canScrollVertically(1);

                Instance.chatMessageAdapter.add(message);

                if (!canScrollNow) {
                    Instance.chatMessagesView.setSelection(Instance.chatMessageAdapter.getCount() - 1);
                }
            }
        });
    }

    private void CreateService() {
        if (serviceIntent != null) return;
        serviceIntent = new Intent(getApplicationContext(), AppService.class);
        getApplicationContext().startService(serviceIntent);
    }

    private void enableService(){
        AppService.setEnable();
    }

    private void disableService(){
        AppService.setDisable();
    }

    private void InitConsoleInput(){

        consoleInput = (EditText) findViewById(R.id.consoleInput);

        consoleInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (consoleInput.getText().toString().equals("Type a message") ||
                        consoleInput.getText().toString().equals("Type a command")) {
                    consoleInput.setText("");
                    consoleInput.setTextColor(getResources().getColor(R.color.colorAccent));
                }
                return false;
            }
        });

        consoleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String prefix = "";
                if (chatMessagesView.getVisibility() == View.VISIBLE){
                    prefix+="say ";
                }
                String text = consoleInput.getText().toString();
                SendToRcons(prefix + text);
                consoleInput.setText("");
                consoleInput.requestFocus();
                if (prefix != ""){
                    OutputChat("You", text);
                }
                handled = true;
            }
            return handled;
        }
        });
    }

    private PlayersAdapter playersAdapter;
    private ListView playerListView;

    private void initPlayerList(){
        layoutPlayers = findViewById(R.id.layout_players);
        playersAdapter = new PlayersAdapter(this);
        playerListView = findViewById(R.id.playerlist);
        playerListView.setAdapter(playersAdapter);

        playerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayerInfo.ShowInfo(Instance, (Player)playersAdapter.getItem(position));
            }
        });
    }

    private void InitDrawer(){
        drawer = findViewById(R.id.container);
        try{
            Field mDragger = drawer.getClass().getDeclaredField(
                    "mLeftDragger");//mRightDragger for right obviously
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger
                    .get(drawer);

            Field mEdgeSize = draggerObj.getClass().getDeclaredField(
                    "mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);

            mEdgeSize.setInt(draggerObj, edge * 5);
        }
        catch (Exception ex){

        }
    }

    Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Instance = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mainHandler = new Handler(Looper.getMainLooper());
        Notifications.RemoveOnGoing(getApplicationContext());

        // Here we saying all next config iterations will be provided by this context
        Config.contextOwner = getApplicationContext();

        // Setting up an app service
        CreateService();

        // Disable the service if it's already enabled
        disableService();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        linearLayoutConsoleInput = (LinearLayout) findViewById(R.id.consoleInputLayout);

        InitConsoleInput();
        initPlayerList();
        InitMessageAdapters();
        InitDrawer();

        // Fitch history from service rcons
        List<Message> historyMessages = History.getMessages();
        for (int i = 0; i < historyMessages.size(); i++){
            Message m = historyMessages.get(i);
            Output(m);
        }

        History.clear();

        getSupportActionBar().hide();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigationViewMenu = navigationView.getMenu();

        Config config = Config.getConfig();

        RconManager.removeAll();

        for (int i = 0; i < config.ServerList.size(); i++) {
            Config.Server server = config.ServerList.get(i);
            RconManager.add(server);
        }

        UpdateMenu();

        View headerLayout = navigationView.getHeaderView(0);
        menuOnline = (TextView) headerLayout.findViewById(R.id.menu_Online);
        menuServers = (TextView) headerLayout.findViewById(R.id.menu_Servers);
        UpdateServers();

        Button sortByName = findViewById(R.id.players_sort_by_name);
        Button sortByServer = findViewById(R.id.players_sort_by_server);

        sortByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playersAdapter.sortByName();
            }
        });

        sortByServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playersAdapter.sortByServer();
            }
        });
    }

    public class ServerSorter implements Comparator<Config.Server>
    {
        @Override
        public int compare(Config.Server s1, Config.Server s2)
        {
            return s1.Name.compareToIgnoreCase(s2.Name);
        }
    }

    public void UpdateMenu(){
        navigationViewMenu.clear();
        Config config = Config.getConfig();
        Collections.sort(config.ServerList, new ServerSorter());
        for (int i = 0; i < config.ServerList.size(); i++) {
            Config.Server server = config.ServerList.get(i);
            MenuItem item = navigationViewMenu.add(0, i, 0, server.Name);
            if (server.Enabled)
                item.setIcon(R.drawable.ic_radio_button_checked_accent_24dp);
            else
                item.setIcon(R.drawable.ic_radio_button_unchecked_accent_24dp);
        }
        MenuItem settings = navigationViewMenu.add(1, config.ServerList.size(), 0, "Settings");
        settings.setIcon(android.R.drawable.ic_menu_preferences);
        Drawable drawable = settings.getIcon();
        if(drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        }
        MenuItem exit = navigationViewMenu.add(2, config.ServerList.size()+1, 0, "Exit");
        exit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        drawable = exit.getIcon();
        if(drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onDestroy() {
        enableService();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawer.isDrawerOpen(GravityCompat.START)){
                drawer.closeDrawer(GravityCompat.START);
            }
            else
                drawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private TextView menuOnline;
    private TextView menuServers;
    private Menu navigationViewMenu;

    public void UpdateOnline() {
        menuOnline.post(new Runnable() {
            @Override
            public void run() {
                int online = 0;
                int onlineMax = 0;
                Config config = Config.getConfig();
                for (int i = 0; i < config.ServerList.size(); i++) {
                    Config.Server server = config.ServerList.get(i);
                    Rcon rcon = RconManager.Rcons.get(server);
                    if (rcon == null) continue;
                    MenuItem item = navigationViewMenu.getItem(i);
                    if (server.Enabled && rcon.socket != null && rcon.socket.getState() == WebSocketState.OPEN) {
                        online+= rcon.serverInfo.Online;
                        onlineMax+=rcon.serverInfo.MaxOnline;
                        String newTitle = server.Name + " [" + rcon.serverInfo.Online + "/" + rcon.serverInfo.MaxOnline + "] " +
                                + rcon.serverInfo.FPS + " fps";
                        if (!item.getTitle().equals(newTitle))
                            item.setTitle(newTitle);
                    } else {
                        if (!item.getTitle().equals(server.Name + " Offline"))
                            item.setTitle(server.Name + " Offline");
                    }
                }
                String text = "Online: " + online + "/" + onlineMax;
                if (!menuOnline.getText().equals(text))
                    menuOnline.setText(text);
            }
        });
    }

    public void UpdateServers() {
        UpdateOnline();
        menuServers.post(new Runnable() {
            @Override
            public void run() {
                int online = 0;
                for (Map.Entry<Config.Server, Rcon> entry : RconManager.Rcons.entrySet()) {
                    if (entry.getKey().Enabled) {
                        Rcon rcon = entry.getValue();
                        if (rcon == null) continue;
                        if (rcon.socket != null && rcon.socket.getState() == WebSocketState.OPEN)
                            online++;
                    }
                }
                String text = "Servers: " + online + "/" + RconManager.Rcons.size();
                if (!menuServers.getText().equals(text))
                    menuServers.setText(text);
            }
        });
    }

    public void SendToRcons(String command) {
        if (command.isEmpty()) return;
        for (Map.Entry<Config.Server, Rcon> entry : RconManager.Rcons.entrySet()) {
            if (entry.getKey().Enabled)
                entry.getValue().Send(command);
        }
    }

}
