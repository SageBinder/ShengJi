package com.sage.server;

public enum Team {
    COLLECTORS(1), KEEPERS(0), NO_TEAM(-1);

    int teamNum;

    Team(int i) {
        teamNum = i;
    }

    int getTeamNum() {
        return teamNum;
    }
}
