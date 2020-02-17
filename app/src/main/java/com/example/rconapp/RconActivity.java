package com.example.rconapp;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RconActivity extends Rcon {

    public RconActivity(Config.Server server){
        super(server);
    }

    @Override
    public void createSocket(){
        WebSocketFactory factory = new WebSocketFactory();
        try{
            socket = factory.createSocket("ws://" + server.IP + ":" + server.Port + "/" + server.Password);
        }catch (IOException ex){
            MainActivity.Output(server,"Error" + ex);
            return;
        }
        initListeners();
        connect();
    }

    @Override
    public void Update() {
        super.Update();
        if (server.Enabled && socket != null && socket.getState() == WebSocketState.OPEN) {
            if (MainActivity.isPlayersTabOpen)
                Send("playerlist");
            if (MainActivity.isNavBarOpen())
                Send("serverinfo");
        }
        reconnect();
    }

    @Override
    public void updatePlayerList(){
        if (server.Enabled && socket != null && socket.getState() == WebSocketState.OPEN) {
            Send("playerlist");
        }
    }

    private void updateServerInfo(){
        if (server.Enabled && socket != null && socket.getState() == WebSocketState.OPEN) {
            Send("serverinfo");
        }
    }

    private Boolean IsPlayersInfo(String data) {
        try {
            if (data == null || data.isEmpty()) return false;
            Map<String, String> dic = new Gson().fromJson(data, Map.class);
            if (!dic.containsKey("Message")) return false;
            List<LinkedTreeMap<String, Object>> players = new Gson().fromJson(dic.get("Message"), List.class);
            if (players == null) return false;
            MainActivity.Instance.AddOrUpdatePlayers(server, players);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Boolean IsMessageFiltered(String message){
        Config config = Config.getConfig();
        if (config.FilteredMessages.length() == 0) return false;
        String[] filtered = config.FilteredMessages.split(",");
        for (int i = 0; i < filtered.length; i++) {
            String pr = filtered[i];
            if (message.contains(pr)){
                return true;
            }
        }
        return false;
    }

    private Boolean IsServerInfo(String data) {
        try {
            if (data == null || data.isEmpty()) return false;
            Map<String, String> dic = new Gson().fromJson(data, Map.class);
            if (!dic.containsKey("Message")) return false;
            Map<String, Object> message = new Gson().fromJson(dic.get("Message"), Map.class);
            if (!message.containsKey("Framerate") && !message.containsKey("Players") && !message.containsKey("MaxPlayers")) return false;
            boolean needUpd = false;
            int fps = new Double(message.get("Framerate").toString()).intValue();
            int online = new Double(message.get("Players").toString()).intValue();
            int maxOnline = new Double(message.get("MaxPlayers").toString()).intValue();
            if (fps != serverInfo.FPS) {
                serverInfo.FPS = fps;
                needUpd = true;
            }
            if (fps != serverInfo.Online) {
                serverInfo.Online = online;
                needUpd = true;
            }
            if (fps != serverInfo.MaxOnline) {
                serverInfo.MaxOnline = maxOnline;
                needUpd = true;
            }
            if (needUpd)
                MainActivity.Instance.UpdateOnline();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }



    @Override
    public void onTextMessage(String message, String[] messages) {
        super.onTextMessage(message, messages);
        if (IsPlayersInfo(message)) return;
        if (IsServerInfo(message)) return;
        for (int i = 0; i < messages.length; i++){
            String msg = messages[i];
            String chatMessage = getChatMessage(msg);
            String teamChatMessage = getTeamChatMessage(msg);
            if (chatMessage == null){
                chatMessage = teamChatMessage;
            }
            if (chatMessage != null) {
                MainActivity.OutputChat(server.Name, chatMessage);
                msg = chatMessage;
            }
            if (!IsMessageFiltered(msg) && teamChatMessage == null)
                MainActivity.Output(server, msg);
        }
    }

    @Override
    public void onConnected() {
        super.onConnected();
        isDisconnected = false;
        updatePlayerList();
        updateServerInfo();
        MainActivity.Instance.UpdateServers();
        //MainActivity.Output(server,"Connected");
    }

    @Override
    public void onConnectedError(String error) {
        super.onConnectedError(error);
        if (isDisconnected) return;
        isDisconnected = true;
        MainActivity.Instance.UpdateServers();
        MainActivity.Output(server,"onConnectError " + error);
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        if (isDisconnected) return;
        isDisconnected = true;
        if (isSilentDisconnect) return;
        MainActivity.Instance.AddOrUpdatePlayers(server, null);
        MainActivity.Instance.UpdateServers();
        MainActivity.Output(server,"Disconnected");
    }

    @Override
    public void onError(String message) {
        super.onError(message);
        if (isDisconnected) return;
        isDisconnected = true;
        MainActivity.Instance.UpdateServers();
        MainActivity.Output(server,"Connection problem. Trying to reconnect...");
    }

}
