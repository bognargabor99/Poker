package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
data class Hand(
    val type: HandType,
    val values: List<Int>
)