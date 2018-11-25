package com.sage.shengji;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.badlogic.gdx.utils.Align;
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
    private Stage uiStage;

    private TextButton sendButton;
    private Label messageLabel;

    private BitmapFont font;

    private float delayTimer = 0;

    GameScreen(ScreenManager game, GameState gameState, ShengJiClient client) {
        this.game = game;
        this.gameState = gameState;
        this.client = client;

        float viewportWidth = Gdx.graphics.getWidth() * viewportScale,
                viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);

        var fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Bold.ttf"));

        var playerNameFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        playerNameFontParameter.incremental = true; // <- I don't know what this line does, but the font gets all fucky without it
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

        font = fontGenerator.generateFont(uiFontParameter);

        fontGenerator.dispose();

        uiStage = new Stage(viewport, batch);

        sendButton = new TextButton("I AM A BUTTON", textButtonStyle);
        sendButton.setProgrammaticChangeEvents(true);
        messageLabel = new Label("I AM NOT A BUTTON", labelStyle);

        Table table = new Table();
//        table.setFillParent(true);
//        table.top().padTop(viewportHeight * (0.30f));
//        table.debug();

        table.row();
        table.add(messageLabel);

        table.row().padTop(viewportHeight / 30f);
        table.add(sendButton);

        uiStage.addActor(table);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(this);

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        updateGameState();
        sendButton.setText(gameState.buttonText);
        messageLabel.setText(gameState.message);

        uiStage.getActors().get(0).setPosition(
                (viewport.getWorldWidth() * gameState.players.centerProportion.x)
                        - (uiStage.getActors().get(0).getWidth() * 0.5f),
                (viewport.getWorldHeight() * gameState.players.centerProportion.y)
                        - (uiStage.getActors().get(0).getHeight() * 0.5f)
        );
        uiStage.act(delta);

        checkDelayAndPress(delta,
                Input.Keys.LEFT,
                Input.Keys.RIGHT,
                Input.Keys.UP,
                Input.Keys.DOWN);

        Gdx.gl.glClearColor(ScreenManager.BACKGROUND_COLOR.r, ScreenManager.BACKGROUND_COLOR.g, ScreenManager.BACKGROUND_COLOR.b, ScreenManager.BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        switch(gameState.lastServerCode) {
            case INVALID_CALL:
            case UNSUCCESSFUL_CALL:
            case SUCCESSFUL_CALL:
            case SEND_CALL:
                break;

            // If server sends new winning call, button stays disabled if player has already chosen not to call,
            // else it stays enabled.
            case WAIT_FOR_NEW_WINNING_CALL:
                break;

            case INVALID_KITTY_CALL:
            case NO_ONE_CALLED:
                renderNoOneCalledCard();
                break;

            case SEND_KITTY:
            case INVALID_KITTY:
                gameState.kitty.render(batch, viewport);
                break;

            case SEND_FRIEND_CARDS:
                renderChoosableFriendCards();
                break;

            case SEND_BASE_PLAY:
                break;

            case SEND_PLAY:
                break;

            default:
                disableButton();
        }

        renderPointCards();
        renderPlayerPointCards();
        renderFriendCards();
        renderTrumpCard();
        gameState.players.render(batch, viewport);
        gameState.hand.render(batch, viewport);

        batch.end();

        uiStage.draw();
    }

    private void updateGameState() {
        if(gameState.update(client)) {
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
                    currentBorderGroup = gameState.friendCards;
                    keyDown(Input.Keys.RIGHT); // <- To update borderedCard
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

            if(gameState.lastServerCode != SEND_FRIEND_CARDS) {
                currentBorderGroup = gameState.hand;
            }

            gameState.hand.sort();
        }
    }

    private void renderPointCards() {
        gameState.collectedPointCards.regionWidth = gameState.players.get(0).getPlay().regionWidth * 1.5f;
        gameState.collectedPointCards.cardHeight = gameState.players.get(0).getPlay().cardHeight * 1.2f;
        gameState.collectedPointCards.setPosition(
                viewport.getWorldWidth() * 0.05f,
                viewport.getWorldHeight() - gameState.collectedPointCards.cardHeight - (viewport.getWorldWidth() * 0.027f));
        gameState.collectedPointCards.render(batch, viewport);

        font.draw(batch, "Collectors have " + gameState.numCollectedPoints + " points",
                gameState.collectedPointCards.pos.x + (gameState.collectedPointCards.regionWidth * 0.5f),
                gameState.collectedPointCards.pos.y + (font.getXHeight() * 2) + gameState.collectedPointCards.cardHeight,
                0, Align.center, false);
    }

    private void renderPlayerPointCards() {

    }

    private void renderFriendCards() {
        if(gameState.lastServerCode != SEND_FRIEND_CARDS && !gameState.friendCards.isEmpty()) {
            gameState.friendCards.cardHeight =
                    gameState.thisPlayer.getPlay().cardHeight;

            gameState.friendCards.regionWidth =
                    (RenderableCard.WIDTH_TO_HEIGHT_RATIO * gameState.friendCards.cardHeight) * 4;

            gameState.friendCards.setPosition(
                    gameState.friendCards.regionWidth * 0.1f,
                    (viewport.getWorldHeight() * 0.4f) - (gameState.friendCards.cardHeight * 0.75f));

            font.draw(batch, "Friend\ncards",
                    gameState.friendCards.pos.x + (gameState.friendCards.regionWidth * 0.5f),
                    gameState.friendCards.pos.y + (font.getXHeight() * 4) + gameState.friendCards.cardHeight,
                    0, Align.center, false);
            gameState.friendCards.render(batch, viewport); // !! friendCards should be rendered AFTER font.draw !!
        }
    }

    private void renderChoosableFriendCards() {
        gameState.friendCards.cardHeight =
                gameState.thisPlayer.getPlay().cardHeight;

        gameState.friendCards.regionWidth =
                (RenderableCard.WIDTH_TO_HEIGHT_RATIO * gameState.friendCards.cardHeight) * 4;

        gameState.friendCards.setPosition(
                (viewport.getWorldWidth() - gameState.friendCards.regionWidth) * 0.5f,
                gameState.hand.pos.y + (gameState.hand.cardHeight * 2)
        );
        gameState.friendCards.render(batch, viewport);
    }

    private void renderNoOneCalledCard() {
        gameState.noOneCalledCard.setHeight(gameState.hand.cardHeight);
        gameState.noOneCalledCard.setPosition(
                (gameState.hand.pos.x * 0.5f) + (gameState.noOneCalledCard.getWidth() * 0.5f),
                (viewport.getWorldHeight() - gameState.noOneCalledCard.getHeight()) * 0.5f);
        gameState.noOneCalledCard.render(batch, viewport);

        font.draw(batch, "Card pulled\nfrom kitty",
                gameState.noOneCalledCard.getPosition().x + (gameState.noOneCalledCard.getWidth() * 0.5f),
                gameState.noOneCalledCard.getPosition().y + (font.getXHeight() * 4) + gameState.noOneCalledCard.getHeight(),
                0, Align.center, false);
    }

    private void renderTrumpCard() {
        if(gameState.trumpCard != null) {
            gameState.trumpCard.setHeight(gameState.hand.cardHeight);
            gameState.trumpCard.setPosition(
                    gameState.hand.pos.x * 0.5f,
                    (viewport.getWorldHeight() * 0.6f) - (gameState.trumpCard.getHeight() * 0.5f));
            gameState.trumpCard.render(batch, viewport);

            font.setColor(Color.GOLD);
            font.draw(batch, "Trump\ncard",
                    gameState.trumpCard.getPosition().x + (gameState.trumpCard.getWidth() * 0.5f),
                    gameState.trumpCard.getPosition().y + (font.getXHeight() * 4) + gameState.trumpCard.getHeight(),
                    0, Align.center, false);
            font.setColor(Color.WHITE);
        }
    }

    private void checkDelayAndPress(float delta, int ...keyCodes) {
        for(int keyCode: keyCodes) {
            if(Gdx.input.isKeyPressed(keyCode)) {
                delayTimer += delta;
                if(delayTimer > 0.5f) {
                    delayTimer -= 0.05f;
                    keyDown(keyCode);
                }
                return;
            }
        }
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

        if(gameState.lastServerCode != SEND_FRIEND_CARDS) {
            for(ListIterator<RenderableCard> i = gameState.friendCards.reverseListIterator(); i.hasPrevious();) {
                final RenderableCard c = i.previous();
                if(c.isSelected()) {
                    if(c.displayRectContainsPoint(clickCoordinates)) {
                        break;
                    } else {
                        continue;
                    }
                }

                if(c.displayRectContainsPoint(clickCoordinates) || c.baseRectContainsPoint(clickCoordinates)) {
                    highlightCard(c);
                    return false;
                }
            }
        }

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
                highlightCard(c);
                return false;
            }
        }

        if(lastHighlightedCard != null && !lastHighlightedCard.isSelected()) {
            lastHighlightedCard.resetDisplayRect();
            lastHighlightedCard.resetBothBackgroundColors();
        }

        return false;
    }

    private void highlightCard(RenderableCard c) {
        if(lastHighlightedCard != null && !lastHighlightedCard.isSelected() && lastHighlightedCard != c) {
            lastHighlightedCard.resetDisplayRect();
            lastHighlightedCard.resetBothBackgroundColors();
        }

        c.setDisplayY(c.getY() + (c.getHeight() * 0.4f));
        c.setFaceBackgroundColor(c.faceHighlightedBackgroundColor);
        c.setBackBackgroundColor(c.backHighlightedBackgroundColor);
        lastHighlightedCard = c;
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        Vector2 clickCoordinates = viewport.unproject(new Vector2(screenX, screenY));

        if(gameState.lastServerCode == SEND_FRIEND_CARDS) {
            for(ListIterator<RenderableCard> i = gameState.friendCards.reverseListIterator(); i.hasPrevious(); ) {
                final RenderableCard c = i.previous();
                if(c.displayRectContainsPoint(clickCoordinates)) {
                    if(borderedCard != null) borderedCard.resetBothBorderColors();
                    borderedCard = c;
                    borderedCard.setBothBorderColors(Color.CYAN);
                    return false;
                }
            }
        }

        try {
            for(ListIterator<RenderableCard> i = gameState.hand.reverseListIterator(); i.hasPrevious(); ) {
                final RenderableCard c = i.previous();
                if(c.displayRectContainsPoint(clickCoordinates) || (!c.isSelected() && c.baseRectContainsPoint(clickCoordinates))) {
                    if(gameState.lastServerCode != SEND_FRIEND_CARDS) {
                        if(borderedCard != null) borderedCard.resetBothBorderColors();
                        borderedCard = c;
                        borderedCard.setBothBorderColors(Color.CYAN);
                    }

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

            resetBorderedCard();
            return false;
        } finally {
            mouseMoved(screenX, screenY);
        }
    }


    private RenderableCard borderedCard = null;
    private RenderableCardGroup currentBorderGroup = null;
    @Override
    public boolean keyDown(int keyCode) {
        if(keyCode == Input.Keys.ESCAPE) {
            resetBorderedCard();
            return false;
        }

        if(keyCode == Input.Keys.ENTER) {
            if(gameState.buttonIsEnabled) sendButton.toggle();
            return false;
        }

        if(keyCode == Input.Keys.BACKSPACE) {
            gameState.hand.forEach(AbstractRenderableCard::deselect);
            return false;
        }

        if(currentBorderGroup == null || currentBorderGroup.isEmpty()) {
            return false;
        }

        int direction;

        if(gameState.lastServerCode == SEND_FRIEND_CARDS
                && (keyCode == Input.Keys.UP || keyCode == Input.Keys.DOWN)) {
            scrolled(keyCode == Input.Keys.UP ? -1 : 1);
            return false;
        }

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

        if(keyCode == Input.Keys.RIGHT || keyCode == Input.Keys.LEFT) {
            direction = keyCode == Input.Keys.RIGHT ? 1 : -1;
        } else {
            return false;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            direction *= 2;
        }

        if(borderedCard == null) {
            borderedCard = currentBorderGroup.get(direction < 0 ? currentBorderGroup.size() - 1 : 0);
        } else {
            borderedCard.resetBothBorderColors();

            int index = (currentBorderGroup.indexOf(borderedCard) + direction) % currentBorderGroup.size();
            borderedCard = currentBorderGroup.get(index >= 0 ? index : currentBorderGroup.size() + index);
        }
        borderedCard.setBothBorderColors(Color.CYAN);

        return false;
    }

    @Override
    public boolean keyUp(int keyCode) {
        delayTimer = 0;
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if(gameState.lastServerCode == SEND_FRIEND_CARDS && borderedCard != null) {
            if(!gameState.friendCards.contains(borderedCard)) {
                return false;
            }

            int newCardNum = borderedCard.cardNum();
            newCardNum -= amount;
            newCardNum %= 54;
            if(newCardNum < 0) {
                newCardNum = 54 + newCardNum;
            }
            borderedCard.setCardNum(newCardNum);
        }
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

    private void resetBorderedCard() {
        if(borderedCard != null) {
            borderedCard.resetBothBorderColors();
            borderedCard = null;
        }
    }

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

                gameState.hand.setAllSelected(false);
            }
        }
    };

    private final ChangeListener sendCallChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            ArrayList<RenderableCard> selectedCards = (ArrayList<RenderableCard>)gameState.hand.stream()
                    .filter(AbstractRenderableCard::isSelected)
                    .collect(Collectors.toList());

            if(selectedCards.isEmpty()) {
                if(gameState.thisPlayer.getPlay().isEmpty()) {
                    client.sendInt(ClientCodes.NO_CALL);
                }
                return;
            }

            // Ensure that all the selected cards are the same type of card
            if(!selectedCards.stream().allMatch(c -> selectedCards.get(0).cardNum() == c.cardNum())) {
                gameState.message = "All cards in call must be the same";
                return;
            }

            int cardNumToSend = selectedCards.get(0).cardNum();
            int numCardsInCall = selectedCards.size();
            if(!gameState.thisPlayer.getPlay().isEmpty()
                    && cardNumToSend == gameState.thisPlayer.getPlay().get(0).cardNum()) {
                numCardsInCall += gameState.thisPlayer.getPlay().size();
            } else {
                gameState.hand.addAll(gameState.thisPlayer.getPlay());
                gameState.thisPlayer.clearPlay();
            }
            client.sendInt(cardNumToSend);
            client.sendInt(numCardsInCall);

            gameState.thisPlayer.getPlay().addAll(selectedCards);
            gameState.hand.removeAll(selectedCards);
            selectedCards.forEach(AbstractRenderableCard::deselect);

            gameState.hand.setAllSelected(false);
            resetBorderedCard();
        }
    };

    private final ChangeListener sendKittyChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            ArrayList<RenderableCard> selectedCards = (ArrayList<RenderableCard>)gameState.hand.stream()
                    .filter(RenderableCard::isSelected)
                    .collect(Collectors.toList());

            if(selectedCards.isEmpty()) {
                gameState.message = "You didn't select any cards, you dummy. Select " + gameState.kitty.size() + " cards";
                return;
            }

            if(selectedCards.size() != gameState.kitty.size()) {
                gameState.message =
                        (selectedCards.size() > gameState.kitty.size() ? "Too many" : "Not enough") + " cards, " +
                                "select " + gameState.kitty.size() + " cards";
                return;
            }

            resetBorderedCard();
            gameState.thisPlayer.getPlay().addAll(selectedCards);
            gameState.hand.removeAll(selectedCards);
            selectedCards.forEach(c -> client.sendInt(c.cardNum()));

//            disableButton();
        }
    };

    private final ChangeListener sendPlayChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if(gameState.hand.stream().filter(AbstractRenderableCard::isSelected).count() != gameState.numCardsInBasePlay
                    && gameState.numCardsInBasePlay > 0) {
                gameState.message = "Incorrect number of cards in play";
                return;
            }

            gameState.hand.forEach(card -> {
                if(card.isSelected()) {
                    card.setSelected(false);
                    client.sendInt(card.cardNum());
                    gameState.thisPlayer.addToPlay(card);
                }
            });
            gameState.hand.removeAll(gameState.thisPlayer.getPlay());
            resetBorderedCard();

            boolean isBasePlay = gameState.players.stream()
                    .allMatch(p -> p != gameState.thisPlayer && p.getPlay().isEmpty());
            if(isBasePlay) {
                gameState.thisPlayer.getPlay().forEach(c -> {
                    c.faceUnselectedBackgroundColor.set(Color.LIGHT_GRAY);
                    c.setFaceBackgroundColor(c.faceUnselectedBackgroundColor);
                });
            }
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

    private final ChangeListener sendFriendCardsChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            gameState.friendCards.forEach(c -> client.sendInt(c.cardNum()));
        }
    };
}
