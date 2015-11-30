/* Trick-taking game for three players "1000"
by Sokolova Polina & Kuzmina Liza */

package kotlin

import java.util.*

abstract public class Player() {
    internal var name = ""
    internal var handCards : ArrayList<Card> = ArrayList()
    internal var obligation = 0
    internal var pass = false
    internal var arrayOfMarriages : ArrayList<Char> = ArrayList()
    internal var totalScore   = 0
    internal var currentScore = 0
    internal var onBarrel = false
    internal var barrelBolts = 0
    internal var bolts = 0
    internal var climbDownFromBarrel = 0
    internal var firstCardNumber = 0
    internal var secondCardNumber = 1

    abstract internal fun finalObligation()
    abstract internal fun activeClick() : Card
    abstract internal fun passiveClick() : Card
    abstract internal fun askPointsDivision() : Boolean

    abstract protected fun askPlayerToRaise(nextBid : Int) : Int
    abstract protected fun chooseCardsToGive()

    internal fun printScores(score : Int) {
        print("| $name: $score ")
    }

    internal fun askObligation(bid : Int) : Int {
        val maxSum = sumWithMarriage()
        val step = 5
        val nextBid = bid + step
        if (maxSum < nextBid) { return 0 }
        if (askPlayerToRaise(nextBid) == 0) { return 0 }
        obligation = nextBid
        return nextBid
    }

    protected fun sumWithMarriage() : Int {
        var sum = 120
        arrayOfMarriages = haveMarriage()
        val size = arrayOfMarriages.size
        if (size == 0) { return sum }
        for (i in 0..size - 1) {
            when (arrayOfMarriages[i]) {
                's' -> { sum += 40  }
                'c' -> { sum += 60  }
                'd' -> { sum += 80  }
                'h' -> { sum += 100 }
            }
        }
        return sum
    }

    internal fun giveCards(p1 : Player, p2 : Player) {
        chooseCardsToGive()
        p1.handCards.add(handCards[firstCardNumber])
        p2.handCards.add(handCards[secondCardNumber])
        p1.handCards = Game.sortBySuits(p1.handCards)
        p2.handCards = Game.sortBySuits(p2.handCards)
        handCards.removeAt(Math.max(firstCardNumber, secondCardNumber))
        handCards.removeAt(Math.min(firstCardNumber, secondCardNumber))
    }

    internal fun haveMarriage() : ArrayList<Char> {
        var res : ArrayList<Char> = ArrayList()
        var counter = Array(4, { 0 })
        for (i in 0..handCards.size - 1) {
            if (handCards[i].rank == 4 || handCards[i].rank == 3) {
                when (handCards[i].suit) {
                    's' -> {
                        if (counter[0] > 0) { res.add(handCards[i].suit) }
                        counter[0]++
                    }
                    'c' -> {
                        if (counter[1] > 0) { res.add(handCards[i].suit) }
                        counter[1]++
                    }
                    'd' -> {
                        if (counter[2] > 0) { res.add(handCards[i].suit) }
                        counter[2]++
                    }
                    'h' -> {
                        if (counter[3] > 0) { res.add(handCards[i].suit) }
                        counter[3]++
                    }
                }
            }
        }
        return res
    }

    protected fun availableCards() : ArrayList<Card> {
        var availabCards : ArrayList<Card> = ArrayList()
        for (i in handCards) {
            if (i.suit == Game.activeSuit) {
                availabCards.add(i)
            }
        }
        if (availabCards.size == 0 && Game.trump != null) {
            for (i in handCards) {
                if (i.suit == Game.trump) {
                    availabCards.add(i)
                }
            }
        }
        if (availabCards.size == 0) {
            availabCards = handCards
        }
        return availabCards
    }

    internal fun imposeFines() {
        var fine = 120
        when (totalScore) {
            555, -555 -> { totalScore = 0 }
        }
        if (bolts == 3) { bolts = 0; totalScore -= fine }
        if (barrelBolts == 3) { barrelBolts = 0; totalScore -= fine }
        if (climbDownFromBarrel == 3) { climbDownFromBarrel = 0; totalScore -= fine }
    }

    internal fun clearBarrel() {
        barrelBolts = 0
        climbDownFromBarrel++
        onBarrel = false
    }
}