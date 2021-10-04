package hu.bme.aut.onlab.poker.plugins

import io.ktor.application.*
import io.ktor.features.*

fun Application.configureHttpsRedirect() {
    install(HttpsRedirect) {
        sslPort = 8443
        permanentRedirect = true
    }
}