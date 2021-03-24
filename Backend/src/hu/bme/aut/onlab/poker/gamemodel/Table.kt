package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.network.UserCollection
import java.util.concurrent.atomic.AtomicInteger

class Table(private val rules: TableRules) : PokerActionListener{
    val id: Int = lastTableId.getAndIncrement()
    var bigBlindAmount = rules.bigBlindStartingAmount
    private val players: MutableList<Player> = mutableListOf()
    var isStarted: Boolean = false
    var turnCount = 0
    private val deck = Deck()

    var turnState = TurnState.PREFLOP
    private val playersInTurn: MutableList<Int> = mutableListOf()
    var nextPlayerId: Int = -1
    var maxRaiseThisRound: Int = 0


    fun addPlayer(userName: String): Boolean =
        if (!isStarted && players.size < rules.playerCount) {
            val newPlayer = Player(rules.playerStartingStack)
            newPlayer.userName = userName
            players.add(newPlayer)
            if (players.size == rules.playerCount)
                startGame()
            true
        } else
            false

    private fun startGame() {
        isStarted = true
        val usersInTable = players.map { it.userName }
        UserCollection.notifyGameStarted(usersInTable)
        newTurn()
    }

    private fun newTurn() {
        if (turnCount % rules.doubleBlindsAfterTurnCount == 0)
            bigBlindAmount*=2
        turnCount++
        turnState = TurnState.PREFLOP
        players.add(0, players.removeLast())
        playersInTurn.clear()
        playersInTurn.addAll(players.map { it.id })
        handCards()
        players[players.size - 1].inPot = bigBlindAmount
        players[players.size - 2].inPot = bigBlindAmount / 2
        nextPlayerId = playersInTurn.first()
        players[0].askForAction(bigBlindAmount - players[0].inPot)
    }

    override fun onAction(playerId: Int, action: Action) {
        if (!validateAction(playerId, action))
            return

        when (action.type) {
            ActionType.FOLD -> { onFold() }
            ActionType.CHECK -> { onCheck() }
            ActionType.CALL -> { onCall() }
            ActionType.RAISE -> { onRaise(action.amount) }
        }
    }

    override fun onFold() {
        TODO("Not yet implemented")
    }

    override fun onCheck() {
        TODO("Not yet implemented")
    }

    override fun onCall() {
        TODO("Not yet implemented")
    }

    override fun onRaise(amount: Int) {
        TODO("Not yet implemented")
    }

    private fun handCards() {
        deck.shuffle()
        players.forEach {
            it.handCards(deck.getCards(2))
        }
    }

    fun eliminatePlayer(playerId: Int) {
        TODO("Player elimination")
    }

    fun showdown() {
        TODO("Showdown at the end of the turn, determine winner(s).")
    }

    private fun validateAction(playerId: Int, action: Action): Boolean {
        if (playerId != nextPlayerId)
            return false
        if (action.type == ActionType.CHECK && maxRaiseThisRound != 0)
            return false
        if (action.type == ActionType.RAISE && action.amount - maxRaiseThisRound < bigBlindAmount)
            if (players.single { it.id == playerId }.chipStack < action.amount)
                return false
        return true
    }

    fun evaluateHand(fromCards: MutableList<Card>) = HandEvaluator.evaluateHand(fromCards)

    fun isOpen() = rules.isOpen

    companion object {
        var lastTableId = AtomicInteger(100)
    }
}

interface PokerActionListener {
    fun onAction(playerId: Int, action: Action)
    fun onFold()
    fun onCheck()
    fun onCall()
    fun onRaise(amount: Int)
}