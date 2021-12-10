package hu.bme.aut.thesis.poker.gamemodel

import kotlin.math.max

/**
 * Represents a player at the [Table]. Stores the handed cards and can put money into the pot
 * @author Bognar, Gabor Bela
 */
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

    /**
     * Sets the [Card]s in the hand of the [Player]
     * @author Bognar, Gabor Bela
     */
    fun handCards(cards: List<Card>) {
        inHandCards.clear()
        inHandCards.addAll(cards)
        stats.allHands++
    }

    /**
     * Resets some properties to start a new turn
     * @author Bognar, Gabor Bela
     */
    fun newTurn() {
        inPot = 0
        inPotThisRound = 0
        actedThisRound = false
        isInTurn = true
    }

    /**
     * Resets some properties to start a new round
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Folds the [Card]s of the [Table]
     * @author Bognar, Gabor Bela
     */
    fun fold() {
        isInTurn = false
    }

    /**
     * Puts chips the amount of chips into the pot,
     * or less if it does not have enough.
     * @param amount The amount of chips to put in the pot.
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Increases the pot with an [size] of chips.
     * @param size Amount of chips
     * @param bustedCount Number of players busted while winning the turn
     * @author Bognar, Gabor Bela
     */
    fun potWon(size: Int, bustedCount: Int) {
        chipStack += size
        stats.apply {
            totalChipsWon += size
            biggestPotWon = max(stats.biggestPotWon, size)
            playersBusted += bustedCount
        }
    }
}