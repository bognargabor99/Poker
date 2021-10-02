package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.data.UserAuthInfo
import hu.bme.aut.onlab.poker.network.User
import hu.bme.aut.onlab.poker.network.UserCollection
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.http.cio.websocket.*
import io.ktor.network.tls.certificates.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.websocket.*
import java.io.File
import java.time.*
import java.sql.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
        anyHost()
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/register") {
            //val user = call.receive<UserAuthInfo>()
            call.respond(HttpStatusCode.Created, "itt a srác")
        }

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

