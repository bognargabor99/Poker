package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a poker hand with [HandType] and corresponding values
 * @property type Type of the hand
 * @property values Corresponding values of the hand
 * @author Bognar, Gabor Bela
 */
@Parcelize
data class Hand(
    val type: HandType,
    val values: List<Int>
) : Parcelable, Comparable<Hand> {
    override fun compareTo(other: Hand): Int {
        if (type.compareTo(other.type)!=0)
            return type.compareTo(other.type)
        else
            for (i in values.indices)
                if (values[i]!=other.values[i])
                    return -values[i].compareTo(other.values[i])
        return 0
    }
}