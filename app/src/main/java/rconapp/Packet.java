package rconapp;

public class Packet {
    private String Identifier;
    private String Message;
    private String Name = "WebRcon";

    public Packet(String m, String i) {
        Identifier = i;
        Message = m;
    }
}