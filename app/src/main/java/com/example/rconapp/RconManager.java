package com.example.rconapp;

import com.neovisionaries.ws.client.WebSocketState;
import java.util.HashMap;
import java.util.Map;

public class RconManager {

    public static Map<Config.Server, Rcon> Rcons = new HashMap<>();

    public static boolean addAsService(Config.Server server){
        if (isRconAlreadyExists(server)) return false;
        RconService rcon = new RconService(server);
        Rcons.put(server, rcon);
        rcon.createSocket();
        return true;
    }

    private static boolean isRconAlreadyExists(Config.Server server) {
        for (Map.Entry<Config.Server, Rcon> entry : Rcons.entrySet()) {
            Config.Server currentServer = entry.getKey();
            if (currentServer.IP.equals(server.IP) && currentServer.Port.equals(server.Port))
                return true;
        }
        return false;
    }

    public static Boolean add(Config.Server server) {
        if (isRconAlreadyExists(server)) return false;
        RconActivity rcon = new RconActivity(server);
        Rcons.put(server, rcon);
        rcon.createSocket();
        return true;
    }

    public static void remove(Config.Server server) {
        if (Rcons.containsKey(server)){
            Rcon rcon = Rcons.get(server);
            rcon.disconnect();
            rcon.destroy();
            Rcons.remove(server);
        }
    }

    public static int GetOverallConnectedServers(){
        int online = 0;
        for (Map.Entry<Config.Server, Rcon> entry : Rcons.entrySet()) {
            Rcon rcon = entry.getValue();
            if (rcon.socket.getState() == WebSocketState.OPEN)
                online++;
        }
        return online;
    }

    public static void removeAll(){
        for (Map.Entry<Config.Server, Rcon> entry : Rcons.entrySet()) {
            Rcon rcon = entry.getValue();
            rcon.isSilenceDisconnect = true;
            rcon.destroy();
            rcon.disconnect();
        }
        Rcons.clear();
    }
}
