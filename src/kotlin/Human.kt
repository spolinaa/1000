package kotlin

import java.util.*

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
        println("Введите номер карты")
        val availableCards = availableCards()
        printCards(availableCards)
        val s = readLine()?.toInt() ?: 0
        val card = availableCards[s]
        handCards.remove(card)
        Computer().inaccessibleCards.add(card)
        return card
    }

    override internal fun askPointsDivision() : Boolean {
        println("Расписать? Д/Н")
        return humanInput()
    }

    override protected fun askPlayerToRaise(nextBid : Int) : Int {
        println("${nextBid}? Д/Н")
        if (!humanInput()) { return 0 }
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
    //♦️♥️♣️♠️
    override protected fun chooseCardsToGive() {
        printCards(handCards)
        firstCardNumber  = getArgs(handCards.size - 1)
        secondCardNumber = getArgs(handCards.size - 1)
    }

    internal fun printCards(cards : ArrayList<Card>) {
        print("| ")
        for (i in 0..cards.size - 1) {
            print("${cards[i].name}")
            when (cards[i].suit) {
                "spades"   -> { print("♠ | ") }
                "clubs"    -> { print("♣ | ") }
                "diamonds" -> { print("♦ | ") }
                "hearts"   -> { print("♥ | ") }
            }

        }
        println()
    }

    private fun getArgs(range : Int) : Int {   //Добавить проверку на то, что карты разные (Лиза)
        var args : Int = readLine()?.toInt() ?: getArgs(range)
        if (args > range || args < 0) {
            args = getArgs(range)
        }
        return args
    }


}