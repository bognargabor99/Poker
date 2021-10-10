package hu.bme.aut.onlab.poker.gamemodel

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
