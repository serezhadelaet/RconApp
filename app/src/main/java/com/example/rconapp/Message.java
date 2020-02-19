package com.example.rconapp;

import android.graphics.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Message {

    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String text;
    private boolean isNotificationMessage;
    private boolean isChatMessage;
    private Color backgroundColor;
    private String date;

    public Message(Server server, String text) {
        this.text = "[" + server.Name + "] " + text;
        this.date = dateFormat.format(Calendar.getInstance().getTime());
    }

    public Message(String text) {
        this.text = text;
        this.date = dateFormat.format(Calendar.getInstance().getTime());
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public boolean isNotification() {
        return isNotificationMessage;
    }

    public boolean isChatMessage(){
        return isChatMessage;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor){
        this.backgroundColor = backgroundColor;
    }

    public void setAsNotificationMessage() {
        isNotificationMessage = true;
    }

    public void setAsChatMessage() {
        isChatMessage = true;
    }
}