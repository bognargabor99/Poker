package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.network.UserCollection
import java.util.concurrent.atomic.AtomicInteger

class Player(
    val table: Table,
    startingStack: Int
) {
    val id = lastTableId.getAndIncrement()
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0
    lateinit var userName: String

    suspend fun askForAction(toCall: Int) {
        UserCollection.askForAction(userName, toCall)
    }

    fun handCard(card: Card) {
        inHandCards.add(card)
    }

    companion object {
        var lastTableId = AtomicInteger(1000)
    }
}