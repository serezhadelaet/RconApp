package com.example.rconapp;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class PlayerInfo {

    public static ColorDrawable transparentDrawableColor = new ColorDrawable(android.graphics.Color.TRANSPARENT);

    public static void ShowInfo(final Context context, final Player player) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.activity_player_info);

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(transparentDrawableColor);

        fillData(dialog, player);
        createButtons(context, dialog, player);

        dialog.show();
    }

    public static class DownLoadAvatars extends AsyncTask<String, Void, Bitmap> {

        private List<String> steams;

        public DownLoadAvatars(List<String> list) {
            steams = list;
        }

        protected Bitmap doInBackground(String... str) {
            ConnectivityManager cm = (ConnectivityManager) MainActivity.Instance.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isWifi = false;
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifi = true;
                }
            }
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
                final SteamAPIPlayers.Response.GameData data = players.response.players.get(i);
                final Bitmap mIcon11;
                try {
                    String avatar = data.avatar;
                    if (isWifi)
                        avatar = data.avatarfull;
                    InputStream in = new java.net.URL(avatar).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    PlayersAdapter.updateAvatar(data.steamid, mIcon11);
                } catch (Exception e) {

                }
            }
            return null;
        }
    }

    private static void fillData(Dialog dialog, Player player){
        ImageView image = dialog.findViewById(R.id.activity_player_info_image);
        TextView name =  dialog.findViewById(R.id.activity_player_info_name);
        TextView steamid =  dialog.findViewById(R.id.activity_player_info_steamid);
        TextView server =  dialog.findViewById(R.id.activity_player_info_server);
        TextView time =  dialog.findViewById(R.id.activity_player_info_time);
        TextView ping =  dialog.findViewById(R.id.activity_player_info_ping);

        if (player.Avatar != null)
            image.setImageBitmap(player.Avatar);
        name.setText(player.Name);
        steamid.setText(player.UserID);
        server.setText("Server: " + player.Server);
        time.setText("Time on server: " + player.TimeStr);
        ping.setText("Ping: " + Integer.toString(player.Ping));
    }

    private static void createButtons(final Context context, final Dialog dialog, final Player player) {
        Button mute = dialog.findViewById(R.id.activity_player_info_mute);
        Button kick = dialog.findViewById(R.id.activity_player_info_kick);
        Button ban = dialog.findViewById(R.id.activity_player_info_ban);
        Button steam = dialog.findViewById(R.id.activity_player_info_steam);
        Button copyid = dialog.findViewById(R.id.activity_player_info_copyid);
        mute.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.Instance.SendToRcons("mute " + player.UserID);
                return false;
            }
        });
        kick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.Instance.SendToRcons("kick " + player.UserID);
                dialog.dismiss();
                return false;
            }
        });
        ban.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.Instance.SendToRcons("ban " + player.UserID);
                dialog.dismiss();
                return false;
            }
        });
        steam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://steamcommunity.com/profiles/" + player.UserID));
                context.startActivity(browserIntent);
            }
        });
        copyid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SteamID", player.UserID);
                clipboard.setPrimaryClip(clip);
            }
        });

    }

}
