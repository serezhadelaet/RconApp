package com.example.rconapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String text;
    private String date;

    public Message(String text) {
        this.text = text;
        this.date = dateFormat.format(new Date());
    }

    public Message(String text, String date) {
        this.text = text;
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }
}