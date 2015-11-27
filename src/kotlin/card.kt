package kotlin

import java.util.*

abstract private class Player() {
    internal var handCards : ArrayList<Card> = ArrayList()
    internal var obligation = 0
    internal var pass = false

    abstract internal fun click()
    abstract internal fun askObligation() : Int
    abstract internal fun giveCards(p1 : Player, p2 : Player)
    internal fun haveMarriage() : Char? {
        val suits = arrayOf('s', 'c', 'd', 'h')
        for (i in suits) {
            val king  = handCards.contains(Card(i, 4))
            val queen = handCards.contains(Card(i, 3))
            if (king && queen) { return i }
        }
        return null
    }

    internal fun maxBid() : Int {
        val s = readLine()?.toInt() ?: 0
        if (s > 120 && haveMarriage() == null) { return 120 }
        return s
    }
}

public class Computer() : Player() {
    override internal fun click() {}
    override internal fun askObligation() : Int { return 0 } /////использовать анализ карт на руках
    override internal fun giveCards(p1 : Player, p2 : Player) {} ///////использовать анализ карт на руках
    var goodCards : ArrayList<Card> = ArrayList()
    var sum = 0
    private fun firstAnalysis() {
        val size = handCards.size
        val additionalScore = 5
        for (i in 0..size - 1) {
            val suit = handCards[i].suit
            val rank = handCards[i].rank
            var nextRank = 0
            val lastInGood = goodCards.size - 1
            when (rank) {
                11 -> { goodCards.add(handCards[i]); sum += 11 }
                10 -> { nextRank = 11 }
                4  -> { nextRank = 10 }
                3  -> { nextRank = 4  }
                2  -> { nextRank = 3  }
                0  -> { nextRank = 2  }
            }
            if (goodCards[lastInGood] == Card(suit, nextRank)) {
                goodCards.add(handCards[i])
                sum += rank + additionalScore
            }
        }
        when (haveMarriage()) {
            's' -> { sum += 40 }
            'c' -> { sum += 60 }
            'd' -> { sum += 80 }
            'h' -> { sum += 100 }
        }
    }
}

public class Human() : Player() {
    override internal fun click() {}
    override internal fun askObligation() : Int {
        obligation = maxBid()
        return maxBid()
    }
    override internal fun giveCards(comp1 : Player, comp2 : Player) {
        var humanCards = handCards
        for (i in 0..humanCards.size - 1) {
            println ("(${i})  suit : ${humanCards[i].suit}   rank : ${humanCards[i].rank}")
        }
        fun getCardNubmer() : Int {
            val cardNumber : Int = readLine()?.toInt() ?: getCardNubmer()
            return cardNumber
        }
        val firstCardNumber = getCardNubmer()
        val secondCardNumber = getCardNubmer()
        comp1.handCards.add(comp1.handCards[firstCardNumber])
        comp2.handCards.add(comp2.handCards[secondCardNumber])
        comp1.handCards = Game.sortBySuits(comp1.handCards)
        comp2.handCards = Game.sortBySuits(comp2.handCards)
        handCards.removeAt(Math.max(firstCardNumber, secondCardNumber))
        handCards.removeAt(Math.min(firstCardNumber, secondCardNumber))
    }
}

internal object Game {
    private var currentScore = Array(3, {0})
    private var totalScore   = Array(3, {0})
    private val suits = arrayOf('s', 'c', 'd', 'h') // пики, крести, бубны, черви
    private val ranks = arrayOf(11, 10, 4, 3, 2, 0)
    private var cardArray : Array<Array<Card>> = Array(4,
            { i -> Array(6, { j -> Card(suits[i], ranks[j]) }) })

    internal val HumanPlayer = Human()
    internal val ComputerPlayer1 = Computer()
    internal val ComputerPlayer2 = Computer()
    internal var talon : Array<Card> = Array(3, { Card(' ', 0) })

    internal var activePlayer : Player = ComputerPlayer1

    private fun leftPlayer() : Player {
        when (activePlayer) {
            HumanPlayer     -> { return ComputerPlayer1 }
            ComputerPlayer1 -> { return ComputerPlayer2 }
            ComputerPlayer2 -> { return HumanPlayer     }
        }
        return activePlayer
    }

    private fun rightPlayer() : Player {
        when (activePlayer) {
            HumanPlayer     -> { return ComputerPlayer2 }
            ComputerPlayer1 -> { return HumanPlayer     }
            ComputerPlayer2 -> { return ComputerPlayer1 }
        }
        return activePlayer
    }

    private fun bidding() {
        var bid = 100
        var count = 0
        while (count < 2) {
            activePlayer = leftPlayer()
            if (!activePlayer.pass) {
                var newBid = activePlayer.askObligation()
                if (newBid > bid) { bid = newBid }
                else { count++; activePlayer.pass = true }
            }
            activePlayer.obligation = bid
        }
    }

    public fun start() {
        var shuffledCards = shuffle()
        cardsDeal(shuffledCards)
        //проверка (14 и 9)
        //пересдача, если что

        bidding()
        ///анализ карт в прикупе
        getTalon(activePlayer)
        val opponent1 = leftPlayer()
        val opponent2 = rightPlayer()
        activePlayer.giveCards(opponent1, opponent2)
        ////проверка (14 и 9)
        activePlayer.click()
    }

    private fun shuffle() : Array<Card> {
        var shuffledCards : Array<Card> = Array(24, { Card(' ', 0) })
        for (i in 0..suits.size - 1) {
            for (j in 0..ranks.size - 1) {
                shuffledCards[i * 6 + j] = cardArray[i][j]
            }
        }
        val size = shuffledCards.size - 1
        var card : Card
        var residue : ArrayList<Card> = arrayListOf()
        for (i in 0..shuffledCards.size - 1) {
            residue.add(i, shuffledCards[i])
        }
        var random : Int
        for (i in 0..size) {
            random = Random().nextInt(residue.size)
            card = residue[random]
            shuffledCards[i] = card
            residue.removeAt(random)
        }
        return shuffledCards
    }

    private fun cardsDeal(shuffledCards : Array<Card>) {
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

    internal fun sortBySuits(handC : ArrayList<Card>) : ArrayList<Card> {
        var spades   : ArrayList<Card> = ArrayList()
        var clubs    : ArrayList<Card> = ArrayList()
        var diamonds : ArrayList<Card> = ArrayList()
        var hearts   : ArrayList<Card> = ArrayList()

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

    private fun sortByRanks(suitCards : ArrayList<Card>) : ArrayList<Card> {
        var sortedCards : ArrayList<Card> = ArrayList()
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

    private fun availableCards(playerCards : ArrayList<Card>, trump : Char?, firstSuit : Char)
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
}

class Card(suit : Char, rank : Int) {
    var suit = suit
    var rank = rank
    var isTrump = false
}


//перемешали!; раздали (если что - пересдали); торги (проверка на наличие марьяжа!); --подсчет максимума очков,
// открыли прикуп! (если что - пересдали и снова торги итд); раздали прикуп!
//первый ход (хвалить нельзя); второй ход у того, кто взял взятку; ... - приплюсовываем очки (рисуем на экране)
//кто-то хвалит - меняем статусы всех мастей; играем 8 ходов;
//подсчет набранных очков; проверяем, набрал ли "торгаш" очки;
// заносим очки в таблицу (проверка на болты - хранить счетчик);

//играем, пока кто-то не наберет 880 очков
//игра на бочке