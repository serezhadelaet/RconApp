package com.example.rconapp;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocketFactory;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RconService extends Rcon {

    public RconService(Server server) {
        super(server);
    }

    private Timer disconnectTimer;

    @Override
    public void createSocket(){
        WebSocketFactory factory = new WebSocketFactory();
        try{
            socket = factory.createSocket("ws://" + server.IP + ":" +
                    server.Port + "/" + server.Password);
        } catch (IOException ex){
            Notifications.Create(AppService.getInstance().getApplicationContext(),
                    "Connect error", server.Name);
            UpdateOnGoingNotification();
            return;
        }
        initListeners();
        connect();
    }

    @Override
    public void onTextMessage(String data, String[] messages) {
        super.onTextMessage(data, messages);
        boolean isNotifySended = false;
        for (int m = 0; m < messages.length; m++) {
            String msg = messages[m];

            // Escape filtered messages
            if (isMessageFiltered(msg))
                continue;

            String chatMessage = getChatMessage(msg);
            String teamChatMessage = getTeamChatMessage(msg);
            if (chatMessage == null) {
                chatMessage = teamChatMessage;
            }
            if (chatMessage != null)
                msg = chatMessage;
            Message message = new Message(server.Name, msg);
            if (isNotificationMessage(msg))
                message.setAsNotificationMessage();
            if (chatMessage != null)
                message.setAsChatMessage();
            History.add(message);
            if (isNotifySended) continue;
            if (message.isNotification()) {
                Notifications.Create(AppService.getInstance().getApplicationContext(),
                        "[" + server.Name + "] Notification", msg);
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
        Notifications.CreateOnGoing(AppService.getInstance().getApplicationContext(), msg);
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
                Notifications.Create(AppService.getInstance().getApplicationContext(),
                        "Disconnected", server.Name);
            }
        }, 30000);
    }

    @Override
    public void onError(String message) {
        super.onError(message);
    }
}
