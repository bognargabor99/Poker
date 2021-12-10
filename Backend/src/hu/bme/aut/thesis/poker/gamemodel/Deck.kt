package hu.bme.aut.thesis.poker.gamemodel

/**
 * A class that represents the deck of cards in the game.
 * It can shuffle cards and you can fetch a random one
 * @author Bognar, Gabor Bela
 */
sealed class Deck {
    protected val cards: MutableList<Card> = mutableListOf()
    private val usedCards: MutableList<Card> = mutableListOf()

    /**
     * Shuffles the deck
     * @author Bognar, Gabor Bela
     */
    fun shuffle() = cards.shuffle()

    /**
     * Fetches [Card]s from the [Deck]
     * @param amount The amount of cards to fetch
     * @return List of the fetched cards. Returns less than the [amount] if there are less cards in the [Deck]
     * @author Bognar, Gabor Bela
     */
    fun getCards(amount: Int) : List<Card> {
        val ret = cards.take(amount)
        cards.removeAll(ret)
        usedCards.addAll(ret)
        return ret
    }

    /**
     * Fills the deck with the missing cards
     * @author Bognar, Gabor Bela
     */
    fun reset() {
        cards.addAll(usedCards)
        usedCards.clear()
    }
}

/**
 * Represents a traditional deck containing 52 [Card]s
 * @author Bognar, Gabor Bela
 */
class TraditionalDeck : Deck() {
    init {
        for (i in 2..14)
            for (j in Suit.values())
                cards.add(Card(i, j))
    }
}

/**
 * Represents a royal poker deck containing 20 [Card]s
 * (only 10, J, Q, K, A)
 * @author Bognar, Gabor Bela
 */
class RoyalDeck : Deck() {
    init {
        for (i in 10..14)
            for (j in Suit.values())
                cards.add(Card(i, j))
    }
}