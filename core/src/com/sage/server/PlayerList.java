package com.sage.server;

import java.util.ArrayList;

class PlayerList extends ArrayList<Player> {
    PlayerList() {
        super();
    }

    void sendIntToAll(int num, boolean flushWriteBuffer) {
        forEach(p -> p.sendInt(num, false));
        if(flushWriteBuffer) flushAllWriteBuffers();
    }

    void sendStringToAll(String str, boolean flushWriteBuffer) {
        forEach(p -> p.sendString(str, false));
        if(flushWriteBuffer) flushAllWriteBuffers();
    }

    void sendCardsToAll(ServerCardList cards, boolean flushWriteBuffer) {
        forEach(p -> p.sendCards(cards, false));
        if(flushWriteBuffer) flushAllWriteBuffers();
    }

    void sendIntToAll(int num) {
        sendIntToAll(num, true);
    }

    void sendStringToAll(String str) {
        sendStringToAll(str, true);
    }

    void sendCardsToAll(ServerCardList cards) {
        sendCardsToAll(cards, true);
    }

    void flushAllWriteBuffers() {
        forEach(Player::flushWriteBuffer);
    }

    Player getPlayerFromPlayerNum(int playerNum) {
        return stream().filter(player -> player.getPlayerNum() == playerNum).findFirst().orElse(null);
    }
}
