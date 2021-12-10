package hu.bme.aut.thesis.poker.gamemodel

import hu.bme.aut.thesis.poker.dto.*
import hu.bme.aut.thesis.poker.network.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

/**
 * Represents the table. Manages players, cards, the deck and ensures rules and state of the game.
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
class Table(val rules: TableRules) : PokerActionListener{
    private var fastForwarding: Boolean = false
    val id: Int = lastTableId.getAndIncrement()
    private var bigBlindAmount = rules.bigBlindStartingAmount
    private val players: MutableList<Player> = mutableListOf()
    private val spectators: MutableList<Person> = mutableListOf()
    var isStarted: Boolean = false
    private var turnCount = 0
    private val deck: Deck = if (rules.isRoyal) RoyalDeck() else TraditionalDeck()
    private var turnState = TurnState.PREFLOP
    private var pot = 0

    private val playersInTurn: MutableList<Int> = mutableListOf()
    private var nextPlayerId: Int = -1
    private var maxRaiseThisRound: Int = bigBlindAmount
    private val cardsOnTable: MutableList<Card> = mutableListOf()
    private var previousAction: ActionIncomingMessage? = null

    /**
     * Adds a [Player] to the [Table]
     * @param userName Name of the new player
     * @return Successfulness of adding the player
     * @author Bognar, Gabor Bela
     */
    fun addInGamePlayer(userName: String): Boolean =
        if (!isStarted && players.size < rules.playerCount &&
            !players.any { it.userName == userName } &&
            !spectators.any { it.userName == userName }) {
                val newPlayer = Player(rules.playerStartingStack)
                newPlayer.userName = userName
                players.add(newPlayer)
                UserCollection.tableJoined(userName, id, rules)
                if (players.size == rules.playerCount)
                    startGame()
                true
            } else
                false

    /**
     * Adds a spectator to the [Table]
     * @param Name of the new spectator
     * @return Successfulness of adding the spectator
     * @author Bognar, Gabor Bela
     */
    fun addSpectator(userName: String): Boolean {
        if (players.any { it.userName == userName } || spectators.any { it.userName == userName })
            return false
        UserCollection.tableSpectated(userName, id, rules)
        val newSpectator = Person()
        newSpectator.userName = userName
        spectators.add(newSpectator)
        if (isStarted)
            sendFirstMessageToSpectator(newSpectator.userName)
        return true
    }

    /**
     * Sends the initial message to the spectator
     * @author Bognar, Gabor Bela
     */
    private fun sendFirstMessageToSpectator(spectatorName: String) {
        val spectatorMessage = createSpectatorGSMessage()
        UserCollection.sendToClient(spectatorName, spectatorMessage.toJsonString(), SpectatorGameStateMessage.MESSAGE_CODE)
    }

    /**
     * Removes a spectator from the [Table]
     * @param userName Name of the spectator to remove
     * @author Bognar, Gabor Bela
     */
    fun removeSpectator(userName: String): Boolean = spectators.removeIf { it.userName == userName }

    /**
     * Starts the game
     * @author Bognar, Gabor Bela
     */
    private fun startGame() {
        isStarted = true
        val usersInTable = (spectators + players).map { it.userName }
        UserCollection.notifyGameStarted(id, usersInTable)
        newTurn()
    }

    /**
     * Initializes a new turn
     * @author Bognar, Gabor Bela
     */
    private fun newTurn() {
        if (turnCount != 0 && turnCount % rules.doubleBlindsAfterTurnCount == 0)
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

    /**
     * This function should be called when an [Action] is coming.
     * @param actionMsg Information about the action
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Should be called when the current [Player] folds their [Card]s
     * @author Bognar, Gabor Bela
     */
    override fun onFold() {
        val toRemove = nextPlayerId
        if (playersInTurn.size > 2)
            setNextPlayer()

        playersInTurn.removeIf { it == toRemove }
        players.single { it.id == toRemove }.fold()
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

    /**
     * Should be called when the current [Player] checks and the action is validated
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Should be called when the current [Player] calls
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Should be called when the current [Player] checks and the action is validated
     * @param amount The amount of chips the player has put in the pot this round
     * (so if re-raise, then not just the re-raise amount)
     * @author Bognar, Gabor Bela
     */
    override fun onRaise(amount: Int) {
        maxRaiseThisRound = amount
        players.single { it.id == nextPlayerId }.apply {
            stats.raiseCount++
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

    /**
     * Fast-forwards a turn. Only call if fast forwarding if valid (allInExceptOne -> true)
     * @author Bognar, Gabor Bela
     */
    private fun fastForwardTurn() {
        cardsOnTable.addAll(deck.getCards(5 - cardsOnTable.size))
        fastForwarding = true
        nextPlayerId = 0
        while (turnState != TurnState.AFTER_RIVER) {
            turnState++
            playersInTurn.forEach { players.find { player -> player.id == it }?.nextRound(turnState) }
        }
        previousAction = null
        spreadGameState()
        showdown()
        eliminatePlayers()
        if (players.size > 1) {
            newTurn()
        }
    }

    /**
     * Determines if all players are all-in in the turn except one
     * True, if all players are all-in in the turn except one
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Hands new cards to the players
     * @author Bognar, Gabor Bela
     */
    private fun handCardsToPlayers() {
        with(deck) {
            reset()
            shuffle()
        }
        players.forEach {
            it.handCards(deck.getCards(2))
        }
    }

    /**
     * Sends out state messages of the current state to players and spectators
     * @author Bognar, Gabor Bela
     */
    private fun spreadGameState() {
        spreadToPlayers()
        spreadToSpectators()
        fastForwarding = false
    }

    /**
     * Creates [SpectatorGameStateMessage] of the current state
     * @author Bognar, Gabor Bela
     */
    private fun createSpectatorGSMessage(): SpectatorGameStateMessage {
        val playerDtos: List<PlayerToSpectateDto> = List(players.size) { i -> PlayerToSpectateDto(players[i].toDto(), players[i].inHandCards) }
        return SpectatorGameStateMessage(
            tableId = id,
            tableCards = cardsOnTable,
            players = playerDtos,
            maxRaiseThisRound = maxRaiseThisRound,
            nextPlayer = players.singleOrNull { it.id == nextPlayerId }?.userName ?: "",
            turnState = turnState,
            bigBlind = bigBlindAmount,
            pot = players.sumOf { it.inPot },
            lastAction = previousAction
        )
    }

    /**
     * Sends out state messages of the current state to spectators
     * @author Bognar, Gabor Bela
     */
    private fun spreadToSpectators() {
        val spectatorMessage = createSpectatorGSMessage()

        spectators.forEach {
            UserCollection.sendToClient(it.userName, spectatorMessage.toJsonString(), SpectatorGameStateMessage.MESSAGE_CODE)
        }
    }

    /**
     * Sends out state messages of the current state to players
     * @author Bognar, Gabor Bela
     */
    private fun spreadToPlayers() {
        val playerDtos: List<InGamePlayerDto> = List(players.size) { i -> players[i].toDto() }
        val gameStateMessage = GameStateMessage(
            id,
            cardsOnTable,
            playerDtos,
            maxRaiseThisRound,
            mutableListOf(),
            players.singleOrNull { it.id == nextPlayerId }?.userName ?: "",
            turnState,
            bigBlindAmount,
            players.sumOf { it.inPot },
            previousAction
        )

        players.forEach {
            gameStateMessage.receiverCards.clear()
            gameStateMessage.receiverCards.addAll(it.inHandCards)
            UserCollection.sendToClient(it.userName, gameStateMessage.toJsonString(), GameStateMessage.MESSAGE_CODE)
        }
    }

    /**
     * Determines the next player to wait for an interaction from
     * @author Bognar, Gabor Bela
     */
    private fun setNextPlayer() {
        var thisPlayerIndex = playersInTurn.indexOf(nextPlayerId)
        while (true) {
            thisPlayerIndex = (thisPlayerIndex+1) % playersInTurn.size
            // check if the next player went all-in
            if (players.single { it.id == playersInTurn[thisPlayerIndex] }.run { chipStack != 0 }) {
                nextPlayerId = playersInTurn[thisPlayerIndex]
                return
            }
        }
    }

    /**
     * Eliminates player with 0 chipstack
     * @author Bognar, Gabor Bela
     */
    private fun eliminatePlayers() {
        val toEliminate = players.filter { it.chipStack == 0 }
            .map { it.userName }
        val playerIdsToEliminate = players.filter { it.chipStack == 0 }
            .map { it.id }
        removePlayer(*playerIdsToEliminate.toIntArray())

        UserCollection.eliminateFromTable(id, toEliminate, (players + spectators).map { it.userName })
        if (players.size <= 1)
            declareWinner()
    }

    /**
     * Declares the winner and send out a message about it.
     * Should only be called if only one player is left.
     * @author Bognar, Gabor Bela
     */
    private fun declareWinner() {
        players.first().stats.tablesWon = 1
        val toInform = spectators + players.first()
        toInform.forEach {
            UserCollection.updateStats(players.first().userName, players.first().stats)
            UserCollection.sendToClient(it.userName, WinnerAnnouncerMessage(this@Table.id, players.first().userName).toJsonString(), WinnerAnnouncerMessage.MESSAGE_CODE)
        }
        Casino.closeTable(id)
    }

    /**
     * Handles the situation when a player has been disconnected from the server
     * @author Bognar, Gabor Bela
     */
    fun playerDisconnected(name: String) {
        val index = players.indexOf(players.find { it.userName == name })
        if (index == -1)
            return

        if (!isStarted) {
            removePlayer(players[index].id)
            if (players.size == 0)
                Casino.closeTable(id)
        }
        else if (players.size > 2) {
            playersInTurn.remove(players[index].id)
            if (players[index].id == nextPlayerId) {
                nextPlayerId = players.find { player -> player.userName == previousAction?.name }?.id ?: playersInTurn.last()
                removePlayer(players[index].id)
                setNextPlayer()
            }
            else
                removePlayer(players[index].id)
            (spectators + players).forEach {
                UserCollection.sendToClient(it.userName, DisconnectedPlayerMessage(id, name).toJsonString(), DisconnectedPlayerMessage.MESSAGE_CODE)
            }
            if (isEndOfRound()){
                nextPlayerId = 0
                spreadGameState()
                nextRound()
            }
            else
                spreadGameState()
        } else if (players.size == 2) {
            removePlayer(players[index].id)
            (spectators + players.first()).forEach {
                UserCollection.sendToClient(it.userName, DisconnectedPlayerMessage(id, name).toJsonString(), DisconnectedPlayerMessage.MESSAGE_CODE)
            }
            declareWinner()
        }
        else {
            Casino.closeTable(id)
        }
    }

    /**
     * Removes a player
     * @author Bognar, Gabor Bela
     */
    private fun removePlayer(vararg playerIds: Int) {
        for (pid in playerIds) {
            val p = players.find { it.id == pid }
            if (p != null) {
                UserCollection.updateStats(p.userName, p.stats)
                players.removeAt(players.indexOfFirst { it.id == pid })
            }
        }
    }

    /**
     * Showdown step at the end of a turn
     * @author Bognar, Gabor Bela
     */
    private fun showdown() {
        pot = players.sumOf { it.inPot }
        val handsOfPlayers: MutableList<Pair<Int, Hand>> = mutableListOf()
        playersInTurn.forEach {
            val cardsToUse = mutableListOf<Card>()
            players.single { p -> p.id == it  }.stats.showDownCount++
            cardsToUse.addAll(cardsOnTable)
            cardsToUse.addAll(players.single { p -> p.id == it }.inHandCards)
            val handOfPlayer = evaluateHand(cardsToUse)
            handsOfPlayers.add(Pair(it, handOfPlayer))
        }
        val winnings = getWinners(MutableList(handsOfPlayers.size) { handsOfPlayers[it] })

        val playerOrder: MutableList<TurnEndMsgPlayerDto> = mutableListOf()

        val bustedCount = players.count { player -> player.chipStack == 0 && winnings.any { win -> win.first == player.id && win.second == 0 } }
        winnings.forEach { win ->
            players.single { p -> p.id == win.first }.apply {
                playerOrder.add(TurnEndMsgPlayerDto(
                    this.userName,
                    this.inHandCards,
                    handsOfPlayers.single { it.first == this.id }.second,
                    win.second))
                if (win.second > 0) {
                    potWon(win.second, bustedCount)
                    stats.handsWon++
                }
            }
        }

        val turnEndMessage = TurnEndMessage(this.id, cardsOnTable, playerOrder)
        (players + spectators).forEach {
            UserCollection.sendToClient(it.userName, turnEndMessage.toJsonString(), TurnEndMessage.MESSAGE_CODE)
        }
    }

    /**
     * Gets the winners and the amount of chips won by each in list
     * @author Bognar, Gabor Bela
     */
    private fun getWinners(hands: MutableList<Pair<Int, Hand>>): List<Pair<Int, Int>> {
        hands.sortBy { it.second }

        val winningList: MutableList<Pair<Int, Int>> = mutableListOf()

        while (pot != 0) { // loop until there are no side-pots
            hands.filter { players.single { p -> p.id == it.first }.inPot == 0 }.forEach {
                winningList.add(Pair(it.first, 0))
            }
            hands.removeIf { players.single { p -> p.id == it.first }.inPot == 0 }
            val winnerCount = getWinnerCount(hands)
            val (maxBetOfWinners, winnerBetSum) = getMaxAndSumBetOfWinners(hands.take(winnerCount).map { it.first })
            val sidePot = players.map { it.inPot }.sumOf { min(it, maxBetOfWinners) }

            repeat(winnerCount) {
                val winningOfPlayer = ((players.single { it.id == hands.first().first }.inPot.toDouble() / winnerBetSum.toDouble()) * sidePot.toDouble()).toInt()
                winningList.add(Pair(hands.first().first, winningOfPlayer))
                hands.removeFirstOrNull()
            }

            players.forEach { it.inPot -= min(it.inPot, maxBetOfWinners) }
            pot -= sidePot
        }
        hands.forEach {
            winningList.add(Pair(it.first, 0))
        }
        return winningList
    }

    /**
     * Gets the maximum bet of any player in the turn and the sum of these all bets
     * @author Bognar, Gabor Bela
     */
    private fun getMaxAndSumBetOfWinners(playerIds: List<Int>): Pair<Int, Int> {
        var sum = 0
        var max = 0
        players.filter { playerIds.contains(it.id) }.map { it.inPot }.forEach {
            sum += it
            if (it > max)
                max = it
        }
        return Pair(max, sum)
    }

    /**
     * Helper function in the showdown step.
     * @author Bognar, Gabor Bela
     */
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

    /**
     * Validates an incoming action
     * @author Bognar, Gabor Bela
     */
    private fun validateAction(actionMsg: ActionIncomingMessage): Boolean =
        if (actionMsg.name != players.single { nextPlayerId == it.id }.userName) // if action not from next player
            false
        else if (actionMsg.action.type == ActionType.CHECK && maxRaiseThisRound != players.single { it.id == nextPlayerId }.inPotThisRound) // if checked but has to fold/raise
            false
        else if (actionMsg.action.type == ActionType.RAISE) {
            val (stack, inThisRound) = players.single { it.id == nextPlayerId }.run { Pair(chipStack, inPotThisRound) }
            if (stack < bigBlindAmount)
                stack + inThisRound == actionMsg.action.amount
            else
                actionMsg.action.amount <= stack + inThisRound
        } else
            true

    /**
     * Should be called when only one person remains in the turn
     * @author Bognar, Gabor Bela
     */
    private fun oneLeft() {
        var winnerName: String
        pot = players.sumOf { it.inPot }
        players.single { it.id == playersInTurn.first() }.apply {
            potWon(pot, 0)
            stats.handsWon++
            winnerName = userName
        }
        val turnEndMsg = TurnEndMessage(id, cardsOnTable, listOf(TurnEndMsgPlayerDto(winnerName, null, null, pot)))
        (players + spectators).forEach {
            UserCollection.sendToClient(it.userName, turnEndMsg.toJsonString(), TurnEndMessage.MESSAGE_CODE)
        }
        newTurn()
    }

    /**
     * Determines if there is only one player left in the turn
     * @author Bognar, Gabor Bela
     */
    private fun isOneLeftInTurn(): Boolean = playersInTurn.size == 1

    /**
     * Determines if round has ended
     * @author Bognar, Gabor Bela
     */
    private fun isEndOfRound(): Boolean =
        players.filter { playersInTurn.contains(it.id) }
            .all { it.actedThisRound && (it.inPotThisRound == maxRaiseThisRound || it.chipStack == 0) }


    /**
     * Manages the starting a new round (new card to the table etc.)
     * @author Bognar, Gabor Bela
     */
    private fun nextRound() {
        maxRaiseThisRound = 0
        var state = turnState
        state++
        players.forEach { it.nextRound(state) }
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

    /**
     * Evaluates the hand of a player
     * @author Bognar, Gabor Bela
     */
    private fun evaluateHand(fromCards: MutableList<Card>) = HandEvaluator.evaluateHand(fromCards)

    /**
     * Determines if the [Table] is open
     * @return True, if the table is open
     * @author Bognar, Gabor Bela
     */
    fun isOpen() = rules.isOpen

    private fun Player.toDto() = InGamePlayerDto(
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