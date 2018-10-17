package com.sage.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.sage.Team;

import java.io.*;
import java.util.Collections;

class Player {
    private int playerNum;
    private Hand hand = new Hand();
    private Team team = Team.NO_TEAM;

    private ServerCardList points = new ServerCardList();

    private Socket s;

    private String name;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private int callRank = 2;

    Player(int playerNum, String name, Socket s) {
        this.s = s;
        this.playerNum = playerNum;
        this.name = name;
        bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    void resetForNewRound() {
        team = Team.NO_TEAM;
        hand.clear();
        points.clear();
    }

    boolean readyToRead() {
        try {
            return bufferedReader.ready();
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
            return false;
        }
    }

    Integer readInt() {
        try {
            Integer i = Integer.parseInt(readLine());
            Gdx.app.log("Server.Player.readInt()", "READ INT: " + i);
            return i; // readLine() will return null is player has disconnected
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    String readLine() {
        try {
            String line = bufferedReader.readLine();
            Gdx.app.log("Server.Player.readLine()", "READ STRING: " + line);
            if(line == null) {
                s.dispose();
            }

            return line;
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
            return null;
        }
    }

    void sendString(String string) {
        try {
            Gdx.app.log("Server.Player.sendString()", "SENDING STRING: " + string);
            bufferedWriter.write(string);
            bufferedWriter.write("\n");
            bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
        }
    }

    void sendInt(int i) {
        try {
            Gdx.app.log("Server.Player.sendInt()", "SENDING INT: " + i);
            bufferedWriter.write(Integer.toString(i));
            bufferedWriter.write("\n");
            bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
        }
    }

    void sendCards(ServerCardList cardList) {
        for(ServerCard c : cardList) {
            sendInt(c.cardNum());
        }
    }

    boolean isValidCall(ServerCard c, int numCallCards) {
        return callRank == c.rank().toInt() && hand.stream().filter(card -> card == c).count() >= numCallCards
                && !(c.suit() == Suit.BIG_JOKER || c.suit() == Suit.SMALL_JOKER);
    }

    boolean isValidKitty(ServerCardList newKitty) {
        for(ServerCard c : newKitty) {
            if(Collections.frequency(hand, c) < Collections.frequency(newKitty, c)) {
                return false;
            }
        }
        return true;
    }

    void increaseCallRank(int amount) {
        callRank += amount;
    }

    int getCallRank() {
        return callRank;
    }

    void setCallRank(int newCallRank) {
        callRank = newCallRank;
    }

    void setHand(Hand newHand) {
        hand = newHand;
    }

    Hand getHand() {
        return hand;
    }

    void setTeam(Team team) {
        this.team = team;
    }

    Team getTeam() {
        return team;
    }

    void addToHand(ServerCard c) {
        hand.add(c);
    }

    void addToHand(ServerCardList cards) {
        for(ServerCard c : cards) {
            addToHand(c);
        }
    }

    void addToHand(Rank rank, Suit suit) {
        hand.add(rank, suit);
    }

    void removeFromHand(Rank rank, Suit suit) {
        hand.remove(rank, suit);
    }

    void removeFromHand(int cardNum) {
        removeFromHand(ServerCard.getRankFromCardNum(cardNum), ServerCard.getSuitFromCardNum(cardNum));
    }

    void removeFromHand(ServerCard c) {
        hand.remove(c);
    }

    void removeFromHand(ServerCardList cardList) {
        for(ServerCard c : cardList) {
            removeFromHand(c);
        }
    }

    void addPointCard(ServerCard c) {
        points.add(c);
    }

    void addPointCards(ServerCardList points) {
        this.points.addAll(points);
    }

    void clearPoints() {
        points.clear();
    }

    ServerCardList getPoints() {
        return points;
    }

    String getName() {
        return name;
    }

    int getPlayerNum() {
        return playerNum;
    }

    void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    boolean socketIsConnected() {
        return s.isConnected();
    }
}
