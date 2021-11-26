package hu.bme.aut.onlab.poker.calculation

import android.util.Log
import hu.bme.aut.onlab.poker.dto.PlayerToSpectate
import hu.bme.aut.onlab.poker.model.Card
import hu.bme.aut.onlab.poker.model.Hand
import hu.bme.aut.onlab.poker.model.Suit

object OddsCalculator {
    private const val TRIES = 2000

    fun calculateOdds(tableCards: MutableList<Card>, players: List<PlayerToSpectate>) : Map<String, WinningChance> {
        return if (tableCards.size < 3) {
            getRandomOdds(players)
        } else {
            getExactOdds(tableCards, players)
        }
    }

    private fun getRandomOdds(players: List<PlayerToSpectate>): Map<String, WinningChance> {
        val remainingCards = mutableListOf<Card>()

        val start = 2

        for (value in start..14)
            for (suit in Suit.values())
                if (!players.any { it.inHandCards.contains(Card(value, suit)) })
                    remainingCards.add(Card(value, suit))
        val chanceMap = mutableMapOf<String, WinningChance>()
        players.forEach { player -> chanceMap[player.playerDto.userName] = WinningChance() }
        for (i in 0 until TRIES) {
            if (i % 8 == 0)
                remainingCards.shuffle()
            val tableCards = remainingCards.subList(i % 8 * 5, i % 8 * 5 + 5)
            val odds = getExactOdds0Remains(tableCards, players)
            val winnerCount = odds.values.count { percentage -> percentage == 100}
            players.forEach { player ->
                chanceMap[player.playerDto.userName]?.allHands = chanceMap[player.playerDto.userName]?.allHands?.plus(1)!!
                if (odds[player.playerDto.userName] == 100)
                    if (winnerCount > 1)
                        chanceMap[player.playerDto.userName]?.tieHandsWon = chanceMap[player.playerDto.userName]?.tieHandsWon?.plus(1)!!
                    else
                        chanceMap[player.playerDto.userName]?.handsWon = chanceMap[player.playerDto.userName]?.handsWon?.plus(1)!!
            }
        }
        return chanceMap
    }

    private fun getExactOdds(tableCards: MutableList<Card>, players: List<PlayerToSpectate>): Map<String, WinningChance> {
        Log.d("pokerWeb", "tableCards: $tableCards")
        players.forEach {
            Log.d("pokerWeb", "${it.playerDto.userName}: ${it.inHandCards}")
        }
        val remainingCards = mutableListOf<Card>()

        val start = 2

        for (value in start..14)
            for (suit in Suit.values())
                if (!players.any { it.inHandCards.contains(Card(value, suit)) } && !tableCards.any { it == Card(value, suit) })
                    remainingCards.add(Card(value, suit))

        return when (tableCards.size) {
            3 -> getExactOdds2Remain(tableCards, players, remainingCards)
            4 -> getExactOdds1Remains(tableCards, players, remainingCards)
            else -> {
                val odds = getExactOdds0Remains(tableCards, players)
                val map = mutableMapOf<String, WinningChance>()
                odds.forEach {
                    if (odds.values.count { value -> value == 100 } <= 1) {
                        map[it.key] = WinningChance(1, it.value / 100, 0 )
                    } else {
                        map[it.key] = WinningChance(1, 0, it.value / 100 )
                    }
                }
                map
            }
        }
    }

    private fun getExactOdds0Remains(
        tableCards: List<Card>,
        players: List<PlayerToSpectate>
    ): Map<String, Int> {
        val nameToHand = mutableListOf<Pair<String, Hand>>()
        players.forEach { player ->
            val hand = HandEvaluator.evaluateHand(tableCards + player.inHandCards)
            nameToHand.add(Pair(player.playerDto.userName, hand))
        }
        nameToHand.sortBy { it.second }
        val oddsMap = mutableMapOf<String, Int>()

        nameToHand.forEachIndexed { i, pair ->
            oddsMap[pair.first] = if (i == 0 || nameToHand[0].second == pair.second) 100 else 0
        }
        return oddsMap
    }

    private fun getExactOdds1Remains(
        tableCards: MutableList<Card>,
        players: List<PlayerToSpectate>,
        remainingCards: MutableList<Card>
    ): Map<String, WinningChance> {
        val chanceMap = mutableMapOf<String, WinningChance>()
        players.forEach { chanceMap[it.playerDto.userName] = WinningChance() }
        remainingCards.forEach {
            tableCards.add(it)
            val odds = getExactOdds0Remains(tableCards, players)
            tableCards.removeLast()
            val winnerCount = odds.values.count { percentage -> percentage == 100}
            players.forEach { player ->
                chanceMap[player.playerDto.userName]?.allHands = chanceMap[player.playerDto.userName]?.allHands?.plus(1)!!
                if (odds[player.playerDto.userName] == 100)
                    if (winnerCount > 1)
                        chanceMap[player.playerDto.userName]?.tieHandsWon = chanceMap[player.playerDto.userName]?.tieHandsWon?.plus(1)!!
                    else
                        chanceMap[player.playerDto.userName]?.handsWon = chanceMap[player.playerDto.userName]?.handsWon?.plus(1)!!
            }
        }
        return chanceMap
    }

    private fun getExactOdds2Remain(
        tableCards: MutableList<Card>,
        players: List<PlayerToSpectate>,
        remainingCards: MutableList<Card>
    ): Map<String, WinningChance> {
        val chanceMap = mutableMapOf<String, WinningChance>()
        players.forEach { chanceMap[it.playerDto.userName] = WinningChance() }
        for (i in remainingCards.indices) {
            for (j in (i+1) until remainingCards.size) {
                tableCards.add(remainingCards[i])
                tableCards.add(remainingCards[j])
                val odds = getExactOdds0Remains(tableCards, players)
                tableCards.removeLast()
                tableCards.removeLast()
                val winnerCount = odds.values.count { percentage -> percentage == 100}
                players.forEach { player ->
                    chanceMap[player.playerDto.userName]?.allHands = chanceMap[player.playerDto.userName]?.allHands?.plus(1)!!
                    if (odds[player.playerDto.userName] == 100)
                        if (winnerCount > 1)
                            chanceMap[player.playerDto.userName]?.tieHandsWon = chanceMap[player.playerDto.userName]?.tieHandsWon?.plus(1)!!
                        else
                            chanceMap[player.playerDto.userName]?.handsWon = chanceMap[player.playerDto.userName]?.handsWon?.plus(1)!!
                }
            }
        }
        return chanceMap
    }
}
