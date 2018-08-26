package com.sage.server;

class TrickResult {
    private CardList cardsInvolved;
    private Player winner;

    TrickResult(Player winner, CardList cardsInvolved) {
        this.winner = winner;
        this.cardsInvolved = cardsInvolved;
    }

    Player getWinner() {
        return winner;
    }

    CardList getCardsInvolved() {
        return cardsInvolved;
    }

    CardList getPointCards() {
        return cardsInvolved.getPointCards();
    }
}
