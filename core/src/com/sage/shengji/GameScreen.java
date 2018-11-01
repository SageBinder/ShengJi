package com.sage.shengji;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.stream.Collectors;

import static com.sage.server.ServerCodes.*;

public class GameScreen extends InputAdapter implements Screen {
    private ScreenManager game;
    private GameState gameState;
    private ShengJiClient client;

    private ExtendViewport viewport;

    private SpriteBatch batch = new SpriteBatch();

    private float textProportion = 1f / 7f,
            viewportScale = 5f;

    private InputMultiplexer multiplexer;
    private Stage gameStage;
    private TextButton sendButton;

    private BitmapFont messageFont;

    GameScreen(ScreenManager game, GameState gameState, ShengJiClient client) {
        this.game = game;
        this.gameState = gameState;
        this.client = client;

        float viewportWidth = Gdx.graphics.getWidth() * viewportScale,
                viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        gameState.setViewport(viewport);

        var fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));
        var fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = fontGenerator.generateFont(fontParameter);

        messageFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();

        gameStage = new Stage(viewport, batch);
        sendButton = new TextButton("I AM A BUTTON", textButtonStyle);
//        sendButton.setTransform(true);
//        sendButton.setPosition(viewportWidth / 2, viewportHeight / 2);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.debug();


        table.add(sendButton);

        gameStage.addActor(table);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(gameStage);
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(gameState);

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        gameState.setViewport(viewport);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        if(gameState.update(client)) {
            sendButton.setText(gameState.buttonText);
//            sendButton.setWidth(((float)sendButton.getStyle().font.getRegion().getRegionWidth() / viewport.getScreenWidth()) * viewport.getWorldWidth());

            switch(gameState.lastServerCode) {
                case INVALID_CALL:
                case UNSUCCESSFUL_CALL:
                case SUCCESSFUL_CALL:
                case SEND_CALL:
                    enableButton(sendCallChangeListener);
                    break;

                // If server sends new winning call, button stays disabled if player has already chosen not to call,
                // else it stays enabled.
                case WAIT_FOR_NEW_WINNING_CALL:
                    break;

                case INVALID_KITTY_CALL:
                case NO_ONE_CALLED:
                    enableButton(sendKittyCallChangeListener);
                    break;

                case SEND_KITTY:
                case INVALID_KITTY:
                    enableButton(sendKittyChangeListener);
                    break;

                case SEND_FRIEND_CARDS:
                    enableButton(sendFriendCardsChangeListener);
                    break;

                case SEND_BASE_PLAY:
                    enableButton(sendBasePlayChangeListener);
                    break;

                case SEND_PLAY:
                    enableButton(sendPlayChangeListener);
                    break;

                default:
                    disableButton();
            }

            gameState.hand.sort();
        }

        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, ScreenManager.BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameStage.act(delta);
        gameStage.draw();

        batch.begin();

      //  messageFont.draw(batch, gameState.message, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);

        batch.setProjectionMatrix(viewport.getCamera().combined);

        if(gameState.lastServerCode == INVALID_KITTY || gameState.lastServerCode == SEND_KITTY) {
            gameState.kitty.render(batch, viewport);
        }

        gameState.hand.render(batch, viewport);

        batch.end();
    }

    private void disableButton() {
        while(sendButton.getListeners().size > 1) {
            sendButton.getListeners().removeIndex(sendButton.getListeners().size - 1);
        }

        sendButton.setDisabled(true);
        sendButton.setVisible(false);
    }

    private void enableButton(ChangeListener listener) {
        while(sendButton.getListeners().size > 1) {
            sendButton.getListeners().removeIndex(sendButton.getListeners().size - 1);
        }
        sendButton.addListener(listener);

        sendButton.setDisabled(false);
        sendButton.setVisible(true);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        messageFont.dispose();
    }

    // TODO: Make sure the number of selected cards is valid before sending to server
    // TODO: Disabling the button from button's change listener freezes the program, so uh... find a workaround
    private final ChangeListener sendKittyCallChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if(gameState.hand.stream().anyMatch(RenderableCard::isSelected)) {
                int cardNumToSend = gameState.hand.stream().filter(RenderableCard::isSelected).findFirst().orElse(gameState.hand.get(0)).cardNum();
                client.sendInt(cardNumToSend);

                // thisPlayer.getPlay() should be empty, but add cards back into hand just in case
                gameState.hand.addAll(gameState.thisPlayer.getPlay());
                gameState.thisPlayer.clearPlay();

                gameState.thisPlayer.addToPlay(new RenderableCard(cardNumToSend));

                gameState.hand.removeAll(gameState.thisPlayer.getPlay());

                gameState.hand.setAllSelected(false);
            }
        }
    };

    private final ChangeListener sendCallChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if(gameState.hand.stream().noneMatch(RenderableCard::isSelected) && gameState.thisPlayer.getPlay().isEmpty()) {
                client.sendInt(ClientCodes.NO_CALL);
            } else {
                // This won't work if different cards are selected at the same time, but that's not supposed to happen
                int cardNumToSend = gameState.hand.stream().filter(RenderableCard::isSelected).findFirst().orElse(gameState.hand.get(0)).cardNum();
                int numCardsInCall = (int)gameState.hand.stream().filter(RenderableCard::isSelected).count();
                client.sendInt(cardNumToSend);
                client.sendInt(numCardsInCall);

                // thisPlayer.getPlay() should be empty, but add cards back into hand just in case
                gameState.hand.addAll(gameState.thisPlayer.getPlay());
                gameState.thisPlayer.clearPlay();

                for(int i = 0; i < numCardsInCall; i++) {
                    gameState.thisPlayer.addToPlay(new RenderableCard(cardNumToSend));
                }
                gameState.hand.removeAll(gameState.thisPlayer.getPlay()); // If call is invalid, cards from play will be added back into hand
            }

            gameState.hand.setAllSelected(false);
        }
    };

    private final ChangeListener sendKittyChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            gameState.hand.stream().filter(RenderableCard::isSelected).forEach((c) -> client.sendInt(c.cardNum()));

//            gameState.hand.forEach(card -> {
//                if(card.isSelected()) {
//                    client.sendInt(card.cardNum());
//                }
//            });

            // TODO: Temporarily store removed cards in case kitty is invalid
            gameState.hand.removeIf(AbstractRenderableCard::isSelected);

//            disableButton();
        }
    };

    private final ChangeListener sendPlayChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            gameState.hand.forEach(card -> {
                if(card.isSelected()) {
                    card.setSelected(false);
                    client.sendInt(card.cardNum());
                    gameState.thisPlayer.addToPlay(card);
                }
            });
            gameState.hand.removeAll(gameState.thisPlayer.getPlay());

            disableButton();
        }
    };

    private final ChangeListener sendBasePlayChangeListener = new ChangeListener() {
        @Override
        public void changed (ChangeEvent event, Actor actor) {
            client.sendInt((int)gameState.hand.stream().filter(AbstractRenderableCard::isSelected).count());

            sendPlayChangeListener.handle(event);
        }
    };

    // TODO: Allow user to pick friend cards from all possible cards, not just hand
    private final ChangeListener sendFriendCardsChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            // Will this cast actually work?
            RenderableCardList selectedCards = (RenderableCardList)gameState.hand.stream()
                    .filter(AbstractRenderableCard::isSelected)
                    .collect(Collectors.toList());

            if(selectedCards.size() > gameState.players.size() / 2) { // players.size() / 2 is the number of friend cards. This should be converted into an int and read from GameState
                gameState.message = "Too many friend cards!";
            } else {
                selectedCards.forEach((c) -> {
                    client.sendInt(c.cardNum());
                    c.setSelected(false);
                });
                disableButton();
            }
        }
    };
}
