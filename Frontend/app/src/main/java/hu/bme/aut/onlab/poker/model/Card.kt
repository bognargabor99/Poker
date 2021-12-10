package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a traditional card in the game
 * @property value Value of the card
 * @property suit Suit of the card
 * @author Bognar, Gabor Bela
 */
@Parcelize
data class Card(
    val value: Int,
    val suit: Suit
) : Parcelable, Comparable<Card> {
    init {
        check(value in 2..14)
    }

    override fun compareTo(other: Card): Int = this.value.compareTo(other.value)
}
