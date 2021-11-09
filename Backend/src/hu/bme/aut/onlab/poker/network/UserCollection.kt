package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.data.DatabaseHelper
import hu.bme.aut.onlab.poker.gamemodel.Statistics
import hu.bme.aut.onlab.poker.gamemodel.TableRules
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*

@DelicateCoroutinesApi
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
            ?.sendToClient(networkMsg.toJsonString())
    }

    fun notifyGameStarted(tableId: Int, usersInTable: List<String>) {
        users.filter { usersInTable.contains(it.name) }
            .forEach { user ->
                val msg = GameStartedMessage(tableId)
                sendToClient(user.name, msg.toJsonString(), GameStartedMessage.MESSAGE_CODE)
            }
    }

    fun eliminateFromTable(tableId: Int, toEliminate: List<String>, toInform: List<String>) {
        toEliminate.forEach {
            sendToClient(it, EliminationMessage(tableId).toJsonString(), EliminationMessage.MESSAGE_CODE)
        }
        toInform.forEach { informed ->
            toEliminate.forEach { eliminated ->
                sendToClient(informed, DisconnectedPlayerMessage(tableId, eliminated).toJsonString(), DisconnectedPlayerMessage.MESSAGE_CODE)
            }
        }
    }

    fun tableCreated(user: String, tableId: Int) {
        val answer = TableCreatedMessage(tableId)
        sendToClient(user, answer.toJsonString(), TableCreatedMessage.MESSAGE_CODE)
    }

    fun tableJoined(userName: String, tableId: Int, rules: TableRules) {
        val answer = TableJoinedMessage(tableId, rules)
        sendToClient(userName, answer.toJsonString(), TableJoinedMessage.MESSAGE_CODE)
        users.find { it.name == userName }?.tablePlayingIds?.add(tableId)
    }

    fun removePlayerFromTables(userName: String, tables: MutableList<Int>) {
        users.find { it.name == userName }?.tablePlayingIds?.removeAll(tables)
    }

    fun tableSpectated(userName: String, tableId: Int, rules: TableRules) {
        val answer = SubscriptionAcceptanceMessage(tableId, rules)
        sendToClient(userName, answer.toJsonString(), SubscriptionAcceptanceMessage.MESSAGE_CODE)
        users.find { it.name == userName }?.tableSpectatingIds?.add(tableId)
    }

    fun removeTableFromPLayers(tableId: Int) {
        users.forEach {
            it.tablePlayingIds.remove(tableId)
            it.tableSpectatingIds.remove(tableId)
        }
    }

    fun isAlreadyAuthenticated(user: String) =
        users.any { it.name == user }

    fun updateStats(userName: String, stats: Statistics) {
        if (users.find { it.name == userName }?.isGuest == false)
            DatabaseHelper.updateStatsForUser(userName, stats)
    }

    fun getStatsForPlayer(userName: String): Statistics {
        return DatabaseHelper.getStatisticsByName(userName)
    }
}
