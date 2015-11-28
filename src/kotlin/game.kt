package kotlin

import java.util.*

internal object Game {
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

    private fun pointsDivision() : Boolean {
        val points = 60
        if (activePlayer.askPointsDivision()) {
            leftPlayer().totalScore  += points
            rightPlayer().totalScore += points
            activePlayer.totalScore  -= activePlayer.obligation
            return true
        }
        return false
    }

    public fun start() {
        //обнуление текущего счета
        activePlayer.obligation = 0
        activePlayer.currentScore  = 0
        leftPlayer().currentScore  = 0
        rightPlayer().currentScore = 0
        //переход хода по часовой стрелке
        activePlayer = firstHand
        //раздача карт
        correctShuffle()
        //торговля
        bidding()
        ///анализ карт в прикупе

        //первый игрок получает прикуп
        getTalon()
        //роспись карт
        if (pointsDivision()) { ///кнопка "расписать" активна до тех пор, пока активный не нажмет "играть"
            firstHand = leftPlayer() //с кнопками условий не будет - будет ожидание нажатия + активность кнопки
            start()
        }
        else {
            val opponent1 = leftPlayer()
            val opponent2 = rightPlayer()
            activePlayer.giveCards(opponent1, opponent2)
            activePlayer.obligation = activePlayer.finalObligation()
            if (!firstRetakeChecking()) {
                activePlayer.click()

            } else {
                start()
            }
        }
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




//перемешали!; раздали! (если что - пересдали!); торги (проверка на наличие марьяжа!);
// открыли прикуп! (если что - пересдали и снова торги итд!);
// активный игрок забирает прикуп!; согласен играть - отдает карты соперникам!; выбирает свою ставку!;

//первый ход (хвалить нельзя); второй ход у того, кто взял взятку; ... - приплюсовываем очки (рисуем на экране)
//кто-то хвалит - меняем статусы всех мастей; играем 8 ходов;
//подсчет набранных очков; проверяем, набрал ли активный игрок очки;
// заносим очки в таблицу (проверка на болты - хранить счетчик);

//играем, пока кто-то не наберет 880 очков
//игра на бочке

//стартовые договоренности:
//1 - сброс с бочки, если на нее влез другой
//2 - а) сброс на 0 при +/- 555
//    б) штраф при 3х болтах подряд
//    в) штраф при третьем болте
//    г) сброс на 0 после 3х бочек
//    д) величина штрафа = 120
//3 - пересдачи: а) сумма прикупа < 5 
//               б) на руках < 14 очков
//               в) 4 девятки после раздачи (до торгов)
//               г) 2 девятки в прикупе
//               д) 4 девятки на руках (после сброса карт)
//4 - роспись по 60 очков оппонентам, у активного вычитается столько, сколько заявил на торгах