package com.sage.shengji;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

class ShengJiClient extends Thread {
    private final int PORT;
    private final String serverIP;
    private final ShengJiGame game;
    private final String playerName;

    private NetJavaSocketImpl socket;
    private BufferedWriter writer;

    ShengJiClient(int PORT, String serverIP, String playerName, ShengJiGame game) {
        this.PORT = PORT;
        this.serverIP = serverIP;
        this.game = game;
        this.playerName = playerName;

        SocketHints socketHints = new SocketHints();
        socketHints.socketTimeout = 0;
        socket = new NetJavaSocketImpl(Net.Protocol.TCP, serverIP, PORT, socketHints);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            writer.write("testblahblahblah");
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }

        socket.dispose();
    }
}
