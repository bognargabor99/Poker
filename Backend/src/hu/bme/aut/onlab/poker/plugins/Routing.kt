package hu.bme.aut.onlab.poker.plugins

import hu.bme.aut.onlab.poker.data.DatabaseHelper
import hu.bme.aut.onlab.poker.data.UserAuthInfo
import hu.bme.aut.onlab.poker.network.User
import hu.bme.aut.onlab.poker.network.UserCollection
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        gson()
    }
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "it works!")
        }
        get("/register") {
            val newUser = call.receive<UserAuthInfo>()
            if (DatabaseHelper.registerUser(newUser))
                call.respond(HttpStatusCode.Created,"Registered new user: ${newUser.userName}")
            else
                call.respond(HttpStatusCode.Conflict, "Username already in use: ${newUser.userName}")
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