package com.example.rconapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityPlayer extends AppCompatActivity {

    public static MainActivity.Player player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_info);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#DAE4E5\">" + player.Name +"</font>"));

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        fillData();
        setupButtonActivity();
    }

    private void fillData(){
        ImageView image = findViewById(R.id.activity_player_info_image);
        TextView name =  findViewById(R.id.activity_player_info_name);
        TextView server =  findViewById(R.id.activity_player_info_server);
        TextView time =  findViewById(R.id.activity_player_info_time);
        TextView ping =  findViewById(R.id.activity_player_info_ping);

        if (player.Avatar != null)
            image.setImageBitmap(player.Avatar);
        name.setText(player.Name + "\n[" + player.UserID + "]");
        server.setText("Server: " + player.Server);
        time.setText("Time on server: " + player.TimeStr);
        ping.setText("Ping: " + Integer.toString(player.Ping));
    }

    private void setupButtonActivity(){
        Button mute = findViewById(R.id.activity_player_info_mute);
        Button kick = findViewById(R.id.activity_player_info_kick);
        Button ban = findViewById(R.id.activity_player_info_ban);
        Button steam = findViewById(R.id.activity_player_info_steam);
        Button copyid = findViewById(R.id.activity_player_info_copyid);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Instance.SendToRcons("mute " + player.UserID);
            }
        });
        kick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Instance.SendToRcons("kick " + player.UserID);
                finish();
            }
        });
        ban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Instance.SendToRcons("ban " + player.UserID);
                finish();
            }
        });
        steam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://steamcommunity.com/profiles/" + player.UserID));
                startActivity(browserIntent);
            }
        });
        copyid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SteamID", player.UserID);
                clipboard.setPrimaryClip(clip);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
