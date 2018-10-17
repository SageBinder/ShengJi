package com.sage;

public enum Team {
    COLLECTORS(2), KEEPERS(1), NO_TEAM(0);

    public int teamNum;

    Team(int i) {
        teamNum = i;
    }

    public int getTeamNum() {
        return teamNum;
    }

    public static Team getTeamFromTeamNum(int teamNum){
        switch(teamNum) {
            case 0:
                return NO_TEAM;
            case 1:
                return KEEPERS;
            case 2:
                return COLLECTORS;
            default:
                throw new IllegalArgumentException("No team is associated with the value " + teamNum);
        }
    }
}
