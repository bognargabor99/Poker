@file:Suppress("EXPERIMENTAL_API_USAGE")

package hu.bme.aut.onlab.poker.network

import com.google.gson.Gson

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

abstract class Processor(private val processor: Processor?) {
    open fun process(message: NetworkMessage?) {
        processor?.process(message)
    }
}

class GameStateProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == GameStateMessage.MESSAGE_CODE || message?.messageCode == SpectatorGameStateMessage.MESSAGE_CODE)
                PokerClient.setGameState(message)
            else
                super.process(message)
}

class ConnectionInfoProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == ConnectionInfoMessage.MESSAGE_CODE) {
                val connInfo = Gson().fromJson(message.data, ConnectionInfoMessage::class.java)
                PokerClient.setConnectionInfo(connInfo.userName, connInfo.isGuest)
            } else
                super.process(message)
}

class TurnEndProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == TurnEndMessage.MESSAGE_CODE)
                PokerClient.turnEnded(Gson().fromJson(message.data, TurnEndMessage::class.java))
            else
                super.process(message)
}

class EliminationProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == EliminationMessage.MESSAGE_CODE)
                PokerClient.eliminatedFromTable(Gson().fromJson(message.data, EliminationMessage::class.java).tableId)
            else
                super.process(message)
}

class TableJoinedProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == TableJoinedMessage.MESSAGE_CODE) {
            PokerClient.tableJoined(Gson().fromJson(message.data, TableJoinedMessage::class.java))
            Thread.sleep(1000)
        }
        else
            super.process(message)
}

class GameStartedProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == GameStartedMessage.MESSAGE_CODE)
            PokerClient.tableStarted(Gson().fromJson(message.data, GameStartedMessage::class.java).tableId)
        else
            super.process(message)
}

class DisconnectedPlayerProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == DisconnectedPlayerMessage.MESSAGE_CODE) {
            val disconnectMessage = Gson().fromJson(message.data, DisconnectedPlayerMessage::class.java)
            PokerClient.playerDisconnectedFromTable(disconnectMessage.tableId, disconnectMessage.userName)
        }
        else
            super.process(message)
}

class WinnerAnnouncementProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == WinnerAnnouncerMessage.MESSAGE_CODE) {
            val announcement = Gson().fromJson(message.data, WinnerAnnouncerMessage::class.java)
            PokerClient.tableWon(announcement.tableId, announcement.nameOfWinner)
        }
        else
            super.process(message)
}

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

class SubscriptionAcceptanceProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == SubscriptionAcceptanceMessage.MESSAGE_CODE) {
            PokerClient.tableSpectated(Gson().fromJson(message.data, SubscriptionAcceptanceMessage::class.java))
            Thread.sleep(600)
        }
        else
            super.process(message)
}