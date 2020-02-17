package com.example.rconapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.customview.widget.ViewDragHelper;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.*;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.neovisionaries.ws.client.WebSocketState;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static EditText consoleInput;

    public static SortableTableView tableView;

    public static MainActivity Instance;

    public static LinearLayout linearLayoutConsoleInput;

    private List<Player> playerList;

    private PlayersAdapter adapter;

    private DrawerLayout drawer;

    private Intent serviceIntent;

    private Boolean isPlayersOpened = false;

    private List<String> playersWithoutAvatar = new ArrayList<>();

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
                    chatMessagesView.setVisibility(View.INVISIBLE);
                    tableView.setVisibility(View.INVISIBLE);
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
                    messagesView.setVisibility(View.INVISIBLE);
                    tableView.setVisibility(View.INVISIBLE);
                    linearLayoutConsoleInput.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_players:
                    isPlayersTabOpen = true;

                    if (!isPlayersOpened){
                        if (playersWithoutAvatar.size() > 0) {
                            new DownloadImageTask(new ArrayList<>(playersWithoutAvatar)).execute();
                        }
                        playersWithoutAvatar.clear();
                        isPlayersOpened = true;
                    }
                    View view = MainActivity.Instance.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    chatMessagesView.setVisibility(View.INVISIBLE);
                    messagesView.setVisibility(View.INVISIBLE);
                    linearLayoutConsoleInput.setVisibility(View.INVISIBLE);
                    tableView.setVisibility(View.VISIBLE);
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
            else
                item.setIcon(R.drawable.ic_radio_button_unchecked_accent_24dp);
            Config.saveConfig();
        }
        return true;
    }

    public class Player {
        public String Name;
        public String UserID;
        public String Server;
        public String TimeStr;
        public Integer Time;
        public Bitmap Avatar;
        public Integer Ping;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private List<String> steams;

        public DownloadImageTask(List<String> list) {
            steams = list;
        }

        protected Bitmap doInBackground(String... str) {
            String ids = TextUtils.join(",", steams);
            String link = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + Config.getConfig().SteamAPIKey + "&steamids=" + ids;
            String parsedPage = "";
            try {
                URL url = new URL(link);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;

                StringBuilder sb = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                parsedPage = sb.toString();
            } catch (IOException ex) {

            }
            SteamAPIPlayers players = new Gson().fromJson(parsedPage, SteamAPIPlayers.class);
            if (players == null || players.response == null || players.response.players == null) return null;
            for (int i = 0; i < players.response.players.size(); i++) {
                SteamAPIPlayers.Response.GameData data = players.response.players.get(i);
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(data.avatar).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    final String id = data.steamid;
                    final Bitmap m = mIcon11;
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Player player = GetPlayer(id);
                            if (player != null)
                                player.Avatar = m;
                            if (canUpdate)
                                adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {

                }
            }
            return null;
        }
    }

    public void AddOrUpdatePlayers(Config.Server server, List<LinkedTreeMap<String, Object>> list) {
        final List<Player> oldPlayers = new ArrayList<>();
        List<String> currentPlayers = new ArrayList<>();
        if (list == null)
            list = new ArrayList<>();
        for (int j = 0; j < list.size(); j++) {
            LinkedTreeMap<String, Object> entry = list.get(j);
            String steamID = entry.get("SteamID").toString();
            currentPlayers.add(steamID);
            Player existingPlayer = GetPlayer(steamID);
            if (existingPlayer == null) {
                if (!playersWithoutAvatar.contains(steamID))
                    playersWithoutAvatar.add(steamID);
                Player newPlayer = new Player();
                newPlayer.Server = server.Name;
                UpdatePlayer(newPlayer, entry);
                AddOrUpdatePlayer(newPlayer);
            }
            else
                AddOrUpdatePlayer(existingPlayer);
        }
        for (int i = 0; i < playerList.size(); i++) {
            Player current = playerList.get(i);
            if (current.Server == server.Name && !currentPlayers.contains(current.UserID))
                oldPlayers.add(current);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < oldPlayers.size(); i++)
                    playerList.remove(oldPlayers.get(i));
                if (canUpdate)
                    adapter.notifyDataSetChanged();
            }
        });
        if (isPlayersOpened){
            if (playersWithoutAvatar.size() > 0) {
                //new AvatarDownloader(new ArrayList<>(playersWithoutAvatar));
                new DownloadImageTask(new ArrayList<>(playersWithoutAvatar)).execute();
            }
            playersWithoutAvatar.clear();
        }
    }

    private void UpdatePlayer(Player player, LinkedTreeMap<String, Object> map){
        player.Name = map.get("DisplayName").toString();
        player.Ping = (int)(Math.round(new Double(map.get("Ping").toString())));
        Double time = new Double(map.get("ConnectedSeconds").toString());
        Integer timeInt = (int)Math.round(time);
        player.Time = timeInt;
        Integer hours = timeInt / 3600;
        Integer minutes = (timeInt % 3600) / 60;
        String output = "";
        if (hours != 0) {
            output+=hours + "h";
        }
        if (hours == 0 || minutes != 0) {
            output+=minutes + "m";
        }
        player.TimeStr = output;
        player.UserID = map.get("SteamID").toString();
    }

    private Player GetPlayer(String userID) {
        for (int i =0; i < playerList.size(); i++){
            Player pl = playerList.get(i);
            if (pl.UserID.equals(userID)){
                return pl;
            }
        }
        return null;
    }

    private void AddOrUpdatePlayer(final Player player) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Player existingPlayer = GetPlayer(player.UserID);
                if (existingPlayer != null) {
                    if (!existingPlayer.TimeStr.equals(player.TimeStr)) {
                        existingPlayer.Time = player.Time;
                        if (canUpdate)
                            adapter.notifyDataSetChanged();
                    }
                    if (!existingPlayer.Ping.equals(player.Ping)) {
                        existingPlayer.Ping = player.Ping;
                        if (canUpdate)
                            adapter.notifyDataSetChanged();
                    }
                }
                else {
                    playerList.add(player);
                    if (canUpdate)
                        adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private MessageAdapter chatMessageAdapter;
    private ListView chatMessagesView;

    private void InitMessageAdapters(){
        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        chatMessageAdapter = new MessageAdapter(this);
        chatMessagesView = (ListView) findViewById(R.id.chatmessages_view);
        chatMessagesView.setAdapter(chatMessageAdapter);
    }

    public static void Output(Config.Server server, String text) {
        final Message msg = new Message("[" + server.Name + "] " + text);
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Instance.messageAdapter.add(msg);
                Instance.messagesView.setSelection(Instance.messagesView.getCount() - 1);
            }
        });
    }

    public static void OutputChat(String prefix, String text) {
        final Message msg = new Message("[" + prefix + "] " + text);
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Instance.chatMessageAdapter.add(msg);
                Instance.chatMessagesView.setSelection(Instance.chatMessagesView.getCount() - 1);
            }
        });
    }

    public static void Output(AppService.History history) {
        final Message msg = new Message("[" + history.Server.Name + "] " + history.Message);
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.messageAdapter.add(msg);
                Instance.messagesView.setSelection(Instance.messagesView.getCount() - 1);
            }
        });
    }

    public static void OutputChat(AppService.History history) {
        final Message msg = new Message("[" + history.Server.Name + "] " + history.ChatMessage, history.Date);
        Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Instance.chatMessageAdapter.add(msg);
                Instance.chatMessagesView.setSelection(Instance.chatMessagesView.getCount() - 1);
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

    private void InitTableView(){

        tableView = (SortableTableView) findViewById(R.id.tableView);
        tableView.addDataClickListener(new PlayersClickListener());
        adapter = new PlayersAdapter(Instance, playerList);
        tableView.setDataAdapter(adapter);
        tableView.setColumnComparator(0, new PlayerServerComparator());
        tableView.setColumnComparator(1, new PlayerNameComparator());
        tableView.setColumnComparator(2, new PlayerTimeComparator());
        tableView.setHeaderBackgroundColor(getResources().getColor(R.color.colorAccent));
        SimpleTableHeaderAdapter headerAdapter = new SimpleTableHeaderAdapter(Instance, new String[]{"#", "Player", "Info"});
        headerAdapter.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        TableColumnWeightModel columnModel = new TableColumnWeightModel(3);
        columnModel.setColumnWeight(0, 1);
        columnModel.setColumnWeight(1, 7);
        columnModel.setColumnWeight(2, 2);
        tableView.setColumnModel(columnModel);
        headerAdapter.setPaddings(1, 1, 1, 1);
        tableView.setHeaderAdapter(headerAdapter);
        tableView.setVisibility(View.INVISIBLE);
    }

    private void InitDrawer(){
        drawer = (DrawerLayout) findViewById(R.id.container);
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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mainHandler = new Handler(Looper.getMainLooper());
        Notifications.RemoveOnGoing(getApplicationContext());

        // Here we saying all next config iterations will be provided by this context
        Config.contextOwner = getApplicationContext();

        if (playerList == null)
            playerList = new ArrayList<>();
        playerList.clear();
        // Setting up an app service
        CreateService();

        // Disable the service if it's already enabled
        disableService();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        linearLayoutConsoleInput = (LinearLayout) findViewById(R.id.consoleInputLayout);

        InitConsoleInput();
        InitTableView();
        InitMessageAdapters();
        InitDrawer();

        // Fitch history from service rcons
        for (int i = 0; i < AppService.MessagesHistory.size(); i++){
            AppService.History h = AppService.MessagesHistory.get(i);
            if (h.ChatMessage != null)
                OutputChat(h);
            Output(h);
        }

        // Fix not scrolling down
        //scrollView.fullScroll(View.FOCUS_DOWN);
        AppService.MessagesHistory.clear();

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
        RconManager.removeAll();
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

    private void SendToRcons(String command) {
        if (command.isEmpty()) return;
        for (Map.Entry<Config.Server, Rcon> entry : RconManager.Rcons.entrySet()) {
            if (entry.getKey().Enabled)
                entry.getValue().Send(command);
        }
    }

    private Boolean canUpdate = true;
    private Player clickedPlayer = null;


    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playersmenu, popup.getMenu());
        canUpdate = false;
        popup.show();
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                canUpdate = true;
                clickedPlayer = null;
            }
        });
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                canUpdate = true;
                if (clickedPlayer == null)
                    return true;
                if (item.getItemId() == R.id.steam) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://steamcommunity.com/profiles/" + clickedPlayer.UserID));
                    startActivity(browserIntent);
                }
                if (item.getItemId() == R.id.mute) {
                    SendToRcons("mute " + clickedPlayer.UserID);
                }
                if (item.getItemId() == R.id.kick) {
                    SendToRcons("kick " + clickedPlayer.UserID);
                }
                if (item.getItemId() == R.id.ban) {
                    SendToRcons("ban " + clickedPlayer.UserID);
                }
                clickedPlayer = null;
                return true;
            }
        });
    }

    private class PlayersClickListener implements TableDataClickListener<Player> {
        @Override
        public void onDataClicked(int rowIndex, Player pl) {
            clickedPlayer = pl;
            MainActivity.Instance.showPopup(adapter.getCellView(rowIndex, 0, tableView));
        }
    }

    private static class PlayerServerComparator implements Comparator<Player> {
        @Override
        public int compare(Player player1, Player player2) {
            return player1.Server.compareTo(player2.Server);
        }
    }

    private static class PlayerNameComparator implements Comparator<Player> {
        @Override
        public int compare(Player player1, Player player2) {
            return player1.Name.compareTo(player2.Name);
        }
    }

    private static class PlayerTimeComparator implements Comparator<Player> {
        @Override
        public int compare(Player player1, Player player2) {
            Integer time1 = new Integer(player1.Time);
            Integer time2 = new Integer(player2.Time);
            return time1.compareTo(time2);
        }
    }

    public class Utils {

        public void runOnUiThread(Runnable runnable){
            mainHandler.post(runnable);
        }
    }

}
