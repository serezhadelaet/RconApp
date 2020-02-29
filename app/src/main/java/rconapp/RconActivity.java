package rconapp;

import com.google.gson.internal.LinkedTreeMap;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RconActivity extends Rcon {

    public RconActivity(Server server){
        super(server);
    }

    @Override
    public void createSocket(){
        WebSocketFactory factory = new WebSocketFactory();
        try{
            socket = factory.createSocket("ws://" + server.IP +
                    ":" + server.Port + "/" + server.Password);
        }catch (IOException ex){
            MainActivity.Output(new Message(server.Name, "Error"));
            return;
        }
        initListeners();
        connect();
    }

    @Override
    public void update() {
        super.update();
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
            Map<String, String> dic = GsonHelper.fromJson(data, Map.class);
            if (!dic.containsKey("Message")) return false;
            List<LinkedTreeMap<String, Object>> players =
                    GsonHelper.fromJson(dic.get("Message"), List.class);
            if (players == null) return false;
            PlayersAdapter.getAdapter().addOrUpdatePlayers(server, players);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Boolean IsServerInfo(String data) {
        try {
            if (data == null || data.isEmpty()) return false;
            Map<String, String> dic = GsonHelper.fromJson(data, Map.class);
            if (!dic.containsKey("Message")) return false;
            Map<String, Object> message = GsonHelper.fromJson(dic.get("Message"), Map.class);
            if (!message.containsKey("Framerate") && !message.containsKey("Players") &&
                    !message.containsKey("MaxPlayers")) return false;
            boolean needUpd = false;
            int fps = new Double(message.get("Framerate").toString()).intValue();
            int online = new Double(message.get("Players").toString()).intValue();
            int maxOnline = new Double(message.get("MaxPlayers").toString()).intValue();
            if (fps != serverInfo.fps) {
                serverInfo.fps = fps;
                needUpd = true;
            }
            if (fps != serverInfo.online) {
                serverInfo.online = online;
                needUpd = true;
            }
            if (fps != serverInfo.maxOnline) {
                serverInfo.maxOnline = maxOnline;
                needUpd = true;
            }
            if (needUpd)
                MainActivity.getInstance().UpdateOnline();
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

            // Escape filtered messages
            if (isMessageFiltered(msg))
                return;

            String chatMessage = getChatMessage(msg);
            String teamChatMessage = getTeamChatMessage(msg);

            Message m = new Message(server.Name, msg);
            NotificationsItem notification;
            notification = getNotificationMessage(msg);
            if (notification != null){
                m.setAsNotificationMessage();
            }
            if (chatMessage == null) {
                chatMessage = teamChatMessage;
            }
            if (chatMessage != null) {
                m.setAsChatMessage();
            }
            if (teamChatMessage == null){
                MainActivity.Output(m);
            }
            else {
                MainActivity.OutputChat(m);
            }
        }
    }

    @Override
    public void onConnected() {
        super.onConnected();
        isDisconnected = false;
        updatePlayerList();
        updateServerInfo();
        MainActivity.getInstance().UpdateServers();
    }

    @Override
    public void onConnectedError(String error) {
        super.onConnectedError(error);
        if (isDisconnected) return;
        isDisconnected = true;
        MainActivity.getInstance().UpdateServers();
        MainActivity.Output(new Message(server.Name, "onConnectError:" + error));
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        if (isDisconnected) return;
        isDisconnected = true;
        PlayersAdapter.getAdapter().addOrUpdatePlayers(server, null);
        MainActivity.getInstance().UpdateServers();
    }

    @Override
    public void onError(String message) {
        super.onError(message);
        if (isDisconnected) return;
        isDisconnected = true;
        MainActivity.getInstance().UpdateServers();
        MainActivity.Output(new Message(server.Name,
                "Connection problem. Trying to reconnect..."));
    }

}
