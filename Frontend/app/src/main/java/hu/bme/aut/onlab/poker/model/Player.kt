package hu.bme.aut.onlab.poker.model

/**
 * Represents a player at the table. Stores the handed cards and can put money into the pot
 * @author Bognar, Gabor Bela
 */
data class Player(
    val userName: String,
    var chipStack: Int,
    var inPotThisRound: Int,
    var isInTurn: Boolean
)