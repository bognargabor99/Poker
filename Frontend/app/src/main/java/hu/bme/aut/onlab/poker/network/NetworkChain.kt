package hu.bme.aut.onlab.poker.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NetworkChain {
    private lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = GameStateProcessor(ConnectionInfoProcessor(TableJoinedProcessor(TurnEndProcessor(EliminationProcessor(GameStartedProcessor(DisconnectedPlayerProcessor(WinnerAnnouncementProcessor(null))))))))
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
            if (message?.messageCode == GameStateMessage.MESSAGE_CODE) {
                PokerClient.setGameState(Json.decodeFromString(message.data))
            }
            else
                super.process(message)
}

class ConnectionInfoProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == ConnectionInfoMessage.MESSAGE_CODE) {
                PokerClient.setConnectionInfo(Json.decodeFromString<ConnectionInfoMessage>(message.data).userName)
            }
            else
                super.process(message)
}

class TurnEndProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == TurnEndMessage.MESSAGE_CODE) {
                PokerClient.turnEnded(Json.decodeFromString(message.data))
            }
            else
                super.process(message)
}

class EliminationProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
            if (message?.messageCode == EliminationMessage.MESSAGE_CODE) {
                PokerClient.eliminatedFromTable(Json.decodeFromString<EliminationMessage>(message.data).tableId)
            }
            else
                super.process(message)
}

class TableJoinedProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == TableJoinedMessage.MESSAGE_CODE) {
            PokerClient.tableJoined(Json.decodeFromString<TableJoinedMessage>(message.data))
            Thread.sleep(1000)
        }
        else
            super.process(message)
}

class GameStartedProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == GameStartedMessage.MESSAGE_CODE) {
            PokerClient.tableStarted(Json.decodeFromString<GameStartedMessage>(message.data).tableId)
        }
        else
            super.process(message)
}

class DisconnectedPlayerProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == DisconnectedPlayerMessage.MESSAGE_CODE) {
            PokerClient.playerDisconnectedFromTable(Json.decodeFromString<DisconnectedPlayerMessage>(message.data).userName)
        }
        else
            super.process(message)
}

class WinnerAnnouncementProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == WinnerAnnouncerMessage.MESSAGE_CODE) {
            PokerClient.tableWon(Json.decodeFromString<WinnerAnnouncerMessage>(message.data).tableId)
        }
        else
            super.process(message)
}