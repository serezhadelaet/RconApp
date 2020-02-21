package com.example.rconapp;

public interface IRcon {
    void createSocket();
    void connect();
    void disconnect();
    void reconnect();
    void close();
}
