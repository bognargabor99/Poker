package integrationtestutils

import com.google.gson.Gson
import hu.bme.aut.thesis.poker.gamemodel.*
import hu.bme.aut.thesis.poker.network.GameStateMessage
import hu.bme.aut.thesis.poker.network.NetworkMessage
import hu.bme.aut.thesis.poker.network.TurnEndMessage
import kotlin.math.min

abstract class Bot(val userName: String) {
    protected var previousRoundGameState: GameStateMessage? = null
    protected lateinit var currentGameState: GameStateMessage

    fun processMessage(message: NetworkMessage) {
        if (message.messageCode == GameStateMessage.MESSAGE_CODE)
            processGameState(Gson().fromJson(message.data, GameStateMessage::class.java))
        else if (message.messageCode == TurnEndMessage.MESSAGE_CODE)
            processTurnEnd(Gson().fromJson(message.data, TurnEndMessage::class.java))
    }

    private fun processGameState(gameState: GameStateMessage) {
        if (gameState.tableCards.size < currentGameState.tableCards.size)
            previousRoundGameState = null
        else if (gameState.tableCards.size > currentGameState.tableCards.size)
            previousRoundGameState = currentGameState
        currentGameState = gameState
    }

    abstract fun act() : Action

    private fun processTurnEnd(turnEndMessage: TurnEndMessage) {
        previousRoundGameState = null
    }

    fun isMyTurn() : Boolean = currentGameState.nextPlayer == userName
}

class AggressiveBot(ourName: String) : Bot(ourName) {
    override fun act(): Action {
        val chips = currentGameState.players.single { it.userName == userName }.run { inPotThisRound + chipStack }
        return if (currentGameState.maxRaiseThisRound >= chips)
            Action(ActionType.CALL, 0)
        else
            Action(ActionType.RAISE, chips)
    }
}

class NormalBot(ourName: String) : Bot(ourName) {
    override fun act(): Action {
        val myAllChips = currentGameState.players.single {it.userName == userName}.run { chipStack + inPotThisRound }
        if (currentGameState.lastAction == null) {
            return if (currentGameState.tableCards.isEmpty()) {
                Action(ActionType.CALL, 0)
            } else {
                val currentHand = HandEvaluator.evaluateHand(currentGameState.receiverCards + currentGameState.tableCards)
                val previousHand = HandEvaluator.evaluateHand(previousRoundGameState!!.receiverCards.plus(previousRoundGameState!!.tableCards))
                if (currentHand < previousHand) {
                    val raiseAmount = min(currentGameState.maxRaiseThisRound, myAllChips)
                    Action(ActionType.RAISE, raiseAmount)
                }
                else
                    Action(ActionType.CHECK, 0)
            }
        }
        return if (currentGameState.players.single { it.userName == userName }.inPotThisRound < currentGameState.maxRaiseThisRound) {
            if (currentGameState.tableCards.isEmpty())
                Action(ActionType.CALL, 0)
            else {
                val currentHand = HandEvaluator.evaluateHand(currentGameState.receiverCards + currentGameState.tableCards)
                val previousHand = HandEvaluator.evaluateHand(previousRoundGameState!!.receiverCards.plus(previousRoundGameState!!.tableCards))
                if (currentHand < previousHand || currentHand.type != HandType.HIGH_CARD)
                    Action(ActionType.CALL, 0)
                else
                    Action(ActionType.FOLD, 0)
            }
        } else {
            val currentHand = HandEvaluator.evaluateHand(currentGameState.receiverCards + currentGameState.tableCards)
            val previousHand = HandEvaluator.evaluateHand(previousRoundGameState!!.receiverCards.plus(previousRoundGameState!!.tableCards))
            if (currentHand < previousHand) {
                val raiseAmount = min(currentGameState.maxRaiseThisRound, myAllChips)
                Action(ActionType.RAISE, raiseAmount)
            } else
                Action(ActionType.CHECK, 0)
        }
    }
}