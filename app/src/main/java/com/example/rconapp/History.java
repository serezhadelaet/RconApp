package com.example.rconapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class History {
    public static List<Message> messages = Collections.synchronizedList(new ArrayList());

}