package com.sage.server;

import java.util.ArrayList;

class PlayerList extends ArrayList<Player> {
    PlayerList() {
        super();
    }

    void sendIntToAll(int num, boolean flushWriteBuffer) throws PlayerDisconnectedException {
        boolean anyPlayerDisconnected = false;
        for(var p : this) {
            try {
                p.sendInt(num, false);
            } catch(PlayerDisconnectedException e) {
                anyPlayerDisconnected = true;
            }
        }
        if(flushWriteBuffer) flushAllWriteBuffers();
        if(anyPlayerDisconnected) throw new PlayerDisconnectedException();
    }

    void sendStringToAll(String str, boolean flushWriteBuffer) throws PlayerDisconnectedException {
        boolean anyPlayerDisconnected = false;
        for(var p : this) {
            try {
                p.sendString(str, false);
            } catch(PlayerDisconnectedException e) {
                anyPlayerDisconnected = true;
            }
        }
        if(flushWriteBuffer) flushAllWriteBuffers();
        if(anyPlayerDisconnected) throw new PlayerDisconnectedException();
    }

    void sendCardsToAll(ServerCardList cards, boolean flushWriteBuffer) throws PlayerDisconnectedException {
        boolean anyPlayerDisconnected = false;
        for(var p : this) {
            try {
                p.sendCards(cards, false);
            } catch(PlayerDisconnectedException e) {
                anyPlayerDisconnected = true;
            }
        }
        if(flushWriteBuffer) flushAllWriteBuffers();
        if(anyPlayerDisconnected) throw new PlayerDisconnectedException();
    }

    void sendIntToAll(int num) throws PlayerDisconnectedException {
        sendIntToAll(num, true);
    }

    void sendStringToAll(String str) throws PlayerDisconnectedException {
        sendStringToAll(str, true);
    }

    void sendCardsToAll(ServerCardList cards) throws PlayerDisconnectedException {
        sendCardsToAll(cards, true);
    }

    void flushAllWriteBuffers() throws PlayerDisconnectedException {
        boolean anyPlayerDisconnected = false;
        for(var p : this) {
            try {
                p.flushWriteBuffer();
            } catch(PlayerDisconnectedException e) {
                anyPlayerDisconnected = true;
            }
        }
        if(anyPlayerDisconnected) throw new PlayerDisconnectedException();
    }

    Player getPlayerFromPlayerNum(int playerNum) {
        return stream().filter(player -> player.getPlayerNum() == playerNum).findFirst().orElse(null);
    }
}
