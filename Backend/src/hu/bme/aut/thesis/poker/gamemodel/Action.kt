package hu.bme.aut.thesis.poker.gamemodel

/**
 * Represents an Action at the [Table]
 * @author Bognar, Gabor Bela
 */
data class Action(
    var type: ActionType,
    val amount: Int
)