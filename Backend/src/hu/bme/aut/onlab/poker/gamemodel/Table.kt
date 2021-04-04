package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.dto.PlayerDto
import hu.bme.aut.onlab.poker.dto.TurnEndMsgPlayerDto
import hu.bme.aut.onlab.poker.network.ActionIncomingMessage
import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import hu.bme.aut.onlab.poker.network.UserCollection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

//TODO("All-in except one edge-case")
class Table(private val rules: TableRules) : PokerActionListener{
    val id: Int = lastTableId.getAndIncrement()
    private var bigBlindAmount = rules.bigBlindStartingAmount
    private val players: MutableList<Player> = mutableListOf()
    var isStarted: Boolean = false
    private var turnCount = 0
    private val deck = Deck()
    private var turnState = TurnState.PREFLOP
    private var pot = 0

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
            this@Table.pot += inPot
        }
        players[players.size - 2].apply {
            inPot = minOf(bigBlindAmount / 2, this.chipStack)
            inPotThisRound = inPot
            chipStack -= inPot
            this@Table.pot += inPot
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
        val toRemove = nextPlayerId
        if (playersInTurn.size > 2)
            setNextPlayer()

        playersInTurn.removeIf { it == toRemove }
        players.single { it.id == toRemove }.isInTurn = false
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
     * (so if re-raise, then not just the re-raise amount)
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

    private fun eliminatePlayers() {
        TODO("Player elimination")
    }

    private fun showdown() {
        val handsOfPlayers: MutableList<Pair<Int, Hand>> = mutableListOf()
        playersInTurn.forEach {
            val cardsToUse = cardsOnTable
            cardsToUse.addAll(players.single { p -> p.id == it }.inHandCards)
            val handOfPlayer = evaluateHand(cardsToUse)
            handsOfPlayers.add(Pair(it, handOfPlayer))
        }
        val winnings = getWinners(handsOfPlayers)

        val playerOrder: MutableList<TurnEndMsgPlayerDto> = mutableListOf()
        winnings.forEach { win ->
            players.single { p -> p.id == win.first }.apply {
                playerOrder.add(TurnEndMsgPlayerDto(
                    this.userName,
                    this.inHandCards,
                    handsOfPlayers.single { it.first == this.id }.second,
                    win.second))
                chipStack += win.second
            }
        }

        val turnEndMessage = TurnEndMessage(
            this.id,
            cardsOnTable,
            playerOrder
        )
        players.forEach {
            UserCollection.sendToClient(it.userName, Json.encodeToString(turnEndMessage))
        }

        eliminatePlayers()
        newTurn()
    }

    // Returns Pairs of <playerId, chipsWon>
    private fun getWinners(hands: MutableList<Pair<Int, Hand>>): List<Pair<Int, Int>> {
        hands.sortBy { it.second }

        val winningList: MutableList<Pair<Int, Int>> = mutableListOf()

        while (pot != 0) { // loop until there are no side-pots
            val winnerCount = getWinnerCount(hands)
            if (!anyWinnerBetZero(hands.take(winnerCount).map { it.first })) {
                val (maxBetOfWinners, winnerBetSum) = getMaxAndSumBetOfWinners(hands.take(winnerCount).map { it.first })
                val sidePot = players.map { it.inPot }.sumOf { min(it, maxBetOfWinners) }

                repeat(winnerCount) {
                    val winningOfPlayer: Int = (players.single { it.id == hands.first().first }.inPot / winnerBetSum) * sidePot
                    winningList.add(Pair(hands.first().first, winningOfPlayer))
                    hands.removeFirstOrNull()
                }

                players.forEach { it.inPot -= min(it.inPot, maxBetOfWinners) }
                pot -= sidePot
            }
            else {
                hands.forEach {
                    val winAmount = players.single {p->p.id==it.first}.inPot
                    winningList.add(Pair(it.first, winAmount))
                    pot -= winAmount
                }
            }
        }
        return winningList
    }

    private fun anyWinnerBetZero(playerIds: List<Int>): Boolean =
        players.filter { playerIds.contains(it.id) }.any { it.inPot == 0 }

    private fun getMaxAndSumBetOfWinners(playerIds: List<Int>): Pair<Int, Int> {
        /*
        return Pair(players.filter { playerIds.contains(it.id) }.map { it.inPot }.maxOrNull() ?: 0,
            players.filter { playerIds.contains(it.id) }.map { it.inPot }.sum())

         */
        var sum = 0
        var max = 0
        players.filter { playerIds.contains(it.id) }.map { it.inPot }.forEach {
            sum += it
            if (it > max)
                max = it
        }
        return Pair(max, sum)
    }

    private fun getWinnerCount(hands: MutableList<Pair<Int, Hand>>): Int {
        var winnerCount = 1
        if (hands.size == 1)
            return 1
        for (i in 1 until hands.size)
            if (hands.first().second == hands[i].second)
                winnerCount++
            else
                return winnerCount
        return winnerCount
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
        var winnerName: String
        players.single { it.id == playersInTurn.first() }.apply {
            chipStack += pot
            winnerName = userName
        }
        val turnEndMsg = TurnEndMessage(id, cardsOnTable, listOf(TurnEndMsgPlayerDto(winnerName, null, null, pot)))

        players.forEach {
            UserCollection.sendToClient(it.userName, Json.encodeToString(turnEndMsg))
        }
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
        if (turnState != TurnState.AFTER_RIVER) {
            turnState++
            nextPlayerId = playersInTurn.last()
            setNextPlayer()
            spreadGameState()
        }
    }

    private fun evaluateHand(fromCards: MutableList<Card>) = HandEvaluator.evaluateHand(fromCards)

    fun isOpen() = rules.isOpen

    private fun Player.toDto() = PlayerDto(
        this.userName,
        this.chipStack,
        this.inPotThisRound,
        this.isInTurn
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