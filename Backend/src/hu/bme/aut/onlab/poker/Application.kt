package hu.bme.aut.onlab.poker

import hu.bme.aut.onlab.poker.data.DatabaseHelper
import hu.bme.aut.onlab.poker.plugins.*
import io.ktor.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    configureHttpsRedirect()
    configureWebSockets()
    configureRouting()
    configureAuthentication()

    println(DatabaseHelper.db.name)
}

