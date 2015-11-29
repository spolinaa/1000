package kotlin

import java.util.*

public class Computer() : Player() {

    override internal fun activeClick() : Card {""
        val card = activeStrategy()
        Game.activeSuit = card.suit
        return card
    }

    override internal fun passiveClick() : Card = passiveStrategy()

    override internal fun askPointsDivision() : Boolean {
        if (sum < obligation) { return true }
        return false
    }

    override protected fun askPlayerToRaise(nextBid : Int) : Int {
        sum = cardAnalysis(handCards)
        if (sum < nextBid) { return 0 }
        return sum
    }

    override internal fun finalObligation() : Int = sum

    private fun findLowCards(n : Int, array : ArrayList<Card>) {
        val size = array.size
        var resArray = arrayOf(0, 1)
        for (i in 0..size - 1) {
            val currentCardRank = array[i].rank
            val resCardRank1 = array[0].rank
            val resCardRank2 = array[1].rank
            val currentCardIndex = handCards.indexOf(array[i])
            if (resCardRank1 > currentCardRank) { resArray[0] = currentCardIndex }
            else if (resCardRank2 > currentCardRank) { resArray[1] = currentCardIndex }
        }
        secondCardNumber = resArray[0]
        if (n > 1) { firstCardNumber = resArray[1] }
    }

    override protected fun chooseCardsToGive() {
        val sizeBad = badCards.size
        when (sizeBad) {
            2 -> {
                 firstCardNumber = handCards.indexOf(badCards[0])
                 firstCardNumber = handCards.indexOf(badCards[1])
            }
            1 -> {
                firstCardNumber = handCards.indexOf(badCards[0])
                findLowCards(1, goodCards)
            }
            0 -> { findLowCards(2, goodCards) }
        }
    }

    private var goodCards : ArrayList<Card> = ArrayList() //карты, которыми можно взять взятку
    private var badCards  : ArrayList<Card> = ArrayList() //карты, которыми нельзя взять взятку
    private var sum = 0

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

        if (sizeMarriage > 0) {
            val marriageSuit = arrayOfMarriages[sizeMarriage - 1]
            marriagesToUse(marriageSuit)
        }
        else { Game.sortBySuits(goodCards) }
        return sum
    }

    private fun marriagesToUse(suit : String) {
        val kingRank  = 4
        val queenRank = 3
        var last  = arrayOfMarriages.size - 1
        val king  = Card(suit, kingRank)
        val queen = Card(suit, queenRank)
        when (suit) {
            "spades"   -> { sum += 40  }
            "clubs"    -> { sum += 60  }
            "diamonds" -> { sum += 80  }
            "hearts"   -> { sum += 100 }
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

    private fun activeStrategy() : Card {
        inaccessibleCardsAnalysis()
        val goodSize = goodCards.size
        var card : Card
        if (goodSize > 0) {
            card = goodCards[0]
            goodCards.remove(card)
        }
        else {
            val badSize = badCards.size
            if (badSize > 0) {
                findLowCards(1, badCards)
                card = badCards[secondCardNumber]  ///взятку не взять, поэтому ходить самой маленькой
                badCards.remove(card)
            }
            else {
                card = handCards[0] //тут могут быть только дамы из захваленных пар, но взятку ими не взять
            }
        }
        handCards.remove(card)
        inaccessibleCards.add(card)
        return card
    }

    private fun removeCard(card : Card, array : ArrayList<Card>) : Card {
        array.remove(card)
        handCards.remove(card)
        inaccessibleCards.add(card)
        return card
    }

    private fun passiveStrategy() : Card {
        inaccessibleCardsAnalysis()
        val badSize = badCards.size
        val availableCards = availableCards()
        val availableSize = availableCards.size
        var availableBadCards : ArrayList<Card> = ArrayList()
        if (badSize > 0) {
            for (i in 0..availableSize - 1) { //выбор самой маленькой из доступных badCards
                val card = availableCards[i]
                if (badCards.contains(availableCards[i])) { availableBadCards.add(card) }
            }
            findLowCards(1, availableBadCards)
            return removeCard(handCards[secondCardNumber], badCards)
        }
        findLowCards(1, availableCards) //если badCards нет, тогда отдаем самую маленькую из handCards
        return removeCard(handCards[secondCardNumber], availableCards)
    }

    internal var inaccessibleCards : ArrayList<Card> = ArrayList()

    private fun inaccessibleCardsAnalysis() {
        inaccessibleCards = Game.sortBySuits(inaccessibleCards)
        val size = inaccessibleCards.size
        var pointArray = Array(4, { 0 })
        var index = 0
        for (i in 0..size - 1) {
            index = suitIndex(inaccessibleCards[i].suit)
            pointArray[index] += inaccessibleCards[i].rank
        }
        val handSize = handCards.size
        for (i in 0..handSize - 1) {
            val card = handCards[i]
            if (!goodCards.contains(card)) {
                index = suitIndex(handCards[i].suit)
                if (hasPreviousCards(pointArray[index], card.rank)) {
                    if (badCards.contains(card)) { badCards.remove(card) }
                    goodCards.add(card)
                }
            }
        }
        goodCards = Game.sortBySuits(goodCards)
    }

    private fun suitIndex(suit : String) : Int {
        when (suit) {
            "spades"   -> { return 0 }
            "clubs"    -> { return 1 }
            "diamonds" -> { return 2 }
            "hearts"   -> { return 3 }
        }
        return 0
    }

    private fun hasPreviousCards(sum : Int, rank : Int) : Boolean {
        when (rank) {
            10 -> { return (sum == 11) }
            4  -> { return (sum == 21) }
            3  -> { return (sum == 25) }
            2  -> { return (sum == 28) }
            0  -> { return (sum == 30) }
        }
        return false
    }
}