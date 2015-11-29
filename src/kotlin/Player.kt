package kotlin

import java.util.*

abstract internal class Player() {
    internal var name = ""
    internal var handCards : ArrayList<Card> = ArrayList()
    internal var obligation = 0
    internal var pass = false
    internal var arrayOfMarriages = haveMarriage()
    internal var totalScore   = 0
    internal var currentScore = 0
    internal var onBarrel = false
    internal var barrelBolts = 0
    internal var bolts = 0 ///не прибавлять ничего, если игрок на бочке
    internal var climbDownFromBarrel = 0

    internal var firstCardNumber  = 0
    internal var secondCardNumber = 1

    abstract internal fun finalObligation()
    abstract internal fun activeClick() : Card
    abstract internal fun passiveClick() : Card
    abstract internal fun askPointsDivision() : Boolean

    abstract protected fun askPlayerToRaise(nextBid : Int) : Int
    abstract protected fun chooseCardsToGive()

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
        val size = arrayOfMarriages.size
        if (size == 0) { return sum }
        for (i in 0..size - 1) {
            when (arrayOfMarriages[i]) {
                "spades"   -> { sum += 40  }
                "clubs"    -> { sum += 60  }
                "diamonds" -> { sum += 80  }
                "hearts"   -> { sum += 100 }
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

    protected fun haveMarriage() : ArrayList<String> {
        val suits = arrayOf('s', 'c', 'd', 'h')
        var res : ArrayList<String> = ArrayList()
        for (i in Game.suits) {
            val king  = handCards.contains(Card(i, 4))
            val queen = handCards.contains(Card(i, 3))
            if (king && queen) { res.add(i) }
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
}