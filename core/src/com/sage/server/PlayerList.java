package com.sage.server;

import java.util.ArrayList;

class PlayerList extends ArrayList<Player> {
    PlayerList() {
        super();
    }

    void sendIntToAll(int num) {
        forEach(p -> p.sendInt(num));
    }

    void sendStringToAll(String str) {
        forEach(p -> p.sendString(str));
    }

    void sendCardsToAll(ServerCardList cards) {
        forEach(p -> p.sendCards(cards));
    }

    Player getPlayerFromPlayerNum(int playerNum) {
//        for(Player p : this) {
//            if(p.getPlayerNum() == playerNum) {
//                return p;
//            }
//        }
//        return null;

        return stream().filter(player -> player.getPlayerNum() == playerNum).findFirst().orElse(null);
    }
}
