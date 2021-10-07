package hu.bme.aut.onlab.poker.gamemodel

class Player(
    startingStack: Int
) : Person() {
    var inHandCards: MutableList<Card> = mutableListOf()
    var chipStack: Int = startingStack
    var inPot: Int = 0
    var inPotThisRound: Int = 0
    var actedThisRound = false
    var isInTurn = false

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
}