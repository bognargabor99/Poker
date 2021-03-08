package hu.bme.aut.onlab.poker.gamemodel

import java.util.concurrent.atomic.AtomicInteger

class Table(val settings: TableSettings) {
    val id: Int = lastTableId.getAndIncrement()
    private val players: MutableList<Player> = mutableListOf()
    var isStarted: Boolean = false
    var bigBlindAmount = settings.bigBlindStartingAmount
    var turnCount = 0

    fun addPlayer(player: Player) : Boolean = if (!isStarted && players.size < settings.playerCount) {
        players.add(player)
        true
    } else
        false

    fun startGame() {
        isStarted = true
    }

    fun gameLoop() {

    }

    fun newTurn() {
        if (turnCount % settings.doubleBlindsAfterTurnCount == 0)
            bigBlindAmount*=2
        turnCount++
        TODO("Start new turn")
    }

    fun endTurn() {
        TODO("End turn")
    }

    fun evaluateHand(fromCards: MutableList<Card>) = HandEvaluator.evaluateHand(fromCards)

    companion object {
        var lastTableId = AtomicInteger(100)
    }
}