/* Trick-taking game for three players "1000"
by Sokolova Polina & Kuzmina Liza */

package kotlin

public class Human() : Player() {
    internal fun humanInput() : Boolean {
        val s = readLine() ?: ""
        if (s == "Д") { return true }
        return false
    }

    override internal fun activeClick() : Card {
        val card = click()
        Game.activeSuit = card.suit
        return card
    }

    override internal fun passiveClick() : Card = click()

    private fun click() : Card {
        Game.printCards(availableCards())
        println("Ваш ход: ")
        val args = getArgs(availableCards().size - 1)
        val card = availableCards()[args]
        handCards.remove(card)
        Computer().inaccessibleCards.add(card)
        return card
    }

    override internal fun askPointsDivision() : Boolean {
        println("Расписать? Д/Н")
        return humanInput()
    }

    override protected fun askPlayerToRaise(nextBid : Int) : Int {
        println("$nextBid? Д/Н")
        if (!humanInput()) { return 0 }
        return nextBid
    }

    override internal fun finalObligation() {
        //Game.printCards(handCards)
        println("Выберите ставку")
        val s = readLine()?.toInt() ?: 0
        val maxSum = sumWithMarriage()
        if (s >= obligation) { obligation = s }
        if (s > maxSum) { obligation = maxSum }
        println("Ставка: $obligation")
    }

    override protected fun chooseCardsToGive() {
        Game.printCards(handCards)
        println("Выберите две карты для сноса")
        firstCardNumber  = getArgs(handCards.size - 1)
        secondCardNumber = getArgs(handCards.size - 1)
    }

    internal fun getArgs(range : Int) : Int {   //Добавить проверку на то, что карты разные (Лиза)
        var args : Int = readLine()?.toInt() ?: getArgs(range)
        if (args > range || args < 0) {
            args = getArgs(range)
        }
        return args
    }
}