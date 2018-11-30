package com.sage.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sage.Rank;
import com.sage.shengji.ClientCodes;

import java.io.*;

public class ShengJiServer extends Thread {
    public final int port;

    private int maxPlayers;

    private final Object playersLock = new Object();
    private PlayerList players = new PlayerList();


    // host can be volatile because it's only written to by one thread
//    private final Object hostLock = new Object();
    private volatile Player host;

    // TODO: Set endGame when players want to end game
    private volatile boolean endGame = false;

    // A lock is not needed for roundStarted because it is volatile (it's only written to by one thread)
    private volatile boolean roundStarted;

    private NetJavaServerSocketImpl serverSocket;

    public ShengJiServer(int port, int numPlayers) throws GdxRuntimeException {
        this.port = port;
        maxPlayers = numPlayers;

        ServerSocketHints serverSocketHints = new ServerSocketHints();
        serverSocketHints.acceptTimeout = 0;
        serverSocket = new NetJavaServerSocketImpl(Net.Protocol.TCP, port, serverSocketHints);
    }

    @Override
    public void run() {
        RoundRunner roundRunner;
        synchronized(playersLock) {
            roundRunner = new RoundRunner(players);
        }

        roundStarted = false;
        while(!endGame) {
            // Once roundStarted is set to true, all three threads should exit
            Thread manageNewConnectionsThread = new Thread(manageNewConnections);
            Thread pingPlayersThread = new Thread(pingPlayers);
            Thread managePlayerCommunicationsThread = new Thread(managePlayerCommunication);
            manageNewConnectionsThread.start();
            pingPlayersThread.start();
            managePlayerCommunicationsThread.start();
            try {
                pingPlayersThread.join();
                managePlayerCommunicationsThread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            synchronized(playersLock) {
                try {
                    roundRunner.playNewRound();
                } catch(PlayerDisconnectedException e) {
                    Gdx.app.log("Server.run()",
                            "A player disconnected during round, " +
                                    "removing disconnected players and sending PLAYER_DISCONNECTED_DURING_ROUND to all");
                    removeDisconnectedPlayers(false);
                    try {
                        players.sendIntToAll(ServerCodes.PLAYER_DISCONNECTED_DURING_ROUND, false);
                    } catch(PlayerDisconnectedException e1) {
                        removeDisconnectedPlayers(false);
                    } finally {
                        sendPlayersToAll(true);
                    }
                }
                players.forEach(Player::resetForNewRound);
            }

            manageNewConnectionsThread.interrupt();
            roundStarted = false;
        }

        // manageNewConnectionsThread might still be waiting on a blocking call to serverSocket.accept() at this point.
        // Disposing the serverSocket should unblock that call.

        serverSocket.dispose();
    }

    // TODO: Way of letting players temporarily sit out a round

    // Add new connections while players.size() remains less than maxPlayers
    private Runnable manageNewConnections = () -> {
        while(!isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            // The connection has to be accepted outside of the synchronized block because serverSocket.accept()
            // is a blocking method. Being inside the synchronized block would prevent the other threads from
            // interacting with players because this thread will always be looking to accept new connections.
            // Instead, if the max players is met or the round has already started, dispose of the new
            // connection that was made (because it was made after max players was met or the round was started)
            Gdx.app.log("Server.manageNewConnections", "before serverSocket.accept");
            Socket s = serverSocket.accept(null);
            Gdx.app.log("Server.manageNewConnections", "after serverSocket.accept");
            var bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));

            if(roundStarted) {
                try {
                    var bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    bw.write(Integer.toString(ServerCodes.CONNECTION_DENIED));
                    bw.flush();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                s.dispose();
            }

            synchronized(playersLock) {
                if(players.size() < maxPlayers && !roundStarted) {
                    String pName = "Player " + players.size();

                    try { // New client connections should send the name of the player
                        var writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        Gdx.app.log("Server.manageNewConnections", "JOIN_SUCCESSFUL write");
                        writer.write(Integer.toString(ServerCodes.JOIN_SUCCESSFUL) + "\n");
                        writer.flush();
                        Gdx.app.log("Server.manageNewConnections", "Player name read");
                        pName = bufferedReader.readLine();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }

                    // Setting new player num to be players.size() is okay because player nums are always reduced to minimum possible value
                    Player newPlayer = new Player(players.size(), pName, s);
                    players.add(newPlayer);
                    //                    newPlayer.sendInt(newPlayer.getPlayerNum());

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
    };

    // If any player socket is not connected, remove that player from players and compress player nums
    private Runnable pingPlayers = () -> {
        while(!roundStarted) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            synchronized(playersLock) {
                Gdx.app.log("Server.pingPlayers", "Pinging all players");
                try {
                    players.sendIntToAll(ServerCodes.PING);
                } catch(PlayerDisconnectedException e) {
                    removeDisconnectedPlayers(true);
                }
            }
        }
    };

    // TODO: Instead of having one thread loop through all players, make a different thread for each player to manage their communication?
    // Checks if host started round, or change calling rank of any player
    private Runnable managePlayerCommunication = () -> {
        int maxReadsPerPlayer = 5;

        while(!roundStarted) {
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            synchronized(playersLock) {
                boolean flushBuffers = false;
                boolean anyPlayersDisconnected = false;
                for(Player p : players) {
                    try {
                        thisPlayerReadLoop:
                        for(int i = 0; p.readyToRead() && i < maxReadsPerPlayer; i++) {
                            Integer clientCode = p.readInt();
                            switch(clientCode) {
                                case ClientCodes.START_ROUND:
                                    if(p == host) {
                                        this.roundStarted = true;
                                        return;
                                    }
                                    break;
                                case ClientCodes.WAIT_FOR_NEW_CALLING_RANK:
                                    Gdx.app.log("Server.managePlayerCommunication",
                                            "WAIT_FOR_NEW_CALLING_RANK code received from player " + p);
                                    if(p == host) {
                                        int playerNum = p.readInt();
                                        int callRank = p.readInt();
                                        var player = players.getPlayerFromPlayerNum(playerNum);

                                        try {
                                            assert player != null;
                                            player.setCallRank(Rank.fromInt(callRank));
                                        } catch(AssertionError | IllegalArgumentException e) {
                                            try {
                                                p.clearReadBuffer();
                                            } catch(PlayerDisconnectedException e1) {
                                                removeDisconnectedPlayers(true);
                                            }
                                            break thisPlayerReadLoop;
                                        }

                                        players.forEach(p1 -> {
                                            if(p1 != host) {
                                                p1.sendInt(ServerCodes.WAIT_FOR_NEW_CALLING_RANK, false);
                                                p1.sendInt(playerNum, false);
                                                p1.sendInt(callRank, false);
                                            }
                                        });
                                        flushBuffers = true;
                                    } else {
                                        p.clearReadBuffer();
                                    }
                                    break;
                                case ClientCodes.PING:
                                    break;
                            }
                        }
                    } catch(PlayerDisconnectedException e) {
                        anyPlayersDisconnected = true;
                    }
                }
                if(anyPlayersDisconnected) {
                    removeDisconnectedPlayers(true);
                }
                if(flushBuffers) {
                    try {
                        players.flushAllWriteBuffers();
                    } catch(PlayerDisconnectedException e) {
                        removeDisconnectedPlayers(true);
                    }
                }
            }
        }
    };

    // This method is always called from inside a synchronized block so I don't think it needs another one inside but I don't fucking know
    private void sendPlayersToAll(boolean flushWriteBuffers) {
        boolean anyPlayerDisconnected = false;
        var playersMessage = new StringBuilder();
        playersMessage
                .append(ServerCodes.WAIT_FOR_PLAYERS_LIST).append("\n")
                .append(players.size()).append("\n");
        players.forEach(p -> playersMessage
                .append(p.getPlayerNum()).append("\n")
                .append(p.getName()).append("\n")
                .append(p.getCallRank().rankNum).append("\n"));
        playersMessage.append(host.getPlayerNum());
        // Last append doesn't need "\n" because sendStringToAll automatically appends "\n" to the end

        try {
            players.sendStringToAll(playersMessage.toString(), false);
        } catch(PlayerDisconnectedException e) {
            anyPlayerDisconnected = true;
        }
        for(Player p : players) {
            try {
                p.sendInt(p.getPlayerNum(), false);
            } catch(PlayerDisconnectedException e) {
                anyPlayerDisconnected = true;
            }
        }

        if(flushWriteBuffers) {
            try {
                players.flushAllWriteBuffers();
            } catch(PlayerDisconnectedException e) {
                anyPlayerDisconnected = true;
            }
        }
        if(anyPlayerDisconnected) {
            removeDisconnectedPlayers(true);
        }
    }

    private void sendPlayersToAll() throws PlayerDisconnectedException {
        sendPlayersToAll(true);
    }

    private void removeDisconnectedPlayers(boolean sendPlayersToAll) {
        while(true) {
            if(players.removeIf(p -> !p.socketIsConnected())) {
                // "Compress" player nums ([0, 1, 2, 4, 5] -> [0, 1, 2, 3, 4])
                players.forEach(p -> p.setPlayerNum(players.indexOf(p)));
                try {
                    Gdx.app.log("ShengJiServer.removeDisconnectedPlayers()", "Removed at least one player");
                    if(sendPlayersToAll) sendPlayersToAll(); // If any players were removed, resend players list
                    return;
                } catch(PlayerDisconnectedException ignored) {}
            } else {
                return;
            }
        }
    }
}
