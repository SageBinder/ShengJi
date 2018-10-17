package com.sage.shengji;

import java.util.ArrayList;

@SuppressWarnings("serial")
class PlayerList extends ArrayList<Player> {
    boolean removeByPlayerNum(int playerNum) {
        for(Player p : this) {
            if(p.getPlayerNum() == playerNum) {
                return this.remove(p);
            }
        }
        return false;
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
