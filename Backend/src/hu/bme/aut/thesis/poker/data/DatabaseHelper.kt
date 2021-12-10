package hu.bme.aut.thesis.poker.data

import hu.bme.aut.thesis.poker.gamemodel.Statistics
import hu.bme.aut.thesis.poker.network.User
import io.ktor.auth.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.time.LocalDate
import kotlin.math.max

/**
 * This class implements the whole data access layer, manages the database,
 * creates and deletes [User]s and [Statistics]
 * @author Bognar, Gabor Bela
 */
object DatabaseHelper {
    /**
     * Stores data about the managed database
     * @author Bognar, Gabor Bela
     */
    private val db by lazy {
        Database.connect("jdbc:sqlite:resources/poker.db", "org.sqlite.JDBC")
    }

    init {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }

    /**
     * Extension method for [String] class to create the SHA-256 hash of the string
     * @author Bognar, Gabor Bela
     */
    private fun String.hash(): String {
        val hexChars = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(this.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }

        return result.toString()
    }

    /**
     * Authenticates the [User] with the given credentials
     * @param credentials Credentials of the [User]
     * @return A [Principal] if the database contains a user with the given credentials; otherwise null
     * @author Bognar, Gabor Bela
     */
    fun authenticate(credentials: UserPasswordCredential): Principal? =
        transaction(db) {
            val query = Users.select { Users.userName eq credentials.name }
            return@transaction if (query.count() == 1L && credentials.password.hash() == query.first()[Users.passwordHash])
                UserIdPrincipal(credentials.name)
            else
                null
        }

    /**
     * Register a new [User] with the given credentials
     * @param newUser Credentials of the new [User]
     * @return True, if the [User] was successfully registered; otherwise false
     * @author Bognar, Gabor Bela
     */
    fun registerUser(newUser: UserAuthInfo) : Boolean =
        transaction(db) {
            if (Users.select { Users.userName eq newUser.userName }.count() > 0L)
                return@transaction false

            Users.insert {
                it[userName] = newUser.userName
                it[passwordHash] = newUser.password.hash()
            }

            UserStatistics.insert {
                it[userName] = newUser.userName
            }

            return@transaction Users.select { Users.userName eq newUser.userName }.count() == 1L
        }

    /**
     * Deletes a [User] from the database
     * @param userToDelete Name of the [User] to delete
     * @author Bognar, Gabor Bela
     */
    fun deleteUser(userToDelete: String) {
        transaction(db) {
            Users.deleteWhere { Users.userName eq userToDelete }
        }
    }

    /**
     * Updates the stats of a [User].
     * @param userName Name of the [User]
     * @param stats [Statistics] to update with
     * @author Bognar, Gabor Bela
     */
    fun updateStatsForUser(userName: String, stats: Statistics) {
        transaction(db) {
            val biggestPot = UserStatistics.select { UserStatistics.userName eq userName }.single()[UserStatistics.biggestPotWon]
            UserStatistics.update (where = { UserStatistics.userName eq userName }) {
                it.update(biggestPotWon, Expression.build { biggestPotWon - biggestPotWon })
            }
            commit()
            UserStatistics.update (where = { UserStatistics.userName eq userName }) {
                it.update(allTablesPlayed, Expression.build { allTablesPlayed + 1 })
                it.update(tablesWon, Expression.build { tablesWon + stats.tablesWon })
                it.update(allHands, Expression.build { allHands + stats.allHands })
                it.update(handsWon, Expression.build { handsWon + stats.handsWon })
                it.update(totalChipsWon, Expression.build { totalChipsWon + stats.totalChipsWon })
                it.update(biggestPotWon, Expression.build { biggestPotWon + max(biggestPot, stats.biggestPotWon) })
                it.update(raiseCount, Expression.build { raiseCount + stats.raiseCount })
                it.update(showDownCount, Expression.build { showDownCount + stats.showDownCount })
                it.update(flopsSeen, Expression.build { flopsSeen + stats.flopsSeen })
                it.update(turnsSeen, Expression.build { turnsSeen + stats.turnsSeen })
                it.update(riversSeen, Expression.build { riversSeen + stats.riversSeen })
                it.update(playersBusted, Expression.build { playersBusted + stats.playersBusted })
            }
        }
    }

    /**
     * Queries [Statistics] of a [User]
     * @param userName Name of the user
     * @return The [Statistics] of the user
     * @author Bognar, Gabor Bela
     */
    fun getStatisticsByName(userName: String): Statistics =
        transaction(db) {
            if (Users.select { Users.userName eq userName }.count() > 1L)
                return@transaction Statistics()

            val userStat = UserStatistics.select { UserStatistics.userName eq userName }.limit(1)
            if (userStat.count() != 1L)
                return@transaction Statistics()

            return@transaction userStat.first().run {
                Statistics(
                    registerDate = this[UserStatistics.registerDate],
                    tablesPlayed = this[UserStatistics.allTablesPlayed],
                    tablesWon = this[UserStatistics.tablesWon],
                    allHands = this[UserStatistics.allHands],
                    handsWon = this[UserStatistics.handsWon],
                    totalChipsWon = this[UserStatistics.totalChipsWon],
                    biggestPotWon = this[UserStatistics.biggestPotWon],
                    raiseCount = this[UserStatistics.raiseCount],
                    showDownCount = this[UserStatistics.showDownCount],
                    flopsSeen = this[UserStatistics.flopsSeen],
                    turnsSeen = this[UserStatistics.turnsSeen],
                    riversSeen = this[UserStatistics.riversSeen],
                    playersBusted = this[UserStatistics.playersBusted]
                )
            }
        }
}

/**
 * Static class representing the "users" table in our database
 * @author Bognar, Gabor Bela
 */
object Users: Table(name = "users") {
    override val primaryKey = PrimaryKey(integer("id").autoIncrement(), name = "id")
    val userName = varchar("username", 50)
    val passwordHash = varchar("password", 200)
}

/**
 * Static class representing the "statistics" table in our database
 * @author Bognar, Gabor Bela
 */
object UserStatistics: Table(name = "statistics") {
    override val primaryKey = PrimaryKey(integer("id").autoIncrement(), name = "id")
    val userName = varchar("username", 50) references Users.userName
    val registerDate = varchar("registerDate", 40).default(LocalDate.now().toString())
    val allTablesPlayed = integer("tables").default(0)
    val tablesWon = integer("tablesWon").default(0)
    val allHands = integer("allHands").default(0)
    val handsWon = integer("handsWon").default(0)
    val totalChipsWon = integer("totalChipsWon").default(0)
    val biggestPotWon = integer("biggestPotWon").default(0)
    val raiseCount = integer("raiseCount").default(0)
    val showDownCount = integer("showDownCount").default(0)
    val flopsSeen = integer("flopsSeen").default(0)
    val turnsSeen = integer("turnsSeen").default(0)
    val riversSeen = integer("riversSeen").default(0)
    val playersBusted = integer("playersBusted").default(0)
}