package com.example.rconapp;

import com.neovisionaries.ws.client.WebSocketState;
import java.util.HashMap;
import java.util.Map;

public class RconManager {

    public static Map<Server, Rcon> Rcons = new HashMap<>();

    public static boolean addAsService(Server server){
        if (isRconAlreadyExists(server)) return false;
        RconService rcon = new RconService(server);
        Rcons.put(server, rcon);
        rcon.createSocket();
        return true;
    }

    private static boolean isRconAlreadyExists(Server server) {
        for (Map.Entry<Server, Rcon> entry : Rcons.entrySet()) {
            Server currentServer = entry.getKey();
            if (currentServer.IP.equals(server.IP) && currentServer.Port.equals(server.Port))
                return true;
        }
        return false;
    }

    public static Boolean add(Server server) {
        if (isRconAlreadyExists(server)) return false;
        RconActivity rcon = new RconActivity(server);
        Rcons.put(server, rcon);
        rcon.createSocket();
        return true;
    }

    public static void remove(Server server) {
        if (Rcons.containsKey(server)){
            Rcon rcon = Rcons.get(server);
            rcon.disconnect();
            rcon.destroy();
            Rcons.remove(server);
        }
    }

    public static int GetOverallConnectedServers(){
        int online = 0;
        for (Map.Entry<Server, Rcon> entry : Rcons.entrySet()) {
            Rcon rcon = entry.getValue();
            if (rcon.socket.getState() == WebSocketState.OPEN)
                online++;
        }
        return online;
    }

    public static void removeAll() {
        for (Map.Entry<Server, Rcon> entry : Rcons.entrySet()) {
            Rcon rcon = entry.getValue();
            rcon.isSilenceDisconnect = true;
            rcon.destroy();
            rcon.disconnect();
        }
        Rcons.clear();
    }
}
