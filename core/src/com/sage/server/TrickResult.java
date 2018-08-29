package com.sage.server;

class TrickResult {
    private CardList cardsInvolved;
    private Player winner;
    private Play winningPlay;

    TrickResult(Player winner, CardList cardsInvolved, Play winningPlay) {
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

    CardList getCardsInvolved() {
        return cardsInvolved;
    }

    CardList getPointCards() {
        return cardsInvolved.getPointCards();
    }
}
