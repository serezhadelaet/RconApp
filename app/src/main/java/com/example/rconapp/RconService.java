package com.example.rconapp;

import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RconService extends Rcon {

    public RconService(Config.Server server) {
        super(server);
    }

    @Override
    public void createSocket(){
        WebSocketFactory factory = new WebSocketFactory();
        try{
            socket = factory.createSocket("ws://" + server.IP + ":" + server.Port + "/" + server.Password);
        } catch (IOException ex){
            Notifications.Create(AppService.Instance.getApplicationContext(), "Connect error", server.Name);
            UpdateOnGoingNotification();
            return;
        }
        initListeners();
        connect();
    }

    @Override
    public void onTextMessage(String message, String[] messages) {
        super.onTextMessage(message, messages);
        boolean isNotifySended = false;

        String[] filtered = GetFilteredMessages(Config.getConfig().NotificationMessages);
        for (int m = 0; m < messages.length; m++) {
            if (AppService.MessagesHistory.size() >= 300) {
                AppService.MessagesHistory.remove(0);
            }
            String msg = messages[m];
            String chatMessage = getChatMessage(msg);
            String teamChatMessage = getTeamChatMessage(msg);
            if (chatMessage == null){
                chatMessage = teamChatMessage;
            }
            if (chatMessage != null)
                msg = chatMessage;
            AppService.MessagesHistory.add(new AppService.History(server, msg, chatMessage));
            if (isNotifySended) continue;
            for (int i = 0; i < filtered.length; i++){
                if (msg.contains(filtered[i])){
                    isNotifySended = true;
                    Notifications.Create(AppService.Instance.getApplicationContext(), "[" + server.Name + "] Notification", msg);
                    break;
                }
            }
        }
    }



    @Override
    public void Update() {
        super.Update();
        reconnect();
    }

    private void UpdateOnGoingNotification(){
        int current, all;
        current = RconManager.GetOverallConnectedServers();
        all = RconManager.Rcons.size();
        String msg;
        if (current == all)
            msg = "All servers connected";
        else
            msg = "Connected: " +
                    current + "/" +
                    all + " servers";
        Notifications.CreateOnGoing(AppService.Instance.getApplicationContext(), msg);
    }

    @Override
    public void onConnected() {
        super.onConnected();
        isDisconnected = false;
        UpdateOnGoingNotification();
    }

    @Override
    public void onConnectedError(String error) {
        super.onConnectedError(error);
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        if (isDisconnected) return;
        isDisconnected = true;
        if (isSilentDisconnect) return;
        UpdateOnGoingNotification();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isDisconnected && socket.getState() == WebSocketState.OPEN) return;
                Notifications.Create(AppService.Instance.getApplicationContext(),
                        "Disconnected", server.Name);
            }
        }, 10000);
    }

    @Override
    public void onError(String message) {
        super.onError(message);
    }
}
