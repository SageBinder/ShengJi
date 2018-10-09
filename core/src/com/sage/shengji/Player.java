package com.sage.shengji;

import com.sage.Team;

class Player {
    private int playerNum;
    private String name;
    private RenderablePlay play = new RenderablePlay();
    private Team team = Team.NO_TEAM;
    private int callRank = 2;

    private final RenderableCardList points = new RenderableCardList();

    Player(int playerNum, String name) {
        this.name = name;
        this.playerNum = playerNum;
    }

    Player(int playerNum, String name, int callRank) {
        this.name = name;
        this.playerNum = playerNum;
        this.callRank = callRank;
    }

    void setName(String newName) {
        name = newName;
    }

    String getName() {
        return name;
    }

    void setPlayerNum(int num) {
        playerNum = num;
    }

    int getPlayerNum() {
        return playerNum;
    }

    void setPlay(RenderablePlay newPlay) {
        play.addAll(newPlay);
    }

    void addToPlay(RenderableCard c) {
        play.add(c);
    }

    void addAllToPlay(RenderableCardList list) {
        play.addAll(list);
    }

    void clearPlay() {
        play.clear();
    }

    RenderablePlay getPlay() {
        return play;
    }

    Team getTeam() {
        return team;
    }

    void setTeam(Team team) {
        this.team = team;
    }

    int getCallRank() {
        return callRank;
    }

    void setCallRank(int callRank) {
        this.callRank = callRank;
    }
}
