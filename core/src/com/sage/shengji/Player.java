package com.sage.shengji;

import com.sage.Rank;
import com.sage.Team;

abstract class Player {
    private int playerNum;
    private boolean isHost = false;
    private boolean isThisPlayer = false;
    private String name;
    private Team team = Team.NO_TEAM;
    private Rank callRank = Rank.TWO;

    private final RenderableCardGroup points = new RenderableCardGroup();

    Player(int playerNum, String name) {
        this.name = name;
        this.playerNum = playerNum;
    }

    Player(int playerNum, String name, Rank callRank) {
        this(playerNum, name);
        this.callRank = callRank;
    }

    void setName(String newName) {
        name = newName;
    }

    String getName(int maxCharacters) {
        return name.substring(0, Math.min(maxCharacters, name.length()));
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

    RenderableCardGroup getPoints() {
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

    void setThisPlayer(boolean isThisPlayer) {
        this.isThisPlayer = isThisPlayer;
    }

    boolean isThisPlayer() {
        return isThisPlayer;
    }
}
