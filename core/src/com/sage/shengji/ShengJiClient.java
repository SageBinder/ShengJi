package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.SocketHints;
import com.sage.server.ServerCodes;

import java.io.*;

import static com.sage.server.ServerCodes.CONNECTION_DENIED;

class ShengJiClient extends Thread {
    private final int PORT;
    private final String serverIP;
    private final ScreenManager game;
    private final String playerName;

    private NetJavaSocketImpl socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    private volatile boolean quit = false;

    private volatile boolean waitingForServerCode = true;
    private final Object waitingForServerCodeLock = new Object();

    private volatile Integer consumableServerCode = null;
    private final Object consumableServerCodeLock = new Object();

    ShengJiClient(int PORT, String serverIP, String playerName, ScreenManager game) {
        this.PORT = PORT;
        this.serverIP = serverIP;
        this.game = game;
        this.playerName = playerName;

        SocketHints socketHints = new SocketHints();
        socketHints.socketTimeout = 0;
        socket = new NetJavaSocketImpl(Net.Protocol.TCP, serverIP, PORT, socketHints);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> socket.dispose()));
    }

    // TODO: Send start command to server when host wants to start
    @Override
    public void run() {
        Gdx.app.log("Client run", "Beginning");
        if(readInt() == CONNECTION_DENIED) {
            return;
        }
        Gdx.app.log("Client run", "JOIN_SUCCESSFUL");
        sendString(playerName);
        Gdx.app.log("Client run", "sent name");

        while(socket.isConnected() && !quit) {
            while(!waitingForServerCode) {
                try {
                    Thread.sleep(500);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Integer serverCode = readInt();
            if(serverCode == null) {
                return;
            }
            while(serverCode > -1) {
                Gdx.app.log("ShengJiClient.run","Oh shit, server code is > -1. This should never happen. IT'S BORKED.");
                serverCode = readInt();
                if(serverCode == null) {
                    return;
                }
            }

            if(serverCode == ServerCodes.PING) {
                sendInt(ClientCodes.PING);
                continue;
            }

            // Dear future me: please find a solution that isn't retarded. Sincerely, past me.
            // (Maybe implement your own buffer and only allow the gamestate to update when server sends enough info?)

            try { // Give server time to send extra information so the render thread doesn't have to wait for the server
                Thread.sleep(500);
            } catch(InterruptedException e) {
                e.printStackTrace();
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

    Integer readInt() {
        try {
            Integer i = Integer.parseInt(readLine());
            Gdx.app.log("Shengji.ShengJiClient.readInt()", "READ INT: " + i);
            return i;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    String readLine() {
        try {
            String line = reader.readLine();
            Gdx.app.log("Shengji.ShengJiClient.readLine()", "READ STRING: " + line);
            if(line == null) {
                socket.dispose();
                return null;
            }

            return line;
        } catch(IOException e) {
            e.printStackTrace();
            quit();
            return null;
        }
    }

    void sendInt(int i) {
        Gdx.app.log("Shengji.ShengJiClient.sendInt()", "SENDING INT: " + i);
        try {
            writer.write(Integer.toString(i) + "\n");
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
            quit();
        }
    }

    void sendString(String s) {
        Gdx.app.log("Shengji.ShengJiClient.sendString()", "SENDING STRING: \"" + s + "\"");
        try {
            writer.write(s + "\n");
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
            quit();
        }
    }

    boolean readyToRead() {
        try {
            return reader.ready();
        } catch(IOException e) {
            e.printStackTrace();
            quit();
            return false;
        }
    }

    Integer consumeServerCode() {
        synchronized(consumableServerCodeLock) {
            Integer returnValue = consumableServerCode;
            consumableServerCode = null;
            return returnValue;
        }
    }

    void waitForServerCode() {
        synchronized(waitingForServerCodeLock) {
            waitingForServerCode = true;
        }
    }

    // TODO: send server a quit code if player decides to quit
    void quit() {
        socket.dispose();
        quit = true;

        synchronized(waitingForServerCodeLock) {
            waitingForServerCode = false;
        }
    }
}
