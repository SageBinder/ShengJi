package com.sage.server;

import com.sage.Rank;
import com.sage.Suit;
import com.sage.Team;

import java.util.ArrayList;
import java.util.Collections;

class Round {
    private PlayerList players;

    Round(PlayerList players) {
        this.players = players;
    }

    void playNewRound() {
        // Reorganize "seating"
        Collections.shuffle(players);
        players.sendIntToAll(ServerCodes.WAIT_FOR_PLAYER_ORDER);
        for(Player p : players) {
            players.sendIntToAll(p.getPlayerNum());
        }


        int numFullDecks = players.size() / 2;
        Deck deck = new Deck(numFullDecks);
        ServerCardList kitty = getKittyFromDeck(deck);
        ServerCardList friendCards;

        final int numPointsNeeded = 40 * numFullDecks;
        ServerCardList pointCardsCollected = new ServerCardList();

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

            players.sendIntToAll(ServerCodes.WAIT_FOR_TRICK_WINNER);
            players.sendIntToAll(trickWinner.getPlayerNum());
            players.sendIntToAll(ServerCodes.WAIT_FOR_TRICK_POINT_CARDS);
            players.sendCardsToAll(trickResult.getPointCards());

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
        players.sendIntToAll(ServerCodes.WAIT_FOR_COLLECTED_POINTS);
        players.sendIntToAll(totalPointsCollected);

        // Send kitty to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_KITTY);
        players.sendCardsToAll(kitty);

        // Send round winners to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_ROUND_WINNERS);
        Team winningTeam;
        if(totalPointsCollected >= numPointsNeeded) {
            winningTeam = Team.COLLECTORS;
        } else {
            winningTeam = Team.KEEPERS;
        }
        for(Player p : players) {
            if(p.getTeam() == winningTeam) {
                players.sendIntToAll(p.getPlayerNum());
                p.increaseCallRank(1); // TODO: calculate whether or not winners should go up 2 or 3 ranks
            }
        }

        // Send new calling numbers to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_CALLING_NUMBERS);
        for(Player p : players) {
            players.sendIntToAll(p.getPlayerNum());
            players.sendIntToAll(p.getCallRank());
        }
        players.sendIntToAll(ServerCodes.ROUND_OVER);
    }

    private ServerCardList getKittyFromDeck(Deck deck) {
        int kittySize = deck.size() % players.size();
        if(kittySize == 0) {
            kittySize = players.size();
        }
        ServerCardList kitty = new ServerCardList();
        for(int i = 0; i < kittySize; i++) {
            ServerCard c = deck.getRandomCard();
            kitty.add(c);
            deck.remove(c);
        }

        return kitty;
    }

    private void dealDeckToPlayers(Deck deck) {
        deck.dealAllRandomly(players);
        players.sendIntToAll(ServerCodes.WAIT_FOR_HAND);
        for(Player p : players) {
            p.sendInt(p.getHand().size());
            p.sendCards(p.getHand());
        }
    }

    private ServerCardList sendKittyToCallerAndGetNewKitty(ServerCardList kitty, Player caller) {
        ServerCardList newKitty = new ServerCardList();

        // Send caller cards in kitty
        caller.sendInt(ServerCodes.WAIT_FOR_KITTY);
        caller.sendCards(kitty);
        for(ServerCard card : kitty) {
            caller.addToHand(new ServerCard(card.cardNum()));
        }

        // Get cards that caller put in kitty and remove them from their hand
        caller.sendInt(ServerCodes.SEND_KITTY_REPLACEMENTS);
        for(int i = 0; i < kitty.size(); i++) {
            newKitty.add(new ServerCard(caller.readInt()));
            caller.removeFromHand(newKitty.get(i).cardNum());
        }

        return newKitty;
    }

    private ServerCardList getFriendCardsAndSendToOtherPlayers(Player caller) {
        // Get friend cards from caller
        caller.sendInt(ServerCodes.SEND_FRIEND_CARDS);
        ServerCardList friendCards = new ServerCardList();
        for(int i = 0; i < players.size() / 2; i++) {
            friendCards.add(new ServerCard(caller.readInt()));
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

    // TODO: Make sure synchronization in this method is correct
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
                            if(p.isValidCall(new ServerCard(callCardNum)) && numCallCards > highestNumCallCards) {
                                synchronized(lock) {
                                    highestNumCallCards = numCallCards;
                                    if(caller != null) { // Send previous caller SEND_CALL code to check if they want to overtake the new call
                                        caller.sendInt(ServerCodes.SEND_CALL);
                                    }
                                    caller = p;
                                    p.sendInt(ServerCodes.SUCCESSFUL_CALL);
                                    Rank.setCurrentTrumpRank(ServerCard.getRankFromCardNum(callCardNum));
                                    Suit.setCurrentTrumpSuit(ServerCard.getSuitFromCardNum(callCardNum));

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

        players.sendIntToAll(ServerCodes.WAIT_FOR_CALL_WINNER);
        players.sendIntToAll(caller.getPlayerNum());
        return caller;
    }
}
