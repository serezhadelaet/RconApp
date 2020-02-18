package com.example.rconapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String text;
    private boolean isNotificationMessage;
    private boolean isChatMessage;
    private String date;

    public Message(Config.Server server, String text, boolean isNotify, boolean isChatMessage) {
        this.text = "[" + server.Name + "] " + text;
        this.date = dateFormat.format(new Date());
        this.isNotificationMessage = isNotify;
        this.isChatMessage = isChatMessage;
    }

    public Message(String text, boolean isNotify, boolean isChatMessage) {
        this.text = text;
        this.date = dateFormat.format(new Date());
        this.isNotificationMessage = isNotify;
        this.isChatMessage = isChatMessage;
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
}