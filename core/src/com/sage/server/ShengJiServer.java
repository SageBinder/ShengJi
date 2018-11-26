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
    private int maxPlayers;

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

    public ShengJiServer(int port, int numPlayers) throws GdxRuntimeException {
        maxPlayers = numPlayers;

        ServerSocketHints serverSocketHints = new ServerSocketHints();
        serverSocketHints.acceptTimeout = 0;
        serverSocket = new NetJavaServerSocketImpl(Net.Protocol.TCP, port, serverSocketHints);
    }

    @Override
    public void run() {
        Thread manageNewConnectionsThread = new Thread(manageNewConnections);
        Thread manageDisconnectionsThread = new Thread(manageDisconnections);
        Thread managePlayerCommunicationsThread = new Thread(managePlayerCommunication);

        // Once roundStarted is set to true, all three threads should exit

        RoundRunner roundRunner;
        synchronized(playersLock) {
            roundRunner = new RoundRunner(players);
        }
        do {
            if(!manageNewConnectionsThread.isAlive()) {
                manageNewConnectionsThread.start();
            }
            if(!manageDisconnectionsThread.isAlive()) {
                manageDisconnectionsThread.start();
            }
            if(!managePlayerCommunicationsThread.isAlive()) {
                managePlayerCommunicationsThread.start();
            }
            try {
              //  manageNewConnectionsThread.join();
                manageDisconnectionsThread.join();
                managePlayerCommunicationsThread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            synchronized(playersLock) {
                roundRunner.setPlayers(players);
                Gdx.app.log("Server.run", "before starting new round");
                roundRunner.playNewRound();
                for(Player p : players) {
                    p.resetForNewRound();
                }
            }
        } while(!endGame);

        // manageNewConnectionsThread might still be waiting on a blocking call to serverSocket.accept() at this point.
        // Disposing the serverSocket should unblock that call.

        serverSocket.dispose();
    }

    // TODO: Way of letting players temporarily sit out a round

    // Add new connections while players.size() remains less than maxPlayers
    private Runnable manageNewConnections = () -> {
        while(!roundStarted) {
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
    private Runnable manageDisconnections = () -> {
        while(!roundStarted) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            Gdx.app.log("Server.manageDisconnections", "in while");
            synchronized(playersLock) {
                players.forEach(p -> p.sendInt(ServerCodes.PING));

                if(players.removeIf(p -> !p.socketIsConnected())) {
                    // "Compress" player nums ([0, 1, 2, 4, 5] -> [0, 1, 2, 3, 4])
                    for(Player p : players) {
                        p.setPlayerNum(players.indexOf(p));
                    }

                    sendPlayersToAll(); // If any players were removed, resend to players list
                }
            }
        }
    };

    // TODO: Instead of having one thread loop through all players, make a different thread for each player to manage their communication?
    // Checks if host started round, or change calling rank of any player
    private Runnable managePlayerCommunication = () -> {
        while(!roundStarted) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            Gdx.app.log("Server.managePlayerCommunication", "in while");
            synchronized(playersLock) {
                for(Player p : players) {
                    Integer clientCode = null;

                    if(p.readyToRead()) {
                        clientCode = p.readInt();
                    }

                    if(clientCode == null) {
                        continue;
                    }

                    switch(clientCode) {
                        case ClientCodes.START_ROUND:
                            if(p == host) {
                                this.roundStarted = true;
                                return;
                            }
                            break;
                        case ClientCodes.WAIT_FOR_NEW_CALLING_RANK:
                            if(p == host) {
                                int playerNum = p.readInt();
                                int callRank = p.readInt();
                                synchronized(playersLock) {
                                    players.getPlayerFromPlayerNum(playerNum).setCallRank(Rank.fromInt(callRank));

                                    players.sendIntToAll(ServerCodes.WAIT_FOR_CALLING_NUMBERS);
                                    players.sendIntToAll(players.size());
                                    players.forEach(p1 -> {
                                        players.sendIntToAll(p1.getPlayerNum());
                                        players.sendIntToAll(p1.getCallRank().rankNum);
                                    });
                                }
                            }
                            break;
                        case ClientCodes.PING:
                            Gdx.app.log("Server.managePlayerCommunication",
                                    "RECEIVED PING FROM PLAYER " + p.getPlayerNum() + " (" + p.getName() + ")");
                            break;
                    }
                }
            }
        }
    };

    // This method is always called from inside a synchronized block so I don't think it needs another one inside but I don't fucking know
    private void sendPlayersToAll() {
        Gdx.app.log("Server run", "WAIT_FOR_PLAYERS_LIST write");
        players.sendIntToAll(ServerCodes.WAIT_FOR_PLAYERS_LIST);

        players.forEach(p -> {
            p.sendInt(p.getPlayerNum()); // As playerNum can change, first send player p their playerNum
            p.sendInt(players.size());
        });

        players.forEach(p -> {
            players.sendIntToAll(p.getPlayerNum());
            players.sendStringToAll(p.getName());
            players.sendIntToAll(p.getCallRank().rankNum);
        });

        players.sendIntToAll(host.getPlayerNum());
    }
}
