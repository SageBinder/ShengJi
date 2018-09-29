package com.sage.server;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.sage.shengji.ClientCodes;

import java.io.*;

public class ShengJiServer extends Thread {
    private final int maxPlayers;

    private final Object playersLock = new Object();
    private PlayerList players = new PlayerList();


    // host can be volatile because it's only written to by one thread
//    private final Object hostLock = new Object();
    private volatile Player host;

    // TODO: Set endGame when players want to end game
    private volatile boolean endGame = false;

    // A lock is not needed for roundStarted because it is volatile (it's only written to by one thread)
    private volatile boolean roundStarted = false;

    private NetJavaServerSocketImpl serverSocket;

    public ShengJiServer(int port, int numPlayers) {
        maxPlayers = numPlayers;

        ServerSocketHints serverSocketHints = new ServerSocketHints();
        serverSocketHints.acceptTimeout = 0;
        // TODO: figure out a way to catch GdxRuntimeException or something if an error is thrown here
        serverSocket = new NetJavaServerSocketImpl(Net.Protocol.TCP, port, serverSocketHints);
    }

    @Override
    public void run() {
        Thread manageNewConnectionsThread = new Thread(this::manageNewConnections);
        Thread manageDisconnectionsThread = new Thread(this::manageDisconnections);
        Thread manageHostCommunicationThread = new Thread(this::manageHostCommunication);
        manageNewConnectionsThread.start();
        manageDisconnectionsThread.start();
        manageHostCommunicationThread.start();
        try {
            manageNewConnectionsThread.join();
            manageDisconnectionsThread.join();
            manageHostCommunicationThread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        // Once roundStarted is set to true, all three threads should exit

        synchronized(playersLock) {
            Round round = new Round(players);
            while(!endGame) {
                round.playNewRound();
                for(Player p : players) {
                    p.resetForNewRound();
                }

                if(!manageNewConnectionsThread.isAlive()) {
                    manageNewConnectionsThread.start();
                }
                if(!manageDisconnectionsThread.isAlive()) {
                    manageDisconnectionsThread.start();
                }
                if(!manageHostCommunicationThread.isAlive()) {
                    manageHostCommunicationThread.start();
                }
                try {
                    manageNewConnectionsThread.join();
                    manageDisconnectionsThread.join();
                    manageHostCommunicationThread.join();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // manageNewConnectionsThread might still be waiting on a blocking call to serverSocket.accept() at this point.
        // Disposing the serverSocket should unblock that call.

        serverSocket.dispose();
    }

    // TODO: Way of letting players temporarily sit out a round

    // Add new connections while players.size() remains less than maxPlayers
    private void manageNewConnections() {
        while(!roundStarted) {
            // The connection has to be accepted outside of the synchronized block because serverSocket.accept()
            // is a blocking method. Being inside the synchronized block would prevent the other threads from
            // interacting with players because this thread will always be looking to accept new connections.
            // Instead, if the max players is met or the round has already started, dispose of the new
            // connection that was made (because it was made after max players was met or the round was started)
            Socket s = serverSocket.accept(null);
            var bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));

            synchronized(playersLock) {
                if(players.size() < maxPlayers && !roundStarted) {
                    String pName = "Player " + players.size();

                    try { // New client connections should send the name of the player
                        var writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        writer.write(Integer.toString(ServerCodes.JOIN_SUCCESSFUL));
                        writer.flush();
                        pName = bufferedReader.readLine();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }

                    // Setting new player num to be players.size() is okay because player nums are always reduced to minimum possible value
                    Player newPlayer = new Player(players.size(), pName, s);
                    players.add(newPlayer);
                    newPlayer.sendInt(newPlayer.getPlayerNum());

                    if(host == null) {
                        host = newPlayer;
                    }
                    // Send the names, playerNums, and call ranks of all players in lobby to all players
                    sendPlayersToAll();
                } else {
                    try {
                        var bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        bw.write(Integer.toString(ServerCodes.CONNECTION_DENIED));
                        bw.flush();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    s.dispose();
                }
            }
        }
    }

    // If any player socket is not connected, remove that player from players and compress player nums
    private void manageDisconnections() {
        while(!roundStarted) {
            synchronized(playersLock) {
                if(players.removeIf(p->!p.socketIsConnected())) {
                    // "Compress" player nums ([0, 1, 2, 4, 5] -> [0, 1, 2, 3, 4])
                    for(Player p : players) {
                        p.setPlayerNum(players.indexOf(p));
                    }

                    sendPlayersToAll(); // If any players were removed, resend to players list
                }
            }
        }
    }

    // Checks if host started round, or change calling rank of any player
    private void manageHostCommunication() {
        while(!roundStarted) {
            if(host != null) {
                int clientCode = host.readInt();
                switch(clientCode) {
                    case ClientCodes.START_ROUND:
                        this.roundStarted = true;
                        return;
                    case ClientCodes.WAIT_FOR_NEW_CALLING_RANK:
                        int playerNum = host.readInt();
                        int callRank = host.readInt();
                        synchronized(playersLock) {
                            players.getPlayerFromPlayerNum(playerNum).setCallRank(callRank);
                        }
                }
            }
        }
    }

    // This method is always called from inside a synchronized block so I don't think it needs another one inside but I don't fucking know
    private void sendPlayersToAll() {
        players.sendIntToAll(ServerCodes.WAIT_FOR_PLAYERS_LIST);

        StringBuilder playersString = new StringBuilder();
        for(Player p : players) {
            p.sendInt(p.getPlayerNum()); // As playerNum can change, first send player p their playerNum
            p.sendInt(players.size());
            playersString.append(p.getPlayerNum()).append("\n");
            playersString.append(p.getName()).append("\n");
            playersString.append(p.getCallRank()).append("\n");
        }
        players.sendStringToAll(playersString.toString());
    }
}
