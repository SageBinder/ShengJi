package com.sage.shengji;

import com.sage.Rank;
import com.sage.Team;

class Player {
    private int playerNum;
    private boolean isHost = false;
    private String name;
    private RenderablePlay play = new RenderablePlay();
    private Team team = Team.NO_TEAM;
    private Rank callRank = Rank.TWO;

    private final RenderableCardList points = new RenderableCardList();

    Player(int playerNum, String name) {
        this.name = name;
        this.playerNum = playerNum;
    }

    Player(int playerNum, String name, Rank callRank) {
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

    void addToPlay(RenderableCardList list) {
        play.addAll(list);
    }

    void addToPlay(RenderableCard c) {
        play.add(c);
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

    Rank getCallRank() {
        return callRank;
    }

    void setCallRank(int callRank) {
        this.callRank = Rank.fromInt(callRank);
    }

    RenderableCardList getPoints() {
        return points;
    }

    void addToPoints(RenderableCardList list) {
        points.addAll(list);
    }

    void addToPoints(RenderableCard c) {
        points.add(c);
    }

    void clearPoints() {
        points.clear();
    }

    void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    boolean isHost() {
        return isHost;
    }
}
