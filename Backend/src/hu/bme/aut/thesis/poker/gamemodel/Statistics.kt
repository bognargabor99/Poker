package hu.bme.aut.thesis.poker.gamemodel

import java.time.LocalDate

/**
 * Represents the statistics of a [User] in the database or at the [Table]
 * @param allHands All hands played by the player
 * @param biggestPotWon Biggest pot won by the player
 * @param flopsSeen Number of flops seen (stayed in the turn when it came down)
 * @param handsWon All hands won by the player
 * @param playersBusted Number of players busted from a [Table]
 * @param raiseCount Number of times the player raised at a [Table]
 * @param registerDate Date of registration
 * @param riversSeen Number of rivers seen (stayed in the turn when it came down)
 * @param showDownCount Number of showdowns the player participated in
 * @param tablesPlayed Number of [Table]s the player played at
 * @param tablesWon Number of [Tables]s the player has won
 * @param totalChipsWon Total number of chips won by the player
 * @param turnsSeen Number of turns seen (stayed in the turn when it came down)
 * @author Bognar, Gabor Bela
 */
data class Statistics (
    var registerDate: String = LocalDate.now().toString(),
    var tablesPlayed: Int = 0,
    var tablesWon: Int = 0,
    var allHands: Int = 0,
    var handsWon: Int = 0,
    var totalChipsWon: Int = 0,
    var biggestPotWon: Int = 0,
    var raiseCount: Int = 0,
    var showDownCount: Int = 0,
    var flopsSeen: Int = 0,
    var turnsSeen: Int = 0,
    var riversSeen: Int = 0,
    var playersBusted: Int = 0 // TODO
)