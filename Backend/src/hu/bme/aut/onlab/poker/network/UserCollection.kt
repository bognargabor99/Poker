package hu.bme.aut.onlab.poker.network

import java.util.*

object UserCollection {
    private val users = Collections.synchronizedSet<User?>(LinkedHashSet())

    operator fun plusAssign(newUser: User) {
        users.add(newUser)
    }

    operator fun minusAssign(user: User) {
        users.remove(user)
    }
}
