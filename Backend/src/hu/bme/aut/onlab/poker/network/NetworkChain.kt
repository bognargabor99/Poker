package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.Game
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NetworkChain {
    lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = JoinTableProcessor(StartTableProcessor(null))
    }

    fun process(request: NetworkRequest) = chain.process(request)
}

abstract class Processor(private val processor: Processor?) {
    open fun process(request: NetworkRequest?) {
        processor?.process(request)
    }
}

class StartTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(request: NetworkRequest?) =
        if (request?.messageCode == 1) {
            val startMessage = Json.decodeFromString<StartTableMessage>(request.data)
            val tableId = Game.startTable(startMessage.settings)
            Game.joinTable(tableId, startMessage.playerId)
        }
        else
            super.process(request)
}

class JoinTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(request: NetworkRequest?) =
        if (request?.messageCode == 2) {
            val joinMessage = Json.decodeFromString<JoinTableMessage>(request.data)
            Game.joinTable(joinMessage.tableId, joinMessage.playerId)
        }
        else
            super.process(request)
}

class GetOpenTablesProcessor(processor: Processor?) : Processor(processor) {
    override fun process(request: NetworkRequest?) =
        if (request?.messageCode == 3) {
            val getTablesMessage = Json.decodeFromString<GetOpenTablesMessage>(request.data)
            val tables = Game.getOpenTables()
            //UserCollection.sendToClient(getTablesMessage.playerId, tables)
        }
        else
            super.process(request)
}