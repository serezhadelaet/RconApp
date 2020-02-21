package com.example.rconapp;

import android.content.ContentValues;
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

    public Message(String serverName, String text) {
        this.text = "[" + serverName + "] " + text;
        this.date = dateFormat.format(Calendar.getInstance().getTime());
    }

    public Message(String text) {
        this.text = text;
        this.date = dateFormat.format(Calendar.getInstance().getTime());
    }

    public Message(String text, String date, boolean isNotify, boolean isChat) {
        this.text = text;
        this.date = date;
        this.isNotificationMessage = isNotify;
        this.isChatMessage = isChat;
    }

    public ContentValues getSQLContentValues(){
        ContentValues cv = new ContentValues();
        cv.put(SQLContract.DataEntry.COLUMN_MESSAGE, text);
        cv.put(SQLContract.DataEntry.COLUMN_DATE, date);
        cv.put(SQLContract.DataEntry.COLUMN_ISNOTIFY, isNotification() ? 1 : 0);
        cv.put(SQLContract.DataEntry.COLUMN_ISCHAT, isChatMessage() ? 1 : 0);
        return cv;
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