package com.example.rconapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class History {
    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static List<History> messages = Collections.synchronizedList(new ArrayList());

    private Config.Server server;
    private String message;
    private Boolean isChatMessage;
    private String date;

    public History(Config.Server server, String message, boolean isChatMessage) {
        this.server = server;
        this.message = message;
        this.isChatMessage = isChatMessage;
        this.date = dateFormat.format(new Date());
    }

    public Config.Server getServer() {
        return server;
    }

    public String getMessage(){
        return message;
    }

    public Boolean isChatMessage(){
        return isChatMessage;
    }

    public String getDate(){
        return date;
    }
}