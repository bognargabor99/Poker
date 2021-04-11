package hu.bme.aut.onlab.poker.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*

object PokerAPI {
    private val client = HttpClient {
        install(WebSockets)
    }

    fun connect(domain: String) {
        GlobalScope.launch {
            client.webSocket(method = HttpMethod.Get, host = "$domain.ngrok.io", path = "/") {
                PokerClient.session = this
                val messageReceivingRoutine = launch { receiveMessages() }
                messageReceivingRoutine.join()
            }
        }
        
    }

    private suspend fun DefaultClientWebSocketSession.receiveMessages() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val receivedBytes = message.readBytes()
                val text = String(receivedBytes)
                Log.d("pokerWebSocket", text)
                PokerClient.receiveText(text)
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }
}