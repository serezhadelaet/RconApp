package rconapp;

public class NotificationsItem {
    private String text;
    private Boolean notify = true;
    private Boolean vibration = true;
    private Boolean sound = false;

    public NotificationsItem(String name){
        this.text = name;
    }

    public void SetText(String text){
        this.text = text;
    }

    public void SetNotify(Boolean f) {
        notify = f;
    }

    public void SetVibration(Boolean f) {
        vibration = f;
    }

    public void SetSound(Boolean f) {
        sound = f;
    }

    public String getText(){
        return text;
    }

    public Boolean isNotify(){
        return notify;
    }

    public Boolean hasVibration(){
        return vibration;
    }

    public Boolean hasSound(){
        return sound;
    }
}