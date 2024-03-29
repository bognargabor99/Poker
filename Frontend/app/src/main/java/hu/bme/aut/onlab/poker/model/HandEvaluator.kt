package hu.bme.aut.onlab.poker.model

object HandEvaluator {
    fun evaluateHand(fromCards: MutableList<Card>) : Hand {
        val sortedCards: List<Card> = fromCards.sortedDescending()
        //Flush section
        val flushHand = getFlushType(sortedCards)
        if (flushHand!=null)
            return flushHand
        //Straight section
        val straightStartValue = getStraightSequenceFrom(sortedCards.map { it.value }.distinct())
        if (straightStartValue != -1)
            return Hand(HandType.STRAIGHT, listOf(straightStartValue))
        //Pairs
        val valueCounts = MutableList(13) { Pair(it + 2, 0) }
        sortedCards.forEach { valueCounts[it.value-2] = valueCounts[it.value-2].copy(second = valueCounts[it.value-2].second + 1) }
        return getPairType(valueCounts)
    }

    private fun getFlushType(sortedCards: List<Card>): Hand? {
        val suitCounts = IntArray(4)
        sortedCards.forEach { suitCounts[it.suit.ordinal]++ }
        val indexOfFlush = suitCounts.indexOfFirst { it >= 5 }
        if (indexOfFlush != -1) {
            val flushValues = sortedCards.filter { it.suit.ordinal == indexOfFlush }
                .map { it.value } as MutableList<Int>
            if (flushValues[0] == 14)
                flushValues.add(1)

            val startValue = getStraightSequenceFrom(flushValues)
            if (startValue != -1) {
                if (startValue == 14)
                    return Hand(HandType.ROYAL_FLUSH, listOf(14))
                return Hand(HandType.STRAIGHT_FLUSH, listOf(startValue))
            }
            return Hand(HandType.FLUSH, List(5) { flushValues[it] })
        }
        return null
    }

    private fun getPairType(valueCounts: MutableList<Pair<Int, Int>>): Hand {
        val sortedCounts = valueCounts.sortedWith(compareBy({-it.second}, {-it.first}))
        when (sortedCounts[0].second) {
            4 -> return Hand(HandType.FOUR_OF_A_KIND, List(2) { sortedCounts[it].first })
            3 -> {
                if (sortedCounts[1].second >= 2)
                    return Hand(HandType.FULL_HOUSE, List(2) { sortedCounts[it].first })
                return Hand(HandType.THREE_OF_A_KIND, List(3) { sortedCounts[it].first })
            }
            2 -> {
                if (sortedCounts[1].second == 2)
                    return Hand(HandType.TWO_PAIR, List(3) { sortedCounts[it].first })
                return Hand(HandType.PAIR, List(4) { sortedCounts[it].first })
            }
            else -> return Hand(HandType.HIGH_CARD, List(5) { sortedCounts[it].first })
        }
    }

    private fun getStraightSequenceFrom(values: List<Int>) : Int {
        var startingValue = values[0]
        var straightCount = 1
        for (i in 1 until values.size) {
            if (values[i] + 1 == values[i - 1]) {
                straightCount++
                if (straightCount==5)
                    return startingValue
            }
            else {
                straightCount = 1
                startingValue = values[i]
            }
        }
        return -1
    }
}