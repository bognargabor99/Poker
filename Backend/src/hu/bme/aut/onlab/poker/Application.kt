package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.network.User
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    val pokerUsers = Collections.synchronizedSet<User?>(LinkedHashSet())
    routing {
        webSocket("/poker") {
            val thisUser = User(this)
            pokerUsers += thisUser
            thisUser.sendToClient("General Kenobi")
            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    thisUser.receiveFromClient(receivedText)
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisUser!")
                pokerUsers -= thisUser
            }
        }
    }
}

