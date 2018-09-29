package com.sage.server;

import java.util.ArrayList;

class PlayerList extends ArrayList<Player> {
    PlayerList() {
        super();
    }

    void sendIntToAll(int num) {
        for(Player p : this) {
            p.sendInt(num);
        }
    }

    void sendStringToAll(String str) {
        for(Player p : this) {
            p.sendString(str);
        }
    }

    void sendCardsToAll(ServerCardList cards) {
        for(Player p : this) {
            p.sendCards(cards);
        }
    }

    Player getPlayerFromPlayerNum(int playerNum) {
        for(Player p : this) {
            if(p.getPlayerNum() == playerNum) {
                return p;
            }
        }

        return null;
    }
}
