package rconapp;

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

            if (socket != null){
                socket.disconnect();
                socket.clearListeners();
            }

            socket = factory.createSocket("ws://" + server.IP + ":" +
                    server.Port + "/" + server.Password);
            initListeners();
            connect();
        } catch (IOException ex){
            Notifications.Create(AppService.getInstance().getApplicationContext(),
                    "Connect error", server.Name, null);
            return;
        }
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

            NotificationsItem notification;
            notification = getNotificationMessage(msg);

            if (notification != null)
                message.setAsNotificationMessage();
            if (chatMessage != null)
                message.setAsChatMessage();
            History.add(message);
            if (isNotifySended) continue;
            if (notification != null && notification.isNotify()) {
                Notifications.Create(AppService.getInstance().getApplicationContext(),
                        "[" + server.Name + "] Notification", msg, notification);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        reconnect();
    }

    @Override
    public void onConnected() {
        super.onConnected();
        isDisconnected = false;
        cancelOrCreateDisconnectTimer();
        Notifications.updateOnGoingNotification(0);
        isSilenceDisconnect = false;
    }

    @Override
    public void onConnectedError(String error) {
        super.onConnectedError(error);
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
        Notifications.updateOnGoingNotification(0);
        if (isSilenceDisconnect) return;
        cancelOrCreateDisconnectTimer();
        disconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Notifications.Create(AppService.getInstance().getApplicationContext(),
                        "Disconnected", server.Name, null);
            }
        }, 30000);
    }

    @Override
    public void onError(String message) {
        super.onError(message);
    }
}
