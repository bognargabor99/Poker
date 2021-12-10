package hu.bme.aut.thesis.poker.gamemodel

import java.util.concurrent.atomic.AtomicInteger

/**
 * Represents a [User] at the [Table] regardless of its function (player or spectator)
 * @property userName Name of the represented user
 * @author Bognar, Gabor Bela
 */
open class Person {
    lateinit var userName: String
    val id = lastPlayerId.getAndIncrement()

    companion object {
        var lastPlayerId = AtomicInteger(1000)
    }
}