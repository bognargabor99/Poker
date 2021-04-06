package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.dto.PlayerDto
import hu.bme.aut.onlab.poker.dto.TurnEndMsgPlayerDto
import hu.bme.aut.onlab.poker.network.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

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
        cardsOnTable.clear()
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
            allinExceptOne() -> {
                nextPlayerId = 0
                spreadGameState()
                fastForwardTurn()
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
        players.single { it.id == nextPlayerId }.actedThisRound = true
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
        players.single { it.id == nextPlayerId }.apply {
            putInPot(maxRaiseThisRound)
            actedThisRound = true
        }
        when {
            allinExceptOne() -> {
                nextPlayerId = 0
                spreadGameState()
                fastForwardTurn()
            }
            isEndOfRound() -> {
                nextPlayerId = 0
                spreadGameState()
                nextRound()
            }
            else -> {
                setNextPlayer()
                spreadGameState()
            }
        }
    }

    /*
     * @param amount The amount of chips the player has put in the pot this round
     * (so if re-raise, then not just the re-raise amount)
    */
    override fun onRaise(amount: Int) {
        maxRaiseThisRound = amount
        players.single { it.id == nextPlayerId }.apply {
            putInPot(amount)
            actedThisRound = true
        }
        if (allinExceptOne()) {
            nextPlayerId = 0
            spreadGameState()
            fastForwardTurn()
        }
        else {
            setNextPlayer()
            spreadGameState()
        }
    }

    private fun fastForwardTurn() {
        cardsOnTable.addAll(deck.getCards(5 - cardsOnTable.size))
        nextPlayerId=0
        spreadGameState()
        showdown()
        eliminatePlayers()
        newTurn()
    }

    private fun allinExceptOne(): Boolean {
        var noAllInCount = 0
        players.filter { playersInTurn.contains(it.id) }
            .forEach {
                if (it.chipStack > 0) {
                    if (it.inPotThisRound < maxRaiseThisRound)
                        return false
                    noAllInCount++
                }
            }
        return noAllInCount <= 1
    }

    private fun handCardsToPlayers() {
        with(deck) {
            reset()
            shuffle()
        }
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
            0,
            mutableListOf(),
            players.singleOrNull { it.id == nextPlayerId }?.userName ?: "",
            previousAction
        )

        players.forEach {
            gameStateMessage.receiverPID = it.id
            gameStateMessage.receiverCards.clear()
            gameStateMessage.receiverCards.addAll(it.inHandCards)
            UserCollection.sendToClient(it.userName, Json.encodeToString(gameStateMessage), GameStateMessage.MESSAGE_CODE)
        }
    }

    private fun setNextPlayer() {
        var thisPlayerIndex = playersInTurn.indexOf(nextPlayerId)
        val done = false
        while (!done) {
            thisPlayerIndex = (thisPlayerIndex+1) % playersInTurn.size
            // check if the next player went all-in
            if (players.single { it.id == playersInTurn[thisPlayerIndex] }.run { chipStack != 0 }) {
                nextPlayerId = playersInTurn[thisPlayerIndex]
                return
            }
        }
    }

    private fun eliminatePlayers() {
        val toEliminate = players.filter { it.chipStack == 0 }
            .map { it.userName }
        players.removeIf { it.chipStack == 0 }
        UserCollection.eliminateFromTable(id, toEliminate)
        if (players.size <= 1)
            declareWinner()
    }

    private fun declareWinner() {
        players.first().apply {
            UserCollection.sendToClient(userName, Json.encodeToString(WinnerAnnouncerMessage(this@Table.id)), WinnerAnnouncerMessage.MESSAGE_CODE)
        }
        Game.closeTable(id)
    }

    fun playerDisconnected(name: String) {
        val index = players.indexOf(players.single { it.userName == name })
        if (players.size > 2) {
            if (players[index].id == nextPlayerId) {
                nextPlayerId = previousAction?.playerId ?: playersInTurn.last()
                setNextPlayer()
            }
            playersInTurn.remove(players[index].id)
            players.removeAt(index)
            players.forEach {
                UserCollection.sendToClient(it.userName, Json.encodeToString(DisconnectedPlayerMessage(id, name)), DisconnectedPlayerMessage.MESSAGE_CODE)
            }
            if (isEndOfRound()){
                nextPlayerId = 0
                spreadGameState()
                nextRound()
            }
            else
                spreadGameState()
        } else {
            players.removeAt(index)
            UserCollection.sendToClient(players.first().userName, Json.encodeToString(DisconnectedPlayerMessage(id, name)), DisconnectedPlayerMessage.MESSAGE_CODE)
            declareWinner()
        }
    }

    private fun showdown() {
        pot = players.map { it.inPot }.sum()
        val handsOfPlayers: MutableList<Pair<Int, Hand>> = mutableListOf()
        playersInTurn.forEach {
            val cardsToUse = mutableListOf<Card>()
            cardsToUse.addAll(cardsOnTable)
            cardsToUse.addAll(players.single { p -> p.id == it }.inHandCards)
            val handOfPlayer = evaluateHand(cardsToUse)
            handsOfPlayers.add(Pair(it, handOfPlayer))
        }
        val winnings = getWinners(MutableList(handsOfPlayers.size) { handsOfPlayers[it]})

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
            UserCollection.sendToClient(it.userName, Json.encodeToString(turnEndMessage), TurnEndMessage.MESSAGE_CODE)
        }
    }

    // Parameter: Pairs of <playerId, handOfPlayer>
    // Returns Pairs of <playerId, chipsWon>
    private fun getWinners(hands: MutableList<Pair<Int, Hand>>): List<Pair<Int, Int>> {
        hands.sortBy { it.second }

        val winningList: MutableList<Pair<Int, Int>> = mutableListOf()

        while (pot != 0) { // loop until there are no side-pots
            hands.removeIf { players.single { p -> p.id == it.first }.inPot == 0 }
            val winnerCount = getWinnerCount(hands)
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
        return winningList
    }

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
        if (actionMsg.playerId != nextPlayerId) // if action not from next player
            return false
        if (actionMsg.action.type == ActionType.CHECK && maxRaiseThisRound != players.single { it.id == nextPlayerId }.inPotThisRound) // if checked but has to fold/raise
            return false
        if (actionMsg.action.type == ActionType.RAISE) {
            if ((actionMsg.action.amount - maxRaiseThisRound < bigBlindAmount) && actionMsg.action.amount == players.single { it.id == nextPlayerId }.chipStack)
                return true
            if (players.single { it.id == nextPlayerId }.chipStack < actionMsg.action.amount)
                return false
        }
        return true
    }

    private fun oneLeft() {
        var winnerName: String
        pot = players.map { it.inPot }.sum()
        players.single { it.id == playersInTurn.first() }.apply {
            chipStack += pot
            winnerName = userName
        }
        val turnEndMsg = TurnEndMessage(id, cardsOnTable, listOf(TurnEndMsgPlayerDto(winnerName, null, null, pot)))

        players.forEach {
            UserCollection.sendToClient(it.userName, Json.encodeToString(turnEndMsg), TurnEndMessage.MESSAGE_CODE)
        }
        newTurn()
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
            TurnState.AFTER_RIVER -> {
                showdown()
                eliminatePlayers()
                newTurn()
                return
            }
            else -> cardsOnTable.addAll(deck.getCards(1))
        }
        turnState++
        nextPlayerId = playersInTurn.last()
        setNextPlayer()
        spreadGameState()
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