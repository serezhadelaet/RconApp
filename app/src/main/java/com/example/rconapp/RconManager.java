package com.example.rconapp;

import java.util.HashMap;
import java.util.Map;

public class RconManager {

    public static Map<Config.Server, Rcon> Rcons = new HashMap<>();

    public static Boolean add(Config.Server server) {
        for (Map.Entry<Config.Server, Rcon> entry : Rcons.entrySet()) {
            Config.Server currentServer = entry.getKey();
            if (currentServer.IP.equals(server.IP) && currentServer.Port.equals(server.Port)) return false;
        }
        Rcon rcon = new Rcon(server);
        Rcons.put(server, rcon);
        return true;
    }

    public static void remove(Config.Server server) {
        if (Rcons.containsKey(server)){
            Rcon rcon = Rcons.get(server);
            rcon.Disconnect();
            rcon.Destroy();
            Rcons.remove(server);
        }
    }

    public static void removeAll(){
        Rcon.isAppQuiting = true;
        for (Map.Entry<Config.Server, Rcon> entry : Rcons.entrySet()) {
            Rcon rcon = entry.getValue();
            rcon.Destroy();
            rcon.Disconnect();
        }
        Rcons.clear();
    }
}
