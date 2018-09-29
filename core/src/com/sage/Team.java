package com.sage;

public enum Team {
    COLLECTORS(1), KEEPERS(0), NO_TEAM(-1);

    public int teamNum;

    Team(int i) {
        teamNum = i;
    }

    public int getTeamNum() {
        return teamNum;
    }
}
