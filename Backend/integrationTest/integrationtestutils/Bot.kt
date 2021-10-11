package hu.bme.aut.onlab.poker.integrationtestutils

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.gamemodel.*
import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import io.ktor.http.content.*

abstract class Bot(protected val ourName: String) {
    protected var previousGameState: GameStateMessage? = null
    protected var currentGameState: GameStateMessage? = null

    fun processMessage(message: NetworkMessage) {
        if (message.messageCode == GameStateMessage.MESSAGE_CODE)
            processGameState(Gson().fromJson(message.data, GameStateMessage::class.java))
        else if (message.messageCode == TurnEndMessage.MESSAGE_CODE)
            processTurnEnd(Gson().fromJson(message.data, TurnEndMessage::class.java))
    }

    private fun processGameState(gameState: GameStateMessage) {
        previousGameState = currentGameState
        currentGameState = gameState
    }

    abstract fun act() : Action

    private fun processTurnEnd(turnEndMessage: TurnEndMessage) {
        previousGameState = null
        currentGameState = null
    }

    fun isMyTurn() : Boolean = currentGameState?.nextPlayer == ourName
}

class AggressiveBot(ourName: String) : Bot(ourName) {
    override fun act(): Action {
        val chips = currentGameState!!.players.single { it.userName == ourName }.run { inPotThisRound + chipStack }
        return if (currentGameState!!.maxRaiseThisRound >= chips)
            Action(ActionType.CALL, currentGameState!!.maxRaiseThisRound)
        else
            Action(ActionType.RAISE, chips)
    }
}

class NormalBot(ourName: String) : Bot(ourName) {
    override fun act(): Action {
        TODO("act normal")
    }
}