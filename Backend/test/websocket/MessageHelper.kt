package hu.bme.aut.onlab.poker.websocket

import hu.bme.aut.onlab.poker.gamemodel.TableRules
import hu.bme.aut.onlab.poker.network.*

object MessageHelper {
    fun getStartTableMessage(userName: String, rules: TableRules = TableRules.defaultRules) : String =
        NetworkMessage(
            messageCode = CreateTableMessage.MESSAGE_CODE,
            data = CreateTableMessage(userName, rules).toJsonString()).toJsonString()

    fun getConnectionInfoMessage(userName: String) : String =
        NetworkMessage(
            messageCode = ConnectionInfoMessage.MESSAGE_CODE,
            data = ConnectionInfoMessage(userName).toJsonString()).toJsonString()

    fun getTableCreatedMessage(tableId: Int) : String =
        NetworkMessage(
            messageCode = TableCreatedMessage.MESSAGE_CODE,
            data = TableCreatedMessage(tableId).toJsonString()).toJsonString()

    fun getTableJoinedMessage(tableId: Int, rules: TableRules = TableRules.defaultRules) : String =
        NetworkMessage(
            messageCode = TableJoinedMessage.MESSAGE_CODE,
            data = TableJoinedMessage(tableId, rules).toJsonString()).toJsonString()

    fun getGetOpenTablesMessage(userName: String) : String =
        NetworkMessage(
            messageCode = GetOpenTablesMessage.MESSAGE_CODE,
            data = GetOpenTablesMessage(userName).toJsonString()).toJsonString()

    fun getSendOpenTablesMessage(tableIds: List<Int>) : String =
        NetworkMessage(
            messageCode = SendOpenTablesMessage.MESSAGE_CODE,
            data = SendOpenTablesMessage(tableIds).toJsonString()).toJsonString()
}