package hu.bme.aut.onlab.poker.model

class Table {
    val players = mutableListOf<Player>()
    val tableCards = mutableListOf<Card>()
    var pot: Int = 0
}