package com.example.rconapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class History {
    private static List<Message> messages = Collections.synchronizedList(new ArrayList());
    private static final int maxAmount = 300;
    public static List<Message> getMessages() {
        return messages;
    }

    public static void add(Message message) {
        if (messages.size() >= maxAmount){
            messages.remove(0);
        }
        messages.add(message);
    }

    public static void clear(){
        messages.clear();
    }
}