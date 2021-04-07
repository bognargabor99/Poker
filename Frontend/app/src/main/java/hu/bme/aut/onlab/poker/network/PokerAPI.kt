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

    fun connect(block: () -> Unit) {
        GlobalScope.launch {
            client.webSocket(method = HttpMethod.Get, host = "bf3ca77d6aca.ngrok.io", path = "/") {
                val messageOutputRoutine = launch { outputMessages() }
                //val userInputRoutine = launch { inputMessages() }
                block()
                //userInputRoutine.join() // Wait for completion; either "exit" or error
                messageOutputRoutine.join()
            }
        }
        
    }

    private suspend fun DefaultClientWebSocketSession.outputMessages() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val receivedBytes = message.readBytes()
                Log.d("pokerWebSocket", String(receivedBytes))
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }

    /*private suspend fun DefaultClientWebSocketSession.inputMessages() {
        while (true) {
            val message = readLine() ?: ""
            if (message.equals("exit", true)) return
            try {
                send(message)
            } catch (e: Exception) {
                println("Error while sending: " + e.localizedMessage)
                return
            }
        }
    }*/
}