package com.example.rconapp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.neovisionaries.ws.client.*;
import java.io.IOException;
import java.util.Map;
import java.util.List;

public class Rcon extends LightBehaviour {

    public WebSocket socket;

    private Config.Server server;

    public ServerInfo serverInfo;

    private boolean isDisconnected = true;

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
        if (MainActivity.Config.FilteredMessages.length() == 0) return false;
        String[] filtered = MainActivity.Config.FilteredMessages.split(",");
        for (int i = 0; i < filtered.length; i++) {
            String pr = filtered[i];
            if (message.contains(pr)){
                return true;
            }
        }
        return false;
    }

    private Boolean OnСhatMessage(String data) {
        if (MainActivity.Config.ChatPrefixes.length() == 0) return false;
        String[] prefixes = MainActivity.Config.ChatPrefixes.split(",");

        for (int i = 0; i < prefixes.length; i++) {
            String pr = prefixes[i];
            if (data.contains(pr)) {
                MainActivity.Instance.OutputChat(server.Name, data);
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
    public void Update() {
        if (server.Enabled && socket != null && socket.getState() == WebSocketState.OPEN) {
            Send("playerlist");
            Send("serverinfo");
        }
        Reconnect();
    }

    private int lastIdentificator = 1001;

    public void OnActivityChange() {
        if (server.Enabled) {
            Reconnect();
        }
        else {
            Disconnect();
        }
    }

    public void Send(String command) {
        if (!server.Enabled || socket == null || socket.getState() != WebSocketState.OPEN) return;
        lastIdentificator++;
        Packet packet = new Packet(command, Integer.toString(lastIdentificator));
        socket.sendText(new Gson().toJson(packet));
    }

    public Rcon(final Config.Server server) {
        serverInfo = new ServerInfo();
        this.server = server;
        WebSocketFactory factory = new WebSocketFactory();
        try{
            Log.d("Rcon","ws://" + server.IP + ":" + server.Port + "/" + server.Password);
            socket = factory.createSocket("ws://" + server.IP + ":" + server.Port + "/" + server.Password);
        }catch (IOException ex){
            if (MainActivity.Instance != null)
                MainActivity.Output(server,"Error" + ex);
            return;
        }
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                if (IsPlayersInfo(message)) return;
                if (IsServerInfo(message)) return;
                Map<String, String> dic = new Gson().fromJson(message, Map.class);
                if (dic.size() == 1 && dic.containsKey("Identifier")) {
                    return;
                }
                String msg = null;
                if (dic.containsKey("Message")) {
                    msg = dic.get("Message");
                    try {
                        Map<String, String> friendMessage =
                                new Gson().fromJson(dic.get("Message"), Map.class);
                        if (friendMessage.containsKey("Channel")) {
                            String friendMsg = "[TEAM CHAT] " + friendMessage.get("Username") + ": " +
                                    friendMessage.get("Message");
                            MainActivity.Instance.OutputChat(server.Name, friendMsg);
                            return;
                        }
                    } catch(Exception ex) { }
                }
                if (dic.containsKey("Error"))
                    msg = dic.get("Error");
                if (dic.containsKey("Generic"))
                    msg = dic.get("Generic");
                if (dic.containsKey("0"))
                    msg = dic.get("0");
                if (msg != null && !msg.isEmpty()) {
                    OnСhatMessage(msg);
                    if (!IsMessageFiltered(msg))
                        MainActivity.Output(server, msg);
                }
                else{
                    for (Map.Entry<String, String> entry : dic.entrySet()) {
                        String value = entry.getValue();
                        if (value.isEmpty()) continue;
                        OnСhatMessage(value);
                        if (!IsMessageFiltered(value))
                            MainActivity.Output(server, value);
                    }
                }
            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onError(WebSocket websocket, WebSocketException cause) {
                if (isDisconnected) return;
                isDisconnected = true;
                MainActivity.Instance.UpdateServers();
                MainActivity.Output(server,"Connection problem. Trying to reconnect...");
            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                isDisconnected = false;
                Update();
                MainActivity.Instance.UpdateServers();
                MainActivity.Output(server,"Connected");
            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onConnectError(WebSocket websocket, WebSocketException cause) {
                if (isDisconnected) return;
                isDisconnected = true;
                MainActivity.Instance.UpdateServers();
                MainActivity.Output(server,"onConnectError " + cause);
            }
        });
        socket.addListener(new WebSocketAdapter() {
            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                       WebSocketFrame clientCloseFrame, boolean closedByServer) {
                if (isDisconnected) return;
                if (isAppQuiting) return;
                isDisconnected = true;
                MainActivity.Instance.AddOrUpdatePlayers(server, null);
                MainActivity.Instance.UpdateServers();
                MainActivity.Output(server,"Disconnected");
            }
        });
        if (server.Enabled)
            socket.connectAsynchronously();
    }

    public static Boolean isAppQuiting = false;

    public void Disconnect() {
        if (socket != null && (socket.getState() != WebSocketState.CLOSED ||
                socket.getState() != WebSocketState.CLOSING)) {
            socket.disconnect(0, "by user", 0);
        }
    }

    private void Reconnect() {
        if (isAppQuiting) return;
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
