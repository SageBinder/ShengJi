package com.sage.server;

import com.badlogic.gdx.net.Socket;

import java.io.*;
import java.util.ArrayList;

class Player {
    private int playerNum;
    private Hand hand = new Hand();
//    private Play currentPlay;
    private Team team = Team.NO_TEAM;

    private String name;
    private String ip;
    private Socket s;
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

    Player(int playerNum, String name, Socket s) {
        this.s = s;
        this.playerNum = playerNum;
        this.name = name;
        ip = s.getRemoteAddress();
        bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
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
            char c;
            StringBuilder intString = new StringBuilder();
            while(Character.isDigit((c = (char)bufferedReader.read()))) {
                intString.append(c);
            }
            return Integer.parseInt(intString.toString());
        } catch(IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    String readLine() {
        try {
            return bufferedReader.readLine();
        } catch(IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    void sendString(String string) {
        try {
            bufferedWriter.write(string);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    void sendInt(int i) {
        try {
            bufferedWriter.write(i);
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

    void incrementCallRank() {
        callRank++;
    }

    int getCallRank() {
        return callRank;
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
}
