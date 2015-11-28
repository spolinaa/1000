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

    override protected fun askPlayerToRaise(nextBid : Int) : Int {
        println("${nextBid}? Д/Н")
        val s = readLine() ?: ""
        if (s != "Д") { return 0 }
        return nextBid
    }

    override internal fun finalObligation() : Int {
        println("Выберите ставку")
        val s = readLine()?.toInt() ?: 0
        if (s < obligation) { return obligation }
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