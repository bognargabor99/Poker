package hu.bme.aut.onlab.poker.gamemodel

import java.util.concurrent.atomic.AtomicInteger

class Table(private val rules: TableRules) : PokerActionListener{
    val id: Int = lastTableId.getAndIncrement()
    private val players: MutableList<Player> = mutableListOf()
    var isStarted: Boolean = false
    var bigBlindAmount = rules.bigBlindStartingAmount
    var turnCount = 0

    fun addPlayer(userName: String): Boolean = if (!isStarted && players.size < rules.playerCount) {
        val newPlayer = Player(this, rules.playerStartingStack)
        newPlayer.userName = userName
        players.add(newPlayer)
        true
    } else
        false

    fun startGame() {
        isStarted = true
    }

    fun gameLoop() {

    }

    fun newTurn() {
        if (turnCount % rules.doubleBlindsAfterTurnCount == 0)
            bigBlindAmount*=2
        turnCount++
        TODO("Start new turn")
    }

    fun endTurn() {
        TODO("End turn")
    }

    fun eliminatePlayer(playerId: Int) {
        TODO("Player elimination")
    }

    override fun onAction(playerId: Int, action: Action) {
        TODO("Not yet implemented")
    }

    fun declareWinner() {
        TODO("Declare winner, then close the Table")
    }

    fun evaluateHand(fromCards: MutableList<Card>) = HandEvaluator.evaluateHand(fromCards)

    private suspend fun getAction(toCall: Int, player: Player) = player.askForAction(toCall)

    fun isOpen() = rules.isOpen

    companion object {
        var lastTableId = AtomicInteger(100)
    }
}

interface PokerActionListener {
    fun onAction(playerId: Int, action: Action)
}