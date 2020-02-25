# RconApp

Usefull and simple android tool to manage more than one RUST server at the same time

## Features
* Multiple servers management
* Players list with options, like Mute/Kick/Ban/Steam link open
* Separated console/chat tabs
* Works in background as a service to get notifications and store console logs
* Filters: unwanted data, chat messages, messages that instantiate notifications

## Builds
* Latest release: [RconApp v0.0.6](https://github.com/serezhadelaet/RconApp/releases/tag/v0.0.6)

## How to
* Settings field '<b>Steam API KEY</b>' can be used to load players avatars. Google to get it.
* Field '<b>Words to filter</b>' should use the separator "<b>,</b>" (comma symbol) to prevent to get messages with choosed words.
* Field '<b>Chat prefixes</b>' using the separator "<b>,</b>" to make the app understand what messages should go into the "Chat" tab.
* Field '<b>Notification messages</b>' use the same separator "<b>,</b>". Used to make notifications when the app runs as a service.
* Example of fields with the "<b>,</b>" separator: <b>servergibs,saving complete,invalid position</b>

## Requirements

* Android Studio 3.5.3 or higher
* Java 1.8.0_241 or higher
* Android 4.3.0 or higher

## Built With

* [nv-websocket-client](https://github.com/TakahikoKawasaki/nv-websocket-client) - Websockets
* [gson](https://github.com/google/gson) - Json
* [CircleImageView](https://github.com/hdodenhof/CircleImageView) - Fit images into circle

## TODO
* Push up Notifications with vibration, sound, LED options
* Add full smooth scrolldown circle button
* Hide navbar when scrolling up

## Screenshots

![Preview1](/preview1.jpg)
![Preview2](/preview2.jpg)
![Preview3](/preview3.jpg)
