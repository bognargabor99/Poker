package hu.bme.aut.thesis.poker.plugins

import hu.bme.aut.thesis.poker.data.DatabaseHelper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * This function configures the authentication
 * with a helper REST endpoint to help client with authentication
 * @author Bognar, Gabor Bela
 */
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
                call.respond(HttpStatusCode.OK,"Hello, ${call.principal<UserIdPrincipal>()?.name}!")
            }
        }
    }
}