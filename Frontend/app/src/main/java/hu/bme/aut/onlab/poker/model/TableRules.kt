package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
data class TableRules(
    val isOpen: Boolean,
    val playerCount: Int,
    val bigBlindStartingAmount: Int,
    val doubleBlindsAfterTurnCount: Int,
    val playerStartingStack: Int
)
