package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.dto.PlayerDto
import hu.bme.aut.onlab.poker.network.ActionIncomingMessage
import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.UserCollection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

//TODO("SpreadGameState with last Action")
//TODO("OnFold edge-cases")
//TODO("All-in except one edge-case")
class Table(private val rules: TableRules) : PokerActionListener{
    val id: Int = lastTableId.getAndIncrement()
    private var bigBlindAmount = rules.bigBlindStartingAmount
    private val players: MutableList<Player> = mutableListOf()
    var isStarted: Boolean = false
    private var turnCount = 0
    private val deck = Deck()
    private var turnState = TurnState.PREFLOP

    private val playersInTurn: MutableList<Int> = mutableListOf()
    private var nextPlayerId: Int = -1
    private var maxRaiseThisRound: Int = bigBlindAmount
    private val cardsOnTable: MutableList<Card> = mutableListOf()
    private var previousAction: ActionIncomingMessage? = null

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
        UserCollection.notifyGameStarted(id, usersInTable)
        newTurn()
    }

    private fun newTurn() {
        if (turnCount % rules.doubleBlindsAfterTurnCount == 0)
            bigBlindAmount*=2
        maxRaiseThisRound = bigBlindAmount
        turnCount++
        turnState = TurnState.PREFLOP
        players.add(0, players.removeLast())
        players.forEach { it.newTurn() }
        playersInTurn.clear()
        playersInTurn.addAll(players.map { it.id })
        handCardsToPlayers()
        players[players.size - 1].apply {
            inPot = minOf(bigBlindAmount, this.chipStack)
            inPotThisRound = inPot
            chipStack -= inPot
        }
        players[players.size - 2].apply {
            inPot = minOf(bigBlindAmount / 2, this.chipStack)
            inPotThisRound = inPot
            chipStack -= inPot
        }
        previousAction = null
        nextPlayerId = playersInTurn.first()
        spreadGameState()
    }

    override fun onAction(actionMsg: ActionIncomingMessage) {
        if (!validateAction(actionMsg))
            return

        previousAction = actionMsg
        when (actionMsg.action.type) {
            ActionType.FOLD -> { onFold() }
            ActionType.CHECK -> { onCheck() }
            ActionType.CALL -> { onCall() }
            ActionType.RAISE -> { onRaise(actionMsg.action.amount) }
        }
    }

    override fun onFold() {
        players.single { id == nextPlayerId }.actedThisRound = true
        val toRemove = nextPlayerId
        if (playersInTurn.size > 2)
            setNextPlayer()

        playersInTurn.removeIf { it == toRemove }

        when {
            isOneLeftInTurn() -> {
                nextPlayerId = 0
                spreadGameState()
                oneLeft()
            }
            isEndOfRound() -> {
                nextPlayerId = 0
                spreadGameState()
                nextRound()
            }
            else -> spreadGameState()
        }
    }

    override fun onCheck() {
        players.single { id == nextPlayerId }.actedThisRound = true
        if (isEndOfRound()) {
            nextPlayerId = 0
            spreadGameState()
            nextRound()
        }
        else {
            setNextPlayer()
            spreadGameState()
        }
    }

    override fun onCall() {
        players.single { id == nextPlayerId }.apply {
            putInPot(maxRaiseThisRound)
            actedThisRound = true
        }
        if (isEndOfRound()) {
            nextPlayerId = 0
            spreadGameState()
            nextRound()
        }
        else {
            setNextPlayer()
            spreadGameState()
        }
    }

    /*
     * @param amount The amount of chips the player has put in the pot this round
    */
    override fun onRaise(amount: Int) {
        maxRaiseThisRound = amount
        players.single { id == nextPlayerId }.apply {
            putInPot(amount)
            actedThisRound = true
        }
        setNextPlayer()
        spreadGameState()
    }

    private fun handCardsToPlayers() {
        deck.shuffle()
        players.forEach {
            it.handCards(deck.getCards(2))
        }
    }

    private fun spreadGameState() {
        val playerDtos: List<PlayerDto> = List(players.size) { i -> players[i].toDto() }
        val gameStateMessage = GameStateMessage(
            id,
            cardsOnTable,
            playerDtos,
            mutableListOf(),
            players.find { id == nextPlayerId }?.userName ?: "",
            previousAction
        )

        players.forEach {
            gameStateMessage.receiverCards.clear()
            gameStateMessage.receiverCards.addAll(it.inHandCards)
            UserCollection.sendToClient(it.userName, Json.encodeToString(gameStateMessage))
        }
    }

    private fun setNextPlayer() {
        var thisPlayerIndex = playersInTurn.indexOf(nextPlayerId)
        val done = false
        while (!done) {
            thisPlayerIndex = (thisPlayerIndex+1) % playersInTurn.size
            // check if the next player went all-in
            if (players.single { id == playersInTurn[thisPlayerIndex] }.run { chipStack != 0 }) {
                nextPlayerId = playersInTurn[thisPlayerIndex]
                return
            }
        }
    }

    private fun eliminatePlayer(playerId: Int) {
        TODO("Player elimination")
    }

    private fun showdown() {
        TODO("Showdown at the end of the turn, determine winner(s).")
    }

    private fun validateAction(actionMsg: ActionIncomingMessage): Boolean {
        if (actionMsg.playerId != nextPlayerId)
            return false
        if (actionMsg.action.type == ActionType.CHECK && maxRaiseThisRound != players.single { it.id == nextPlayerId }.inPotThisRound)
            return false
        if (actionMsg.action.type == ActionType.RAISE && actionMsg.action.amount - maxRaiseThisRound < bigBlindAmount)
            if (players.single { it.id == actionMsg.playerId }.chipStack < actionMsg.action.amount)
                return false
        return true
    }

    private fun oneLeft() {
        var winAmount = 0
        players.forEach {
            winAmount += it.inPot
        }
        players.single { it.id == playersInTurn.first() }
            .chipStack += winAmount
        TODO("Send turnEnd message, with winner and amount")
    }

    private fun isOneLeftInTurn(): Boolean = playersInTurn.size == 1

    private fun isEndOfRound(): Boolean =
        players.filter { playersInTurn.contains(it.id) }
            .all { it.actedThisRound && (it.inPotThisRound == maxRaiseThisRound || it.chipStack == 0) }

    private fun nextRound() {
        maxRaiseThisRound = 0
        players.forEach { it.nextRound() }
        previousAction = null
        when (turnState) {
            TurnState.PREFLOP -> cardsOnTable.addAll(deck.getCards(3))
            TurnState.AFTER_RIVER -> showdown()
            else -> cardsOnTable.addAll(deck.getCards(1))
        }
        turnState++
        nextPlayerId = playersInTurn.last()
        setNextPlayer()
        spreadGameState()
    }

    fun evaluateHand(fromCards: MutableList<Card>) = HandEvaluator.evaluateHand(fromCards)

    fun isOpen() = rules.isOpen

    private fun Player.toDto() = PlayerDto(
        this.userName,
        this.chipStack,
        this.inPotThisRound
    )

    companion object {
        var lastTableId = AtomicInteger(100)
    }
}

interface PokerActionListener {
    fun onAction(actionMsg: ActionIncomingMessage)
    fun onFold()
    fun onCheck()
    fun onCall()
    fun onRaise(amount: Int)
}