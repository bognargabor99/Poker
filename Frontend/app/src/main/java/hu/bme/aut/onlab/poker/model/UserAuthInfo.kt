package hu.bme.aut.onlab.poker.model

import java.util.Base64

data class UserAuthInfo(val userName: String = "", val password: String = "") {
    fun toAuthHeaderValue() : String {
        val base64 = Base64.getEncoder().encodeToString("$userName:$password".toByteArray())
        return "Basic $base64"
    }
}