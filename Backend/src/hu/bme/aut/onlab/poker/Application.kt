package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.plugins.*
import io.ktor.application.*
import io.ktor.network.tls.certificates.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File

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

@DelicateCoroutinesApi
fun Application.module() {
    configureLogging()
    configureHttpsRedirect()
    configureWebSockets()
    configureRouting()
    configureAuthentication()
}

