package com.sage.shengji;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.sage.server.ServerCodes.*;

class GameState {
    private GameStateUpdater updater = new GameStateUpdater();

    private PlayerList players = new PlayerList();

    private Player thisPlayer;
    private RenderableHand hand = new RenderableHand();

    private SpriteBatch batch = new SpriteBatch();

    volatile boolean calling = false;

    synchronized void render(Viewport viewport) {
        batch.setProjectionMatrix(viewport.getCamera().combined);
    }

    synchronized void update(int serverCode, ShengJiClient client) {
        updater.update(serverCode, client);
    }

    private class GameStateUpdater {
        ShengJiClient client;

        void update(int serverCode, ShengJiClient client) {
            this.client = client;

            switch(serverCode) {
                case INVALID_CALL:
                    invalidCall();
                    break;
                case NO_CALL:
                    noCall();
                    break;
                case SUCCESSFUL_CALL:
                    successfulCall();
                    break;
                case SEND_CALL:
                    sendCall();
                    break;
                case WAIT_FOR_OTHER_PLAYER_CALL:
                    waitForOtherPlayerCall();
                    break;
                case WAIT_FOR_CALL_WINNER:
                    waitForCallWinner();
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
                case SEND_KITTY_REPLACEMENTS:
                    sendKittyReplacements();
                    break;
                case SEND_FRIEND_CARDS:
                    sendFriendCards();
                    break;
                case WAIT_FOR_FRIEND_CARDS:
                    waitForFriendCards();
                    break;
                case SEND_PLAY_LENGTH:
                    sendPlayLength();
                    break;
                case WAIT_FOR_PLAY_LENGTH:
                    waitForPlayLength();
                    break;
                case SEND_PLAY:
                    sendPlay();
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
                case WAIT_FOR_PLAYERS_LIST:
                    waitForPlayersList();
                    break;
                default:
                    System.out.println("GameState update switch went to default. This really really really shouldn't happen");
            }
        }

        // Server should send:
        // [thisPlayerNum]\n
        // [# of players]\n
        // [[playerNum]\n[name]\n[callRank]\n] for each player
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
        }

        // CALLING CODES:

        private void invalidCall() {

        }

        private void noCall() {

        }

        private void successfulCall() {

        }

        private void sendCall() {

        }

        private void waitForOtherPlayerCall() {

        }

        private void waitForCallWinner() {

        }

        // GAME SETUP CODES:

        // Server should send:
        // [playerNum]\n for each player
        private void waitForPlayerOrder() {
            int[] playerOrder = new int[players.size()];
            for(int i = 0; i < playerOrder.length; i++) {
                playerOrder[i] = client.readInt();
            }
        }

        private void waitForKitty() {

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
        }

        private void sendKittyReplacements() {

        }

        private void sendFriendCards() {

        }

        private void waitForFriendCards() {

        }

        // GAME CODES:
        private void sendPlayLength() {

        }

        private void waitForPlayLength() {

        }

        private void sendPlay() {

        }

        private void waitForPlay() {

        }

        private void invalidPlay() {

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

        }
    }
}
