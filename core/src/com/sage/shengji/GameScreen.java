package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static com.sage.server.ServerCodes.*;

public class GameScreen extends InputAdapter implements Screen, InputProcessor {
    private ScreenManager game;
    private GameState gameState;
    private ShengJiClient client;

    private ExtendViewport viewport;

    private SpriteBatch batch = new SpriteBatch();

    private float textProportion = 1f / 10f,
            viewportScale = 5f;

    private InputMultiplexer multiplexer;
    private Stage gameStage;

    private TextButton sendButton;
    private Label messageLabel;

//    private BitmapFont messageFont;

    GameScreen(ScreenManager game, GameState gameState, ShengJiClient client) {
        this.game = game;
        this.gameState = gameState;
        this.client = client;

        float viewportWidth = Gdx.graphics.getWidth() * viewportScale,
                viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        var fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));

        var playerNameFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        playerNameFontParameter.incremental = true;
        playerNameFontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion * 0.8f);
        RenderablePlayer.nameFont = fontGenerator.generateFont(playerNameFontParameter);

        var uiFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        uiFontParameter.incremental = true; // <- I don't know what this line does, but the font gets all fucky without it
        uiFontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        var skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = fontGenerator.generateFont(uiFontParameter);

        var labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = fontGenerator.generateFont(uiFontParameter);

        fontGenerator.dispose();

        gameStage = new Stage(viewport, batch);

        sendButton = new TextButton("I AM A BUTTON", textButtonStyle);
        sendButton.setProgrammaticChangeEvents(true);
        messageLabel = new Label("I AM NOT A BUTTON", labelStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.top().padTop(viewportHeight * (0.30f));
//        table.debug();

        table.row();
        table.add(messageLabel);

        table.row().padTop(viewportHeight / 30f);
        table.add(sendButton);

        gameStage.addActor(table);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(gameStage);
        multiplexer.addProcessor(this);

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        sendButton.setText(gameState.buttonText);
        messageLabel.setText(gameState.message);
        if(gameState.update(client)) {
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
        batch.setProjectionMatrix(viewport.getCamera().combined);

        if(gameState.lastServerCode == INVALID_KITTY || gameState.lastServerCode == SEND_KITTY) {
            gameState.kitty.render(batch, viewport);
        }

        gameState.hand.render(batch, viewport);
        gameState.players.render(batch, viewport);

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


    // I'm thinking RenderableHand should implement InputProcessor and these features should be put there instead.
    private RenderableCard lastHighlightedCard = null;
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 clickCoordinates = viewport.unproject(new Vector2(screenX, screenY));

        for(ListIterator<RenderableCard> i = gameState.hand.reverseListIterator(); i.hasPrevious();) {
            final RenderableCard c = i.previous();

            if(c.isSelected()) {
                if(c.displayRectContainsPoint(clickCoordinates)) {
                    break;
                } else {
                    continue;
                }
            }

            if(c.displayRectContainsPoint(clickCoordinates) || c.baseRectContainsPoint(clickCoordinates)) {
                if(lastHighlightedCard != null && !lastHighlightedCard.isSelected() && lastHighlightedCard != c) {
                    lastHighlightedCard.resetDisplayRect();
                    lastHighlightedCard.resetBothBackgroundColors();
                }

                c.setDisplayY(c.getY() + (c.getHeight() * 0.4f));
                c.setFaceBackgroundColor(c.faceHighlightedBackgroundColor);
                c.setBackBackgroundColor(c.backHighlightedBackgroundColor);
                lastHighlightedCard = c;

                return false;
            }
        }

        if(lastHighlightedCard != null && !lastHighlightedCard.isSelected()) {
            lastHighlightedCard.resetDisplayRect();
            lastHighlightedCard.resetBothBackgroundColors();
        }

        return false;
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        Vector2 clickCoordinates = viewport.unproject(new Vector2(screenX, screenY));

        try {
            for(ListIterator<RenderableCard> i = gameState.hand.reverseListIterator(); i.hasPrevious(); ) {
                final RenderableCard c = i.previous();
                if(c.displayRectContainsPoint(clickCoordinates) || (!c.isSelected() && c.baseRectContainsPoint(clickCoordinates))) {
                    if(borderedCard != null) borderedCard.resetBothBorderColors();
                    borderedCard = c;
                    borderedCard.setBothBorderColors(Color.CYAN);

                    if(button == Input.Buttons.LEFT && c.isFaceUp()) {
                        c.toggleSelected();
                        return true;
                    } else if(button == Input.Buttons.RIGHT) {
                        c.flip();
                        return true;
                    }

                    return false;
                }
            }

            if(borderedCard != null) {
                borderedCard.resetBothBorderColors();
                borderedCard = null;
            }
            return false;
        } finally {
            mouseMoved(screenX, screenY);
        }
    }


    RenderableCard borderedCard = null;
    @Override
    public boolean keyDown(int keyCode) {
        if(keyCode == Input.Keys.ENTER) {
            sendButton.toggle();
            return false;
        }

        if(keyCode == Input.Keys.BACKSPACE) {
            gameState.hand.forEach(AbstractRenderableCard::deselect);
            return false;
        }

        int direction;

        if(keyCode == Input.Keys.SPACE
                || keyCode == Input.Keys.UP
                || keyCode == Input.Keys.DOWN
                || keyCode == Input.Keys.S) {
            if(borderedCard != null) borderedCard.toggleSelected();
            return false;
        }

        if(keyCode == Input.Keys.F) {
            if(borderedCard != null) borderedCard.flip();
            return false;
        }

        if(keyCode == Input.Keys.LEFT) {
            direction = -1;
        } else if(keyCode == Input.Keys.RIGHT) {
            direction = 1;
        } else {
            return false;
        }

        if(borderedCard == null) {
            borderedCard = gameState.hand.get(direction == -1 ? gameState.hand.size() - 1 : 0);
        } else {
            borderedCard.resetBothBorderColors();

            int index = (gameState.hand.indexOf(borderedCard) + direction) % gameState.hand.size();
            borderedCard = gameState.hand.get(index >= 0 ? index : gameState.hand.size() - 1);
        }
        borderedCard.setBothBorderColors(Color.CYAN);

        return false;
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
        messageLabel.getStyle().font.dispose();
        sendButton.getStyle().font.dispose();
    }

    // TODO: Make sure the number of selected cards is valid before sending to server
    // TODO: Disabling the button from button's change listener freezes the program, so uh... find a workaround
    private final ChangeListener sendKittyCallChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if(gameState.hand.stream().anyMatch(RenderableCard::isSelected)) {
                if(gameState.hand.stream().filter(RenderableCard::isSelected).count() != 1) {
                    gameState.message = "Only one card for kitty call";
                    return;
                }

                RenderableCard cardToSend = gameState.hand.stream()
                        .filter(RenderableCard::isSelected)
                        .findFirst().orElse(gameState.hand.get(0));
                client.sendInt(cardToSend.cardNum());

                // thisPlayer.getPlay() should be empty, but add cards back into hand just in case
                gameState.hand.addAll(gameState.thisPlayer.getPlay());
                gameState.thisPlayer.clearPlay();

                gameState.thisPlayer.addToPlay(cardToSend);

                gameState.hand.remove(cardToSend);

                cardToSend.deselect();
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
                ArrayList<RenderableCard> selectedCards = (ArrayList<RenderableCard>)gameState.hand.stream()
                        .filter(AbstractRenderableCard::isSelected).collect(Collectors.toList());

                // Ensure that all the selected cards are the same type of card
                for(RenderableCard c : selectedCards) {
                    if(c.cardNum() != selectedCards.get(0).cardNum()) {
                        gameState.message = "All cards in call must be the same";
                        return;
                    }
                }

                int cardNumToSend = selectedCards.get(0).cardNum();
                int numCardsInCall = selectedCards.size();
                client.sendInt(cardNumToSend);
                client.sendInt(numCardsInCall);

                // thisPlayer.getPlay() should be empty, but add cards back into hand just in case
                gameState.hand.addAll(gameState.thisPlayer.getPlay());
                gameState.thisPlayer.clearPlay();

                gameState.thisPlayer.getPlay().addAll(selectedCards);
                gameState.hand.removeAll(selectedCards);
                selectedCards.forEach(AbstractRenderableCard::deselect);
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

//            disableButton();
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
//                disableButton();
            }
        }
    };
}
