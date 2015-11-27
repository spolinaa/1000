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
    var talon : Array<Card> = Array(3, { Card(' ', 0) })

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
        var shuffledCards = shuffle()
        cardsDeal(shuffledCards)
        bidding()
        //getTalon()
        //getCards()
        when (turn) {
            0 -> HumanPlayer.click()
            1 -> ComputerPlayer1.click()
            2 -> ComputerPlayer2.click()
        }
    }

    private fun shuffle(): Array<Card> {
        var shuffledCards: Array<Card> = Array(24, { Card(' ', 0) })
        for (i in 0..suits.size - 1) {
            for (j in 0..ranks.size - 1) {
                shuffledCards[i * 6 + j] = cardArray[i][j]
            }
        }
        val size = shuffledCards.size - 1
        var card: Card
        var residue: ArrayList<Card> = arrayListOf()
        for (i in 0..shuffledCards.size - 1) {
            residue.add(i, shuffledCards[i])
        }
        var random: Int
        for (i in 0..size) {
            random = Random().nextInt(residue.size)
            card = residue[random]
            shuffledCards[i] = card
            residue.removeAt(random)
        }
        return shuffledCards
    }

    private fun cardsDeal(shuffledCards: Array<Card>) {
        for (i in 0..6) {
            HumanPlayer.handCards[i] = shuffledCards[i]
        }
        for (i in 7..13) {
            ComputerPlayer1.handCards[i - 7] = shuffledCards[i]
        }
        for (i in 14..20) {
            ComputerPlayer2.handCards[i - 14] = shuffledCards[i]
        }
        val startCard = 21
        talon = Array(3, {i -> shuffledCards[i + startCard]})
        HumanPlayer.handCards = sortBySuits(HumanPlayer.handCards)
        ComputerPlayer1.handCards = sortBySuits(ComputerPlayer1.handCards)
        ComputerPlayer2.handCards = sortBySuits(ComputerPlayer2.handCards)
    }

    internal fun sortBySuits(handC: ArrayList<Card>): ArrayList<Card> {
        var spades: ArrayList<Card> = ArrayList()
        var clubs: ArrayList<Card> = ArrayList()
        var diamonds: ArrayList<Card> = ArrayList()
        var hearts: ArrayList<Card> = ArrayList()

        for (i in 0..handC.size - 1) {
            when (handC[i].suit) {
                's' -> spades.add(handC[i])
                'c' -> clubs.add(handC[i])
                'd' -> diamonds.add(handC[i])
                'h' -> hearts.add(handC[i])
            }
        }
        spades = sortByRanks(spades)
        clubs = sortByRanks(clubs)
        diamonds = sortByRanks(diamonds)
        hearts = sortByRanks(hearts)
        for (i in 0..clubs.size - 1) {
            spades.add(spades.size, clubs[i])
        }
        for (i in 0..diamonds.size - 1) {
            spades.add(spades.size, diamonds[i])
        }
        for (i in 0..hearts.size - 1) {
            spades.add(spades.size, hearts[i])
        }
        return spades
    }

    internal fun sortByRanks(suitCards: ArrayList<Card>): ArrayList<Card> {
        var sortedCards: ArrayList<Card> = ArrayList()
        for (j in ranks) {
            for (i in suitCards) {
                if (i.rank == j) {
                    sortedCards.add(sortedCards.size, i)
                    println(sortedCards[0].rank)
                }
            }
        }
        return sortedCards
    }

    internal fun availableCards(playerCards : ArrayList<Card>, trump : Char?, firstSuit : Char)
            : ArrayList<Card> {
        var availabCards : ArrayList<Card> = ArrayList()

        for (i in playerCards) {
            if (i.suit == firstSuit) {
                availabCards.add(i)
            }
        }

        if (availabCards.size == 0 && trump != null) {
            for (i in playerCards) {
                if (i.suit == trump) {
                    availabCards.add(i)
                }
            }
        }

        if (availabCards.size == 0) {
            availabCards = playerCards
        }
        return availabCards
    }

    private fun getTalon(player : Player){
        for (i in 0..talon.size - 1) {
            player.handCards.add(talon[i])
        }
        player.handCards = sortBySuits(player.handCards)
    }

    private fun humanGiveCards() {
        var humanCards = HumanPlayer.handCards
        for (i in 0..humanCards.size - 1) {
            println ("(${i})  suit : ${humanCards[i].suit}   rank : ${humanCards[i].rank}")
        }
        fun getCardNubmer() : Int {
            val cardNumber: Int = readLine()?.toInt() ?: getCardNubmer()
            return cardNumber
        }
        val firstCardNumber = getCardNubmer()
        val secondCardNumber = getCardNubmer()
        ComputerPlayer1.handCards.add(ComputerPlayer1.handCards[firstCardNumber])
        ComputerPlayer2.handCards.add(ComputerPlayer2.handCards[secondCardNumber])
        ComputerPlayer1.handCards = sortBySuits(ComputerPlayer1.handCards)
        ComputerPlayer2.handCards = sortBySuits(ComputerPlayer2.handCards)
        HumanPlayer.handCards.removeAt(Math.max(firstCardNumber, secondCardNumber))
        HumanPlayer.handCards.removeAt(Math.min(firstCardNumber, secondCardNumber))
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