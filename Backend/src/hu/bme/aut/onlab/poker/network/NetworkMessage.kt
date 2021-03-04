package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.TableSettings
import kotlinx.serialization.Serializable

@Serializable
sealed class NetworkMessage(
    val messageCode: Int,
    val data: String
)

/*@Serializable
data class StartTableMessage(
    val settings: TableSettings
)

@Serializable
data class JoinTableMessage(
    val tableId: Int,
    var mCode: Int
) : NetworkMessage(1)

@Serializable
data class GetOpenTablesMessage(
    var mCode: Int
) : NetworkMessage(2)*/