package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.Suit
import hu.bme.aut.onlab.poker.network.User
import hu.bme.aut.onlab.poker.network.UserCollection
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.*
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/poker") {
            val thisUser = User(this)
            UserCollection += thisUser
            thisUser.sendToClient("You're connected")
            val card = Card(10, Suit.SPADES)
            thisUser.sendToClient(Json.encodeToString(card))
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
                UserCollection -= thisUser
            }
        }
    }
}

