package com.example.rconapp;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class GsonHelper {

    public static Gson gson;

    public static <T> T fromJson(String text, Class<T> typeClass){
        return getGson().fromJson(text, (Type)typeClass);
    }

    public static String toJson(Object obj){
        return getGson().toJson(obj);
    }

    private static Gson getGson(){
        if (gson == null){
            gson = new Gson();
        }
        return gson;
    }

}
