package com.sage.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class Round {
    private ArrayList<Player> players = new ArrayList<>();

    Round(ArrayList<Player> players) {
        this.players.addAll(players);
    }

    void playNewRound() {
        // Reorganize "seating"
        Collections.shuffle(players);
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_PLAYER_ORDER);
        for(Player p : players) {
            Player.sendIntToAll(players, p.getPlayerNum());
        }


        int numFullDecks = players.size() / 2;
        Deck deck = new Deck(numFullDecks);
        CardList kitty = getKittyFromDeck(deck);
        CardList friendCards;

        final int numPointsNeeded = 40 * numFullDecks;
        CardList pointCardsCollected = new CardList();

        dealDeckToPlayers(deck);
        Player caller = establishCaller();
        kitty = sendKittyToCallerAndGetNewKitty(kitty, caller);
        friendCards = getFriendCardsAndSendToOtherPlayers(caller);


        Player trickWinner = caller;
        Play winningPlay = null;
        while(players.get(0).getHand().size() > 0) {
            Trick trick = new Trick(players, friendCards);
            TrickResult trickResult = trick.startNewTrick(trickWinner);
            trickWinner = trickResult.getWinner();
            if(trickWinner.getTeam() == Team.COLLECTORS) {
                pointCardsCollected.addAll(trickResult.getPointCards());
            }

            Player.sendIntToAll(players, ServerCodes.WAIT_FOR_TRICK_WINNER);
            Player.sendIntToAll(players, trickWinner.getPlayerNum());
            Player.sendIntToAll(players, ServerCodes.WAIT_FOR_TRICK_POINT_CARDS);
            Player.sendCardsToAll(players, trickResult.getPointCards());

            winningPlay = trickResult.getWinningPlay();
        }

        int totalPointsCollected = pointCardsCollected.getTotalPoints();
        int kittyPointsMultiplier;

        assert winningPlay != null : "This should never happen";
        kittyPointsMultiplier = winningPlay.size();
        if(winningPlay.getPlayer().getTeam() == Team.COLLECTORS) {
            totalPointsCollected += kittyPointsMultiplier * (kitty.getTotalPoints());
        }
        // Send total collected points to all players
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_COLLECTED_POINTS);
        Player.sendIntToAll(players, totalPointsCollected);

        // Send kitty to all players
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_KITTY);
        Player.sendCardsToAll(players, kitty);

        // Send round winners to all players
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_ROUND_WINNERS);
        Team winningTeam;
        if(totalPointsCollected >= numPointsNeeded) {
            winningTeam = Team.COLLECTORS;
        } else {
            winningTeam = Team.KEEPERS;
        }
        for(Player p : players) {
            if(p.getTeam() == winningTeam) {
                Player.sendIntToAll(players, p.getPlayerNum());
                p.increaseCallRank(1); // TODO: calculate whether or not winners should go up 2 or 3 ranks
            }
        }

        // Send new calling numbers to all players
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_CALLING_NUMBERS);
        for(Player p : players) {
            Player.sendIntToAll(players, p.getPlayerNum());
            Player.sendIntToAll(players, p.getCallRank());
        }
    }

    private CardList getKittyFromDeck(Deck deck) {
        int kittySize = deck.size() % players.size();
        if(kittySize == 0) {
            kittySize = players.size();
        }
        CardList kitty = new CardList();
        for(int i = 0; i < kittySize; i++) {
            Card c = deck.getRandomCard();
            kitty.add(c);
            deck.remove(c);
        }

        return kitty;
    }

    private void dealDeckToPlayers(Deck deck) {
        deck.dealAllRandomly(players);
        Player.sendIntToAll(players, ServerCodes.WAIT_FOR_HAND);
        for(Player p : players) {
            p.sendCards(p.getHand());
        }
    }

    private CardList sendKittyToCallerAndGetNewKitty(CardList kitty, Player caller) {
        CardList newKitty = new CardList();

        // Send caller cards in kitty
        caller.sendInt(ServerCodes.WAIT_FOR_KITTY);
        caller.sendCards(kitty);
        for(Card card : kitty) {
            caller.addToHand(new Card(card.getCardNum()));
        }

        // Get cards that caller put in kitty and remove them from their hand
        caller.sendInt(ServerCodes.SEND_KITTY_REPLACEMENTS);
        for(int i = 0; i < kitty.size(); i++) {
            newKitty.add(new Card(caller.readInt()));
            caller.removeFromHand(newKitty.get(i).getCardNum());
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
                p.sendCards(friendCards);
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
