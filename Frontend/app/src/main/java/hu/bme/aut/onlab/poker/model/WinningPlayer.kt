package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
data class WinningPlayer(
        val userName: String,
        val inHandCards: List<Card>?,
        val hand: Hand?,
        val winAmount: Int
)