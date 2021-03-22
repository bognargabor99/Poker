package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.Action
import hu.bme.aut.onlab.poker.gamemodel.TableRules
import kotlinx.serialization.Serializable

@Serializable
data class NetworkRequest(
    val messageCode: Int,
    val data: String
)

@Serializable
data class StartTableMessage( // 1
    val userName: String,
    val rules: TableRules
)

@Serializable
data class JoinTableMessage( // 2
    val userName: String,
    val tableId: Int
)

@Serializable
data class GetOpenTablesMessage( // 3
    val userName: String
)

@Serializable
data class ActionIncomingMessage( // 4
    val tableId: Int,
    val playerId: Int,
    val action: Action
)

@Serializable
data class AskActionMessage(
    val toCall: Int
)