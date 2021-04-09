package hu.bme.aut.onlab.poker.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NetworkChain {
    private lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = GameStateProcessor(ConnectionInfoProcessor(TurnEndProcessor(EliminationProcessor(null))))
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
                PokerClient.eliminated(Json.decodeFromString<EliminationMessage>(message.data).tableId)
            }
            else
                super.process(message)
}