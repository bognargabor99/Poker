package hu.bme.aut.onlab.poker.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    suspend fun sendToClient(userName: String, tables: List<Int>) {
        users.single { it.name == userName }
            ?.sendToClient(Json.encodeToString(tables))
    }

    suspend fun askForAction(userName: String, toCall: Int) {
        users.single { it.name == userName }
            .askForAction(toCall)
    }

    fun notifyGameStarted(usersInTable: List<String>) {
        users.filter { usersInTable.contains(it.name) }
            .forEach { user ->
                user.notifyGameStarted()
            }
    }
}
