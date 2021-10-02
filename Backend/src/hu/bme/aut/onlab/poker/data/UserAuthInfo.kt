package hu.bme.aut.onlab.poker.data

import io.ktor.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class UserAuthInfo(val userName: String, val password: String): Principal