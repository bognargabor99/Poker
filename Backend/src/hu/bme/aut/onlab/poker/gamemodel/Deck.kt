package hu.bme.aut.onlab.poker.gamemodel

sealed class Deck {
    protected val cards: MutableList<Card> = mutableListOf()
    private val usedCards: MutableList<Card> = mutableListOf()

    fun shuffle() = cards.shuffle()

    fun getCards(amount: Int) : List<Card> {
        val ret = cards.take(amount)
        cards.removeAll(ret)
        usedCards.addAll(ret)
        return ret
    }

    fun reset() {
        cards.addAll(usedCards)
        usedCards.clear()
    }
}

class TraditionalDeck : Deck() {
    init {
        for (i in 2..14)
            for (j in Suit.values())
                cards.add(Card(i, j))
    }
}

class RoyalDeck : Deck() {
    init {
        for (i in 10..14)
            for (j in Suit.values())
                cards.add(Card(i, j))
    }
}