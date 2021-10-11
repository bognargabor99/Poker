package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.plugins.*
import io.ktor.application.*
import kotlinx.coroutines.DelicateCoroutinesApi

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@DelicateCoroutinesApi
fun Application.module() {
    configureLogging()
    configureHttpsRedirect()
    configureWebSockets()
    configureRouting()
    configureAuthentication()
}

