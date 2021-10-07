package hu.bme.aut.onlab.poker.gamemodel

import java.util.concurrent.atomic.AtomicInteger

open class Person {
    lateinit var userName: String
    val id = lastPlayerId.getAndIncrement()

    companion object {
        var lastPlayerId = AtomicInteger(1000)
    }
}