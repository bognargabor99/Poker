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
    val userName: String,
    val settings: TableSettings
)

@Serializable
data class JoinTableMessage(
    val userName: String,
    val tableId: Int
)

@Serializable
data class GetOpenTablesMessage(
    val userName: String
)

@Serializable
data class AskActionMessage(
    val toCall: Int
)