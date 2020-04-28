package com.example.maptry

import com.github.nkzawa.socketio.client.IO
import java.net.URISyntaxException

private var socket : com.github.nkzawa.socketio.client.Socket? = null
public fun callServer(id: String){
    socket = IO.socket("http://192.168.56.1:3000");
    socket?.connect()
        try {
            //if you are using a phone device you should connect to same local network as your laptop and disable your pubic firewall as well


            // emit the event join along side with the nickname
            socket?.emit("add", id);
        } catch (e: URISyntaxException) {
            e.printStackTrace();
        }
}