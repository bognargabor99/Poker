@file:Suppress("EXPERIMENTAL_API_USAGE")

package hu.bme.aut.onlab.poker.network

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.gamemodel.Game

class NetworkChain {
    private lateinit var chain: Processor

    init {
        buildChain()
    }

    private fun buildChain() {
        chain = CreateTableProcessor(JoinTableProcessor(GetOpenTablesProcessor(ActionProcessor(LeaveTableProcessor(SpectatorSubscriptionProcessor(SpectatorUnsubscriptionProcessor(GetStatisticsProcessor(null))))))))
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
            val startMessage = Gson().fromJson(message.data, CreateTableMessage::class.java)
            val tableId = Game.startTable(startMessage.rules)
            UserCollection.tableCreated(startMessage.userName, tableId)
            Game.joinTable(tableId, startMessage.userName)
        }
        else
            super.process(message)
}

class JoinTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == JoinTableMessage.MESSAGE_CODE) {
            val joinMessage = Gson().fromJson(message.data, JoinTableMessage::class.java)
            Game.joinTable(joinMessage.tableId, joinMessage.userName)
        }
        else
            super.process(message)
}

class GetOpenTablesProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == GetOpenTablesMessage.MESSAGE_CODE) {
            val getTablesMessage = Gson().fromJson(message.data, GetOpenTablesMessage::class.java)
            val tableIds = Game.getOpenTables()
            val tablesMessage = SendOpenTablesMessage(tableIds)
            UserCollection.sendToClient(getTablesMessage.userName, tablesMessage.toJsonString(), SendOpenTablesMessage.MESSAGE_CODE)
        }
        else
            super.process(message)
}

class ActionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == ActionIncomingMessage.MESSAGE_CODE) {
            val actionMessage = Gson().fromJson(message.data, ActionIncomingMessage::class.java)
            Game.performAction(actionMessage)
        }
        else
            super.process(message)
}

class LeaveTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == LeaveTableMessage.MESSAGE_CODE) {
            val leaveMessage = Gson().fromJson(message.data, LeaveTableMessage::class.java)
            UserCollection.removePlayerFromTables(leaveMessage.userName, mutableListOf(leaveMessage.tableId))
            Game.removePlayerFromTable(leaveMessage.userName, leaveMessage.tableId)
        }
        else
            super.process(message)
}

class SpectatorSubscriptionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) {
        if (message?.messageCode == SpectatorSubscriptionMessage.MESSAGE_CODE) {
            val subMessage = Gson().fromJson(message.data, SpectatorSubscriptionMessage::class.java)
            Game.addSpectator(subMessage)
        } else {
            super.process(message)
        }
    }
}

class SpectatorUnsubscriptionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == SpectatorUnsubscriptionMessage.MESSAGE_CODE) {
            val unSubMessage = Gson().fromJson(message.data, SpectatorUnsubscriptionMessage::class.java)
            Game.removeSpectator(unSubMessage.tableId, unSubMessage.userName)
        }
        else
            super.process(message)
}

class GetStatisticsProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == StatisticsMessage.MESSAGE_CODE) {
            val getStatsMessage = Gson().fromJson(message.data, StatisticsMessage::class.java)
            val stats = UserCollection.getStatsForPlayer(getStatsMessage.userName)
            getStatsMessage.statistics = stats
            UserCollection.sendToClient(getStatsMessage.userName, getStatsMessage.toJsonString(), StatisticsMessage.MESSAGE_CODE)
        }
        else
            super.process(message)
}