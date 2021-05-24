package hu.bme.aut.onlab.poker.gamemodel

import kotlinx.serialization.Serializable

@Serializable
data class TableRules(
    val isOpen: Boolean,
    val playerCount: Int,
    val bigBlindStartingAmount: Int,
    val doubleBlindsAfterTurnCount: Int,
    val playerStartingStack: Int,
    val isRoyal: Boolean
) {
    init {
        require(playerCount >= 2)
    }
}
