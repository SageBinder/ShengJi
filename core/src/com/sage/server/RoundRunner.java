package com.sage.server;

import com.badlogic.gdx.Gdx;
import com.sage.Rank;
import com.sage.Suit;
import com.sage.Team;
import com.sage.shengji.ClientCodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

class RoundRunner {
    static Suit trumpSuit;
    static Rank trumpRank;

    private PlayerList players;

    RoundRunner(PlayerList players) {
        setPlayers(players);
    }

    void setPlayers(PlayerList players) {
        this.players = players;
    }

    void playNewRound() {
        int numFullDecks;
        Deck deck;
        ServerCardList kitty;
        ServerCardList friendCards;

        final int numPointsNeeded;
        final int totalAvailablePoints;
        ServerCardList pointCardsCollected;

        Player caller;
        Player trickWinner;
        Play winningPlay;

        trumpSuit = null;
        trumpRank = null;

        // Reorganize "seating"
        Collections.shuffle(players);
        players.sendIntToAll(ServerCodes.ROUND_START);
        players.sendIntToAll(ServerCodes.WAIT_FOR_PLAYER_ORDER);
        players.sendIntToAll(players.size());
        players.forEach(p -> players.sendIntToAll(p.getPlayerNum()));

        numFullDecks = Math.max(players.size() / 2, 1);
        deck = new Deck(numFullDecks);
        numPointsNeeded = 40 * numFullDecks;

        players.sendIntToAll(ServerCodes.WAIT_FOR_NUM_POINTS_NEEDED, false);
        players.sendIntToAll(numPointsNeeded, false);
        players.flushAllWriteBuffers();

        kitty = getKittyFromDeck(deck);

        totalAvailablePoints = deck.getTotalPoints();
        pointCardsCollected = new ServerCardList();

        dealDeckToPlayers(deck);
        caller = establishCaller(kitty);

        kitty = sendKittyToCallerAndGetNewKitty(kitty, caller);
        friendCards = getFriendCardsAndSendToOtherPlayers(caller);

        trickWinner = caller;
        winningPlay = null;
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
        int callRankIncrease;
        Team winningTeam;

        try {
            assert winningPlay != null;
        } catch(AssertionError e) {
            e.printStackTrace();
            players.sendIntToAll(ServerCodes.ROUND_OVER);
            Gdx.app.log("Server.RoundRunner.playNewRound()", "assert winningPlay != null FAILED. SHOULDN'T FUCKING HAPPEN.\n" +
                    "Returning now because at this point it's way fucked.");
            return;
        }

        kittyPointsMultiplier = winningPlay.size() + 1;
        if(winningPlay.getPlayer().getTeam() == Team.COLLECTORS) {
            totalPointsCollected += kittyPointsMultiplier * kitty.getTotalPoints();
        }

        if(totalPointsCollected >= numPointsNeeded) {
            winningTeam = Team.COLLECTORS;
            if(totalPointsCollected >= totalAvailablePoints) {
                callRankIncrease = 3;
            } else if (totalPointsCollected >= numPointsNeeded * 1.5f) {
                callRankIncrease = 2;
            } else {
                callRankIncrease = 1;
            }
        } else {
            winningTeam = Team.KEEPERS;
            if(totalPointsCollected == 0) {
                callRankIncrease = 3;
            } else if(totalPointsCollected < numPointsNeeded / 2) {
                callRankIncrease = 2;
            } else {
                callRankIncrease = 1;
            }
        }

        // Send total collected points to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_NUM_COLLECTED_POINTS);
        players.sendIntToAll(totalPointsCollected);

        // Send kitty to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_ROUND_END_KITTY);
        players.sendIntToAll(kitty.size());
        players.sendCardsToAll(kitty);

        PlayerList roundWinners = new PlayerList();
        roundWinners.addAll(players.stream().filter(p -> p.getTeam() == winningTeam).collect(Collectors.toList()));
        roundWinners.forEach(p -> p.increaseCallRank(callRankIncrease));

        // Send round winners to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_ROUND_WINNERS);
        players.sendIntToAll(roundWinners.size());
        roundWinners.forEach(p -> players.sendIntToAll(p.getPlayerNum()));

        // Send new calling numbers to all players
        players.sendIntToAll(ServerCodes.WAIT_FOR_CALLING_NUMBERS);
        players.sendIntToAll(players.size());
        players.forEach(p -> {
            players.sendIntToAll(p.getPlayerNum());
            players.sendIntToAll(p.getCallRank().rankNum);
        });

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
        int numFriendCards = Math.max(((players.size() / 2) - 1), 0);

        if(numFriendCards > 0) {
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
        }

        return friendCards;
    }

    // EVERYTHING BELOW THIS LINE IS AWFUL
    // DEAR GOD I'M SO SORRY

    private final Object lock = new Object();
    volatile private int highestNumCallCards = 0; // This variable is the highest number of cards used to call (single, double, or triple)
    volatile private Player caller;
    volatile private int numNoCallPlayers = 0;

    // TODO: Make sure synchronization in this method is correct
    // TODO: Try going single-threaded and continuously loop over every player and check if they're ready to read?
    private Player establishCaller(ServerCardList kitty) {
        ArrayList<Thread> threads = new ArrayList<>();

        // Creates a thread for each player listening for their call.
        for(Player p : players) {
            Runnable r = () -> {
                try {
                    p.sendInt(ServerCodes.SEND_CALL);
                    int callCardNum;
                    int numCardsInCall;
                    while(true) {
                        Thread.sleep(100);
                        if(numNoCallPlayers == players.size() - 1 && caller != null) {
                            return;
                        }
                        if(p.readyToRead()) { // As a way to avoid the blocking call to readLine(),
                            try {                   // check first if the bufferedReader has input available
                                callCardNum = p.readInt();
                                if(callCardNum == ClientCodes.NO_CALL) {
                                    p.sendInt(ServerCodes.NO_CALL);
                                    synchronized(lock) {
                                        numNoCallPlayers++;
                                    }
                                    return;
                                } else if(callCardNum < 0) {
                                    p.sendInt(ServerCodes.SEND_CALL);
                                    continue;
                                }
                                numCardsInCall = p.readInt();
                                if(numCardsInCall < 0) {
                                    p.sendInt(ServerCodes.SEND_CALL);
                                    continue;
                                }
                            } catch(NullPointerException | NumberFormatException e) {
                                p.sendInt(ServerCodes.SEND_CALL);
                                continue;
                            }

                            ServerCard callCard = new ServerCard(callCardNum);
                            boolean isValidCall = p.isValidCall(callCard, numCardsInCall);
                            if(isValidCall && numCardsInCall > highestNumCallCards) {
                                synchronized(lock) {
                                    highestNumCallCards = numCardsInCall;

                                    caller = p;
                                    caller.sendInt(ServerCodes.SUCCESSFUL_CALL);
                                    caller.sendInt(callCardNum);
                                    caller.sendInt(numCardsInCall);

                                    trumpSuit = callCard.suit();
                                    trumpRank = callCard.rank();

                                    for(Player p1 : players) {
                                        if(p1 != caller) {
                                            p1.sendInt(ServerCodes.WAIT_FOR_NEW_WINNING_CALL);
                                            p1.sendString(caller.getPlayerNum() + "\n" + callCardNum + "\n" + numCardsInCall);
                                        }
                                    }
                                }
                            } else if(isValidCall) {
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

        // If caller == null then no one called, and we have to pull a card from the kitty and do all that shit
        if(caller == null) {
            ServerCard kittyCard = kitty.get(0);

            players.sendIntToAll(ServerCodes.NO_ONE_CALLED);
            for(ServerCard c : kitty) {
                if(!c.isJoker()) {
                    kittyCard = c;
                    break;
                }
            }
            players.sendIntToAll(kittyCard.cardNum());

            kittyCallLoop:
            while(true) {
                for(Player p : players) {
                    if(p.readyToRead()) {
                        int callCardNum = p.readInt();
                        ServerCard callCard = new ServerCard(callCardNum);

                        boolean isValidCall = callCard.suit() == kittyCard.suit() && p.getHand().contains(callCard);
                        if(isValidCall) {
                            p.sendInt(ServerCodes.SUCCESSFUL_KITTY_CALL);

                            caller = p;

                            trumpSuit = callCard.suit();
                            trumpRank = caller.getCallRank();

                            players.forEach((p1) -> {
                                if(p1 != caller) {
                                    p1.sendInt(ServerCodes.WAIT_FOR_KITTY_CALL_WINNER);
                                    p1.sendInt(p.getPlayerNum());
                                    p1.sendInt(p.getCallRank().rankNum);
                                    p1.sendInt(callCardNum);
                                }
                            });

                            break kittyCallLoop;
                        } else {
                            p.sendInt(ServerCodes.INVALID_KITTY_CALL);
                        }
                    }
                }
            }
        } else {
            players.sendIntToAll(ServerCodes.WAIT_FOR_CALL_WINNER);
            players.sendIntToAll(caller.getPlayerNum());
        }

        caller.setTeam(Team.KEEPERS);
        players.sendIntToAll(ServerCodes.WAIT_FOR_NEW_PLAYER_TEAM);
        players.sendIntToAll(caller.getPlayerNum());
        players.sendIntToAll(caller.getTeam().getTeamNum());

        return caller;
    }
}
