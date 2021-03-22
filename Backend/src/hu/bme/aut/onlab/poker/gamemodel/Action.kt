package hu.bme.aut.onlab.poker.gamemodel

import kotlinx.serialization.Serializable

@Serializable
data class Action(
    val type: ActionType,
    val amount: Int
)