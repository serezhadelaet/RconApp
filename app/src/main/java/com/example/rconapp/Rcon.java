package com.example.rconapp;

import android.util.Log;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Rcon extends LightBehaviour {

    public WebSocket socket;

    public Config.Server server;

    public ServerInfo serverInfo;

    public boolean isDisconnected = true;

    public class ServerInfo {
        public int Online;
        public int MaxOnline;
        public int FPS;
    }

    public class Packet {
        public String Identifier;
        public String Message;
        public String Name = "WebRcon";

        public Packet(String m, String i) {
            Identifier = i;
            Message = m;
        }
    }

    private int lastIdentificator = 1001;

    public void OnActivityChange() {
        if (server.Enabled) {
            reconnect();
        }
        else {
            Disconnect(false);
        }
    }

    public void Send(String command) {
        if (!server.Enabled || socket == null || socket.getState() != WebSocketState.OPEN) return;
        lastIdentificator++;
        Packet packet = new Packet(command, Integer.toString(lastIdentificator));
        socket.sendText(new Gson().toJson(packet));
    }

    public void onTextMessage(String message, String[] messages){

    }

    public void onError(String message){

    }

    public void onConnected(){

    }

    public void onConnectedError(String error){

    }

    public void onDisconnected(){

    }

    public String getTeamChatMessage(String original){
        try {
            Map<String, String> friendMessage =
                    new Gson().fromJson(original, Map.class);
            if (friendMessage.containsKey("Channel")) {
                String friendMsg = "[TEAM CHAT] " + friendMessage.get("Username") + ": " +
                        friendMessage.get("Message");
                return friendMsg;
            }
        } catch(Exception ex) { }
        return null;
    }

    public String getChatMessage(String original) {
        Config config = Config.getConfig();
        if (config.ChatPrefixes.length() > 0){
            String[] prefixes = config.ChatPrefixes.split(",");

            for (int i = 0; i < prefixes.length; i++) {
                String pr = prefixes[i];
                if (original.contains(pr)) {
                    return original;
                }
            }
        }
        return null;
    }

    public String[] GetFilteredMessages(String original) {
        if (original.length() == 0) return new String[0];
        String[] filtered = original.split(",");
        return filtered;
    }

    public String[] GetMessages(Map<String, String> map) {
        String msg = null;
        String[] output;
        if (map.containsKey("Message")) {
            msg = map.get("Message");
            if (!msg.isEmpty()){
                output = new String[] {msg};
                return output;
            }
        }
        if (map.containsKey("Error"))
            msg = map.get("Error");
        if (map.containsKey("Generic"))
            msg = map.get("Generic");
        if (map.containsKey("0"))
            msg = map.get("0");
        if (msg != null && !msg.isEmpty()) {
            output = new String[] {msg};
            return output;
        }
        else{
            output = new String[map.size()];
            int index = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getValue();
                if (value.isEmpty()) continue;
                output[index] = value;
            }
        }
        return output;
    }

    public void initListeners() {
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) {
                Map<String, String> dic = new Gson().fromJson(message, Map.class);
                // Ignoring empty messages
                if (dic.size() == 1 && dic.containsKey("Identifier"))
                    return;
                String[] messages = GetMessages(dic);
                Rcon.this.onTextMessage(message, messages);
            }
        });

        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onError(WebSocket websocket, WebSocketException cause) {
                Rcon.this.onError(cause.toString());

            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                Rcon.this.onConnected();

            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onConnectError(WebSocket websocket, WebSocketException cause) {
                onConnectedError(cause.toString());

            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                       WebSocketFrame clientCloseFrame, boolean closedByServer) {
                Rcon.this.onDisconnected();

            }
        });
    }

    public void createSocket() {

    }

    public void updatePlayerList(){

    }

    public void connect(){
        try{
            if (server.Enabled)
                socket.connectAsynchronously();

        } catch (Exception ex){}
    }

    public Rcon(){

    }

    public Rcon(Config.Server server) {
        serverInfo = new ServerInfo();
        this.server = server;
    }

    public boolean isSilentDisconnect = false;

    public void Disconnect(boolean silent) {
        isSilentDisconnect = silent;
        if (socket != null && (socket.getState() != WebSocketState.CLOSED ||
                socket.getState() != WebSocketState.CLOSING)) {
            socket.disconnect(0, "by user", 0);
        }
    }

    public void reconnect() {
        if (isSilentDisconnect) return;
        if (socket.getState() == WebSocketState.OPEN ||
                socket.getState() == WebSocketState.CONNECTING) return;
        if (!server.Enabled) return;
        if (!isDisconnected) return;
        try{
            socket = socket.recreate(2000).connectAsynchronously();
        }
        catch (IOException ex){

        }
    }

}
