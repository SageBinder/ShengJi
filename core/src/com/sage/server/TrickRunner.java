package com.sage.server;

import com.sage.Rank;
import com.sage.Team;

import java.util.ArrayList;

class TrickRunner {
    private PlayerList players = new PlayerList();
    private ServerCardList friendCards = new ServerCardList();

    TrickRunner(PlayerList players, ServerCardList friendCards) {
        this.players.addAll(players);
        this.friendCards.addAll(friendCards);
    }
    
    TrickResult startNewTrick(Player startingPlayer) {
        Player turnPlayer = startingPlayer;
        ArrayList<Play> plays = new ArrayList<>();
        Play basePlay = null;
        Play winningPlay;

        players.sendIntToAll(ServerCodes.TRICK_START);

        int numCardsInPlay = 0;
        int playOrderIndex = 0;
        // Outer loop iterates through all the players and gets their play
        do {
            players.sendIntToAll(ServerCodes.WAIT_FOR_TURN_PLAYER);
            players.sendIntToAll(turnPlayer.getPlayerNum());

            // Inner loop repeats if player submitted an invalid play, and breaks once player submits valid play
            while(true) {
                Play turnPlay;
                ServerCardList cardsInPlay = new ServerCardList();

                if(playOrderIndex == 0) {
                    turnPlayer.sendInt(ServerCodes.SEND_BASE_PLAY);
                    numCardsInPlay = turnPlayer.readInt();
                } else {
                    turnPlayer.sendInt(ServerCodes.SEND_PLAY);
                    turnPlayer.sendInt(numCardsInPlay);
                }

                for(int i = 0; i < numCardsInPlay; i++) {
                    cardsInPlay.add(new ServerCard(turnPlayer.readInt()));
                }

                turnPlay = new Play(playOrderIndex, cardsInPlay, turnPlayer, basePlay);
                if(turnPlay.isLegal()) {
                    plays.add(turnPlay);
                    winningPlay = getWinningPlay(plays);
                    if(playOrderIndex == 0) {
                        basePlay = turnPlay;
                    }

                    // Send the play that turnPlayer made to all other players
                    for(Player p : players) {
                        if(p != turnPlayer) {
                            p.sendInt(ServerCodes.WAIT_FOR_PLAY);
                            p.sendInt(turnPlay.size());
                            p.sendCards(turnPlay);
                        }
                    }

                    // Check if any teams were established during that play, and send to all players
                    if(turnPlayer.getTeam() != Team.COLLECTORS) {
                        if(turnPlay.containsAny(friendCards) && turnPlayer.getTeam() == Team.NO_TEAM) {
                            turnPlayer.setTeam(Team.KEEPERS);
                            turnPlayer.clearPoints(); // Once a player becomes a keeper, their points are discarded

                            players.sendIntToAll(ServerCodes.WAIT_FOR_NEW_PLAYER_TEAM);
                            players.sendIntToAll(turnPlayer.getPlayerNum());
                            players.sendIntToAll(turnPlayer.getTeam().getTeamNum());
                        }

                        for(ServerCard c : turnPlay) {
                            if(friendCards.remove(c)) {
                                players.sendIntToAll(ServerCodes.WAIT_FOR_INVALIDATED_FRIEND_CARD);
                                players.sendIntToAll(c.cardNum());
                            }
                            if(friendCards.isEmpty()) {
                                for(Player p : players) {
                                    if(p.getTeam() == Team.NO_TEAM) {
                                        p.setTeam(Team.COLLECTORS);
                                        players.sendIntToAll(ServerCodes.WAIT_FOR_NEW_PLAYER_TEAM);
                                        players.sendIntToAll(p.getPlayerNum());
                                        players.sendIntToAll(p.getTeam().getTeamNum());
                                    }
                                }
                                break;
                            }
                        }
                    }

                    players.sendIntToAll(ServerCodes.WAIT_FOR_PLAYER_IN_LEAD);
                    players.sendIntToAll(winningPlay.getPlayer().getPlayerNum());

                    turnPlayer.removeFromHand(turnPlay);
                    break;
                } else {
                    turnPlayer.sendInt(ServerCodes.INVALID_PLAY);
                }
            }

            turnPlayer = getNextPlayer(turnPlayer);
            playOrderIndex++;
        } while(turnPlayer != startingPlayer);

        return new TrickResult(getTrickWinner(plays), getPointCardsInPlays(plays), winningPlay);
    }

    private Player getTrickWinner(ArrayList<Play> plays) {
        return getWinningPlay(plays).getPlayer();
    }

    private Play getWinningPlay(ArrayList<Play> plays) {
        Play currentWinningPlay = plays.get(0);

        for(Play p : plays) {
            int thisHierarchicalNum = p.getPlayHierarchicalNum();
            int winnerHierarchicalNum = currentWinningPlay.getPlayHierarchicalNum();

            if(thisHierarchicalNum > winnerHierarchicalNum) {
                currentWinningPlay = p;
            } else if(thisHierarchicalNum == winnerHierarchicalNum) {
                if(p.getPlayOrder() < currentWinningPlay.getPlayOrder()) {
                    currentWinningPlay = p; // If player p played first and both plays are equal, player p is the winner
                }
            }
        }

        return currentWinningPlay;
    }

    private ServerCardList getPointCardsInPlays(ArrayList<Play> plays) {
        ServerCardList pointCards = new ServerCardList();

        for(Play play : plays) {
            for(ServerCard c : play) {
                if(c.rank() == Rank.KING || c.rank() == Rank.TEN || c.rank() == Rank.FIVE) {
                    pointCards.add(c);
                }
            }
        }

        return pointCards;
    }

    private Player getNextPlayer(Player p) {
        int pIndex = players.indexOf(p);
        return pIndex == players.size() - 1 ? players.get(0) : players.get(pIndex + 1);
    }
}
