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

    suspend fun sendToClient(playerId: Int, tables: List<Int>) {
        users.find { it.player.id == playerId }
            ?.sendToClient(Json.encodeToString(tables))
    }
}
