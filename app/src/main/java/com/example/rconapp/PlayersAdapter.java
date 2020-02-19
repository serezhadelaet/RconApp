package com.example.rconapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.rconapp.AppService.runOnUiThread;

public class PlayersAdapter extends BaseAdapter {

    private static PlayersAdapter Instance;

    public static PlayersAdapter getAdapter(){
        return Instance;
    }

    private Comparator currentComparator;

    private List<Player> players = new ArrayList<>();
    Context context;

    public PlayersAdapter(Context context) {
        Instance = this;
        currentComparator = comparatorServer;
        this.context = context;
    }

    public void add(Player player) {
        this.players.add(player);
        Collections.sort(players, currentComparator);
        notifyDataSetChanged();
    }

    public void remove(Player player){
        this.players.remove(player);
        Collections.sort(players, currentComparator);
        notifyDataSetChanged();
    }

    List<String> playersWithoutAvatar = new ArrayList<>();

    public void addOrUpdatePlayers(Config.Server server, List<LinkedTreeMap<String, Object>> list) {

        final List<Player> oldPlayers = new ArrayList<>();
        List<String> currentPlayers = new ArrayList<>();
        if (list == null)
            list = new ArrayList<>();
        for (int j = 0; j < list.size(); j++) {
            LinkedTreeMap<String, Object> entry = list.get(j);
            String steamID = entry.get("SteamID").toString();
            currentPlayers.add(steamID);
            Player existingPlayer = getPlayer(steamID);
            if (existingPlayer == null) {
                if (!playersWithoutAvatar.contains(steamID))
                    playersWithoutAvatar.add(steamID);
                Player newPlayer = new Player();
                newPlayer.Server = server.Name;
                updatePlayer(newPlayer, entry);
                addOrUpdatePlayer(newPlayer);
            }
            else
                addOrUpdatePlayer(existingPlayer);
        }
        List<Player> players = getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player current = players.get(i);
            if (current.Server == server.Name && !currentPlayers.contains(current.UserID))
                oldPlayers.add(current);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < oldPlayers.size(); i++)
                    remove(oldPlayers.get(i));
            }
        });
        if (MainActivity.isPlayersOpened){
            if (playersWithoutAvatar.size() > 0) {
                new PlayerInfo.DownLoadAvatars(new ArrayList<>(playersWithoutAvatar)).execute();
            }
            playersWithoutAvatar.clear();
        }
    }

    private void updatePlayer(Player player, LinkedTreeMap<String, Object> map){
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

    private void addOrUpdatePlayer(final Player player) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Player existingPlayer = getPlayer(player.UserID);
                if (existingPlayer != null) {
                    if (!existingPlayer.TimeStr.equals(player.TimeStr)) {
                        existingPlayer.Time = player.Time;
                        notifyDataSetChanged();
                    }
                    if (!existingPlayer.Ping.equals(player.Ping)) {
                        existingPlayer.Ping = player.Ping;
                        notifyDataSetChanged();
                    }
                }
                else {
                    add(player);
                }
            }
        });
    }

    public static void updateAvatar(final String userID, final Bitmap avatar) {
        if (MainActivity.Instance == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Player player = Instance.getPlayer(userID);
                if (player != null){
                    player.Avatar = avatar;
                    Instance.notifyDataSetChanged();
                }
            }
        });
    }

    public Player getPlayer(String userID){
        for (int i =0; i < players.size(); i++){
            Player pl = players.get(i);
            if (pl.UserID.equals(userID)){
                return pl;
            }
        }
        return null;
    }

    public List<Player> getPlayers(){
        return players;
    }

    public void sortByServer(){
        if (currentComparator == comparatorServer)
            currentComparator = comparatorServerDesc;
        else
            currentComparator = comparatorServer;
        Collections.sort(this.players, currentComparator);
        notifyDataSetChanged();
    }

    public void sortByName(){
        if (currentComparator == comparatorName)
            currentComparator = comparatorNameDesc;
        else
            currentComparator = comparatorName;
        Collections.sort(this.players, currentComparator);
        notifyDataSetChanged();
    }

    private Comparator comparatorServer = new Comparator<Player>() {
        @Override
        public int compare(Player o1, Player o2) {
            return o1.Server.compareTo(o2.Server);
        }
    };

    private Comparator comparatorServerDesc = new Comparator<Player>() {
        @Override
        public int compare(Player o1, Player o2) {
            return o2.Server.compareTo(o1.Server);
        }
    };

    private Comparator comparatorName = new Comparator<Player>() {
        @Override
        public int compare(Player o1, Player o2) {
            return o1.Name.compareTo(o2.Name);
        }
    };

    private Comparator comparatorNameDesc = new Comparator<Player>() {
        @Override
        public int compare(Player o1, Player o2) {
            return o2.Name.compareTo(o1.Name);
        }
    };

    @Override
    public int getCount() {
        return players.size();
    }

    @Override
    public Object getItem(int i) {
        return players.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Player player = players.get(i);
        LayoutInflater playerInflatter = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = playerInflatter.inflate(R.layout.layout_player, null);

        TextView name = convertView.findViewById(R.id.player_name);
        TextView server = convertView.findViewById(R.id.player_server);
        ImageView image = convertView.findViewById(R.id.player_image);

        String fixedName = player.Name;
        if (fixedName.length() > 30)
            fixedName = fixedName.substring(0, 30) + "...";
        name.setText(fixedName);

        server.setText(player.Server);
        image.setImageBitmap(player.Avatar);

        return convertView;
    }
}