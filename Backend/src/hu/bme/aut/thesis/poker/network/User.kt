package hu.bme.aut.thesis.poker.network

import com.google.gson.Gson
import hu.bme.aut.thesis.poker.gamemodel.Casino
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

@DelicateCoroutinesApi
class User(private val session: DefaultWebSocketSession, val isGuest: Boolean, userName: String = "") {
    var name: String
    val tablePlayingIds = mutableListOf<Int>()
    val tableSpectatingIds = mutableListOf<Int>()
    private val chain = NetworkChain()

    init {
        UserCollection += this
        name = if (isGuest)
            "guest${lastId.getAndIncrement()}"
        else
            userName.ifBlank { "guest${lastId.getAndIncrement()}" }
    }

    suspend fun sendNameToClient() {
        val networkMsg = NetworkMessage(ConnectionInfoMessage.MESSAGE_CODE, ConnectionInfoMessage(name, isGuest).toJsonString())
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
        Casino.removePlayerFromTables(name, tablePlayingIds, tableSpectatingIds)
    }

    companion object {
        var lastId = AtomicInteger(1)
    }
}