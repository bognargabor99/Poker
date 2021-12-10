package hu.bme.aut.thesis.poker.gamemodel

/**
 * Represents a traditional card in the game
 * @property value Value of the card
 * @property suit Suit of the card
 * @author Bognar, Gabor Bela
 */
data class Card(
    val value: Int,
    val suit: Suit
) : Comparable<Card> {
    init {
        check(value in 2..14)
    }

    override fun compareTo(other: Card): Int {
        val byValue = this.value.compareTo(other.value)
        if (byValue != 0)
            return byValue

        return this.suit.compareTo(other.suit)
    }
}
