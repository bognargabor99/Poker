package hu.bme.aut.thesis.poker.gamemodel

data class Action(
    var type: ActionType,
    val amount: Int
)