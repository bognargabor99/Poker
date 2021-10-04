package hu.bme.aut.onlab.poker.data

import io.ktor.auth.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest

object DatabaseHelper {
    val db by lazy {
        Database.connect("jdbc:sqlite:resources/poker.db", "org.sqlite.JDBC")
    }

    init {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(Users)
            /*Users.insert {
                it[userName] = "admin"
                it[passwordHash] = "admin".hash()
            }*/
        }
    }

    private fun String.hash(): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(this.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }

    fun authenticate(credentials: UserPasswordCredential): Principal? {
        return transaction {
            val query = Users.select { Users.userName eq credentials.name }
            return@transaction if (query.count() == 1L && credentials.password.hash() == query.first()[Users.passwordHash])
                UserIdPrincipal(credentials.name)
            else
                null
        }
    }

    fun registerUser(newUser: UserAuthInfo) : Boolean = transaction {
        if (Users.select { Users.userName eq newUser.userName }.count() > 0L)
            return@transaction false

        Users.insert {
            it[userName] = newUser.userName
            it[passwordHash] = newUser.password.hash()
        }

        return@transaction Users.select { Users.userName eq newUser.userName }.count() == 1L
    }
}

object Users: Table(name = "users") {
    override val primaryKey = PrimaryKey(integer("id").autoIncrement(), name = "id")
    val userName = varchar("username", 50)
    val passwordHash = varchar("password", 200)
}