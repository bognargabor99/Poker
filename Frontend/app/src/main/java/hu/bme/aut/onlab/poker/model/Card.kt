package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Card(
    val value: Int,
    val suit: Suit
) : Parcelable, Comparable<Card> {
    init {
        check(value in 2..14)
    }

    override fun compareTo(other: Card): Int = this.value.compareTo(other.value)
}
