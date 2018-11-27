package com.sage.server;

import com.badlogic.gdx.net.Socket;
import com.sage.Rank;
import com.sage.Suit;
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

    private Rank callRank = Rank.TWO;

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

    int clearReadBuffer() {
        try {
            int i = 0;
            while(bufferedReader.ready()) {
                bufferedReader.read();
                i++;
            }
            return i;
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
            return -1;
        }
    }

    Integer readInt() {
        try {
            Integer i = Integer.parseInt(readLine()); // readLine() will return null if player has disconnected
//            Gdx.app.log("Server.Player.readInt()", "READ INT: " + i);
            return i;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    String readLine() {
        try {
            String line = bufferedReader.readLine();
//            Gdx.app.log("Server.Player.readLine()", "READ STRING: " + line);
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

    void sendString(String string, boolean flushWriteBuffer) {
        try {
//            Gdx.app.log("Server.Player.sendString()", "SENDING STRING: " + string);
            bufferedWriter.write(string);
            bufferedWriter.write("\n");
            if(flushWriteBuffer) bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
        }
    }

    void sendInt(int i, boolean flushWriteBuffer) {
        try {
//            Gdx.app.log("Server.Player.sendInt()", "SENDING INT: " + i);
            bufferedWriter.write(Integer.toString(i));
            bufferedWriter.write("\n");
            if(flushWriteBuffer) bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
        }
    }

    void sendCards(ServerCardList cardList, boolean flushWriteBuffer) {
        for(ServerCard c : cardList) {
            sendInt(c.cardNum(), false);
        }
        if(flushWriteBuffer) flushWriteBuffer();
    }

    void sendString(String string) {
        sendString(string, true);
    }

    void sendInt(int i) {
        sendInt(i, true);
    }

    void sendCards(ServerCardList cardList) {
        for(ServerCard c : cardList) {
            sendInt(c.cardNum());
        }
    }

    void flushWriteBuffer() {
        try {
            bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
            s.dispose();
        }
    }

    boolean isValidCall(ServerCard c, int numCallCards) {
        return callRank == c.rank() && hand.stream().filter(card -> card.valueEquals(c)).count() >= numCallCards
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
        int newCallRankIndex = ((callRank.rankNum - 2) + amount) % Rank.values().length;
        callRank = Rank.values()[newCallRankIndex];
    }

    Rank getCallRank() {
        return callRank;
    }

    void setCallRank(Rank newCallRank) {
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
