package hu.bme.aut.onlab.poker.network

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

    fun sendToClient(userName: String, data: String) {
        users.single { it.name == userName }
            ?.sendToClient(data)
    }

    fun notifyGameStarted(tableId: Int, usersInTable: List<String>) {
        users.filter { usersInTable.contains(it.name) }
            .forEach { user ->
                user.notifyGameStarted(tableId)
            }
    }

    fun eliminateFromTable(tableId: Int, toEliminate: List<String>) {
        toEliminate.forEach {
            users.single { user -> user.name == it }
                .sendToClient(Json.encodeToString(EliminationMessage(tableId)))
        }
    }

    fun tableJoined(user: String, tableId: Int) {
        users.single { it.name == user }.tableIds.add(tableId)
    }
}
