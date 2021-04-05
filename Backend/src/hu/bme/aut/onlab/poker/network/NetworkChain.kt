package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.Game
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NetworkChain {
    lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = StartTableProcessor(JoinTableProcessor(GetOpenTablesProcessor(ActionProcessor(null))))
    }

    fun process(message: NetworkMessage) = chain.process(message)
}

abstract class Processor(private val processor: Processor?) {
    open fun process(message: NetworkMessage?) {
        processor?.process(message)
    }
}

class StartTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == StartTableMessage.MESSAGE_CODE) {
            val startMessage = Json.decodeFromString<StartTableMessage>(message.data)
            val tableId = Game.startTable(startMessage.rules)
            Game.joinTable(tableId, startMessage.userName)
        }
        else
            super.process(message)
}

class JoinTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == JoinTableMessage.MESSAGE_CODE) {
            val joinMessage = Json.decodeFromString<JoinTableMessage>(message.data)
            Game.joinTable(joinMessage.tableId, joinMessage.userName)
            UserCollection.tableJoined(joinMessage.userName, joinMessage.tableId)
        }
        else
            super.process(message)
}

class GetOpenTablesProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == GetOpenTablesMessage.MESSAGE_CODE) {
            val getTablesMessage = Json.decodeFromString<GetOpenTablesMessage>(message.data)
            val tableIds = Game.getOpenTables()
            val tablesMessage = SendOpenTablesMessage(tableIds)
            UserCollection.sendToClient(getTablesMessage.userName, Json.encodeToString(tablesMessage), SendOpenTablesMessage.MESSAGE_CODE)
        }
        else
            super.process(message)
}

class ActionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == ActionIncomingMessage.MESSAGE_CODE) {
            val actionMessage = Json.decodeFromString<ActionIncomingMessage>(message.data)
            Game.performAction(actionMessage)
        }
        else
            super.process(message)
}