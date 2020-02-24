package com.example.rconapp;

import android.util.Log;

import com.neovisionaries.ws.client.*;
import java.io.IOException;
import java.util.Map;
import java.util.List;

public class Rcon extends LightBehaviour implements IRcon {

    public WebSocket socket;

    public Server server;

    public ServerInfo serverInfo;

    public boolean isDisconnected = true;

    public boolean isSilenceDisconnect = false;

    private int lastIdentificator = 1001;

    public void OnActivityChange() {
        if (server.Enabled) {
            reconnect();
        }
        else {
            disconnect();
        }
    }

    public void Send(String command) {
        if (!server.Enabled || socket == null || socket.getState() != WebSocketState.OPEN) return;
        lastIdentificator++;
        Packet packet = new Packet(command, Integer.toString(lastIdentificator));
        socket.sendText(GsonHelper.toJson(packet));
    }

    public void onTextMessage(String message, String[] messages) {

    }

    public void onError(String message) {

    }

    public void onConnected() {

    }

    public void onConnectedError(String error) {

    }

    public void onDisconnected() {

    }

    public String getTeamChatMessage(String original){
        try {
            Map<String, String> friendMessage = GsonHelper.fromJson(original, Map.class);
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
            String originalLower = original.toLowerCase();
            for (int i = 0; i < prefixes.length; i++) {
                String pr = prefixes[i].toLowerCase();
                if (originalLower.contains(pr)) {
                    return original;
                }
            }
        }
        return null;
    }

    public boolean isMessageFiltered(String original){
        Config config = Config.getConfig();
        if (config.FilteredMessages.length() == 0) return false;
        String originalLower = original.toLowerCase();
        String[] filtered = config.FilteredMessages.split(",");
        for (int i = 0; i < filtered.length; i++) {
            String pr = filtered[i].toLowerCase();
            if (originalLower.contains(pr)){
                return true;
            }
        }
        return false;
    }

    public boolean isNotificationMessage(String original){
        Config config = Config.getConfig();
        if (config.NotificationMessages.length() == 0) return false;
        String originalLower = original.toLowerCase();
        String[] notificationMessages = config.NotificationMessages.split(",");
        for (int i = 0; i < notificationMessages.length; i++) {
            String nm = notificationMessages[i].toLowerCase();
            if (originalLower.contains(nm)){
                return true;
            }
        }
        return false;
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
                Map<String, String> dic = GsonHelper.fromJson(message, Map.class);
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
                Rcon.this.onConnectedError(cause.toString());

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
            if (socket.getState() == WebSocketState.OPEN ||
            socket.getState() == WebSocketState.CONNECTING){
                return;
            }
            if (server.Enabled)
                socket.connectAsynchronously();

        } catch (Exception ex){}
    }

    public Rcon(Server server) {
        serverInfo = new ServerInfo();
        this.server = server;
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect(0, "by user", 0);
        }
    }

    public void reconnect() {
        if (socket.getState() == WebSocketState.OPEN ||
                socket.getState() == WebSocketState.CONNECTING) return;
        if (!server.Enabled) return;
        try{
            socket = socket.recreate(2000).connectAsynchronously();
        }
        catch (IOException ex){

        }
    }

}
