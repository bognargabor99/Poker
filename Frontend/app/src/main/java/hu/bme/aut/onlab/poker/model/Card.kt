package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val value: Int,
    val suit: Suit
)
