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
    internal var currentBolt = false

    abstract internal fun finalObligation()
    abstract internal fun activeClick() : Card
    abstract internal fun passiveClick() : Card
    abstract internal fun askPointsDivision() : Boolean

    abstract protected fun askPlayerToRaise(nextBid : Int) : Int
    abstract protected fun chooseCardsToGive()

    internal fun printScores(score : Int, bolt : Boolean) {
        print("| $name: $score ")
        if (currentBolt && bolt) { print("⊥ "); currentBolt = false }
    }

    internal fun roundScore() {
        val mod = currentScore mod 5
        when {
            (mod < 3) -> {
                currentScore = (currentScore div 5) * 5
            }
            else -> {
                currentScore = (currentScore div 5) * 5 + 5
            }
        }
    }
    internal fun askObligation(bid : Int) : Int {
        val maxSum = sumWithMarriage()
        val step = 5
        val nextBid = bid + step
        if (maxSum < nextBid) { return 0 }
        if (askPlayerToRaise(nextBid) == 0) { return 0 }
        //obligation = nextBid
        return nextBid
    }

    protected fun sumWithMarriage() : Int {
        var sum = 120
        arrayOfMarriages = haveMarriage()
        val size = arrayOfMarriages.size
        if (size == 0) { return sum }
        for (i in arrayOfMarriages) {
            when (i) {
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
        for (i in Game.suits) {
            val queen = Card(i, 3).containsIn(handCards)
            val king = Card(i, 4).containsIn(handCards)
            if (king && queen) { res.add(i) }
        }
        return res
    }

    private fun review4Nines() : Boolean {
        var counter9 = 0
        for (i in handCards) {
            if (i.rank == 9) {
                counter9++
            }
        }
        if (counter9 == 4) {
            return true
        }
        return false
    }

    private fun reviewSum14() : Boolean {
        var ranksSum = 0
        for (i in handCards) {
            ranksSum += i.rank
        }
        if (ranksSum < 14) { return true }
        return false
    }

    private fun reviewTalonSum5() : Boolean {
        var talonRankSum = 0
        for (i in Game.talon) {
            talonRankSum += i.rank
        }
        if (talonRankSum < 5) { return true }
        return false
    }

    private fun reviewTalon2Nines() : Boolean {
        var counter9 = 0
        for (i in Game.talon) {
            if (i.rank == 0) { counter9++ }
        }
        if (counter9 > 1) { return true }
        return false
    }

    abstract protected fun askToRetake(a : Int) : Boolean

    internal fun firstRetakeChecking() : Boolean {
        if (reviewSum14()) { return askToRetake(14) }
        else if (review4Nines()) { return askToRetake(49) }
        return false
    }

    internal fun talonRetakeChecking() : Boolean {
        if (reviewTalon2Nines()) { return askToRetake(29) }
        else if (reviewTalonSum5()) { return askToRetake(5) }
        return false
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
        return Game.sortBySuits(availabCards)
    }

    internal fun imposeFines() {
        var fine = 120
        when (totalScore) {
            555, -555 -> { totalScore = 0 }
        }
        if (bolts == 3) { bolts = 0; totalScore -= fine; currentBolt = false }
        if (barrelBolts == 3) { barrelBolts = 0; totalScore -= fine }
        if (climbDownFromBarrel == 3) { climbDownFromBarrel = 0; totalScore -= fine }
    }

    internal fun clearBarrel() {
        barrelBolts = 0
        climbDownFromBarrel++
        onBarrel = false
    }
}