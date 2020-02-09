package com.example.rconapp;

import android.content.Context;
import android.support.design.card.MaterialCardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import de.codecrafters.tableview.TableDataAdapter;

public class PlayersAdapter extends TableDataAdapter<MainActivity.Player> {

    public PlayersAdapter(Context context, List<MainActivity.Player> data) {
        super(context, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        MainActivity.Player player = getRowData(rowIndex);
        View renderedView = null;
        TextView textView = new TextView(MainActivity.Instance);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        switch (columnIndex) {

            case 0:
                textView.setText(player.Server);
                renderedView = textView;
                break;
            case 1:
                RelativeLayout relativeLayout = new RelativeLayout(MainActivity.Instance);
                MaterialCardView cardView = new MaterialCardView(MainActivity.Instance);
                cardView.setRadius(60);
                ImageView image = new ImageView(MainActivity.Instance);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(120, 120);
                image.setLayoutParams(layoutParams);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setImageBitmap(player.Avatar);
                cardView.addView(image);
                relativeLayout.addView(cardView);
                TextView textViewName = new TextView(MainActivity.Instance);
                String name = player.Name;
                if (name.length() > 20)
                    name = name.substring(0, 20) + "...";
                textViewName.setText(name + "\n" + player.UserID);
                textViewName.setTextSize(15);
                textViewName.setPadding(140, 0,0,0);
                relativeLayout.setPadding(0, 10, 0, 10);
                relativeLayout.addView(textViewName);
                renderedView = relativeLayout;
                break;
            case 2:
                textView.setText(player.TimeStr + "\n" + player.Ping + " ping");
                renderedView = textView;
                break;

        }

        return renderedView;
    }
}