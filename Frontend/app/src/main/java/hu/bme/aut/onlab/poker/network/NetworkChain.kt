@file:Suppress("EXPERIMENTAL_API_USAGE")

package hu.bme.aut.onlab.poker.network

import com.google.gson.Gson

/**
 * This class represents the chain in the "Chain of responsibility" pattern
 * @author Bognar, Gabor Bela
 */
class NetworkChain {
    private lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = GameStateProcessor(ConnectionInfoProcessor(TableJoinedProcessor(TurnEndProcessor(EliminationProcessor(GameStartedProcessor(DisconnectedPlayerProcessor(WinnerAnnouncementProcessor(WinnerAnnouncementProcessor(StatisticsProcessor(SubscriptionAcceptanceProcessor(null)))))))))))
    }

    fun process(message: NetworkMessage) = chain.process(message)
}

/**
 * Base class of all the Processors in the [NetworkChain]
 * @author Bognar, Gabor Bela
 */
abstract class Processor(private val processor: Processor?) {
    open fun process(message: NetworkMessage?) {
        processor?.process(message)
    }
}

/**
 * This processor class handles recieving game states
 * @author Bognar, Gabor Bela
 */
class GameStateProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == GameStateMessage.MESSAGE_CODE || message?.messageCode == SpectatorGameStateMessage.MESSAGE_CODE)
                PokerClient.setGameState(message)
            else
                super.process(message)
}

/**
 * This processor class handles recieving connection information
 * @author Bognar, Gabor Bela
 */
class ConnectionInfoProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == ConnectionInfoMessage.MESSAGE_CODE) {
                val connInfo = Gson().fromJson(message.data, ConnectionInfoMessage::class.java)
                PokerClient.setConnectionInfo(connInfo.userName, connInfo.isGuest)
            } else
                super.process(message)
}

/**
 * This processor class handles recieving [TurnEndMessage]s
 * @author Bognar, Gabor Bela
 */
class TurnEndProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == TurnEndMessage.MESSAGE_CODE)
                PokerClient.turnEnded(Gson().fromJson(message.data, TurnEndMessage::class.java))
            else
                super.process(message)
}

/**
 * This processor class handles being eliminated from a table
 * @author Bognar, Gabor Bela
 */
class EliminationProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == EliminationMessage.MESSAGE_CODE)
                PokerClient.eliminatedFromTable(Gson().fromJson(message.data, EliminationMessage::class.java).tableId)
            else
                super.process(message)
}

/**
 * This processor class handles receiving [TableJoinedMessage]
 * @author Bognar, Gabor Bela
 */
class TableJoinedProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == TableJoinedMessage.MESSAGE_CODE) {
            PokerClient.tableJoined(Gson().fromJson(message.data, TableJoinedMessage::class.java))
            Thread.sleep(1000)
        }
        else
            super.process(message)
}

/**
 * This processor class handles receiving [GameStartedMessage]
 * @author Bognar, Gabor Bela
 */
class GameStartedProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == GameStartedMessage.MESSAGE_CODE)
            PokerClient.tableStarted(Gson().fromJson(message.data, GameStartedMessage::class.java).tableId)
        else
            super.process(message)
}

/**
 * This processor class handles receiving [DisconnectedPlayerMessage]
 * @author Bognar, Gabor Bela
 */
class DisconnectedPlayerProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == DisconnectedPlayerMessage.MESSAGE_CODE) {
            val disconnectMessage = Gson().fromJson(message.data, DisconnectedPlayerMessage::class.java)
            PokerClient.playerDisconnectedFromTable(disconnectMessage.tableId, disconnectMessage.userName)
        }
        else
            super.process(message)
}

/**
 * This processor class handles receiving [WinnerAnnouncerMessage]
 * @author Bognar, Gabor Bela
 */
class WinnerAnnouncementProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == WinnerAnnouncerMessage.MESSAGE_CODE) {
            val announcement = Gson().fromJson(message.data, WinnerAnnouncerMessage::class.java)
            PokerClient.tableWon(announcement.tableId, announcement.nameOfWinner)
        }
        else
            super.process(message)
}

/**
 * This processor class handles receiving [StatisticsMessage]
 * @author Bognar, Gabor Bela
 */
class StatisticsProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) {
        if (message?.messageCode == StatisticsMessage.MESSAGE_CODE) {
            val statsMessage = Gson().fromJson(message.data, StatisticsMessage::class.java)
            if (statsMessage.statistics != null)
                PokerClient.receiveStats(statsMessage.statistics!!)
        } else
            super.process(message)
    }
}

/**
 * This processor class handles receiving [SubscriptionAcceptanceMessage]
 * @author Bognar, Gabor Bela
 */
class SubscriptionAcceptanceProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == SubscriptionAcceptanceMessage.MESSAGE_CODE) {
            PokerClient.tableSpectated(Gson().fromJson(message.data, SubscriptionAcceptanceMessage::class.java))
            Thread.sleep(600)
        }
        else
            super.process(message)
}