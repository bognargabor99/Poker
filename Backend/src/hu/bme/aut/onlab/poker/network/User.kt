package hu.bme.aut.onlab.poker.network

import io.ktor.http.cio.websocket.*
import hu.bme.aut.onlab.poker.gamemodel.Player
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

class User(private val session: DefaultWebSocketSession) {
    val name = "user${lastId.getAndIncrement()}"
    private val chain = NetworkChain()

    init {
        UserCollection += this
    }

    suspend fun receiveFromClient(receivedText: String) {
        val request = Json.decodeFromString<NetworkRequest>(receivedText)
        println("decoded JSON request")
        chain.process(request)
    }

    suspend fun sendToClient(textToSend: String) = session.send(textToSend)

    suspend fun askForAction(toCall: Int) = sendToClient(Json.encodeToString(AskActionMessage(toCall)))

    companion object {
        var lastId = AtomicInteger(1)
    }
}