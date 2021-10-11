package hu.bme.aut.onlab.poker.model

data class Player(
    val userName: String,
    var chipStack: Int,
    var inPotThisRound: Int,
    var isInTurn: Boolean
)