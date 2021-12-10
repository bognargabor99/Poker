package hu.bme.aut.thesis.poker.network

import com.google.gson.Gson
import hu.bme.aut.thesis.poker.gamemodel.Casino
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class manages a WebSocket connection
 * @param session The session of the WebSocket connection
 * @param isGuest Is the [User] a guest?
 * @param userName Name of the new user
 * @author Bognar, Gabor Bela
 */
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

    /**
     * Send the name to the client
     * @author Bognar, Gabor Bela
     */
    suspend fun sendNameToClient() {
        val networkMsg = NetworkMessage(ConnectionInfoMessage.MESSAGE_CODE, ConnectionInfoMessage(name, isGuest).toJsonString())
        coroutineScope {
            session.send(networkMsg.toJsonString())
        }
    }

    /**
     * Receives text from a client and forwards it to its [NetworkChain]
     * @param receivedText The received text
     * @author Bognar, Gabor Bela
     */
    fun receiveFromClient(receivedText: String) {
        val request = Gson().fromJson(receivedText, NetworkMessage::class.java)
        chain.process(request)
    }

    /**
     * Sends text to a client
     * @param textToSend The text to send
     * @author Bognar, Gabor Bela
     */
    fun sendToClient(textToSend: String) = GlobalScope.launch { session.send(textToSend) }

    /**
     * This removes this [User] from all tables
     * @author Bognar, Gabor Bela
     */
    fun disconnect() {
        Casino.removePlayerFromTables(name, tablePlayingIds, tableSpectatingIds)
    }

    companion object {
        var lastId = AtomicInteger(1)
    }
}