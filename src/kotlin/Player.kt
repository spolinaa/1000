package kotlin

import java.util.*

abstract internal class Player() {
    internal var handCards : ArrayList<Card> = ArrayList()
    internal var obligation = 0
    internal var pass = false
    internal var arrayOfMarriages = haveMarriage()

    abstract internal fun click()
    abstract internal fun askObligation(bid : Int) : Int
    abstract protected var firstCardNumber : Int
    abstract protected var secondCardNumber : Int
    abstract protected fun chooseCards()
    internal fun giveCards(p1 : Player, p2 : Player) {
        chooseCards()
        p1.handCards.add(p1.handCards[firstCardNumber])
        p2.handCards.add(p2.handCards[secondCardNumber])
        p1.handCards = Game.sortBySuits(p1.handCards)
        p2.handCards = Game.sortBySuits(p2.handCards)
        handCards.removeAt(Math.max(firstCardNumber, secondCardNumber))
        handCards.removeAt(Math.min(firstCardNumber, secondCardNumber))
    }

    internal fun haveMarriage() : ArrayList<Char> {
        val suits = arrayOf('s', 'c', 'd', 'h')
        var res : ArrayList<Char> = ArrayList()
        for (i in suits) {
            val king  = handCards.contains(Card(i, 4))
            val queen = handCards.contains(Card(i, 3))
            if (king && queen) { res.add(i) }
        }
        return res
    }

    private fun sumWithMarriage() : Int {
        var sum = 120
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

    internal fun maxBid() : Int {
        val s = readLine()?.toInt() ?: 0
        val maxSum = sumWithMarriage()
        if (s > maxSum) { return maxSum }
        return s
    }
}