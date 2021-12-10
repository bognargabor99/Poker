package hu.bme.aut.thesis.poker.plugins

import io.ktor.application.*
import io.ktor.features.*

/**
 * Configures forcing of HTTPS redirection
 * @author Bognar, Gabor Bela
 */
fun Application.configureHttpsRedirect() {
    install(HttpsRedirect) {
        sslPort = 8443
        permanentRedirect = true
    }
}