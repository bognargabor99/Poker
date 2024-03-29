@file:Suppress("EXPERIMENTAL_API_USAGE")

package hu.bme.aut.thesis.poker.network

import com.google.gson.Gson
import hu.bme.aut.thesis.poker.gamemodel.Casino
import hu.bme.aut.thesis.poker.gamemodel.Table
import hu.bme.aut.thesis.poker.gamemodel.Statistics

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
        chain = CreateTableProcessor(JoinTableProcessor(GetOpenTablesProcessor(ActionProcessor(LeaveTableProcessor(SpectatorSubscriptionProcessor(SpectatorUnsubscriptionProcessor(GetStatisticsProcessor(null))))))))
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
 * This processor class handles creating [Table]s
 * @author Bognar, Gabor Bela
 */
class CreateTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == CreateTableMessage.MESSAGE_CODE) {
            val startMessage = Gson().fromJson(message.data, CreateTableMessage::class.java)
            val tableId = Casino.startTable(startMessage.rules)
            UserCollection.tableCreated(startMessage.userName, tableId)
            Casino.joinTable(tableId, startMessage.userName)
        }
        else
            super.process(message)
}

/**
 * This processor class handles joining [Table]s
 * @author Bognar, Gabor Bela
 */
class JoinTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == JoinTableMessage.MESSAGE_CODE) {
            val joinMessage = Gson().fromJson(message.data, JoinTableMessage::class.java)
            Casino.joinTable(joinMessage.tableId, joinMessage.userName)
        }
        else
            super.process(message)
}

/**
 * This processor class handles asking for open [Table]s
 * @author Bognar, Gabor Bela
 */
class GetOpenTablesProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == GetOpenTablesMessage.MESSAGE_CODE) {
            val getTablesMessage = Gson().fromJson(message.data, GetOpenTablesMessage::class.java)
            val tableIds = Casino.getOpenTables()
            val tablesMessage = SendOpenTablesMessage(tableIds)
            UserCollection.sendToClient(getTablesMessage.userName, tablesMessage.toJsonString(), SendOpenTablesMessage.MESSAGE_CODE)
        }
        else
            super.process(message)
}

/**
 * This processor class forwards interactions on [Table]s
 * @author Bognar, Gabor Bela
 */
class ActionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == ActionIncomingMessage.MESSAGE_CODE) {
            val actionMessage = Gson().fromJson(message.data, ActionIncomingMessage::class.java)
            Casino.performAction(actionMessage)
        }
        else
            super.process(message)
}

/**
 * This processor class handles requests to leave a [Table]
 * @author Bognar, Gabor Bela
 */
class LeaveTableProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == LeaveTableMessage.MESSAGE_CODE) {
            val leaveMessage = Gson().fromJson(message.data, LeaveTableMessage::class.java)
            UserCollection.removePlayerFromTables(leaveMessage.userName, mutableListOf(leaveMessage.tableId))
            Casino.removePlayerFromTable(leaveMessage.userName, leaveMessage.tableId)
        }
        else
            super.process(message)
}

/**
 * This processor class handles requests to start spectating a [Table]
 * @author Bognar, Gabor Bela
 */
class SpectatorSubscriptionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) {
        if (message?.messageCode == SpectatorSubscriptionMessage.MESSAGE_CODE) {
            val subMessage = Gson().fromJson(message.data, SpectatorSubscriptionMessage::class.java)
            Casino.addSpectator(subMessage)
        } else {
            super.process(message)
        }
    }
}

/**
 * This processor class handles requests to stop spectating a [Table]
 * @author Bognar, Gabor Bela
 */
class SpectatorUnsubscriptionProcessor(processor: Processor?) : Processor(processor) {
    override fun process(message: NetworkMessage?) =
        if (message?.messageCode == SpectatorUnsubscriptionMessage.MESSAGE_CODE) {
            val unSubMessage = Gson().fromJson(message.data, SpectatorUnsubscriptionMessage::class.java)
            Casino.removeSpectator(unSubMessage.tableId, unSubMessage.userName)
        }
        else
            super.process(message)
}

/**
 * This processor class handles requests for [Statistics]
 * @author Bognar, Gabor Bela
 */
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