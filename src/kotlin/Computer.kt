package kotlin

import java.util.*

public class Computer() : Player() {
    override internal fun click() {}
    override internal fun askPointsDivision() : Boolean {
        if (sum < obligation) { return true }
        return false
    }
    override internal fun askObligation(bid : Int) : Int {
        val step = 5
        sum = cardAnalysis(handCards)
        if (sum >= bid + step) { return sum }
        return 0
    }

    override internal fun finalObligation() : Int = sum

    private fun findLowCards(n : Int) {
        val size = goodCards.size
        var resArray = arrayOf(0, 1)
        for (i in 2..size - 1) {
            val currentCardRank = goodCards[i].rank
            val resCardRank1 = goodCards[0].rank
            val resCardRank2 = goodCards[1].rank
            val currentCardIndex = handCards.indexOf(goodCards[i])
            if (resCardRank1 > currentCardRank) { resArray[0] = currentCardIndex }
            else if (resCardRank2 > currentCardRank) { resArray[1] = currentCardIndex }
        }
        secondCardNumber = resArray[0]
        if (n > 1) { firstCardNumber = resArray[1] }
    }

    override protected fun chooseCards() {
        val sizeBad = badCards.size
        when (sizeBad) {
            2 -> {
                 firstCardNumber = handCards.indexOf(badCards[0])
                 firstCardNumber = handCards.indexOf(badCards[1])
            }
            1 -> {
                firstCardNumber = handCards.indexOf(badCards[0])
                findLowCards(1)
            }
            0 -> { findLowCards(2) }
        }
    }

    override protected var firstCardNumber = 0
    override protected var secondCardNumber = 1

    var goodCards : ArrayList<Card> = ArrayList()
    var badCards  : ArrayList<Card> = ArrayList()
    var sum = 0

    private fun cardAnalysis(cards : ArrayList<Card>) : Int {
        var sum = 0
        val size = cards.size
        val additionalScore = 5
        for (i in 0..size - 1) {
            val suit = cards[i].suit
            val rank = cards[i].rank
            var nextRank = 0
            if (rank != 11) {
                when (rank) {
                    10 -> { nextRank = 11 }
                    4  -> { nextRank = 10 }
                    3 ->  { nextRank = 4  }
                    2 ->  { nextRank = 3  }
                    0 ->  { nextRank = 2  }
                }
                val highestCard = Card(suit, nextRank)
                if (goodCards.contains(highestCard)) {
                    goodCards.add(handCards[i])
                    sum += rank + additionalScore
                }
                else { badCards.add(handCards[i]) }
            }
            else { goodCards.add(handCards[i]); sum += 11 + additionalScore }
        }
        val sizeMarriage = arrayOfMarriages.size
        val marriageSuit = arrayOfMarriages[sizeMarriage - 1]
        if (sizeMarriage > 0) { marriagesToUse(marriageSuit) }
        else { Game.sortBySuits(goodCards) }
        return sum
    }

    private fun marriagesToUse(suit : Char) {
        val kingRank  = 4
        val queenRank = 3
        var last  = arrayOfMarriages.size - 1
        val king  = Card(suit, kingRank)
        val queen = Card(suit, queenRank)
        when (suit) {
            's' -> { sum += 40  }
            'c' -> { sum += 60  }
            'd' -> { sum += 80  }
            'h' -> { sum += 100 }
        }
        if (!goodCards.contains(king)) {
            Game.sortBySuits(goodCards)
            val sizeGood = goodCards.size
            goodCards.add(sizeGood, king)
            val sizeBad = badCards.size
            badCards.remove(queen)
        }
        else {
            last--
            if (last >= 0) { marriagesToUse(arrayOfMarriages[last]) }
        }
    }
}