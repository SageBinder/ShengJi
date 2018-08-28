package com.sage.server;

import java.util.ArrayList;
import java.util.Collections;

class Round {
    private ArrayList<Player> players = new ArrayList<>();

    Round(ArrayList<Player> players) {
        this.players.addAll(players);
    }

    Player playNewRound() {
        Collections.shuffle(players); // TODO: send all players the new player order

        int numFullDecks = players.size() / 2;
        Deck deck = makeNewDeck(numFullDecks);
        int[] kitty = getKittyFromDeck(deck);
        CardList friendCards;

        final int numPointsNeeded = 40 * numFullDecks;
        CardList pointCardsCollected = new CardList();

        dealDeckToPlayers(deck);
        Player caller = establishCaller();
        kitty = sendKittyToCallerAndGetNewKitty(kitty, caller);
        friendCards = getFriendCardsAndSendToOtherPlayers(caller);


        Player trickWinner = caller;
        while(players.get(0).getHand().size() > 0) {
            Trick trick = new Trick(players, friendCards);
            TrickResult trickResult = trick.startNewTrick(trickWinner);
            trickWinner = trickResult.getWinner();
            pointCardsCollected.addAll(trickResult.getPointCards());

            Player.sendIntToAll(players, ServerCodes.WAIT_FOR_TRICK_WINNER);
            Player.sendIntToAll(players, trickWinner.getPlayerNum());
            Player.sendIntToAll(players, ServerCodes.WAIT_FOR_TRICK_POINT_CARDS);
            for(Card c : trickResult.getPointCards()) {
                Player.sendIntToAll(players, c.getCardNum());
            }
        }

        return players.get(0);
    }

    private Deck makeNewDeck(int numFullDecks) {
        Deck deck = new Deck();
        for(int i = 0; i < numFullDecks; i++) {
            for(int j = 0; j < 54; j++) {
                deck.add(new Card(j));
            }
        }
        return deck;
    }

    private int[] getKittyFromDeck(Deck deck) {
        int kittySize = deck.size() % players.size();
        if(kittySize == 0) {
            kittySize = players.size();
        }
        int[] kittyCardNums = new int[kittySize];
        for(int i = 0; i < kittySize; i++) {
            Card c = deck.getRandomCard();
            kittyCardNums[i] = c.getCardNum();
            deck.remove(c);
        }

        return kittyCardNums;
    }

    private void dealDeckToPlayers(Deck deck) {
        deck.dealAllRandomly(players);
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_HAND);
        for(Player p : players) {
            for(Card c : p.getHand()) {
                p.sendInt(c.getCardNum());
            }
        }
    }

    private int[] sendKittyToCallerAndGetNewKitty(int[] kittyCardNums, Player caller) {
        int[] newKitty = new int[kittyCardNums.length];

        // Send caller cards in kittyCardNums
        caller.sendInt(ServerCodes.WAIT_FOR_KITTY);
        for(int cardNum : kittyCardNums) {
            caller.sendInt(cardNum);
            caller.addToHand(new Card(cardNum));
        }

        // Get cards that caller put in kittyCardNums and remove them from their hand
        caller.sendInt(ServerCodes.SEND_KITTY_REPLACEMENTS);
        for(int i = 0; i < newKitty.length; i++) {
            newKitty[i] = caller.readInt();
            caller.removeFromHand(Card.getRankFromCardNum(newKitty[i]), Card.getSuitFromCardNum(newKitty[i]));
        }

        return newKitty;
    }

    private CardList getFriendCardsAndSendToOtherPlayers(Player caller) {
        // Get friend cards from caller
        caller.sendInt(ServerCodes.SEND_FRIEND_CARDS);
        CardList friendCards = new CardList();
        for(int i = 0; i < players.size() / 2; i++) {
            friendCards.add(new Card(caller.readInt()));
        }

        // Send friend cards to other players
        for(Player p : players) {
            if(p != caller) {
                p.sendInt(ServerCodes.WAIT_FOR_FRIEND_CARDS);
                for(Card c : friendCards) {
                    p.sendInt(c.getCardNum());
                }
            }
        }

        return friendCards;
    }

    private final Object lock = new Object();
    volatile private int highestNumCallCards = 0; // This variable is the highest number of cards used to call (single, double, or triple)
    volatile private Player caller;
    volatile private int numNoCallPlayers = 0;

    private Player establishCaller() {
        ArrayList<Thread> threads = new ArrayList<>();

        // Creates a thread for each player listening for their call.
        for(Player p : players) {
            Runnable r = () -> {
                try {
                    int callCardNum;
                    int numCallCards;
                    while(true) {
                        Thread.sleep(100);
                        if(numNoCallPlayers == players.size() - 1 && caller != null) {
                            return;
                        }
                        if(p.readyToRead()) { // As a way to avoid the blocking call to readLine(),
                            try {                   // check first if the bufferedReader has input available
                                callCardNum = Integer.parseInt(p.readLine());
                                // A value of -1 means the player does not want to call.
                                if(callCardNum == -1) {
                                    p.sendInt(ServerCodes.NO_CALL);
                                    synchronized(lock) {
                                        numNoCallPlayers++;
                                    }
                                    return;
                                }
                                numCallCards = Integer.parseInt(p.readLine());
                            } catch(NumberFormatException e) {
                                p.sendInt(ServerCodes.INVALID_CALL);
                                continue;
                            }
                            if(p.isValidCall(new Card(callCardNum)) && numCallCards > highestNumCallCards) {
                                synchronized(lock) {
                                    highestNumCallCards = numCallCards;
                                    if(caller != null) { // Send previous caller SEND_CALL code to check if they want to overtake the new call
                                        caller.sendInt(ServerCodes.SEND_CALL);
                                    }
                                    caller = p;
                                    p.sendInt(ServerCodes.SUCCESSFUL_CALL);
                                    Rank.setCurrentTrumpRank(Card.getRankFromCardNum(callCardNum));
                                    Suit.setCurrentTrumpSuit(Card.getSuitFromCardNum(callCardNum));

                                    for(Player p1 : players) {
                                        if(p1 != caller) {
                                            p1.sendInt(ServerCodes.WAIT_FOR_OTHER_PLAYER_CALL);
                                            p1.sendString(caller.getPlayerNum() + "\n" + callCardNum + "\n" + numCallCards);
                                        }
                                    }
                                }
                            } else {
                                p.sendInt(ServerCodes.INVALID_CALL); // -1 indicates invalid call, 0 indicates someone else called (or p chose not to call), 1 indicates successful call
                            }
                        }
                    }
                } catch(InterruptedException e) {
                    p.sendInt(ServerCodes.NO_CALL);
                    synchronized(lock) {
                        numNoCallPlayers++;
                    }
                }
            };
            p.sendInt(ServerCodes.SEND_CALL);
            Thread t = new Thread(r);
            t.start();
            threads.add(t);
        }

        for(Thread t : threads) {
            try {
                t.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(caller == null) { // TODO: if no one calls, do something other than this
            caller = players.get(0);
        }

        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_CALL_WINNER);
        Player.sendIntToAll(players, caller.getPlayerNum());
        return caller;
    }
}
