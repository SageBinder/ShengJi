package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;

import java.io.*;

import static com.sage.server.ServerCodes.CONNECTION_DENIED;

class ShengJiClient extends Thread {
    private final int PORT;
    private final String serverIP;
    private final ShengJiGame game;
    private final String playerName;

    private final GameState gameState;

    private NetJavaSocketImpl socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    private boolean quit = false;

    private volatile boolean waitingForServerCode = true;
    private final Object waitingForServerCodeLock = new Object();

    private volatile int consumableServerCode = 0;
    private final Object consumableServerCodeLock = new Object();

    ShengJiClient(int PORT, String serverIP, String playerName, ShengJiGame game, GameState gameState) {
        this.PORT = PORT;
        this.serverIP = serverIP;
        this.game = game;
        this.playerName = playerName;
        this.gameState = gameState;

        SocketHints socketHints = new SocketHints();
        socketHints.socketTimeout = 0;
        socket = new NetJavaSocketImpl(Net.Protocol.TCP, serverIP, PORT, socketHints);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // TODO: Send start command to server when host wants to start
    @Override
    public void run() {
        if(readInt() == CONNECTION_DENIED) {
            return;
        }
        sendString(playerName);

        while(socket.isConnected() && !quit) {
            while(!waitingForServerCode) {
                try {
                    Thread.sleep(500);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int serverCode = readInt();
            while(serverCode > -1) {
                serverCode = readInt();
                Gdx.app.log("ShengJiClient.run","Oh shit, server code is > -1. This should never happen. IT'S BORKED.");
            }

            synchronized(waitingForServerCodeLock) {
                waitingForServerCode = false;

            }
            synchronized(consumableServerCodeLock) {
                consumableServerCode = serverCode;
            }
        }

        if(socket.isConnected()) {
            socket.dispose();
        }
    }

    int readInt() {
        try {
            return Integer.parseInt(reader.readLine());
        } catch(IOException e) {
            e.printStackTrace();
            return -666;
        }
    }

    String readLine() {
        try {
            return reader.readLine();
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void sendInt(int i) {
        try {
            writer.write(Integer.toString(i) + "\n");
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    void sendString(String s) {
        try {
            writer.write(s + "\n");
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    boolean readyToRead() {
        try {
            return reader.ready();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    int consumeServerCode() {
        synchronized(consumableServerCodeLock) {
            int returnValue = consumableServerCode;
            consumableServerCode = 0;
            return returnValue;
        }
    }

    void waitForServerCode() {
        synchronized(waitingForServerCodeLock) {
            waitingForServerCode = true;
        }
    }

    void quit() {
        try {
            reader.close();
            writer.close();
            socket.dispose();
        } catch(IOException e) {
            e.printStackTrace();
        }
        quit = true;
    }
}
