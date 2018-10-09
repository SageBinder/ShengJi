package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.server.ServerCodes;

import static com.sage.server.ServerCodes.*;

class GameState implements InputProcessor {
    private ShengJiGame game;

    private GameStateUpdater updater = new GameStateUpdater();

    private PlayerList players = new PlayerList();
    private int[] playerOrder;

    private Player turnPlayer;

    private Player thisPlayer;
    private final RenderableHand hand = new RenderableHand();

    private final RenderableCardList collectedCards = new RenderableCardList();

    private final RenderableCardList kitty = new RenderableCardList();

    private final RenderableCardList friendCards = new RenderableCardList();

    private RenderableCard trumpCard;

    private BitmapFont messageFont;
    private String message = "";

    private SpriteBatch batch = new SpriteBatch();

    private Viewport viewport;

    private Stage stage;
    private TextButton sendButton;

    private int lastServerPrompt = ServerCodes.ROUND_OVER;

    // TODO: I don't think any of these methods still need to be synchronized but I'm not sure

    GameState(ShengJiGame game) {
        this.game = game;

        for(int i = 0; i < 10; i++) {
            hand.add(new RenderableCard());
        }

        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 15;

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = generator.generateFont(parameter);

        messageFont = generator.generateFont(parameter);

        generator.dispose();

        stage = new Stage();
        sendButton = new TextButton("Send", textButtonStyle);

        stage.addActor(sendButton);
    }

    synchronized void render(float delta) {
        stage.act(delta);
        stage.draw();
        messageFont.draw(batch, message, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);

        batch.setProjectionMatrix(viewport.getCamera().combined);


        if(lastServerPrompt == INVALID_KITTY || lastServerPrompt == SEND_KITTY) {
            kitty.render(batch);
        }

        hand.render(batch, viewport);
    }

    synchronized void setViewport(Viewport viewport) {
        this.viewport = viewport;
        stage.setViewport(viewport);
    }

    synchronized void update(ShengJiClient client) {
        int serverCode = client.consumeServerCode();
        if(serverCode != 0) {
            updater.update(serverCode, client);
            client.waitForServerCode();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        stage.keyDown(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        stage.keyUp(keycode);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        stage.keyTyped(character);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        stage.touchDown(screenX, screenY, pointer, button);

        Vector2 clickPos = viewport.unproject(new Vector2(screenX, screenY));
        hand.click(clickPos, button);

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        stage.touchUp(screenX, screenY, pointer, button);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        stage.touchDragged(screenX, screenY, pointer);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        stage.mouseMoved(screenX, screenY);
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        stage.scrolled(amount);
        return false;
    }

    private class GameStateUpdater {
        private ShengJiClient client;

        private final EventListener sendCallEventListener = new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(hand.stream().noneMatch(RenderableCard::isSelected) && thisPlayer.getPlay().isEmpty()) {
                    client.sendInt(ClientCodes.NO_CALL);
                } else {
                    // This won't work if different cards are selected at the same time, but that's not supposed to happen
                    int cardNumToSend = hand.stream().filter(RenderableCard::isSelected).findFirst().orElse(hand.get(0)).cardNum();
                    int numCardsInCall = (int)hand.stream().filter(RenderableCard::isSelected).count();
                    client.sendInt(cardNumToSend);
                    client.sendInt(numCardsInCall);

                    hand.addAll(thisPlayer.getPlay());
                    thisPlayer.clearPlay();
                    for(int i = 0; i < numCardsInCall; i++) {
                        thisPlayer.addToPlay(new RenderableCard(cardNumToSend));
                    }
                    hand.removeAll(thisPlayer.getPlay()); // If call is invalid, cards from play will be added back into hand
                }

                hand.setAllSelected(false);

                return true;
            }
        };

        private final EventListener sendKittyEventListener = new EventListener() {
            @Override
            public boolean handle(Event event) {
                hand.forEach(card -> {
                    if(card.isSelected()) {
                        client.sendInt(card.cardNum());
                    }
                });

                // TODO: Temporarily store removed cards in case kitty is invalid
                hand.removeIf(AbstractRenderableCard::isSelected);
                return true;
            }
        };

        private final EventListener sendPlayEventListener = new EventListener() {
            @Override
            public boolean handle(Event event) {
                hand.forEach(card -> {
                    if(card.isSelected()) {
                        card.setSelected(false);
                        client.sendInt(card.cardNum());
                        thisPlayer.addToPlay(card);
                    }
                });
                hand.removeAll(thisPlayer.getPlay());

                disableButton();

                return true;
            }
        };

        private final EventListener sendBasePlayEventListener = new EventListener() {
            @Override
            public boolean handle(Event event) {
                client.sendInt((int)hand.stream().filter(AbstractRenderableCard::isSelected).count());

                return sendPlayEventListener.handle(event);
            }
        };

        void update(int serverCode, ShengJiClient client) throws NullPointerException {
            this.client = client;
            if(!client.readyToRead()) {
                Gdx.app.log("GameState.GameStateUpdater.update", "client read not ready. This shouldn't happen.");
                return;
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

                // Game setup codes:
                case ROUND_START:
                    roundStart();
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
                case WAIT_FOR_COLLECTED_POINTS:
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
                    Gdx.app.log("GameState.Updater.update",
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
                players.add(new Player(playerNum, name, callRank));
            }

            GameState.this.players = players;
            thisPlayer = players.getPlayerFromPlayerNum(thisPlayerNum);

            disableButton();
        }

        // CALLING CODES:

        private void invalidCall() {
            message = "Invalid call, try again";
            lastServerPrompt = ServerCodes.INVALID_CALL;

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            sendButton.setText("Send call");
            enableButton(sendCallEventListener);
        }

        private void noCall() {
            message = "You have chosen not to call";
            lastServerPrompt = ServerCodes.NO_CALL;

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            disableButton();
        }

        private void unsuccessfulCall() {
            message = "Call not strong enough";
            lastServerPrompt = ServerCodes.UNSUCCESSFUL_CALL;

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();

            sendButton.setText("Send call");
            enableButton(sendCallEventListener);
        }

        private void successfulCall() {
            message = "Successful call";
            lastServerPrompt = ServerCodes.SUCCESSFUL_CALL;

            players.forEach(player -> {
                if(player != thisPlayer) {
                    player.clearPlay();
                }
            });

            enableButton(sendCallEventListener);
        }

        private void sendCall() {
            message = "Make your call";
            lastServerPrompt = ServerCodes.SEND_CALL;

            sendButton.setText("Send call");

            enableButton(sendCallEventListener);
        }

        private void waitForNewWinningCall() {
            int callLeaderPlayerNum = client.readInt();
            int callCardNum = client.readInt();
            int numCallCards = client.readInt();

            Player callLeader = players.getPlayerFromPlayerNum(callLeaderPlayerNum);

            trumpCard = new RenderableCard(callCardNum);

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

            sendButton.clearListeners();
            disableButton();
        }

        private void noOneCalled() {
            Player startingPlayer = players.getPlayerFromPlayerNum(client.readInt());
            trumpCard = new RenderableCard(client.readInt());

            message = "No one called, " + startingPlayer.getName() + " will start";

            sendButton.clearListeners();
            disableButton();
        }

        // GAME SETUP CODES:

        private void roundStart() {
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
            lastServerPrompt = ServerCodes.SEND_KITTY;

            sendButton.setText("Confirm kitty");

            enableButton(sendKittyEventListener);
        }

        private void invalidKitty() {
            message = "Invalid kitty!";
            lastServerPrompt = ServerCodes.INVALID_KITTY;

            sendButton.setText("Confirm kitty");

            enableButton(sendKittyEventListener);
        }

        private void sendFriendCards() {
            int numFriendCards = client.readInt();

            message = "Select " + numFriendCards + " friend cards";
            lastServerPrompt = ServerCodes.SEND_FRIEND_CARDS;

            // enableButton(); TODO
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
            lastServerPrompt = ServerCodes.SEND_BASE_PLAY;

            sendButton.setText("Send play");

            enableButton(sendBasePlayEventListener);
        }

        // Should send to server:
        // [[cardNum] for each selected card in hand]
        private void sendPlay() {
            message = "Your turn";
            lastServerPrompt = ServerCodes.SEND_PLAY;

            sendButton.setText("Send play");

            enableButton(sendPlayEventListener);
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
            lastServerPrompt = ServerCodes.INVALID_PLAY;

            hand.addAll(thisPlayer.getPlay());
            thisPlayer.clearPlay();
        }

        private void waitForNewPlayerTeam() {

        }

        private void waitForPlayerInLead() {

        }

        private void waitForTrickWinner() {

        }

        private void waitForTrickPointCards() {

        }

        private void waitForRoundWinners() {

        }

        private void waitForCollectedPoints() {

        }

        private void waitForCallingNumbers() {

        }

        private void roundOver() {
            game.showLobbyScreen(GameState.this);
        }
        
        private void enableButton(EventListener listener) {
            sendButton.setVisible(true);
            sendButton.setDisabled(false);
            sendButton.clearListeners();
            sendButton.addListener(listener);
        }
        
        private void disableButton() {
            sendButton.setVisible(false);
            sendButton.setDisabled(true);
        }
    }
}

