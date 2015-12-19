/* Trick-taking game for three players "1000"
by Sokolova Polina & Kuzmina Liza */

package console

import java.util.*

public class Human() : Player() {
    internal fun humanInput() : Boolean {
        val s = readLine() ?: ""
        println()
        if (s == "Д") { return true }
        return false
    }

    override internal fun activeClick() : Card {
        val card = click(handCards)
        Game.activeSuit = card.suit
        return card
    }

    override internal fun passiveClick() : Card {
        println()
        return click(availableCards())
    }

    private fun printNumbers(cards : ArrayList<Card>, indent : Int) {
        print("\n")
        for (i in 0..indent) { print(" ") }
        for (i in 0..cards.size - 1) {
            if (cards[i].rank == 10) { print(" ") }
            print("$i    ")
        }
        println("\n")
    }

    private fun click(cards : ArrayList<Card>) : Card {
        print("\nДоступные для хода карты: ")
        Game.printCards(cards)
        printNumbers(cards, 27)
        println("Ваш ход: ")
        val args = getArgs(cards.size - 1, 1)[0]
        val card = cards[args]
        handCards.remove(card)
        println()
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

    override protected fun askToRetake(a : Int) : Boolean {
        when (a) {
            5  -> {
                println ("Сумма очков прикупа меньше 5. Хотите пересдать карты? Д/Н")
                return humanInput()
            }
            14 -> {
                println ("У вас на руках сумма карт меньше 14. Хотите пересдать карты? Д/Н")
                return humanInput()
            }
            29 -> {
                println ("Две девятки в прикупе. Хотите пересдать карты? Д/Н")
                return humanInput()
            }
            49 -> {
                println ("У вас на руках четыре девятки. Хотите пересдать карты? Д/Н")
                return humanInput()
            }
        }
        return false
    }

    internal fun printHumanCards() {
        print("Ваши карты: ")
        Game.printCards(Game.HumanPlayer.handCards)
    }

    override internal fun finalObligation() {
        println("Выберите ставку")
        val bid = readLine() ?: ""
        if (bid.length > 0) {
            val s = bid.toInt()
            println()
            val maxSum = sumWithMarriage()
            if (s >= obligation) { obligation = s }
            if (s > maxSum) { obligation = maxSum }
        }
        println("Ставка: $obligation\n")
    }

    override protected fun chooseCardsToGive() {
        printHumanCards()
        printNumbers(handCards, 13)
        println("Выберите две карты для сноса")
        val toGive = getArgs(handCards.size - 1, 2)
        firstCardNumber  = toGive[0]
        secondCardNumber = toGive[1]
    }

    internal fun getArgs(range : Int, numberOfCards : Int) : ArrayList<Int> {
        var result : ArrayList<Int> = arrayListOf()
        while (result.size < numberOfCards) {
            val line = readLine()
            var i = 0
            var waitingForSpace = false
            if (line != null) {
                while (i < line.length) {
                    if (i != line.length - 1) {
                        if (line[i + 1] == ' ') {
                            if (waitingForSpace) {
                                waitingForSpace = false
                            }
                            else {
                                val value = line[i].toInt() - 48
                                if (value <= range && value >= 0 && result.size < numberOfCards) {
                                    result.add(value)
                                }
                            }
                            i += 2
                        }
                        else {
                            waitingForSpace = true
                            i++
                        }
                    }
                    else {
                        if (!waitingForSpace) {
                            val value = line[i].toInt() - 48
                            if (value <= range && value >= 0 && result.size < numberOfCards) {
                                result.add(value)
                            }
                        }
                        i++
                    }
                }
            }
        }
        return result
    }
}