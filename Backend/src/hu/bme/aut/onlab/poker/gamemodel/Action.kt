package hu.bme.aut.onlab.poker.gamemodel

import kotlinx.serialization.Serializable

@Serializable
data class Action(
    var type: ActionType,
    val amount: Int
)