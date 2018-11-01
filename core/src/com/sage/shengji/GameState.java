package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.Card;
import com.sage.Rank;
import com.sage.Suit;
import com.sage.Team;
import com.sage.server.ServerCodes;

import static com.sage.server.ServerCodes.*;

class GameState implements InputProcessor {
    private ScreenManager game;

    private GameStateUpdater updater = new GameStateUpdater();

    static Suit trumpSuit = null;
    static Rank trumpRank = null;

    PlayerList players = new PlayerList();
    int[] playerOrder;

    RenderableCard noOneCalledCard;

    PlayerList roundWinners = new PlayerList();

    Player turnPlayer;
    Player lastTrickWinner;
    Player leadingPlayer;

    Player thisPlayer;
    final RenderableHand hand = new RenderableHand();
    boolean thisPlayerIsHost = false;

    final RenderableCardList collectedPointCards = new RenderableCardList();
    int numCollectedPoints = 0;

    final RenderableCardList kitty = new RenderableCardList();

    final RenderableCardList friendCards = new RenderableCardList();

    String message = "";
    String buttonText = "";

    ChangeListener buttonAction = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
        }
    };
    boolean buttonIsEnabled = false;

    private Viewport viewport;

    int lastServerCode = ServerCodes.ROUND_OVER;

    GameState(ScreenManager game) {
        this.game = game;

//        for(int i = 0; i < 10; i++) {
//            hand.add(new RenderableCard());
//        }
    }

    void setViewport(Viewport viewport) {
        this.viewport = viewport;
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

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 clickPos = viewport.unproject(new Vector2(screenX, screenY));
        hand.click(clickPos, button);

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    private class GameStateUpdater {
        private ShengJiClient client;

        void update(int serverCode, ShengJiClient client) throws NullPointerException {
            this.client = client;
            lastServerCode = serverCode;

            if(!client.readyToRead()) {
                Gdx.app.log("Shengji.GameStateUpdater.update", "client read not ready. This usually shouldn't happen. " +
                        "Last server code: " + serverCode);
            }

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
                    waitForCollectedPoints();
                    break;
                case WAIT_FOR_CALLING_NUMBERS:
                    waitForCallingNumbers();
                    break;
                case ROUND_OVER:
                    roundOver();
                    break;

                // Lobby codes:
                case WAIT_FOR_PLAYERS_LIST:
                    waitForPlayersList();
                    break;
                default:
                    Gdx.app.log("Shengji.GameState.Updater.update",
                            "GameState update switch went to default. This really really really shouldn't happen");
                    break;
            }
        }

        // Server should send:
        // [thisPlayerNum]\n
        // [# of players]\n
        // [[[playerNum]\n[name]\n[callRank]\n] for each player]
        private void waitForPlayersList() {
            // Server will send this player's playerNum first
            int thisPlayerNum = client.readInt();

            // Server will send the number of players
            int numPlayers = client.readInt();

            PlayerList players = new PlayerList();

            for(int i = 0; i < numPlayers; i++) {
                // Server should send [[playerNum]\n[name]\n[callRank]]
                int playerNum = client.readInt();
                String name = client.readLine();
                int callRank = client.readInt();
                players.add(new Player(playerNum, name, Rank.fromInt(callRank)));
            }

            int hostPlayerNum = client.readInt();
            Player host = players.getPlayerFromPlayerNum(hostPlayerNum);

            players.forEach(p -> p.setHost(false));
            host.setHost(true);

            GameState.this.players = players;
            thisPlayer = players.getPlayerFromPlayerNum(thisPlayerNum);

            thisPlayerIsHost = thisPlayer == host;

            disableButton();
        }

        // CALLING CODES:

        private void invalidCall() {
            message = "Invalid call, try again";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            buttonText = "Send call";
            enableButton();
        }

        private void noCall() {
            message = "You have chosen not to call";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            disableButton();
        }

        private void unsuccessfulCall() {
            message = "Call not strong enough";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            buttonText = "Send call";
            enableButton();
        }

        private void successfulCall() {
            message = "Successful call";
            
            players.forEach(player -> {
                if(player != thisPlayer) {
                    player.clearPlay();
                }
            });

            enableButton();
        }

        private void sendCall() {
            message = "Make your call";
            
            buttonText = "Send call";

            enableButton();
        }

        private void waitForNewWinningCall() {
            int callLeaderPlayerNum = client.readInt();
            int callCardNum = client.readInt();
            int numCallCards = client.readInt();

            Player callLeader = players.getPlayerFromPlayerNum(callLeaderPlayerNum);

            setTrump(callCardNum);

            hand.addAll(thisPlayer.getPlay());
            players.forEach(Player::clearPlay);

            for(int i = 0; i < numCallCards; i++) {
                callLeader.addToPlay(new RenderableCard(callCardNum));
            }

            sendCall();
        }

        private void waitForCallWinner() {
            hand.addAll(thisPlayer.getPlay());
            players.forEach(Player::clearPlay);

            Player callWinner = players.getPlayerFromPlayerNum(client.readInt());

            message = "Call winner: " + callWinner.getName();

            disableButton();
        }

        private void noOneCalled() {
            noOneCalledCard = new RenderableCard(client.readInt());

            
            message = "No one called";

            enableButton();
        }

        private void waitForKittyCallWinner() {
            Player callWinner = players.getPlayerFromPlayerNum(client.readInt());
            Rank callWinnerCallRank = Rank.fromInt(client.readInt());
            RenderableCard callCard = new RenderableCard(client.readInt());

            message = "Call winner: " + callWinner.getName();

            callWinner.getPlay().clear();
            callWinner.getPlay().add(callCard);

            noOneCalledCard = null;

            setTrump(Card.getCardNumFromRankAndSuit(callWinnerCallRank, callCard.suit()));
            
            disableButton();
        }

        private void successfulKittyCall() {
            
            message = "Successful call!";

            setTrump(Card.getCardNumFromRankAndSuit(thisPlayer.getCallRank(), noOneCalledCard.suit()));

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.getPlay().clear();

            noOneCalledCard = null;

            disableButton();
        }

        private void invalidKittyCall() {
            
            message = "Invalid call. Try again.";

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.getPlay().clear();

            enableButton();
        }

        // GAME SETUP CODES:

        private void roundStart() {
            noOneCalledCard = null;

            playerOrder = null;

            roundWinners.clear();
            kitty.clear();
            collectedPointCards.clear();
            hand.clear();
            friendCards.clear();

            numCollectedPoints = 0;

            turnPlayer = null;
            lastTrickWinner = null;
            leadingPlayer = null;

            message = "";
            buttonText = "";

            resetTrump();

            disableButton();
            game.showGameScreen(GameState.this);
        }

        // Server should send:
        // [# of players]\n
        // [[playerNum]\n for each player]
        private void waitForPlayerOrder() {
            int numPlayers = client.readInt();
            playerOrder = new int[numPlayers];
            for(int i = 0; i < playerOrder.length; i++) {
                playerOrder[i] = client.readInt();
            }

            disableButton();
        }

        // Server should send:
        // [kitty size]\n
        // [[cardNum]\n for each card in kitty]
        private void waitForKitty() {
            int kittySize = client.readInt();
            for(int i = 0; i < kittySize; i++) {
                kitty.add(new RenderableCard(client.readInt()));
            }
            hand.addAll(kitty);

            disableButton();
        }

        // Server should send:
        // [cardNum]\n for each card in this player's hand
        private void waitForHand() {
            int[] cardNums = new int[client.readInt()];

            for(int i = 0; i < cardNums.length; i++) {
                cardNums[i] = client.readInt();
            }

            for(int cardNum : cardNums) {
                hand.add(new RenderableCard(cardNum));
            }

            disableButton();
        }

        private void sendKitty() {
            message = "Select cards to put in kitty";
            
            buttonText = "Confirm kitty";

            enableButton();
        }

        private void invalidKitty() {
            message = "Invalid kitty!";
            
            buttonText = "Confirm kitty";

            enableButton();
        }

        private void sendFriendCards() {
            int numFriendCards = client.readInt();

            message = "Select " + numFriendCards + " friend cards";

            buttonText = "Send friend cards";

            enableButton();
        }

        private void waitForFriendCards() {
            int numFriendCards = client.readInt();
            for(int i = 0; i < numFriendCards; i++) {
                friendCards.add(new RenderableCard(client.readInt()));
            }

            disableButton();
        }

        private void invalidFriendCards() {
            // I don't think this will need to be implemented but I don't want to delete it
        }

        // GAME CODES:

        private void trickStart() {
            players.forEach(Player::clearPlay);

            disableButton();
        }


        // Should send to server:
        // [numCardsInPlay]\n
        // [[cardNum] for each selected card in hand]
        private void sendBasePlay() {
            message = "Your turn";
            
            buttonText = "Send play";

            enableButton();
        }

        // Should send to server:
        // [[cardNum] for each selected card in hand]
        private void sendPlay() {
            message = "Your turn";
            
            buttonText = "Send play";

            enableButton();
        }

        private void waitForTurnPlayer() {
            turnPlayer = players.getPlayerFromPlayerNum(client.readInt());

            message = "Waiting on " + turnPlayer.getName();

            disableButton();
        }

        private void waitForPlay() {
            int numCardsInPlay = client.readInt();

            turnPlayer.clearPlay();
            for(int i = 0; i < numCardsInPlay; i++) {
                turnPlayer.addToPlay(new RenderableCard(client.readInt()));
            }

            message = "";

            disableButton();
        }

        private void invalidPlay() {
            message = "Invalid play";
            
            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();
        }

        private void waitForNewPlayerTeam() {
            Player p = players.getPlayerFromPlayerNum(client.readInt());
            int teamNum = client.readInt();
            Team newTeam = Team.getTeamFromTeamNum(teamNum);

            p.setTeam(newTeam);

            // I don't think newTeam will ever be NO_TEAM but I'm not sure
            if(newTeam == Team.COLLECTORS) {
                collectedPointCards.addAll(p.getPoints());
                p.clearPoints();
            } else if(newTeam == Team.KEEPERS) {
                p.clearPoints();
            }
        }

        private void waitForPlayerInLead() {
            leadingPlayer = players.getPlayerFromPlayerNum(client.readInt());
        }

        private void waitForTrickWinner() {
            lastTrickWinner = players.getPlayerFromPlayerNum(client.readInt());

            String teamString = lastTrickWinner.getTeam() == Team.COLLECTORS ? "(collector)"
                    : lastTrickWinner.getTeam() == Team.KEEPERS ? "(keeper)"
                    : "(no team)";
            message = lastTrickWinner.getName() + " " + teamString + " won the trick!";

            players.forEach(Player::clearPlay);

            disableButton();
        }

        private void waitForTrickPointCards() {
            int numCards = client.readInt();
            RenderableCardList pointCards = new RenderableCardList();

            for(int i = 0; i < numCards; i++) {
                pointCards.add(new RenderableCard(client.readInt()));
            }

            if(lastTrickWinner.getTeam() == Team.NO_TEAM) {
                lastTrickWinner.addToPoints(pointCards);
            } else if(lastTrickWinner.getTeam() == Team.COLLECTORS) {
                collectedPointCards.addAll(pointCards);
            }

            disableButton();
        }

        private void waitForRoundWinners() {
            int numWinners = client.readInt();

            roundWinners.clear();
            for(int i = 0; i < numWinners; i++) {
                roundWinners.add(players.getPlayerFromPlayerNum(client.readInt()));
            }

            message = "Winners: ";
            roundWinners.forEach(p -> message += roundWinners.lastIndexOf(p) == roundWinners.size() - 1
                    ? p.getName() + ", "
                    : p.getName());

            disableButton();
        }

        private void waitForCollectedPoints() {
            numCollectedPoints = client.readInt();

            disableButton();
        }

        private void waitForCallingNumbers() {
            int numPlayers = client.readInt();

            for(int i = 0; i < numPlayers; i++) {
                int playerNum = client.readInt();
                int callRank = client.readInt();
                players.getPlayerFromPlayerNum(playerNum).setCallRank(callRank);
            }

            disableButton();
        }

        private void roundOver() {
            game.showLobbyScreen(GameState.this);
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

            Gdx.app.log("ShengJi.GameState.Updater.setTrump()", "Trump rank = " + trumpRank.toString() + ", trump suit = " + trumpSuit.toString());
        }

        private void resetTrump() {
            trumpRank = null;
            trumpSuit = null;
        }
    }
}