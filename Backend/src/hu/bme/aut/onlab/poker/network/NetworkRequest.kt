package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.TableSettings
import kotlinx.serialization.Serializable

@Serializable
data class NetworkRequest(
    val messageCode: Int,
    val data: String
)

@Serializable
data class StartTableMessage(
    val playerId: Int,
    val settings: TableSettings
)

@Serializable
data class JoinTableMessage(
    val playerId: Int,
    val tableId: Int
)

@Serializable
data class GetOpenTablesMessage(
    val playerId: Int
)

@Serializable
data class AskActionMessage(
    val toCall: Int
)