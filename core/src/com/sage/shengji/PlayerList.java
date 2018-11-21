package com.sage.shengji;

import java.util.ArrayList;

class PlayerList<T extends Player> extends ArrayList<T>{
    boolean removeByPlayerNum(int playerNum) {
        for(T p : this) {
            if(p.getPlayerNum() == playerNum) {
                return this.remove(p);
            }
        }
        return false;
    }

    T getPlayerFromPlayerNum(int playerNum) {
        for(T p : this) {
            if(p.getPlayerNum() == playerNum) {
                return p;
            }
        }

        return null;
    }

    T getThisPlayer() {
        return stream().filter(Player::isThisPlayer).findFirst().orElse(get(0));
    }
}
