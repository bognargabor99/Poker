package hu.bme.aut.onlab.poker.gamemodel

class Deck {
    private val cards: MutableList<Card> = mutableListOf()
    private val usedCards: MutableList<Card> = mutableListOf()

    init {
        for (i in 2..14)
            for (j in Suit.values())
                cards.add(Card(i, j))
    }

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