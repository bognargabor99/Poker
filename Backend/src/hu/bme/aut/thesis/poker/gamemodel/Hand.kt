package hu.bme.aut.thesis.poker.gamemodel

data class Hand(
    val type: HandType,
    val values: List<Int>
) : Comparable<Hand> {
    override fun compareTo(other: Hand): Int {
        if (type.compareTo(other.type)!=0)
            return type.compareTo(other.type)
        else
            for (i in values.indices)
                if (values[i]!=other.values[i])
                    return -values[i].compareTo(other.values[i])
        return 0
    }
}
