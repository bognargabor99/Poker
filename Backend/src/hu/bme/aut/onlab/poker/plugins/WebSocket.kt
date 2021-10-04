package hu.bme.aut.onlab.poker.plugins

import hu.bme.aut.onlab.poker.network.User
import hu.bme.aut.onlab.poker.network.UserCollection
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/") {
            println(call.request.headers.toMap().toString())
            val thisUser = User(this)
            UserCollection += thisUser
            println("adding ${thisUser.name}")
            thisUser.sendNameToClient()
            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    println(receivedText)
                    thisUser.receiveFromClient(receivedText)
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing ${thisUser.name}!")
                thisUser.disconnect()
                UserCollection -= thisUser
            }
        }
    }
}