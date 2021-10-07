package hu.bme.aut.onlab.poker.data

import io.ktor.auth.Principal

data class UserAuthInfo(val userName: String, val password: String): Principal