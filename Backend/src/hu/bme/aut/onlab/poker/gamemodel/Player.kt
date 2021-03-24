package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.network.UserCollection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class Player(
    startingStack: Int
) {
    val id = lastTableId.getAndIncrement()
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0
    lateinit var userName: String

    fun askForAction(toCall: Int) =
        GlobalScope.launch {
            UserCollection.askForAction(userName, toCall)
        }

    fun handCards(cards: List<Card>) {
        inHandCards.clear()
        inHandCards.addAll(cards)
    }

    companion object {
        var lastTableId = AtomicInteger(1000)
    }
}