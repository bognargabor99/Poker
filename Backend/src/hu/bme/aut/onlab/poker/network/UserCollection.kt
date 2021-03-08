package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.gamemodel.Player
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

    fun setPlayerForUser(userName: String, player: Player) {
        users.single { it.name == userName }
            .player = player
    }

    suspend fun sendToClient(userName: String, tables: List<Int>) {
        users.find { it.name == userName }
            ?.sendToClient(Json.encodeToString(tables))
    }
}
