package com.example.rconapp;

public class Server {
    public String Name;
    public String IP;
    public String Port;
    public String Password;
    public Boolean Enabled;

    public Server(String name, String address, String port, String password) {
        Name = name;
        IP = address;
        Port = port;
        Password =  password;
        Enabled = false;
    }
}
