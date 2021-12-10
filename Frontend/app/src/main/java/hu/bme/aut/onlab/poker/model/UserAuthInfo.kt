package hu.bme.aut.onlab.poker.model

import java.util.Base64

/**
 * Model class for credentials
 * @property userName Name of the user
 * @property password Password of the user
 * @author Bognar, Gabor Bela
 */
data class UserAuthInfo(val userName: String = "", val password: String = "") {
    fun toAuthHeaderValue() : String {
        val base64 = Base64.getEncoder().encodeToString("$userName:$password".toByteArray())
        return "Basic $base64"
    }
}