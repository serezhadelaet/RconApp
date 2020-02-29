package rconapp;

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
    private static final String ConfigVersion = "1.0";
    private static Context contextOwner;
    public String SteamAPIKey = "";
    private List<Server> ServerList = new ArrayList<>();
    private List<NotificationsItem> Notifications = new ArrayList<>();
    public String FilteredMessages = "";
    public String ChatPrefixes = "";

    public static void setAsContentOwner(Context context) {
        contextOwner = context;
    }

    public static boolean addServer(Server newServer){
        for (int i = 0; i < Config.getConfig().getServerList().size(); i++){
            Server server = Config.getConfig().getServerList().get(i);
            if (newServer.IP.equals(server.IP) &&
                    newServer.Port.equals(server.Port)){
                return false;
            }
        }
        getConfig().getServerList().add(newServer);
        return true;
    }

    public static boolean removeServer(Server server){
        return getConfig().getServerList().remove(server);
    }

    private static Config config;

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

    public List<Server> getServerList() {
        return ServerList;
    }

    public List<NotificationsItem> getNotifications(){
        return Notifications;
    }

}
