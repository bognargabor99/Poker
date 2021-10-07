package hu.bme.aut.onlab.poker.network

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.gamemodel.Game
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

@DelicateCoroutinesApi
class User(private val session: DefaultWebSocketSession, userName: String = "") {
    var name = userName.ifBlank { "user${lastId.getAndIncrement()}" }
    val tablePlayingIds = mutableListOf<Int>()
    val tableSpectatingIds = mutableListOf<Int>()
    private val chain = NetworkChain()

    init {
        UserCollection += this
    }

    suspend fun sendNameToClient() {
        val networkMsg = NetworkMessage(ConnectionInfoMessage.MESSAGE_CODE, ConnectionInfoMessage(name).toJsonString())
        coroutineScope {
            session.send(networkMsg.toJsonString())
        }
    }

    fun receiveFromClient(receivedText: String) {
        val request = Gson().fromJson(receivedText, NetworkMessage::class.java)
        chain.process(request)
    }

    fun sendToClient(textToSend: String) = GlobalScope.launch { session.send(textToSend) }

    fun disconnect() {
        Game.removePlayerFromTables(name, tablePlayingIds, tableSpectatingIds)
    }

    companion object {
        var lastId = AtomicInteger(1)
    }
}