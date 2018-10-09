package com.sage.server;

class TrickResult {
    private ServerCardList pointCards;
    private Player winner;
    private Play winningPlay;

    TrickResult(Player winner, ServerCardList pointCards, Play winningPlay) {
        this.winner = winner;
        this.pointCards = pointCards;
        this.winningPlay = winningPlay;
    }

    Player getWinner() {
        return winner;
    }

    Play getWinningPlay() {
        return winningPlay;
    }

    ServerCardList getPointCards() {
        return pointCards;
    }
}
