package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Hand(
    val type: HandType,
    val values: List<Int>
) : Parcelable