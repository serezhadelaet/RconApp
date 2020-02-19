package com.example.rconapp;

import com.neovisionaries.ws.client.WebSocketFactory;
import java.io.IOException;
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
        for (int m = 0; m < messages.length; m++) {
            String msg = messages[m];

            // Escape filtered messages
            if (isMessageFiltered(msg))
                continue;

            String chatMessage = getChatMessage(msg);
            String teamChatMessage = getTeamChatMessage(msg);
            if (chatMessage == null){
                chatMessage = teamChatMessage;
            }
            if (chatMessage != null)
                msg = chatMessage;
            boolean isNotify = isNotificationMessage(msg);
            History.add(new Message(server, msg, isNotify, chatMessage != null ));
            if (isNotifySended) continue;
            if (isNotify){
                Notifications.Create(AppService.Instance.getApplicationContext(), "[" + server.Name + "] Notification", msg);
            }
        }
    }



    @Override
    public void update() {
        super.update();
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
        cancelOrCreateDisconnectTimer();
        UpdateOnGoingNotification();
        isSilenceDisconnect = false;
    }

    @Override
    public void onConnectedError(String error) {
        super.onConnectedError(error);
        UpdateOnGoingNotification();
    }

    Timer disconnectTimer;

    private void cancelOrCreateDisconnectTimer(){
        if (disconnectTimer != null)
            disconnectTimer.cancel();
        else
            disconnectTimer = new Timer();
    }

    @Override
    public void disconnect() {
        cancelOrCreateDisconnectTimer();
        super.disconnect();
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        if (isDisconnected) return;
        isDisconnected = true;
        if (isSilenceDisconnect) return;
        cancelOrCreateDisconnectTimer();
        disconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Notifications.Create(AppService.Instance.getApplicationContext(),
                        "Disconnected", server.Name);
            }
        }, 5000);
    }

    @Override
    public void onError(String message) {
        super.onError(message);
    }
}
