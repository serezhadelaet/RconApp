package com.example.rconapp;

public class RconService extends Rcon {

    public RconService(Config.Server server) {
        super(server);
    }

    @Override
    public void onTextMessage(String message, String[] messages){
        super.onTextMessage(message, messages);
        if (message.contains(Config.TriggerMessages)){
            Notifications.Create(AppService.Instance, messages[0], messages[0]);
        }
    }
    int i = 0;
    @Override
    public void Update() {
        super.Update();
        Notifications.Create(AppService.Instance, "Updated: " + i, "");
        i++;
    }

    @Override
    public void onConnected() {
        super.onConnected();
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
