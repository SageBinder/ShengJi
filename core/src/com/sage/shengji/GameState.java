package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;
import com.sage.Team;
import com.sage.server.PlayerDisconnectedException;
import com.sage.server.ServerCodes;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sage.server.ServerCodes.*;

class GameState {
    private ScreenManager game;

    private GameStateUpdater updater = new GameStateUpdater();

    // Light Goldenrod
    static final Color winningPlayColor = new Color(238f / 255f, 221f / 255f, 130f / 255f, 1f);

    // Oof I dunno how I ended up with static variables for trumpSuit and trumpRank but I guess we're doing that now
    static Suit trumpSuit = null;
    static Rank trumpRank = null;
    RenderableCard trumpCard = null;

    RenderablePlayerList players = new RenderablePlayerList();

    RenderableCard noOneCalledCard;

    PlayerList<Player> roundWinners = new PlayerList<>();

    // These are all RenderablePlayer instead of just Player because I want to change render properties of them
    RenderablePlayer turnPlayer;
    RenderablePlayer lastTrickWinner;
    RenderablePlayer leadingPlayer;

    RenderablePlayer thisPlayer;
    final RenderableHand hand = new RenderableHand();
    final RenderableCardList thisPlayerCurrentCall = new RenderableCardList();

    int numCardsInBasePlay = 0;

    final RenderableCardGroup collectedPointCards = new RenderableCardGroup();
    int numCollectedPoints = 0;
    int numPointsNeeded = 0;

    final RenderableCardGroup kitty = new RenderableCardGroup();

    final RenderableCardGroup friendCards = new RenderableCardGroup();

    String message = "";
    String buttonText = "";

    boolean buttonIsEnabled = false;

    int lastServerCode = ServerCodes.ROUND_OVER;

    boolean roundStarted = false;

    GameState(ScreenManager game) {
        this.game = game;
    }

    boolean update(ShengJiClient client) {
        Integer serverCode = client.consumeServerCode();
        if(serverCode != null) {
            Gdx.app.log("Shengji.GameState.update()", "Updating with server code " + serverCode);
            updater.update(serverCode, client);
            client.waitForServerCode();
            return true;
        }

        return false;
    }

    private class GameStateUpdater {
        private ShengJiClient client;

        void update(int serverCode, ShengJiClient client) throws NullPointerException {
            this.client = client;
            lastServerCode = serverCode;

            try {
                switch(serverCode) {
                    // Calling codes:
                    case INVALID_CALL:
                        invalidCall();
                        break;
                    case NO_CALL:
                        noCall();
                        break;
                    case UNSUCCESSFUL_CALL:
                        unsuccessfulCall();
                        break;
                    case SUCCESSFUL_CALL:
                        successfulCall();
                        break;
                    case SEND_CALL:
                        sendCall();
                        break;
                    case WAIT_FOR_NEW_WINNING_CALL:
                        waitForNewWinningCall();
                        break;
                    case WAIT_FOR_CALL_WINNER:
                        waitForCallWinner();
                        break;
                    case NO_ONE_CALLED:
                        noOneCalled();
                        break;
                    case WAIT_FOR_KITTY_CALL_WINNER:
                        waitForKittyCallWinner();
                        break;
                    case SUCCESSFUL_KITTY_CALL:
                        successfulKittyCall();
                        break;
                    case INVALID_KITTY_CALL:
                        invalidKittyCall();
                        break;

                    // Game setup codes:
                    case ROUND_START:
                        roundStart();
                        break;
                    case WAIT_FOR_PLAYER_ORDER:
                        waitForPlayerOrder();
                        break;
                    case WAIT_FOR_KITTY:
                        waitForKitty();
                        break;
                    case WAIT_FOR_HAND:
                        waitForHand();
                        break;
                    case SEND_KITTY:
                        sendKitty();
                        break;
                    case INVALID_KITTY:
                        invalidKitty();
                        break;
                    case SEND_FRIEND_CARDS:
                        sendFriendCards();
                        break;
                    case WAIT_FOR_FRIEND_CARDS:
                        waitForFriendCards();
                        break;
                    case INVALID_FRIEND_CARDS:
                        invalidFriendCards();
                        break;
                    case WAIT_FOR_NUM_POINTS_NEEDED:
                        waitForNumPointsNeeded();
                        break;

                    // Game codes:
                    case TRICK_START:
                        trickStart();
                        break;
                    case SEND_BASE_PLAY:
                        sendBasePlay();
                        break;
                    case SEND_PLAY:
                        sendPlay();
                        break;
                    case WAIT_FOR_TURN_PLAYER:
                        waitForTurnPlayer();
                        break;
                    case WAIT_FOR_PLAY:
                        waitForPlay();
                        break;
                    case INVALID_PLAY:
                        invalidPlay();
                        break;
                    case WAIT_FOR_INVALIDATED_FRIEND_CARD:
                        waitForInvalidatedFriendCard();
                        break;
                    case WAIT_FOR_NEW_PLAYER_TEAM:
                        waitForNewPlayerTeam();
                        break;
                    case WAIT_FOR_PLAYER_IN_LEAD:
                        waitForPlayerInLead();
                        break;
                    case WAIT_FOR_TRICK_WINNER:
                        waitForTrickWinner();
                        break;
                    case WAIT_FOR_TRICK_POINT_CARDS:
                        waitForTrickPointCards();
                        break;
                    case WAIT_FOR_ROUND_WINNERS:
                        waitForRoundWinners();
                        break;
                    case WAIT_FOR_NUM_COLLECTED_POINTS:
                        waitForNumCollectedPoints();
                        break;
                    case WAIT_FOR_CALLING_NUMBERS:
                        waitForCallingNumbers();
                        break;
                    case WAIT_FOR_ROUND_END_KITTY:
                        waitForRoundEndKitty();
                        break;
                    case ROUND_OVER:
                        roundOver();
                        break;

                    // Lobby codes:
                    case WAIT_FOR_PLAYERS_LIST:
                        waitForPlayersList();
                        break;
                    case ServerCodes.WAIT_FOR_NEW_CALLING_RANK:
                        waitForNewCallingRank();
                        break;
                    case PLAYER_DISCONNECTED_DURING_ROUND:
                        playerDisconnectedDuringRound();
                        break;
                    default:
                        Gdx.app.log("Shengji.GameState.Updater.update",
                                "GameState update switch went to default. This really really really shouldn't happen");
                        break;
                }
            } catch(PlayerDisconnectedException e) {
                Gdx.app.log("GameState.Updater.update()",
                        "Switch threw PlayerDisconnectedException," +
                                "went to catch, calling playerDisconnectedDuringRound");
                playerDisconnectedDuringRound();
            }
        }

        // Server should send:
        // [# of players]\n
        // [[[playerNum]\n[name]\n[callRank]\n] for each RenderablePlayer]
        // [hostPlayerNum]\n
        // [thisPlayerNum]\n
        private void waitForPlayersList() {
            // Server will send this RenderablePlayer's playerNum first

            // Server will send the number of players
            int numPlayers = client.readInt();

            RenderablePlayerList players = new RenderablePlayerList();

            for(int i = 0; i < numPlayers; i++) {
                // Server should send [[playerNum]\n[name]\n[callRank]]
                int playerNum = client.readInt();
                String name = client.readLine();
                int callRank = client.readInt();
                try {
                    players.add(new RenderablePlayer(playerNum, name, Rank.fromInt(callRank)));
                } catch(IllegalArgumentException e) {
                    e.printStackTrace(); // Print stack trace cause this shouldn't happen
                    players.add(new RenderablePlayer(playerNum, name, Rank.values()[0]));
                }
            }

            int hostPlayerNum = client.readInt();
            RenderablePlayer host = players.getPlayerFromPlayerNum(hostPlayerNum);

            players.forEach(p -> {
                p.setHost(false);
                p.setThisPlayer(false);
            });
            host.setHost(true);

            GameState.this.players = players;
            int thisPlayerNum = client.readInt();
            thisPlayer = players.getPlayerFromPlayerNum(thisPlayerNum);
            thisPlayer.setHost(thisPlayer == host);
            thisPlayer.setThisPlayer(true);

            // Don't disable button so that it stays enabled if a player disconnects during ROUND_OVER phase
//            disableButton();
        }

        private void waitForNewCallingRank() throws PlayerDisconnectedException {
            int playerNum = client.readInt();
            int callRank = client.readInt();
            players.getPlayerFromPlayerNum(playerNum).setCallRank(callRank);
        }

        private void playerDisconnectedDuringRound() throws PlayerDisconnectedException {
            message = "[#FF3333]A player disconnected during the round!";
            roundOver();
            game.showLobbyScreen(GameState.this);
            Gdx.app.log("GameState.Updater.playerDisconnectedDuringRound",
                    "detected player disconnect, calling roundOver() and showing lobby screen");
        }

        // CALLING CODES:

        private void invalidCall() throws PlayerDisconnectedException {
            message = "Invalid call, try again";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            buttonText = "Send call";
            enableButton();
        }

        private void noCall() throws PlayerDisconnectedException {
            message = "You have chosen not to call";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            disableButton();
        }

        private void unsuccessfulCall() throws PlayerDisconnectedException {
            message = "Call not strong enough";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();
            thisPlayer.getPlay().addAll(thisPlayerCurrentCall);

            buttonText = "Send call";
            enableButton();
        }

        private void successfulCall() throws PlayerDisconnectedException {
            message = "Successful call";

            int callCardNum = client.readInt();
            int numCardsInCall = client.readInt();

            hand.addAll(thisPlayer.getPlay());
            Collection<RenderableCard> call = hand.stream()
                    .filter(c -> c.cardNum() == callCardNum)
                    .limit(numCardsInCall)
                    .collect(Collectors.toList());

            hand.removeAll(call);
            thisPlayer.getPlay().clear();
            thisPlayer.getPlay().addAll(call);
            thisPlayerCurrentCall.clear();
            thisPlayerCurrentCall.addAll(thisPlayer.getPlay());

            players.forEach(p -> {
                if(p != thisPlayer) {
                    p.clearPlay();
                }
            });

            enableButton();
        }

        private void sendCall() throws PlayerDisconnectedException {
            message = "Make your call";
            buttonText = "Send call";

            players.forEach(p -> {
                p.getPoints().clear();
                p.getPoints().add(new RenderableCard(p.getCallRank(), Suit.SPADES));
            });

            enableButton();
        }

        private void waitForNewWinningCall() throws PlayerDisconnectedException {
            int callLeaderPlayerNum = client.readInt();
            int callCardNum = client.readInt();
            int numCallCards = client.readInt();

            RenderablePlayer callLeader = players.getPlayerFromPlayerNum(callLeaderPlayerNum);

            hand.addAll(thisPlayer.getPlay());
            players.forEach(RenderablePlayer::clearPlay);

            for(int i = 0; i < numCallCards; i++) {
                callLeader.addToPlay(new RenderableCard(callCardNum));
            }

            sendCall();
        }

        private void waitForCallWinner() throws PlayerDisconnectedException {
            RenderablePlayer callWinner = players.getPlayerFromPlayerNum(client.readInt());
            setTrump(callWinner.getPlay().get(0).cardNum());
            callWinner.setTeam(Team.KEEPERS);

            hand.addAll(thisPlayer.getPlay());
            players.forEach(RenderablePlayer::clearPlay);
            players.forEach(p -> p.getPoints().clear());

            message = "Call winner: [GREEN]" + callWinner.getName(17);

            disableButton();
        }

        private void noOneCalled() throws PlayerDisconnectedException {
            noOneCalledCard = new RenderableCard(client.readInt());
            message = "No one called";
            enableButton();
        }

        private void waitForKittyCallWinner() throws PlayerDisconnectedException {
            RenderablePlayer callWinner = players.getPlayerFromPlayerNum(client.readInt());
            Rank callWinnerCallRank = Rank.fromInt(client.readInt());
            RenderableCard callCard = new RenderableCard(client.readInt());

            message = "Call winner: [GREEN]" + callWinner.getName(17);

            callWinner.setTeam(Team.KEEPERS);
            callWinner.clearPlay();
            callWinner.getPlay().add(callCard);

            noOneCalledCard = null;
            players.forEach(p -> p.getPoints().clear());

            setTrump(Card.getCardNumFromRankAndSuit(callWinnerCallRank, callCard.suit()));
            
            disableButton();
        }

        private void successfulKittyCall() throws PlayerDisconnectedException {
            message = "Successful call!";

            setTrump(Card.getCardNumFromRankAndSuit(thisPlayer.getCallRank(), noOneCalledCard.suit()));

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            thisPlayer.setTeam(Team.KEEPERS);

            noOneCalledCard = null;

            disableButton();
        }

        private void invalidKittyCall() throws PlayerDisconnectedException {
            message = "Invalid call. Try again.";

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            enableButton();
        }

        // GAME SETUP CODES:

        private void roundStart() throws PlayerDisconnectedException {
            noOneCalledCard = null;
            resetTrump();

            roundWinners.clear();
            kitty.clear();
            collectedPointCards.clear();
            hand.clear();
            friendCards.clear();
            thisPlayerCurrentCall.clear();
            players.forEach(p -> {
                p.getPoints().clear();
                p.clearPlay();
            });

            numCardsInBasePlay = 0;
            numCollectedPoints = 0;

            turnPlayer = null;
            lastTrickWinner = null;
            leadingPlayer = null;

            message = "";
            buttonText = "";

            disableButton();

            roundStarted = true;
            game.showGameScreen(GameState.this);
        }

        // Server should send:
        // [# of players]\n
        // [[playerNum]\n for each RenderablePlayer]
        private void waitForPlayerOrder() throws PlayerDisconnectedException {
            int numPlayers = client.readInt();
            int[] playerOrder = new int[numPlayers];
            for(int i = 0; i < playerOrder.length; i++) {
                playerOrder[i] = client.readInt();
            }

            // Oof lazy way of changing order of players
            RenderablePlayerList temp = new RenderablePlayerList();
            for(int i : playerOrder) {
                temp.add(players.get(i));
            }
            players.clear();
            players.addAll(temp);

            disableButton();
        }

        // Server should send:
        // [kitty size]\n
        // [[cardNum]\n for each card in kitty]
        private void waitForKitty() throws PlayerDisconnectedException {
            int kittySize = client.readInt();
            kitty.clear();
            for(int i = 0; i < kittySize; i++) {
                var kittyCardNum = client.readInt();
                kitty.add(new RenderableCard(kittyCardNum));
                hand.add(new RenderableCard(kittyCardNum));
            }

            disableButton();
        }

        // Server should send:
        // [cardNum]\n for each card in this RenderablePlayer's hand
        private void waitForHand() throws PlayerDisconnectedException {
            int[] cardNums = new int[client.readInt()];

            for(int i = 0; i < cardNums.length; i++) {
                cardNums[i] = client.readInt();
            }

            for(int cardNum : cardNums) {
                hand.add(new RenderableCard(cardNum));
            }

            disableButton();
        }

        private void sendKitty() throws PlayerDisconnectedException {
            message = "Select " + kitty.size() +  " cards to put in kitty";
            buttonText = "Confirm kitty";

            enableButton();
        }

        private void invalidKitty() throws PlayerDisconnectedException {
            message = "Invalid kitty!";
            buttonText = "Confirm kitty";

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();
            
            enableButton();
        }

        private void sendFriendCards() throws PlayerDisconnectedException {
            // First, clear the this player's play because it contains the selected kitty cards
            thisPlayer.clearPlay(); // UPDATE 2018-11-22 actually I don't think it does but I'm leaving it here
            
            int numFriendCards = client.readInt();
            message = "Select " + numFriendCards + " friend cards";
            buttonText = "Send friend cards";

            friendCards.prefDivisionProportion = 1.1f;
            for(int i = 0; i < numFriendCards; i++) {
                friendCards.add(
                        new RenderableCard(Rank.ACE, Suit.getSuitFromNum(i % 4))
                        .setFlippable(false)
                        .setSelectable(false));
            }

            enableButton();
        }

        private void waitForFriendCards() throws PlayerDisconnectedException {
            int numFriendCards = client.readInt();
            friendCards.clear();
            friendCards.prefDivisionProportion = 1.1f;
            for(int i = 0; i < numFriendCards; i++) {
                friendCards.add(new RenderableCard(client.readInt()));
            }

            disableButton();
        }

        private void invalidFriendCards() throws PlayerDisconnectedException {
            // I don't think this will need to be implemented but I don't want to delete it
        }

        private void waitForNumPointsNeeded() throws PlayerDisconnectedException {
            numPointsNeeded = client.readInt();
            disableButton();
        }

        // GAME CODES:

        private void trickStart() throws PlayerDisconnectedException {
            players.forEach(RenderablePlayer::clearPlay);
            numCardsInBasePlay = 0;
            disableButton();
        }


        // Should send to server:
        // [numCardsInPlay]\n
        // [[cardNum] for each selected card in hand]
        private void sendBasePlay() throws PlayerDisconnectedException {
            message = "Your turn";
            buttonText = "Send play";
            enableButton();
        }

        // Should send to server:
        // [[cardNum] for each selected card in hand]
        private void sendPlay() throws PlayerDisconnectedException {
            message = "Your turn";
            buttonText = "Send play";
            numCardsInBasePlay = client.readInt();
            enableButton();
        }

        private void waitForTurnPlayer() throws PlayerDisconnectedException {
            turnPlayer = players.getPlayerFromPlayerNum(client.readInt());
            message = "Waiting on "
                    + (turnPlayer.getTeam() == Team.COLLECTORS ? "[ORANGE]"
                    : turnPlayer.getTeam() == Team.KEEPERS ? "[GREEN]"
                    : "[WHITE]") + turnPlayer.getName(17);
            disableButton();
        }

        private void waitForPlay() throws PlayerDisconnectedException {
            int numCardsInPlay = client.readInt();

            turnPlayer.clearPlay();
            for(int i = 0; i < numCardsInPlay; i++) {
                turnPlayer.addToPlay(new RenderableCard(client.readInt()));
            }

            boolean sentPlayIsBasePlay = players.stream()
                    .filter(p -> p != turnPlayer)
                    .allMatch(p -> p.getPlay().isEmpty());
            if(sentPlayIsBasePlay) {
                turnPlayer.getPlay().forEach(c -> {
                    c.faceUnselectedBackgroundColor.set(Color.LIGHT_GRAY);
                    c.setFaceBackgroundColor(c.faceUnselectedBackgroundColor);
                });
            }

            message = "";
            disableButton();
        }

        private void invalidPlay() throws PlayerDisconnectedException {
            message = "Invalid play";

            thisPlayer.getPlay().forEach(c -> {
                c.faceUnselectedBackgroundColor.set(Color.WHITE);
                c.setFaceBackgroundColor(c.faceUnselectedBackgroundColor);
            });
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();
        }

        private void waitForInvalidatedFriendCard() throws PlayerDisconnectedException {
            int invalidatedFriendCard = client.readInt();
            Optional<RenderableCard> invalidatedCardOptional =
                    friendCards.stream().filter(c -> c.cardNum() == invalidatedFriendCard).limit(1).findFirst();
            invalidatedCardOptional.ifPresent(c -> {
                c.faceUnselectedBackgroundColor.set(c.faceSelectedBackgroundColor);
                c.setFaceBackgroundColor(c.faceUnselectedBackgroundColor);
                c.setFaceBorderColor(Color.RED);
            });
        }

        private void waitForNewPlayerTeam() throws PlayerDisconnectedException {
            RenderablePlayer p = players.getPlayerFromPlayerNum(client.readInt());
            int teamNum = client.readInt();
            Team newTeam = Team.getTeamFromTeamNum(teamNum);

            p.setTeam(newTeam);

            // I don't think newTeam will ever be NO_TEAM but I'm not sure
            if(newTeam == Team.COLLECTORS) {
                collectedPointCards.addAll(p.getPoints());
                numCollectedPoints = collectedPointCards.getTotalPoints();
                p.clearPoints();
            } else if(newTeam == Team.KEEPERS) {
                p.clearPoints();
            }
        }

        private void waitForPlayerInLead() throws PlayerDisconnectedException {
            leadingPlayer = players.getPlayerFromPlayerNum(client.readInt());

            players.forEach(p -> {
                if(p != leadingPlayer) {
                    p.getPlay().forEach(c -> c.setFaceBackgroundColor(c.faceUnselectedBackgroundColor));
                }
            });
            leadingPlayer.getPlay()
                    .forEach(c -> c.setFaceBackgroundColor(winningPlayColor));
        }

        private void waitForTrickWinner() throws PlayerDisconnectedException {
            lastTrickWinner = players.getPlayerFromPlayerNum(client.readInt());
            message =
                    (lastTrickWinner.getTeam() == Team.COLLECTORS ? "[ORANGE]"
                    : lastTrickWinner.getTeam() == Team.KEEPERS ? "[GREEN]"
                    : "[WHITE]") + lastTrickWinner.getName(17) + "[] won the trick!";

            // We don't want to clear plays here because we want the plays to be visible for a second after trick winner
            // is decided. Clear plays in trickStart() instead.
//            players.forEach(RenderablePlayer::clearPlay);

            disableButton();
        }

        private void waitForTrickPointCards() throws PlayerDisconnectedException {
            int numCards = client.readInt();
            RenderableCardList pointCards = new RenderableCardList();

            for(int i = 0; i < numCards; i++) {
                pointCards.add(new RenderableCard(client.readInt()));
            }

            if(lastTrickWinner.getTeam() == Team.NO_TEAM) {
                lastTrickWinner.addToPoints(pointCards);
            } else if(lastTrickWinner.getTeam() == Team.COLLECTORS) {
                collectedPointCards.addAll(pointCards);
                numCollectedPoints = collectedPointCards.getTotalPoints();
            }

            disableButton();
        }

        private void waitForRoundWinners() throws PlayerDisconnectedException {
            int numWinners = client.readInt();

            roundWinners.clear();
            for(int i = 0; i < numWinners; i++) {
                roundWinners.add(players.getPlayerFromPlayerNum(client.readInt()));
            }

            message = (roundWinners.get(0).getTeam() == Team.COLLECTORS ? "[ORANGE]" : "[GREEN]") + "Winners: \n";
            roundWinners.forEach(p -> message += roundWinners.lastIndexOf(p) != roundWinners.size() - 1
                    ? p.getName(17) + ", \n"
                    : p.getName(17));
            disableButton();
        }

        private void waitForNumCollectedPoints() throws PlayerDisconnectedException {
            numCollectedPoints = client.readInt();
            disableButton();
        }

        private void waitForCallingNumbers() throws PlayerDisconnectedException {
            int numPlayers = client.readInt();

            for(int i = 0; i < numPlayers; i++) {
                int playerNum = client.readInt();
                int callRank = client.readInt();
                players.getPlayerFromPlayerNum(playerNum).setCallRank(callRank);
            }
            disableButton();
        }

        private void waitForRoundEndKitty() throws PlayerDisconnectedException {
            int kittySize = client.readInt();
            kitty.clear();
            for(int i = 0; i < kittySize; i++) {
                kitty.add(new RenderableCard(client.readInt()));
            }
            disableButton();
        }

        private void roundOver() throws PlayerDisconnectedException {
            roundStarted = false;
            buttonText = "Return to lobby";
        }

        // Whether the button is enabled should be stored as boolean and the event listener should be stored as well,
        // then the renderer can render the button based on those values.
        private void enableButton() {
            buttonIsEnabled = true;
        }
        
        private void disableButton() {
            buttonIsEnabled = false;
        }

        private void setTrump(int cardNum) {
            trumpSuit = Card.getSuitFromCardNum(cardNum);
            trumpRank = Card.getRankFromCardNum(cardNum);
            trumpCard = new RenderableCard(trumpRank, trumpSuit);
            Gdx.app.log("ShengJi.GameState.Updater.setTrump()", "Trump rank = " + trumpRank.toString() + ", trump suit = " + trumpSuit.toString());
        }

        private void resetTrump() {
            trumpRank = null;
            trumpSuit = null;
            trumpCard = null;
        }
    }
}