package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.TableRules
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

object UserCollection {
    private val users = Collections.synchronizedSet<User?>(LinkedHashSet())

    operator fun plusAssign(newUser: User) {
        users.add(newUser)
    }

    operator fun minusAssign(user: User) {
        users.remove(user)
    }

    fun sendToClient(userName: String, data: String, code: Int) {
        val networkMsg = NetworkMessage(code, data)
        users.find { it.name == userName }
            ?.sendToClient(Json.encodeToString(networkMsg))
    }

    fun notifyGameStarted(tableId: Int, usersInTable: List<String>) {
        users.filter { usersInTable.contains(it.name) }
            .forEach { user ->
                val msg = GameStartedMessage(tableId)
                sendToClient(user.name, Json.encodeToString(msg), GameStartedMessage.MESSAGE_CODE)
            }
    }

    fun eliminateFromTable(tableId: Int, toEliminate: List<String>, toInform: List<String>) {
        toEliminate.forEach {
            sendToClient(it, Json.encodeToString(EliminationMessage(tableId)), EliminationMessage.MESSAGE_CODE)
        }
        toInform.forEach { informed ->
            toEliminate.forEach { eliminated ->
                sendToClient(informed, Json.encodeToString(DisconnectedPlayerMessage(tableId, eliminated)), EliminationMessage.MESSAGE_CODE)
            }
        }
    }

    fun tableCreated(user: String, tableId: Int) {
        val answer = TableCreatedMessage(tableId)
        sendToClient(user, Json.encodeToString(answer), TableCreatedMessage.MESSAGE_CODE)
    }

    fun tableJoined(user: String, tableId: Int, rules: TableRules) {
        val answer = TableJoinedMessage(tableId, rules)
        sendToClient(user, Json.encodeToString(answer), TableJoinedMessage.MESSAGE_CODE)
        users.single { it.name == user }.tableIds.add(tableId)
    }

    fun removePlayerFromTables(userName: String, tables: MutableList<Int>) {
        users.single { it.name == userName }.tableIds.removeAll(tables)
    }
}
