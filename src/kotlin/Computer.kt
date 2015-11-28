package kotlin

import java.util.*

public class Computer() : Player() {

    override internal fun activeClick() : Card {
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

    override internal var firstCardNumber = 0
    override internal var secondCardNumber = 1

    private var goodCards : ArrayList<Card> = ArrayList()
    private var badCards  : ArrayList<Card> = ArrayList()
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

    private fun activeStrategy() : Card {  /// *учитывать подсчет выпавших карт
        val goodSize = goodCards.size
        var card : Card
        if (goodSize > 0) {
            card = goodCards[0]
            goodCards.removeAt(0)

        }
        else {
            val badSize = badCards.size
            if (badSize > 0) {
                card = badCards[0]  ///тут надо выбирать лучшую карту для хода *
                badCards.removeAt(0)
            }
            else {
                card = handCards[0] ///тут надо выбирать лучшую карту для хода *
            }
        }
        handCards.remove(card)
        return card
    }

    private fun removeCard(card : Card, array : ArrayList<Card>) : Card {
        array.remove(card)
        handCards.remove(card)
        return card
    }

    private fun passiveStrategy() : Card { ///*учитывать подсчет выпавших карт
        val badSize = badCards.size                        ///*после каждого хода активного добавлять карты в goodCards (удалять из badCards), если возможно
        val availableCards = availableCards()
        val availableSize = availableCards.size
        var i = 0
        if (badSize > 0) {
            while (!badCards.contains(availableCards[i]) && i < availableSize)  {
                i++
            }
            if (i < availableSize || badCards.contains(availableCards[i])) {
                return removeCard(availableCards[i], badCards)
            }
        }
        findLowCards(1, availableCards)
        return removeCard(handCards[secondCardNumber], availableCards)
    }
}