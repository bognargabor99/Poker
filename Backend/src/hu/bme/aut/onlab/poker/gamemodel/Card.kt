package hu.bme.aut.onlab.poker.gamemodel

data class Card(
    val value: Int,
    val suit: Suit
) : Comparable<Card> {
    init {
        check(value in 2..14)
    }

    override fun compareTo(other: Card): Int = this.value.compareTo(other.value)
}
