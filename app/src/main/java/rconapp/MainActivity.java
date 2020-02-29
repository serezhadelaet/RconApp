package rconapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.os.*;

import com.example.rconapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.neovisionaries.ws.client.WebSocketState;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static ColorDrawable transparentDrawableColor = new ColorDrawable(android.graphics.Color.TRANSPARENT);

    public static EditText consoleInput;

    private static MainActivity Instance;

    public static LinearLayout linearLayoutConsoleInput;

    private LinearLayout layoutPlayers;

    private DrawerLayout drawer;

    private Intent serviceIntent;

    public static Boolean isPlayersOpened = false;

    public static boolean isPlayersTabOpen;

    public static boolean isNavBarOpen(){
        return getInstance().drawer.isDrawerOpen(GravityCompat.START);
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
                        consoleInput.setTextColor(getResources().getColor(R.color.colorAccentDark));
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
                        consoleInput.setTextColor(getResources().getColor(R.color.colorAccentDark));
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
                            new PlayerInfo.DownLoadAvatars().execute();
                        }
                        PlayersAdapter.getAdapter().playersWithoutAvatar.clear();
                        isPlayersOpened = true;
                    }
                    View view = MainActivity.getInstance().getCurrentFocus();
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

    public static MainActivity getInstance() {
        return Instance;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        //If we click on a settings button
        if (id == Config.getConfig().getServerList().size()) {
            isPlayersTabOpen = false;
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            drawer.closeDrawer(GravityCompat.START);
        }
        else if (id == Config.getConfig().getServerList().size()+1) {
            getApplicationContext().stopService(serviceIntent);
            finishAffinity();
            System.exit(0);
        }
        else {
            Server server = Config.getConfig().getServerList().get(id);
            server.Enabled = !server.Enabled;
            RconManager.Rcons.get(server).OnActivityChange();
            UpdateServers();
            if (server.Enabled)
                item.setIcon(R.drawable.ic_radio_button_checked_accent_24dp);
            else {
                item.setIcon(R.drawable.ic_radio_button_unchecked_accent_24dp);
                Output(new Message(server.Name, "Disconnected"));
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
        getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                boolean canScrollNow = getInstance().messagesView.canScrollVertically(1);

                getInstance().messageAdapter.add(message);
                if (!canScrollNow) {
                    getInstance().fullScrollConsole();
                }
            }
        });
    }

    public void fullScrollConsole(){
        messagesView.setSelection(messageAdapter.getCount() - 1);
    }

    public void fullScrollChat(){
        int count = chatMessageAdapter.getCount();
        chatMessagesView.setSelection(count - 1);
    }

    public static void OutputChat(String prefix, String text) {
        final Message message = new Message("[" + prefix + "] " + text);
        message.setAsChatMessage();
        OutputChat(message);
    }

    public static void OutputChat(final Message message) {
        getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                boolean canScrollNow = getInstance().chatMessagesView.canScrollVertically(1);

                getInstance().chatMessageAdapter.add(message);

                if (!canScrollNow) {
                    getInstance().fullScrollChat();
                }
            }
        });
    }

    private void CreateService() {
        if (serviceIntent != null) return;
        if (AppService.getInstance() != null) return;
        serviceIntent = new Intent(getApplicationContext(), AppService.class);
        getApplicationContext().startService(serviceIntent);
    }

    private void InitConsoleInput(){

        consoleInput = findViewById(R.id.consoleInput);

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
                PlayerInfo.ShowInfo(getInstance(), (Player)playersAdapter.getItem(position));
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
        mainHandler = new Handler(Looper.getMainLooper());
        Notifications.RemoveOnGoing(getApplicationContext());

        // Here we saying all next config iterations will be provided by this context
        Config.setAsContentOwner(this);

        // Setting up an app service
        CreateService();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        linearLayoutConsoleInput = (LinearLayout) findViewById(R.id.consoleInputLayout);

        InitConsoleInput();
        initPlayerList();
        InitMessageAdapters();
        InitDrawer();

        AppService.setDisable();

        getSupportActionBar().hide();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigationViewMenu = navigationView.getMenu();

        Config config = Config.getConfig();

        RconManager.removeAll();

        for (int i = 0; i < config.getServerList().size(); i++) {
            Server server = config.getServerList().get(i);
            RconManager.add(server);
        }

        UpdateMenu();

        View headerLayout = navigationView.getHeaderView(0);
        menuOnline = headerLayout.findViewById(R.id.menu_Online);
        menuServers = headerLayout.findViewById(R.id.menu_Servers);
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

    public class ServerSorter implements Comparator<Server>
    {
        @Override
        public int compare(Server s1, Server s2)
        {
            return s1.Name.compareToIgnoreCase(s2.Name);
        }
    }

    public void UpdateMenu(){
        // TODO: Rework this method
        navigationViewMenu.clear();
        Config config = Config.getConfig();
        Collections.sort(config.getServerList(), new ServerSorter());
        for (int i = 0; i < config.getServerList().size(); i++) {
            Server server = config.getServerList().get(i);
            MenuItem item = navigationViewMenu.add(0, i, 0, server.Name);
            if (server.Enabled)
                item.setIcon(R.drawable.ic_radio_button_checked_accent_24dp);
            else
                item.setIcon(R.drawable.ic_radio_button_unchecked_accent_24dp);
        }
        MenuItem settings = navigationViewMenu.add(1, config.getServerList().size(),
                0, "Settings");
        settings.setIcon(android.R.drawable.ic_menu_preferences);
        MenuItem exit = navigationViewMenu.add(2, config.getServerList().size()+1,
                0, "Exit");
        exit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    }

    @Override
    public void onDestroy() {
        AppService.setEnable();
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
                for (int i = 0; i < config.getServerList().size(); i++) {

                    Server server = config.getServerList().get(i);
                    Rcon rcon = RconManager.Rcons.get(server);
                    if (rcon == null) continue;
                    MenuItem item = navigationViewMenu.getItem(i);
                    if (item == null) continue;
                    if (server.Enabled && rcon.socket != null &&
                            rcon.socket.getState() == WebSocketState.OPEN) {

                        online+= rcon.serverInfo.online;
                        onlineMax+=rcon.serverInfo.maxOnline;
                        String newTitle = server.Name + " [" + rcon.serverInfo.online +
                                "/" + rcon.serverInfo.maxOnline + "] " +
                                + rcon.serverInfo.fps + " fps";
                        if (!item.getTitle().equals(newTitle))
                            item.setTitle(newTitle);

                    } else {
                        if (!item.getTitle().equals(server.Name + " Offline"))
                            item.setTitle(server.Name + " Offline");
                    }
                }

                String text = "online: " + online + "/" + onlineMax;
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

                for (Map.Entry<Server, Rcon> entry : RconManager.Rcons.entrySet()) {
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
        for (Map.Entry<Server, Rcon> entry : RconManager.Rcons.entrySet()) {
            if (entry.getKey().Enabled)
                entry.getValue().Send(command);
        }
    }

}
