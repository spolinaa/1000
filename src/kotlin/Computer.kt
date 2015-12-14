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
        if (sum + c < obligation || goodCards.size == 1) {
            println("$name: Распишем\n")
            return true
        }
        return false
    }

    override protected fun askPlayerToRaise(nextBid : Int) : Int {
        sum = cardAnalysis() + 5
        if (sum < nextBid || goodCards.size == 0) { return 0 }
        return sum
    }

    override internal fun finalObligation() {
        if (obligation < sum) { obligation = (sum div 5) * 5 }
        println("$name: Иду на $obligation")
    }

    private fun findLowCards(array : ArrayList<Card>) {
        val c1 = handCards.indexOf(array[0])
        val c2 = handCards.indexOf(array[1])
        var resArray = arrayOf(c1, c2)
        val size = array.size
        if (size > 2) {
            var resCard1 = array[0]
            var resCard2 = array[1]
            for (i in 0..size - 2) {
                val index = handCards.indexOf(array[i])
                if (resCard1.rank > array[i].rank) { resArray[0] = index }
                else if (resCard2.rank > array[i].rank) { resArray[1] = index }
            }
        }
        if (handCards[resArray[0]].rank < handCards[resArray[1]].rank) {
            secondCardNumber = resArray[0]
            firstCardNumber = resArray[1]
        }
        else {
            secondCardNumber = resArray[1]
            firstCardNumber = resArray[0]
        }
    }

    override protected fun chooseCardsToGive() {
        cardAnalysis()
        val sizeBad = badCards.size
        when (sizeBad) {
            1 -> {
                findLowCards(goodCards)
                firstCardNumber = handCards.indexOf(badCards[0])
            }
            0 -> { findLowCards(goodCards) }
            else -> { findLowCards(badCards) }
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
        var otherCards1 : ArrayList<ArrayList<Card>> = ArrayList()
        goodCards = ArrayList()
        badCards  = ArrayList()
        for (i in Game.cardArray) {
            var suitList: ArrayList<Card> = ArrayList()
            for (j in i) {
                if (!j.containsIn(handCards)) { suitList.add(j) }
            }
            otherCards1.add(suitList)
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
                val notInOther = !high.containsIn(otherCards1[num])
                val notInHand = !high.containsIn(handCards)

                if (inGood || (notInOther && notInHand)) {
                    goodCards.add(i)
                    sumRank += rank + additionalScore
                    val size = otherCards1[num].size
                    if (size > 0) { otherCards1[num].removeAt(size - 1) }
                }
                else { badCards.add(i) }
            }
            else { goodCards.add(i); sumRank += 11 + additionalScore }
        }
        var notInGoodCards : ArrayList<Card> = badCards
        val sizeMarriage = arrayOfMarriages.size
        if (sizeMarriage > 0) {
            for (i in 0..sizeMarriage - 1) {
                if (!marriagesToUse(i)) { break }
            }

            sumRank += sum
        }
        else { Game.sortBySuits(goodCards) }
        if (goodCards.size > 5) { return smartAnalysis(notInGoodCards, sumRank) }
        return sumRank
    }

    private fun smartAnalysis(array : ArrayList<Card>, sum : Int) : Int {
        var r1 : Card? = null
        var r2 : Card? = null
        var suit = ' '
        var notInGoodCards = array
        var sumRank = sum
        for (i in notInGoodCards) {
            if (i.rank == 4) { r1 = i; suit = i.suit }
            if (i.rank == 3 && suit == i.suit) { r2 = i }
        }
        if (r1 != null && r2 != null) {
            val A = Card(suit, 11)
            val K = Card(suit, 4)
            if (A.containsIn(goodCards) || K.containsIn(goodCards)) {
                notInGoodCards = r2.removeFrom(notInGoodCards)
            }
        }
        var sumToSub = 0
        sumRank = sumWithMarriage()
        var A = 4
        var ten = 4
        var K = 4
        for (i in goodCards) {
            when (i.rank) {
                11 -> { A--   }
                10 -> { ten-- }
                4  -> { K--   }
            }
        }
        var amount = notInGoodCards.size * 2
        if (amount > A) {
            amount -= A
            sumToSub += A * 11
            if (amount > ten) {
                amount -= ten
                sumToSub += ten * 10
                if (amount > 0) { sumToSub += amount * 4 }
            }
            else { sumToSub += amount * 10 }
        }
        else { sumToSub += amount * 11 }
        for (i in notInGoodCards) {
            sumToSub += i.rank
        }
        if (sumToSub mod 5 > 0) { sumToSub = (sumToSub div 5) * 5 + 5 }
        sumRank -= sumToSub
        return sumRank
    }



    private fun marriagesToUse(last : Int) : Boolean {
        val kingRank  = 4
        val queenRank = 3
        val suit = arrayOfMarriages[last]
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
            return false
        }
        return true
    }

    private fun inaccessibleTrumpCards() : Boolean {
        var sum = 0
        for (i in Game.inaccessibleCards) {
            if (i.suit == Game.trump) { sum += i.rank }
        }
        if (sum == 30) { return true }
        return false
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
                if (kings.size > 0) {
                    card = kings[kings.size - 1]
                    kings = card.removeFrom(kings)
                }
                else {
                    if (goodSize > 0) {
                        card = goodCards[0]
                        goodCards = card.removeFrom(goodCards)
                    }
                    else { card = findBadCardToTurn() }
                }
            }
        }
        else {
            if (goodSize > 0) {
                card = goodCards[0]
                goodCards = card.removeFrom(goodCards)
            }
            else {
                if (kings.size > 0 && Game.numberOfMotions > 0) {
                    card = kings[kings.size - 1]
                    kings = card.removeFrom(kings)
                }
                else { card = findBadCardToTurn() }
            }
        }
        handCards = card.removeFrom(handCards)
        return card
    }

    private fun findBadCardToTurn() : Card {
        var card = Card(' ', 0)
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
                findLowCards(badCards)
                card = handCards[secondCardNumber]
                badCards = card.removeFrom(badCards)
            }
        }
        return card
    }

    private fun removeKing(card : Card) {
        if (card.rank == 3) {
            for (i in kings) {
                if (card.suit == i.suit) { kings = i.removeFrom(kings) }
            }
        }
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
                0 -> {}
                1 -> {
                    val card = availableBadCards[0]
                    badCards = card.removeFrom(badCards)
                    return removeCard(card)
                }
                else -> {
                    findLowCards(availableBadCards)
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
                    findLowCards(availableGoodCards)
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
            if (bad.suit  != ' ') { removeKing(bad); return bad  }
            /* if (goodCards.size > 1) {
                 val good = findGoodCard()
                 return good
             } */
        }
        else {
            val good = findGoodCard()
            if (good.suit != ' ') { return good }
            val bad = findBadCard()
            if (bad.suit  != ' ') { removeKing(bad); return bad  }
        }
        if (availableSize > 1) {
            findLowCards(availableCards)
            removeKing(handCards[secondCardNumber]);
            return removeCard(handCards[secondCardNumber])
        }
        else {
            removeKing(availableCards[0]);
            return removeCard(availableCards[0])
        }
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