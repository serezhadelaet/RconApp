package com.example.rconapp;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static String ConfigVersion = "1.0";
    public static Context contextOwner;
    public String SteamAPIKey = "";
    public List<Server> ServerList = new ArrayList<>();
    public String FilteredMessages = "";
    public String ChatPrefixes = "";
    public String NotificationMessages = "";

    public static class Server {
        public String Name;
        public String IP;
        public String Port;
        public String Password;
        public Boolean Enabled;

        public Server(String name, String address, String port, String password) {
            Name = name;
            IP = address;
            Port = port;
            Password =  password;
            Enabled = false;
        }
    }

    public static Config config;

    public static void saveConfig(){
        if (config == null)
            config = new Config();
        String json = new Gson().toJson(config);
        writeToJson("appConfig" + ConfigVersion + ".json", json);
    }

    public static Config getConfig() {

        if (config != null)
            return config;

        boolean isFilePresent = Config.isFilePresent("appConfig" + ConfigVersion + ".json");

        if (!isFilePresent) {
            config = new Config();
            String json = new Gson().toJson(config);
            writeToJson("appConfig" + ConfigVersion + ".json", json);
            return config;
        }
        else{
            try{
                String jsonString = Config.readFromJson("appConfig" + ConfigVersion + ".json");
                config = new Gson().fromJson(jsonString, Config.class);
            }
            catch (Exception ex){
                config = new Config();
            }
            return config;
        }
    }

    public static String readFromJson(String fileName) {
        try {
            FileInputStream fis = contextOwner.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException fileNotFound) {
            return null;
        } catch (IOException ioException) {
            return null;
        }
    }

    public static boolean writeToJson(String fileName, String jsonString){
        try {
            FileOutputStream fos = contextOwner.openFileOutput(fileName,Context.MODE_PRIVATE);
            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }
            fos.close();
            return true;
        } catch (FileNotFoundException fileNotFound) {
            return false;
        } catch (IOException ioException) {
            return false;
        }
    }

    public static boolean isFilePresent(String fileName) {
        String path = contextOwner.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

}
