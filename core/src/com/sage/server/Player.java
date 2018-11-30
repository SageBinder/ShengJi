package com.sage.server;

import com.badlogic.gdx.Gdx;
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

    boolean readyToRead() throws PlayerDisconnectedException {
        try {
            return bufferedReader.ready();
        } catch(IOException e) {
            s.dispose();
            throw new PlayerDisconnectedException();
        }
    }

    int clearReadBuffer() throws PlayerDisconnectedException {
        try {
            int i = 0;
            while(bufferedReader.ready()) {
                bufferedReader.read();
                i++;
            }
            return i;
        } catch(IOException e) {
            s.dispose();
            throw new PlayerDisconnectedException();
        }
    }

    Integer readInt() throws PlayerDisconnectedException {
        try {
            return Integer.parseInt(readLine());
        } catch(NumberFormatException e) {
            return null;
        }
    }

    String readLine() throws PlayerDisconnectedException {
        try {
            String line = bufferedReader.readLine();
            if(line == null) {
                s.dispose();
                throw new PlayerDisconnectedException();
            } else {
                return line;
            }
        } catch(IOException e) {
            s.dispose();
            throw new PlayerDisconnectedException();
        }
    }

    void sendString(String string, boolean flushWriteBuffer) throws PlayerDisconnectedException {
        Gdx.app.log("Server.sendString", "Sending string: " + string + " to " + name);
        try {
            bufferedWriter.write(string);
            bufferedWriter.write("\n");
            if(flushWriteBuffer) bufferedWriter.flush();
        } catch(IOException e) {
            s.dispose();
            throw new PlayerDisconnectedException();
        }
    }

    void sendInt(int i, boolean flushWriteBuffer) throws PlayerDisconnectedException {
        Gdx.app.log("Server.sendInt", "Sending int: " + i + " to " + name);
        try {
            bufferedWriter.write(Integer.toString(i));
            bufferedWriter.write("\n");
            if(flushWriteBuffer) bufferedWriter.flush();
        } catch(IOException e) {
            s.dispose();
            throw new PlayerDisconnectedException();
        }
    }

    void sendCards(ServerCardList cardList, boolean flushWriteBuffer) throws PlayerDisconnectedException{
        cardList.forEach(c -> sendInt(c.cardNum(), false));
        if(flushWriteBuffer) flushWriteBuffer();
    }

    void sendString(String string) throws PlayerDisconnectedException{
        sendString(string, true);
    }

    void sendInt(int i) throws PlayerDisconnectedException{
        sendInt(i, true);
    }

    void sendCards(ServerCardList cardList) throws PlayerDisconnectedException {
        cardList.forEach(c -> sendInt(c.cardNum()));
    }

    void flushWriteBuffer() throws PlayerDisconnectedException {
        try {
            bufferedWriter.flush();
        } catch(IOException e) {
            s.dispose();
            throw new PlayerDisconnectedException();
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
