package hu.bme.aut.onlab.poker.model

/**
 * Represents an Action at the table
 * @author Bognar, Gabor Bela
 */
data class Action(
    var type: ActionType,
    val amount: Int
)