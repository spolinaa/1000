import java.util.*

abstract private class Player() {
    //val turn = false
    var handCards : ArrayList<Card> = ArrayList()
    private fun addCard(c : Card) {
        handCards.add(c)
    }
    private fun removeCard(c : Card) {
        handCards.remove(c)
    }
    abstract public fun click()

    public var obligation = 0

    abstract internal fun askObligation() : Int

    internal fun haveMarriage() : Boolean {
        val suits = arrayOf('s', 'c', 'd', 'h')
        for (i in suits) {
            val king  = handCards.contains(Card(i, 4))
            val queen = handCards.contains(Card(i, 3))
            if (king && queen) { return true }
        }
        return false
    }

    internal fun maxBid() : Int {
        val s = readLine()?.toInt() ?: 0
        if (s >= 120 && haveMarriage()) { return s } ///120 можно, если есть хваль
        return 115
    }

    //abstract internal fun giveCards() : Array<Card>

}

public class Computer() : Player() {
    override public fun click() {}
    override internal fun askObligation() : Int { return 0 } /////анализ карт на руках
    //override internal fun giveCards() : Array<Card> { return Array<Card>(2, { Card(' ', 0)}) }
}

public class Human() : Player() {
    override public fun click() {}
    override internal fun askObligation() : Int {
        obligation = maxBid()
        return maxBid()
    }
    //override internal fun giveCards() : Array<Card> { }
}

object Game {
    var currentScore = Array(3, {0})
    var totalScore   = Array(3, {0})
    val suits = arrayOf('s', 'c', 'd', 'h') // крести, пики, бубны, черви
    val ranks = arrayOf(11, 10, 4, 3, 2, 0)
    var cardArray : Array<Array<Card>> = Array(4,
            { i -> Array<Card>(6,
                    { j -> Card(suits[i], ranks[j]) }) })

    val HumanPlayer = Human()
    val ComputerPlayer1 = Computer()
    val ComputerPlayer2 = Computer()
    var Talon : Array<Card> = Array(3, { Card(' ', 0) })

    var turn = 0

    private fun bidding() {
        var bid = 100
        var count = 0
        while (count < 3) {
            var newBid = bid
            when (turn) {
                0 -> { newBid = ComputerPlayer1.askObligation() }
                1 -> { newBid = ComputerPlayer2.askObligation() }
                2 -> { newBid = HumanPlayer.askObligation() }
            }
            if (newBid > bid) { bid = newBid }
            else { count++ }
            turn++ mod 3
        }
        when (turn) {
            0 -> { HumanPlayer.obligation     = bid }
            1 -> { ComputerPlayer1.obligation = bid }
            2 -> { ComputerPlayer2.obligation = bid }
        }
        //return turn
    }

    public fun start() {
        bidding()
        //getTalon()
        //getCards()
        when (turn) {
            0 -> HumanPlayer.click()
            1 -> ComputerPlayer1.click()
            2 -> ComputerPlayer2.click()
        }
    }
}

class Card(suit : Char, rank : Int) {
    var suit = suit
    var rank = rank
    var isTrump = false
}

//перемешали!; раздали (если что - пересдали); торги (проверка на наличие марьяжа!); --подсчет максимума очков,
// открыли прикуп (если что - пересдали и снова торги итд); раздали прикуп
//первый ход (хвалить нельзя); второй ход у того, кто взял взятку; ... - приплюсовываем очки (рисуем на экране)
//кто-то хвалит - меняем статусы всех мастей; играем 8 ходов;
//подсчет набранных очков; проверяем, набрал ли "торгаш" очки;
// заносим очки в таблицу (проверка на болты - хранить счетчик);

//играем, пока кто-то не наберет 880 очков
//игра на бочке