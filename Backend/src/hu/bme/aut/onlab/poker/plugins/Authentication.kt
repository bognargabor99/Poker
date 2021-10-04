package hu.bme.aut.onlab.poker.plugins

import hu.bme.aut.onlab.poker.data.DatabaseHelper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureAuthentication() {
    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to the '/authenticate' path"
            validate { credentials ->
                DatabaseHelper.authenticate(credentials)
            }
        }
    }

    routing {
        authenticate("auth-basic") {
            get("/authenticate") {
                call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
    }
}