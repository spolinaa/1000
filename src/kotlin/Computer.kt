/* Trick-taking game for three players "1000"
by Sokolova Polina & Kuzmina Liza */

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
        sum = cardAnalysis()
        val c = 20
        if (sum + c < obligation) {
            println("$name: Распишем\n")
            return true
        }
        return false
    }

    override protected fun askPlayerToRaise(nextBid : Int) : Int {
        sum = cardAnalysis() + 10
        if (sum < nextBid) { return 0 }
        return sum
    }

    override internal fun finalObligation() {
        if (obligation < sum) {
            obligation = (sum div 5) * 5
        }
        println("$name: Иду на $obligation")
    }

    private fun findLowCards(n : Int, array : ArrayList<Card>) {
        var resArray = arrayOf(0, 1)
        for (i in array) {
            var resCardRank1 = handCards[resArray[0]].rank
            var resCardRank2 = handCards[resArray[1]].rank
            val index = handCards.indexOf(i)
            if (resCardRank1 > i.rank) { resArray[0] = index }
            else if (resCardRank2 > i.rank) { resArray[1] = index }
        }
        secondCardNumber = resArray[0]
        if (n > 1) { firstCardNumber = resArray[1] }
    }

    override protected fun chooseCardsToGive() {
        cardAnalysis()
        val sizeBad = badCards.size
        when (sizeBad) {
            1 -> {
                firstCardNumber = handCards.indexOf(badCards[0])
                findLowCards(1, goodCards)
            }
            0 -> { findLowCards(2, goodCards) }
            else -> { findLowCards(2, badCards) }
        }
    }

    override protected fun askToRetake(a : Int) : Boolean {
        sum = cardAnalysis()
        var bid = obligation
        if (obligation == 0) { bid = 100 }
        val c = 5
        if (bid > sum + c) {
            when (a) {
                5  -> {
                    println ("$name: Сумма очков прикупа меньше 5. Карты будут пересданы\n")
                    return true
                }
                14 -> {
                    println ("$name: У меня на руках сумма карт меньше 14. Карты будут пересданы\n                    ")
                    Game.printCards(handCards)
                    println()
                    return true
                }
                29 -> {
                    println ("$name: Две девятки в прикупе. Карты будут пересданы\n")
                    return true
                }
                49 -> {
                    println ("$name: У меня на руках четыре девятки. Карты будут пересданы\n                    ")
                    Game.printCards(handCards)
                    println()
                    return true
                }
            }

        }
        return false
    }

    private var goodCards : ArrayList<Card> = ArrayList()
    private var badCards  : ArrayList<Card> = ArrayList()
    private var kings     : ArrayList<Card> = ArrayList()
    private var sum = 0

    internal fun cardAnalysis() : Int {
        arrayOfMarriages = haveMarriage()
        var sumRank = 0
        sum = 0
        val additionalScore = 5
        var otherCards : ArrayList<ArrayList<Card>> = ArrayList()
        goodCards = ArrayList()
        badCards  = ArrayList()
        for (i in Game.cardArray) {
            var suitList: ArrayList<Card> = ArrayList()
            for (j in i) {
                if (!j.containsIn(handCards)) { suitList.add(j) }
            }
            otherCards.add(suitList)
        }
        for (i in handCards) {
            val suit = i.suit
            val rank = i.rank
            var nextRank = 0
            if (rank != 11) {
                when (rank) {
                    10 -> { nextRank = 11 }
                    4  -> { nextRank = 10 }
                    3 ->  { nextRank = 4  }
                    2 ->  { nextRank = 3  }
                    0 ->  { nextRank = 2  }
                }
                val high = Card(suit, nextRank)
                val num  = suitIndex(suit)
                val inGood = high.containsIn(goodCards)
                val notInOther = !high.containsIn(otherCards[num])
                val notInHand = !high.containsIn(handCards)

                if (inGood || (notInOther && notInHand)) {
                    goodCards.add(i)
                    sumRank += rank + additionalScore
                    val size = otherCards[num].size
                    if (size > 0) { otherCards[num].removeAt(size - 1) }
                }
                else { badCards.add(i) }
            }
            else { goodCards.add(i); sumRank += 11 + additionalScore }
        }

        val sizeMarriage = arrayOfMarriages.size
        if (sizeMarriage > 0) {
            val marriageSuit = arrayOfMarriages[sizeMarriage - 1]
            marriagesToUse(marriageSuit)
            sumRank += sum
        }
        else { Game.sortBySuits(goodCards) }
        return sumRank
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
        if (!king.containsIn(goodCards)) {
            kings.add(king)
            badCards = king.removeFrom(badCards)
            badCards = queen.removeFrom(badCards)
        }
        else {
            last--
            if (last >= 0) { marriagesToUse(arrayOfMarriages[last]) }
        }
    }

    private fun activeStrategy() : Card {
        inaccessibleCardsAnalysis()
        val goodSize = goodCards.size
        var card = Card(' ', 0)
        if (Game.trump != null) {
            val trump = Game.trump ?: ' '
            var rank = 12
            for (i in Game.ranks) {
                val trumpCard = Card(trump, i)
                if (trumpCard.containsIn(goodCards)) {
                    card = trumpCard
                    rank = i
                    goodCards = card.removeFrom(goodCards)
                    break
                }
            }
            if (rank == 12) {
                card = findBadCardToTurn()
            }
        }
        else {
            if (goodSize > 0) {
                card = goodCards[0]
                goodCards = card.removeFrom(goodCards)
            } else { card = findBadCardToTurn() }
        }
        handCards = card.removeFrom(handCards)
        return card
    }

    private fun findBadCardToTurn() : Card {
        var card = Card(' ', 0)
        if (kings.size > 0) {
            card = kings[kings.size - 1]
            kings = card.removeFrom(kings)
        }
        else {
            val badSize = badCards.size
            when (badSize) {
                0 -> {
                    for (i in handCards) {
                        if (i.suit != Game.trump) { card = i; break }
                    }
                }
                1 -> {
                    card = badCards[0]; badCards = card.removeFrom(badCards)
                }
                else -> {
                    findLowCards(1, badCards)
                    card = handCards[secondCardNumber]
                    badCards = card.removeFrom(badCards)
                }
            }
        }
        return card
    }

    private fun removeCard(card : Card) : Card {
        handCards = card.removeFrom(handCards)
        return card
    }

    private fun findBadCard() : Card {
        val size = badCards.size
        val availableCards = availableCards()
        var availableBadCards : ArrayList<Card> = ArrayList()
        if (size > 0) {
            for (i in availableCards) {
                if (i.containsIn(badCards)) { availableBadCards.add(i) }
            }
            val availableBadSize = availableBadCards.size
            when (availableBadSize) {
                0 -> { }
                1 -> {
                    val card = availableBadCards[0]
                    badCards = card.removeFrom(badCards)
                    return removeCard(card)
                }
                else -> {
                    findLowCards(1, availableBadCards)
                    val card = handCards[secondCardNumber]
                    badCards = card.removeFrom(badCards)
                    return removeCard(card)
                }
            }
        }
        return Card(' ', 0)
    }

    private fun findGoodCard() : Card {
        val size = goodCards.size
        val availableCards = availableCards()
        var availableGoodCards: ArrayList<Card> = ArrayList()
        if (size > 0) {
            for (i in availableCards) {
                if (i.containsIn(goodCards)) { availableGoodCards.add(i) }
            }
            val availableGoodSize = availableGoodCards.size
            when (availableGoodSize) {
                0 -> { }
                1 -> {
                    val card = availableGoodCards[0]
                    goodCards = card.removeFrom(goodCards)
                    return removeCard(card)
                }
                else -> {
                    findLowCards(1, availableGoodCards)
                    val card = handCards[secondCardNumber]
                    goodCards = card.removeFrom(goodCards)
                    return removeCard(card)
                }
            }
        }
        return Card(' ', 0)
    }

    private fun passiveStrategy() : Card {
        inaccessibleCardsAnalysis()
        val availableCards = availableCards()
        val availableSize = availableCards.size
        val aSuit = availableCards[0].suit
        val cSuit = Game.activeSuit
        if (aSuit != cSuit && Game.trump != aSuit) {
            val bad = findBadCard()
            if (bad.suit  != ' ') { return bad  }
            /* if (goodCards.size > 1) {
                 val good = findGoodCard()
                 return good
             } */
        }
        else {
            val good = findGoodCard()
            if (good.suit != ' ') { return good }
            val bad = findBadCard()
            if (bad.suit  != ' ') { return bad  }
        }
        if (availableSize > 1) { findLowCards(1, availableCards) }
        else { secondCardNumber = 0 }
        return removeCard(availableCards[secondCardNumber])
    }



    private fun inaccessibleCardsAnalysis() {
        var pointArray = Array(4, { 0 })
        var index : Int
        for (i in Game.inaccessibleCards) {
            index = suitIndex(i.suit)
            pointArray[index] += i.rank
        }
        for (i in handCards) {
            if (!i.containsIn(goodCards)) {
                index = suitIndex(i.suit)
                if (hasPreviousCards(pointArray[index], i.rank)) {
                    if (i.containsIn(badCards)) { badCards = i.removeFrom(badCards) }
                    if (i.containsIn(kings))    { kings = i.removeFrom(kings)    }
                    goodCards.add(i)
                }
            }
        }
        goodCards = Game.sortBySuits(goodCards)
    }

    private fun suitIndex(suit : Char) : Int {
        when (suit) {
            's' -> { return 0 }
            'c' -> { return 1 }
            'd' -> { return 2 }
            'h' -> { return 3 }
        }
        return 0
    }

    private fun hasPreviousCards(sum : Int, rank : Int) : Boolean {
        when (rank) {
            10 -> { return (sum >= 11) }
            4  -> { return (sum >= 21) }
            3  -> { return (sum >= 25) }
            2  -> { return (sum >= 28) }
            0  -> { return (sum >= 30) }
        }
        return false
    }
}