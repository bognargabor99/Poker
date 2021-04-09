package hu.bme.aut.onlab.poker.gamemodel

import java.util.concurrent.atomic.AtomicInteger

class Player(
    startingStack: Int
) {
    val id = lastTableId.getAndIncrement()
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0
    var inPotThisRound: Int = 0
    var actedThisRound = false
    var isInTurn = false
    lateinit var userName: String

    fun handCards(cards: List<Card>) {
        inHandCards.clear()
        inHandCards.addAll(cards)
    }

    fun newTurn() {
        inPot = 0
        inPotThisRound = 0
        actedThisRound = false
        isInTurn = true
    }

    fun nextRound() {
        inPotThisRound = 0
        actedThisRound = false
    }

    fun putInPot(amount: Int) {
        var toPut = amount
        if (toPut > chipStack + inPotThisRound)
            toPut = chipStack

        (toPut - inPotThisRound).also {
            inPot += it
            chipStack -= it
        }
        inPotThisRound = toPut
    }

    companion object {
        var lastTableId = AtomicInteger(1000)
    }
}