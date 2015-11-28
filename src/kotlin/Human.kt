package kotlin

public class Human() : Player() {
    override internal fun click() {}
    override internal fun askPointsDivision() : Boolean {
        println("Расписать? Д/Н")
        val s = readLine() ?: ""
        when (s) {
            "Д" -> { return true }
        }
        return false
    }

    override internal fun askObligation(bid : Int) : Int {
        obligation = maxBid()
        return maxBid()
    }

    override internal fun finalObligation() : Int {
        arrayOfMarriages = haveMarriage()
        if (maxBid() < obligation) { return obligation }
        return maxBid()
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

    private fun maxBid() : Int {
        val s = readLine()?.toInt() ?: 0
        val maxSum = sumWithMarriage()
        if (s > maxSum) { return maxSum }
        return s
    }

    override protected fun chooseCards() {
        var humanCards = handCards
        for (i in 0..humanCards.size - 1) {
            println ("(${i})  suit : ${humanCards[i].suit}   rank : ${humanCards[i].rank}")
        }
    }

    override protected var firstCardNumber = Game.getArgs(handCards.size - 1)
    override protected var secondCardNumber = Game.getArgs(handCards.size - 1)
}