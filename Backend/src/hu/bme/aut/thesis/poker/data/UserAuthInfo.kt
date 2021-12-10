package hu.bme.aut.thesis.poker.data

import io.ktor.auth.Principal
import java.util.Base64

/**
 * Model class for [User] credentials
 * @property userName Name of the user
 * @property password Password of the user
 * @author Bognar, Gabor Bela
 */
data class UserAuthInfo(val userName: String = "", val password: String = ""): Principal {
    companion object {
        fun fromAuthHeaderValue(base64String: String) : UserAuthInfo {
            val toDecode = if (base64String.startsWith("Basic ")) base64String.drop(6) else base64String
            val authInfo = String(Base64.getDecoder().decode(toDecode)).split(':')
            if (authInfo.size != 2)
                return UserAuthInfo()
            return UserAuthInfo(authInfo[0], authInfo[1])
        }
    }
}