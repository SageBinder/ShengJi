package com.sage.server;

import com.sage.Team;
import com.sage.shengji.ClientCodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class RoundRunner {
    private PlayerList players;

    RoundRunner(PlayerList players) {
        setPlayers(players);
    }

    void setPlayers(PlayerList players) {
        this.players = players;
    }

    void playNewRound() {
        // Reorganize "seating"
        Collections.shuffle(players);
        players.sendIntToAll(ServerCodes.ROUND_START);
        players.sendIntToAll(ServerCodes.WAIT_FOR_PLAYER_ORDER);
        players.sendIntToAll(players.size());
        for(Player p : players) {
            players.sendIntToAll(p.getPlayerNum());
        }


        int numFullDecks = players.size() / 2;
        Deck deck = new Deck(numFullDecks);
        ServerCardList kitty = getKittyFromDeck(deck);
        ServerCardList friendCards;

        final int numPointsNeeded = 40 * numFullDecks;
        ServerCardList pointCardsCollected = new ServerCardList();

        Player caller = establishCaller(kitty);

        dealDeckToPlayers(deck);

        kitty = sendKittyToCallerAndGetNewKitty(kitty, caller);
        friendCards = getFriendCardsAndSendToOtherPlayers(caller);


        Player trickWinner = caller;
        Play winningPlay = null;
        while(players.get(0).getHand().size() > 0) {
            TrickRunner trickRunner = new TrickRunner(players, friendCards);
            TrickResult trickResult = trickRunner.startNewTrick(trickWinner);

            trickWinner = trickResult.getWinner();
            if(trickWinner.getTeam() != Team.KEEPERS) {
                trickWinner.addPointCards(trickResult.getPointCards());
            }

            players.forEach(player -> { // If any player became a collector during that trick, the player's points become
                if(player.getTeam() == Team.COLLECTORS) { // the collector's points.
                    pointCardsCollected.addAll(player.getPoints());
                    player.clearPoints();
                }
            });

            players.sendIntToAll(ServerCodes.WAIT_FOR_TRICK_WINNER);
            players.sendIntToAll(trickWinner.getPlayerNum());

            players.sendIntToAll(ServerCodes.WAIT_FOR_TRICK_POINT_CARDS);
            players.sendIntToAll(trickResult.getPointCards().size());
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
        caller.sendInt(kitty.size());
        caller.sendCards(kitty);
        for(ServerCard card : kitty) {
            caller.addToHand(new ServerCard(card.cardNum()));
        }

        // Get cards that caller put in kitty and remove those cards from their hand
        caller.sendInt(ServerCodes.SEND_KITTY);
        while(true) {
            for(int i = 0; i < kitty.size(); i++) {
                newKitty.add(new ServerCard(caller.readInt()));
            }

            if(caller.isValidKitty(newKitty)) {
                caller.removeFromHand(newKitty);
                break;
            } else {
                caller.sendInt(ServerCodes.INVALID_KITTY);
                newKitty.clear();
            }
        }

        return newKitty;
    }

    private ServerCardList getFriendCardsAndSendToOtherPlayers(Player caller) {
        // Get friend cards from caller
        ServerCardList friendCards = new ServerCardList();
        int numFriendCards = players.size() / 2;

        caller.sendInt(ServerCodes.SEND_FRIEND_CARDS);
        caller.sendInt(numFriendCards);
        for(int i = 0; i < numFriendCards; i++) {
            friendCards.add(new ServerCard(caller.readInt()));
        }

        // Send friend cards to other players
        for(Player p : players) {
            if(p != caller) {
                p.sendInt(ServerCodes.WAIT_FOR_FRIEND_CARDS);
                p.sendInt(friendCards.size());
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
    private Player establishCaller(ServerCardList kitty) {
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
                                if(callCardNum == ClientCodes.NO_CALL) {
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

                            ServerCard callCard = new ServerCard(callCardNum);
                            if(p.isValidCall(callCard) && numCallCards > highestNumCallCards) {
                                synchronized(lock) {
                                    highestNumCallCards = numCallCards;
                                    caller = p;
                                    caller.sendInt(ServerCodes.SUCCESSFUL_CALL);
                                    Rank.setCurrentTrumpRank(callCard.rank());
                                    Suit.setCurrentTrumpSuit(callCard.suit());

                                    for(Player p1 : players) {
                                        if(p1 != caller) {
                                            p1.sendInt(ServerCodes.WAIT_FOR_NEW_WINNING_CALL);
                                            p1.sendString(caller.getPlayerNum() + "\n" + callCardNum + "\n" + numCallCards);
                                        }
                                    }
                                }
                            } else if(p.isValidCall(callCard)) {
                                p.sendInt(ServerCodes.UNSUCCESSFUL_CALL);
                            } else {
                                p.sendInt(ServerCodes.INVALID_CALL);
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

        if(caller == null) {
            caller = players.get(new Random(69).nextInt(players.size()));
            players.sendIntToAll(ServerCodes.NO_ONE_CALLED);
            players.sendIntToAll(caller.getPlayerNum());
            for(ServerCard c : kitty) {
                if(!c.isJoker()) {
                    players.sendIntToAll(c.cardNum());
                    break;
                }
            }
        } else {
            players.sendIntToAll(ServerCodes.WAIT_FOR_CALL_WINNER);
            players.sendIntToAll(caller.getPlayerNum());
        }

        return caller;
    }
}
