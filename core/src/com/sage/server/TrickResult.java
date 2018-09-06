package com.sage.server;

class TrickResult {
    private ServerCardList cardsInvolved;
    private Player winner;
    private Play winningPlay;

    TrickResult(Player winner, ServerCardList cardsInvolved, Play winningPlay) {
        this.winner = winner;
        this.cardsInvolved = cardsInvolved;
        this.winningPlay = winningPlay;
    }

    Player getWinner() {
        return winner;
    }

    Play getWinningPlay() {
        return winningPlay;
    }

    ServerCardList getCardsInvolved() {
        return cardsInvolved;
    }

    ServerCardList getPointCards() {
        return cardsInvolved.getPointCards();
    }
}
