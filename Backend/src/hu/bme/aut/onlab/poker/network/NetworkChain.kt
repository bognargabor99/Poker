package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.Game
import hu.bme.aut.onlab.poker.gamemodel.Table
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NetworkChain {
    private lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = CreateTableProcessor(JoinTableProcessor(GetOpenTablesProcessor(ActionProcessor(null))))
    }

    fun process(message: NetworkMessage) = chain.process(message)
}

abstract class Processor(private val processor: Processor?) {
    open fun process(message: NetworkMessage?) {
        processor?.process(message)
    }
}

class CreateTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == CreateTableMessage.MESSAGE_CODE) {
            val startMessage = Json.decodeFromString<CreateTableMessage>(message.data)
            val tableId = Game.startTable(startMessage.rules)
            Game.joinTable(tableId, startMessage.userName)
            val answer = TableCreatedMessage(tableId)
            UserCollection.sendToClient(startMessage.userName, Json.encodeToString(answer), TableCreatedMessage.MESSAGE_CODE)
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