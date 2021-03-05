package hu.bme.aut.onlab.poker.gamemodel

import java.util.concurrent.atomic.AtomicInteger

class Player(
    val table: Table,
    startingStack: Int
) {
    val id = lastTableId.getAndIncrement()
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0

    fun getAction() {
        TODO()
    }

    fun handCard(card: Card) {
        inHandCards.add(card)
    }

    companion object {
        var lastTableId = AtomicInteger(1000)
    }
}