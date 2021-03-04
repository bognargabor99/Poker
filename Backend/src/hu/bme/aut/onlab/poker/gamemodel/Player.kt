package hu.bme.aut.onlab.poker.gamemodel

class Player(
    val id: Int,
    val table: Table,
    startingStack: Int
) {
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0


}