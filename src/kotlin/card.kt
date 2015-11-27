package kotlin

import java.util.*

abstract private class Player() {
    internal var handCards : ArrayList<Card> = ArrayList()
    internal var obligation = 0
    internal var pass = false
    internal var arrayOfMarriages = haveMarriage()

    abstract internal fun click()
    abstract internal fun askObligation(bid : Int) : Int
    abstract internal fun giveCards(p1 : Player, p2 : Player)
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

public class Computer() : Player() {
    override internal fun click() {}
    override internal fun askObligation(bid : Int) : Int {
        val step = 5
        cardAnalysis(handCards)
        if (sum >= bid + step) { return sum }
        return 0
    }
    override internal fun giveCards(p1 : Player, p2 : Player) {} ///////использовать анализ карт на руках
    var goodCards : ArrayList<Card> = ArrayList()
    var badCards  : ArrayList<Card> = ArrayList()
    var sum = 0
    private fun cardAnalysis(cards : ArrayList<Card>) {
        val size = cards.size
        val additionalScore = 5
        for (i in 0..size - 1) {
            val suit = cards[i].suit
            val rank = cards[i].rank
            var nextRank = 0
            if (rank != 11) {
                when (rank) {
                    10 -> { nextRank = 11 }
                    4  -> { nextRank = 10 }
                    3 ->  { nextRank = 4  }
                    2 ->  { nextRank = 3  }
                    0 ->  { nextRank = 2  }
                }
                val highestCard = Card(suit, nextRank)
                if (goodCards.contains(highestCard)) {
                    goodCards.add(handCards[i])
                    sum += rank + additionalScore
                }
                else { badCards.add(handCards[i]) }
            }
            else { goodCards.add(handCards[i]); sum += 11 + additionalScore }
        }
        val sizeMarriage = arrayOfMarriages.size
        val marriageSuit = arrayOfMarriages[sizeMarriage - 1]
        if (sizeMarriage > 0) { marriagesToUse(marriageSuit) }
    }

    private fun marriagesToUse(suit : Char) {
        val kingRank = 4
        var last = arrayOfMarriages.size - 1
        val king = Card(suit, kingRank)
        when (suit) {
            's' -> { sum += 40  }
            'c' -> { sum += 60  }
            'd' -> { sum += 80  }
            'h' -> { sum += 100 }
        }
        if (!goodCards.contains(king)) {
            Game.sortBySuits(goodCards)
            val size = goodCards.size
            goodCards.add(size, king)
        }
        else {
            last--
            if (last >= 0) { marriagesToUse(arrayOfMarriages[last]) }
        }
    }
}

public class Human() : Player() {
    override internal fun click() {}
    override internal fun askObligation(bid : Int) : Int {
        obligation = maxBid()
        return maxBid()
    }
    override internal fun giveCards(comp1 : Player, comp2 : Player) {
        var humanCards = handCards
        for (i in 0..humanCards.size - 1) {
            println ("(${i})  suit : ${humanCards[i].suit}   rank : ${humanCards[i].rank}")
        }
        val firstCardNumber = Game.getArgs(humanCards.size - 1)
        val secondCardNumber = Game.getArgs(humanCards.size - 1)
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

    internal var firstHand : Player = ComputerPlayer1
    internal var activePlayer : Player = firstHand

    internal fun getArgs(range : Int) : Int {
        var args : Int = readLine()?.toInt() ?: getArgs(range)
        if (args > range || args < 0) {
            args = getArgs(range)
        }
        return args
    }

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
                var newBid = activePlayer.askObligation(bid)
                if (newBid > bid) { bid = newBid }
                else { count++; activePlayer.pass = true }
            }
            activePlayer.obligation = bid
        }
    }

    private fun correctShuffle() {
        var shuffledCards : Array<Card>
        var res = true
        while (res) { //если (сумма очков < 14 || четыре 9) - возможно, пересдача
            shuffledCards = shuffle()
            cardsDeal(shuffledCards)
            res = firstRetakeChecking()
        }
    }

    public fun start() {
        activePlayer = firstHand
        correctShuffle()
        bidding()
        ///анализ карт в прикупе
        getTalon()
        ///если хочет расписать - новая игра + переход хода (менять firstHand на leftPlayer)
        val opponent1 = leftPlayer()
        val opponent2 = rightPlayer()
        activePlayer.giveCards(opponent1, opponent2)
        if (!firstRetakeChecking()) {
            activePlayer.click()

        }
        else { start() }


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

    private fun availableCards(playerCards : ArrayList<Card>,
                               trump : Char?, firstSuit : Char) : ArrayList<Card> {
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

    private fun getTalon() {
        for (i in 0..talon.size - 1) {
            activePlayer.handCards.add(talon[i])
        }
        activePlayer.handCards = sortBySuits(activePlayer.handCards)
    }

    private fun reviewNines(player : Player) : Boolean {
        var counter9 = 0
        for (i in player.handCards) {
            if (i.rank == 9) {
                counter9++
            }
        }
        if (counter9 == 4) {
            return true
        }
        return false
    }

    private fun review14(player : Player) : Boolean {
        var ranksSum = 0
        for (i in player.handCards) {
            ranksSum += i.rank
        }
        if (ranksSum < 14) {return true}

        return false
    }

    private fun firstRetakeChecking() : Boolean{
        if (reviewNines(HumanPlayer)) {
            println ("Do you want to retake the cards?\nprint:\n0 - No\n1 - Yes")
            val answer = getArgs(1)
            if (answer == 1) {
                println ("Four nines")
                return true
            }
        }
        if (review14(HumanPlayer)) {
            println ("Do you want to retake the cards?\nprint:\n0 - No\n1 - Yes")
            val answer = getArgs(1)
            if (answer == 1) {
                println ("Fhe sum of points of cards < 14")
                return true
            }
        }
        if (reviewNines(ComputerPlayer1) || reviewNines(ComputerPlayer2)) {
            println ("Four nines")
            return true
        }
        if (review14(ComputerPlayer1) || review14(ComputerPlayer2)) {
            println ("The sum of points of cards < 14")
            return true
        }
        return false
        // если у компьютера есть возможность пересдать карты - он обязательно это делает
        // если кто-то захотел пересдать - показать его карты и написать причину
    }
}

class Card(suit : Char, rank : Int) {
    var suit = suit
    var rank = rank
    var isTrump = false
}


//перемешали!; раздали (если что - пересдали); торги (проверка на наличие марьяжа!); --подсчет максимума очков,
// открыли прикуп! (если что - пересдали и снова торги итд);
// активный игрок забирает прикуп!; согласен играть - отдает карты соперникам!; выбирает свою ставку;
// не согласен - роспись пополам столько, сколько заявил
//первый ход (хвалить нельзя); второй ход у того, кто взял взятку; ... - приплюсовываем очки (рисуем на экране)
//кто-то хвалит - меняем статусы всех мастей; играем 8 ходов;
//подсчет набранных очков; проверяем, набрал ли активный игрок очки;
// заносим очки в таблицу (проверка на болты - хранить счетчик);

//играем, пока кто-то не наберет 880 очков
//игра на бочке