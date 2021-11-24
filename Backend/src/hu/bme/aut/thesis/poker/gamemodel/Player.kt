package hu.bme.aut.thesis.poker.gamemodel

import kotlin.math.max

class Player(
    startingStack: Int
) : Person() {
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0
    var inPotThisRound: Int = 0
    var actedThisRound = false
    var isInTurn = false
    var stats = Statistics()

    fun handCards(cards: List<Card>) {
        inHandCards.clear()
        inHandCards.addAll(cards)
        stats.allHands++
    }

    fun newTurn() {
        inPot = 0
        inPotThisRound = 0
        actedThisRound = false
        isInTurn = true
    }

    fun nextRound(newState: TurnState) {
        inPotThisRound = 0
        actedThisRound = false
        if (isInTurn)
            when (newState) {
                TurnState.AFTER_FLOP -> stats.flopsSeen++
                TurnState.AFTER_TURN -> stats.turnsSeen++
                TurnState.AFTER_RIVER -> stats.riversSeen++
                else -> { }
            }
    }

    fun fold() {
        isInTurn = false
    }

    fun putInPot(amount: Int) {
        var toPut = amount
        if (toPut > chipStack + inPotThisRound)
            toPut = chipStack + inPotThisRound

        (toPut - inPotThisRound).also {
            inPot += it
            chipStack -= it
        }
        inPotThisRound = toPut
    }

    fun potWon(size: Int, bustedCount: Int) {
        chipStack += size
        stats.apply {
            totalChipsWon += size
            biggestPotWon = max(stats.biggestPotWon, size)
            playersBusted += bustedCount
        }
    }
}