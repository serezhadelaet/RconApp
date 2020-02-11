package com.example.rconapp;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

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
            return;
        }
        initListeners();
        connect();
        Notifications.Create(AppService.Instance.getApplicationContext(), "Create", server.Name);
    }

    @Override
    public void onTextMessage(String message, String[] messages){
        super.onTextMessage(message, messages);
        Notifications.Create(AppService.Instance.getApplicationContext(), messages[0], messages[0]);
    }

    @Override
    public void Update() {
        super.Update();
    }

    @Override
    public void onConnected() {
        super.onConnected();
        Notifications.Create(AppService.Instance.getApplicationContext(), "Connected", server.Name);
    }

    @Override
    public void onConnectedError(String error) {
        super.onConnectedError(error);
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
    }

    @Override
    public void onError(String message) {
        super.onError(message);
    }
}
