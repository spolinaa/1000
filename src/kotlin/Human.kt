package kotlin

public class Human() : Player() {
    override internal fun click() {}

    override internal fun askObligation(bid : Int) : Int {
        obligation = maxBid()
        return maxBid()
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