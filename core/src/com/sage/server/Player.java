package com.sage.server;

import com.badlogic.gdx.net.Socket;

import java.io.*;
import java.util.ArrayList;

@SuppressWarnings("StatementWithEmptyBody")
class Player {
    private int playerNum;
    private Hand hand = new Hand();
//    private Play currentPlay;
    private Team team = Team.NO_TEAM;

    private Socket s;

    private String name;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private int callRank = 2;

    static void sendStringToAll(ArrayList<Player> players, String message) {
        for(Player p : players) {
            p.sendString(message);
        }
    }

    static void sendIntToAll(ArrayList<Player> players, int i) {
        for(Player p : players) {
            p.sendInt(i);
        }
    }

    static void sendCardsToAll(ArrayList<Player> players, CardList cards) {
        for(Player p : players) {
            p.sendCards(cards);
        }
    }

    Player(int playerNum, String name, Socket s) {
        this.s = s;
        this.playerNum = playerNum;
        this.name = name;
        bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    void resetForNewRound() {
        team = Team.NO_TEAM;
        hand = new Hand();
    }

    boolean readyToRead() {
        try {
            return bufferedReader.ready();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    int readInt() {
        try {
            return Integer.parseInt(readLine());
        } catch(NullPointerException e) {
            e.printStackTrace();
            return -1; // TODO: NullPointerException may be thrown here if this player disconnects
        }
    }

    String readLine() {
        try {
            String line;
            while((line = bufferedReader.readLine()).trim().isEmpty()); // This empty while loop just ignores empty strings
            return line;
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void sendString(String string) {
        try {
            bufferedWriter.write(string);
            bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    void sendInt(int i) {
        try {
            bufferedWriter.write(i);
            bufferedWriter.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    void sendCards(CardList cardList) {
        for(Card c : cardList) {
            sendInt(c.getCardNum());
        }
    }

    boolean isValidCall(Card c) {
//        if(c.suit == Suit.BIG_JOKER || c.suit == Suit.SMALL_JOKER) {
//            return false;
//        }
        return callRank == c.rank.toInt() && hand.contains(c) && !(c.suit == Suit.BIG_JOKER || c.suit == Suit.SMALL_JOKER);
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

//    void setPlay(Play newPlay) {
//        currentPlay = newPlay;
//    }
//
//    Play getPlay() {
//        return currentPlay;
//    }

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

    void addToHand(Card c) {
        hand.add(c);
    }

    void addToHand(Rank rank, Suit suit) {
        hand.add(rank, suit);
    }

    void removeFromHand(Rank rank, Suit suit) {
        hand.remove(rank, suit);
    }

    void removeFromHand(int cardNum) {
        removeFromHand(Card.getRankFromCardNum(cardNum), Card.getSuitFromCardNum(cardNum));
    }

    void removeFromHand(Card c) {
        hand.remove(c);
    }

    void removeFromHand(CardList cardList) {
        for(Card c : cardList) {
            removeFromHand(c.getRank(), c.getSuit());
        }
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
