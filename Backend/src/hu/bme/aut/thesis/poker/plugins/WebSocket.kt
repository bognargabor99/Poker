package hu.bme.aut.thesis.poker.plugins

import hu.bme.aut.thesis.poker.data.DatabaseHelper
import hu.bme.aut.thesis.poker.data.UserAuthInfo
import hu.bme.aut.thesis.poker.network.User
import hu.bme.aut.thesis.poker.network.UserCollection
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.time.Duration

/**
 * Configures WebSocket connection to the root endpoint with authorization protocol
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
fun Application.configureWebSockets(testing: Boolean = false) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/") {
            var username = if (call.request.headers.contains("Authorization"))
                 tryAuthenticate(call.request.headers["Authorization"]!!)
            else
                ""
            if (UserCollection.isAlreadyAuthenticated(username))
                username = ""


            val thisUser = User(this, username.isBlank(), username)
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
                e.printStackTrace()
            } finally {
                println("Removing ${thisUser.name}!")
                thisUser.disconnect()
                UserCollection -= thisUser
            }
        }
    }
}

/**
 * Tries authenticating the user through the database
 * @param authHeaderValue The value of the "Authorization" header in the WebSocket connection request
 * @return The name that user is signed in with.
 * It's an empty [String] if the credentials did not match any user in the database
 * @author Bognar, Gabor Bela
 */
fun tryAuthenticate(authHeaderValue: String) : String {
    val authInfo = UserAuthInfo.fromAuthHeaderValue(authHeaderValue)
    val principal = DatabaseHelper.authenticate(UserPasswordCredential(authInfo.userName, authInfo.password))
    return if (principal != null) authInfo.userName else ""
}