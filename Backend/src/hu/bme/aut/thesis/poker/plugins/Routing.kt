package hu.bme.aut.thesis.poker.plugins

import hu.bme.aut.thesis.poker.data.DatabaseHelper
import hu.bme.aut.thesis.poker.data.UserAuthInfo
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        gson()
    }
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "it works!")
        }
        post("/register") {
            val newUser = call.receive<UserAuthInfo>()
            if (DatabaseHelper.registerUser(newUser))
                call.respond(HttpStatusCode.Created,"Registered new user: ${newUser.userName}")
            else
                call.respond(HttpStatusCode.Conflict, "Username already in use: ${newUser.userName}")
        }
    }
}