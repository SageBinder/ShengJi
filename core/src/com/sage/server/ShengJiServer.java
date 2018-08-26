package com.sage.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShengJiServer {
    private ArrayList<Player> players = new ArrayList<>();

    // TODO: Set gameEnd when players want to end game
    private boolean gameEnd = false;

    public void start(int PORT, int numPlayers) {
        var serverSocketHints = new ServerSocketHints();
        serverSocketHints.acceptTimeout = 0;
        ServerSocket serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, PORT, serverSocketHints);

        // Accept client connections until all slots are full
        while(players.size() < numPlayers) {
            Socket s = serverSocket.accept(null);
            var bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String pName = "Player" + Integer.toString(players.size());
            try { // New client connections should send the name of the player
                pName = bufferedReader.readLine();
            } catch(IOException e) {
                e.printStackTrace();
            }

            Player newPlayer = new Player(players.size(), pName, s);
            players.add(newPlayer);
            newPlayer.sendInt(newPlayer.getPlayerNum());

            // Send the names of all players in lobby to all players
            StringBuilder playersString = new StringBuilder();
            for(Player p : players) {
                playersString.append(p.getPlayerNum()).append("\n");
                playersString.append(p.getName()).append("\n");
            }
            Player.sendStringToAll(players, playersString.toString());
        }

        Round round = new Round(players);
        while(!gameEnd) {
            Player winner = round.playNewRound();
            winner.incrementCallRank();
        }
    }
}
