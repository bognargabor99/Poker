package hu.bme.aut.thesis.poker

import hu.bme.aut.thesis.poker.plugins.*
import io.ktor.application.*
import io.ktor.network.tls.certificates.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File

/**
 * The main function creates the self-signed SSL certificate
 * and starts the server
 * @author Bognar, Gabor Bela
 */
fun main(args: Array<String>) {
    val keyStoreFile = File("build/keystore.jks")
    generateCertificate(
        file = keyStoreFile,
        keyAlias = "sampleAlias",
        keyPassword = "foobar",
        jksPassword = "foobar"
    )
    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Configures separate functions of the server
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
fun Application.module() {
    configureLogging()
    configureHttpsRedirect()
    configureWebSockets()
    configureRouting()
    configureAuthentication()
}

