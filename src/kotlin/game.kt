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
    internal var lastTrick : Player = firstHand
    internal var trump : Char? = null
    internal var activeSuit = ' '
    internal var numberOfMotions = 0

    internal var barrel : Player? = null

    private fun leftPlayer(activePlayer : Player) : Player {
        when (activePlayer) {
            HumanPlayer     -> { return ComputerPlayer1 }
            ComputerPlayer1 -> { return ComputerPlayer2 }
            ComputerPlayer2 -> { return HumanPlayer     }
        }
        return activePlayer
    }

    private fun rightPlayer(activePlayer : Player) : Player {
        when (activePlayer) {
            HumanPlayer     -> { return ComputerPlayer2 }
            ComputerPlayer1 -> { return HumanPlayer     }
            ComputerPlayer2 -> { return ComputerPlayer1 }
        }
        return activePlayer
    }

    private fun bidding() {
        var bid = 100
        activePlayer.obligation = bid
        printCards(HumanPlayer.handCards)
        activePlayer = leftPlayer(activePlayer)
        while (!(leftPlayer(activePlayer).pass && rightPlayer(activePlayer).pass)){
            if (!activePlayer.pass) {
                var newBid = activePlayer.askObligation(bid)
                if (newBid > bid) {
                    bid = newBid
                    activePlayer.obligation = bid
                }
                else {
                    activePlayer.pass = true
                    println("${activePlayer.name}: Пас")
                }
            }
            activePlayer = leftPlayer(activePlayer)
        }
        if (activePlayer.pass) { activePlayer = leftPlayer(activePlayer) }

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
        val leftPlayer = leftPlayer(activePlayer)
        val rightPlayer = rightPlayer(activePlayer)
        if (activePlayer.askPointsDivision()) {
            if (!leftPlayer.onBarrel) {
                leftPlayer.totalScore += points
            }
            else { leftPlayer.barrelBolts++ }
            if (!rightPlayer.onBarrel) {
                rightPlayer.totalScore += points
            }
            else { rightPlayer.barrelBolts++ }
            activePlayer.totalScore  -= activePlayer.obligation
            println("Общий счет:")
            activePlayer.printScores(activePlayer.totalScore)
            leftPlayer.printScores(leftPlayer.totalScore)
            rightPlayer.printScores(rightPlayer.totalScore)
            println()
            return true
        }
        return false
    }

    private fun clearAll() {
        activePlayer.obligation = 0
        activePlayer.currentScore  = 0
        leftPlayer(activePlayer).currentScore  = 0
        rightPlayer(activePlayer).currentScore = 0
        activePlayer.firstCardNumber = 0
        activePlayer.secondCardNumber = 1
        Computer().inaccessibleCards = ArrayList()
        activePlayer.handCards  = ArrayList()
        leftPlayer(activePlayer).handCards  = ArrayList()
        rightPlayer(activePlayer).handCards = ArrayList()
        activePlayer.pass  = false
        leftPlayer(activePlayer).pass  = false
        rightPlayer(activePlayer).pass = false
        numberOfMotions = 0
    }

    public fun startGame() {
        clearAll()
        meeting()
        HumanPlayer.totalScore = 0
        ComputerPlayer1.totalScore = 0
        ComputerPlayer2.totalScore = 0
        while (firstHand.totalScore < 1000 && leftPlayer(firstHand).totalScore < 1000
                && rightPlayer(firstHand).totalScore < 1000) {
            gameOnBounds()
            simpleGame()
        }
    }

    public fun meeting() {
        ComputerPlayer1.name =  "Компьютер Лиза"
        ComputerPlayer2.name =  "Компьютер Полина"
        println("Введите ваше имя")
        HumanPlayer.name =  readLine() ?: "Игрок"
    }

    private fun gameOnBounds() {
        var onBound : Player? = null
        var boundCounter = 0
        if (firstHand.totalScore >= 880) {
            firstHand.totalScore = 880
            onBound = firstHand
            boundCounter++
        }
        if (leftPlayer(firstHand).totalScore >= 880) {
            leftPlayer(firstHand).totalScore = 880
            boundCounter++
            if (onBound != null) {
                onBound.totalScore = 760
                leftPlayer(firstHand).totalScore = 760
                rightPlayer(firstHand).clearBarrel()
            }
            else {onBound = leftPlayer(firstHand)}
        }
        if (rightPlayer(firstHand).totalScore >= 880) {
            rightPlayer(firstHand).totalScore = 880
            if (onBound != null) {
                onBound.totalScore = 760
                rightPlayer(firstHand).totalScore = 760
                rightPlayer(firstHand).clearBarrel()
            }
            else {onBound = rightPlayer(firstHand)}
            boundCounter++
        }
        if (boundCounter > 1) {
            barrel?.clearBarrel()
        }
        if (boundCounter == 1) {
            barrel?.clearBarrel()
            barrel?.onBarrel = false
            barrel = onBound
            onBound?.onBarrel = true
        }
    }

    private fun simpleGame() {

        startSimpleGame()
        for (i in 1..8) {
            comparison()
        }
        conclusion()
    }

    public fun startSimpleGame() {
        clearAll()
        activePlayer = firstHand
        correctShuffle()
        bidding()
        lastTrick = activePlayer
        ///анализ карт в прикупе
        showTalon()
        taloneRetakeChecking()
        //первый игрок получает прикуп
        getTalon()
        //роспись карт
        val opponent1 = leftPlayer(activePlayer)
        val opponent2 = rightPlayer(activePlayer)
        activePlayer.giveCards(opponent1, opponent2)
        if (activePlayer == HumanPlayer) { printCards(HumanPlayer.handCards) }
        if (pointsDivision()) { ///кнопка "расписать" активна до тех пор, пока активный не нажмет "играть"
            firstHand = leftPlayer(activePlayer) //с кнопками условий не будет - будет ожидание нажатия + активность кнопки
            startSimpleGame()
        }
        else {
            HumanPlayer.finalObligation()
            if (firstRetakeChecking()) {
                startSimpleGame()
            }
        }
    }

    private fun showTalon() {
        val showTalon : ArrayList<Card> = ArrayList()
        print("Прикуп: ")
        for (i in 0..talon.size - 1) {
            showTalon.add(talon[i])
        }
        printCards(showTalon)
    }

    internal fun printCards(cards : ArrayList<Card>) {
        print("| ")
        for (i in 0..cards.size - 1) {
            print("${cards[i].name}")
            when (cards[i].suit) {
                's' -> { print("♠ | ") }
                'c' -> { print("♣ | ") }
                'd' -> { print("♦ | ") }
                'h' -> { print("♥ | ") }
            }
        }
        println()
    }

    private fun conclusion() {
        val leftPlayer = leftPlayer(activePlayer)
        val rightPlayer = rightPlayer(activePlayer)
        if (activePlayer.currentScore < activePlayer.obligation) {
            activePlayer.totalScore -= activePlayer.obligation
            activePlayer.clearBarrel()
        } else {
            //будем считать, что игроку на бочке не разрешается идти меньше, чем на 120
            activePlayer.totalScore += activePlayer.obligation
        }
        if (!leftPlayer.onBarrel) {
            leftPlayer.totalScore += leftPlayer.currentScore
        }
        else {
            leftPlayer.barrelBolts++
        }
        if (!rightPlayer.onBarrel) {
            rightPlayer.totalScore += rightPlayer.currentScore
        }
        else {
            rightPlayer.barrelBolts++
        }
        activePlayer.imposeFines()
        leftPlayer.imposeFines()
        rightPlayer.imposeFines()
        println("Общий счет:")
        activePlayer.printScores(activePlayer.totalScore)
        leftPlayer.printScores(leftPlayer.totalScore)
        rightPlayer.printScores(rightPlayer.totalScore)
        println()
        firstHand = leftPlayer(firstHand)
    }



    private fun comparison() {
        val leftPlayer = leftPlayer(lastTrick)
        val rightPlayer = rightPlayer(lastTrick)
        var card1 = lastTrick.activeClick()
        val card2 = leftPlayer.passiveClick()
        val card3 = rightPlayer.passiveClick()
        val a = arrayListOf(card1, card2, card3)
        printCards(a)
        if ((card1.rank == 3 || card1.rank == 4) && numberOfMotions != 0) {
            for (i in lastTrick.handCards) {
                if ((i.rank == 4 || i.rank == 3) && i.suit == card1.suit) {
                    trump = card1.suit
                    var toAdd = 0
                    when (card1.suit) {
                        'c' -> toAdd = 40
                        's' -> toAdd = 60
                        'd' -> toAdd = 80
                        'h' -> toAdd = 100
                    }
                    lastTrick.currentScore += toAdd
                    break
                }
            }
        }
        var winningCard = card1
        val points = card1.rank + card2.rank + card3.rank
        if (winningCard.suit == trump) {
            if (card2.suit == trump && card2.rank > winningCard.rank) {
                winningCard = card2
            }
            if (card3.suit == trump && card3.rank > winningCard.rank) {
                winningCard = card3
            }
        }
        else {
            when (card2.suit) {
                trump -> winningCard = card2
                winningCard.suit -> {
                    if (card2.rank > winningCard.rank) {
                        winningCard = card2
                    }
                }
            }
            when (card3.suit) {
                trump -> {
                    if (winningCard.suit != trump || winningCard.suit < card3.suit)
                        winningCard = card3
                }
                winningCard.suit -> {
                    if (card3.rank > winningCard.rank) {
                        winningCard = card3
                    }
                }
            }
        }
        if (winningCard == card2) {lastTrick = leftPlayer}
        if (winningCard == card3) {lastTrick = rightPlayer}
        lastTrick.currentScore += points
        println("Текущий счет:")
        lastTrick.printScores(lastTrick.currentScore)
        leftPlayer.printScores(leftPlayer.currentScore)
        rightPlayer.printScores(rightPlayer.currentScore)
        println()
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
            HumanPlayer.handCards.add(shuffledCards[i])
        }
        for (i in 7..13) {
            ComputerPlayer1.handCards.add(shuffledCards[i])
        }
        for (i in 14..20) {
            ComputerPlayer2.handCards.add(shuffledCards[i])
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
                }
            }
        }
        return sortedCards
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

    private fun firstRetakeChecking() : Boolean {
        if (reviewNines(HumanPlayer)) {
            println ("У вас на руках четыре девятки. Хотите пересдать карты? Д/Н")
            return HumanPlayer.humanInput()
        }
        if (review14(HumanPlayer)) {
            println ("У вас на руках сумма карт меньше 14. Хотите пересдать карты? Д/Н")
            return HumanPlayer.humanInput()
        }
        var player : Player? = null
        if (reviewNines(ComputerPlayer1)) {
            player = ComputerPlayer1
            return true
        }
        if (reviewNines(ComputerPlayer2)) {
            player = ComputerPlayer2
            return true
        }
        if (player != null) { println ("${player}: У меня на руках четыре девятки. Карты будут пересданы") }
        if (review14(ComputerPlayer1)) {
            player = ComputerPlayer1
            return true
        }
        if (review14(ComputerPlayer2)) {
            player = ComputerPlayer2
            return true
        }
        if (player != null) { println ("${player}: У меня на руках сумма карт меньше 14. Карты будут пересданы") }
        return false
        // если у компьютера есть возможность пересдать карты - он обязательно это делает
        // если кто-то захотел пересдать - показать его карты и написать причину
    }

    private fun taloneRetakeChecking() {
        var talonRankSum = 0
        var counter9 = 0
        for (i in talon) {
            talonRankSum += i.rank
            if (i.rank == 0) {
                counter9++
            }
        }
        if(talonRankSum < 5) {
            if (activePlayer == HumanPlayer) {
                println ("Do you want to retake the cards?\nprint:\n0 - No\n1 - Yes")
                val answer = HumanPlayer.getArgs(1)
                if (answer == 1) {
                    println ("The sum of points of talon < 5")
                    startSimpleGame()
                }
            }
            else {
                println ("${activePlayer}: The sum of points of talon < 5. Cards will be retaken")
                startSimpleGame()
            }
        }
        if(counter9 > 1) {
            if (activePlayer == HumanPlayer) {
                println ("Do you want to retake the cards?\nprint:\n0 - No\n1 - Yes")
                val answer = HumanPlayer.getArgs(1)
                if (answer == 1) {
                    println ("Two '9' in talon. Cards will be retaken")
                    startSimpleGame()
                }
            }
            else {
                println ("${activePlayer.name}: Two '9' in talon. Cards will be retaken")
                startSimpleGame()
            }
        }
    }
}

//перемешали!; раздали! (если что - пересдали!); торги (проверка на наличие марьяжа!);
// открыли прикуп! (если что - пересдали и снова торги итд!);
// активный игрок забирает прикуп!; согласен играть - отдает карты соперникам!; выбирает свою ставку!;

//первый ход (хвалить нельзя); второй ход у того, кто взял взятку; ... - приплюсовываем очки (рисуем на экране)
//кто-то хвалит - меняем статусы всех мастей; играем 8 ходов;
//подсчет набранных очков; проверяем, набрал ли активный игрок очки;
//заносим очки в таблицу (вызывать imposeFines() для каждого игрока перед тем, как вывести на экран totalScore);


//перед прибавлением к totalScore проверять, есть ли кто-то onBarrel, и,
//если кто-то залазит - сбрасывать первого с бочки:
//             "playerName".barrelBolts = 0;
//             "playerName".onBarrel = false;
//             "playerName".climbDownFromBarrel++

//играем, пока кто-то не наберет 880 очков
//игра на бочке

//если игрок становится активным на бочке и набирает 120 очков - победа
//в остальных случаях "playerName".barrelBolts++

//Если игрок в сумме набирает 880 очков, он «садится на бочку».
// Ему перестают начисляться все очки, кроме очков, взятых при «своём прикупе» (121 очко).
// Прикуп считается «своим», когда игроку на бочке сдаёт сидящий справа, либо игра будет идти как обычно с торгами.
// В этом случае сидящему на бочке необходимо выиграть торги. При сдаче «своего» прикупа игроку, сидящему на бочке,
// карты прикупа вскрываются заблаговременно при раздаче. Подобное правило оговаривается отдельно.


//стартовые договоренности:
//1 - сброс с бочки, если на нее влез другой   (сразу после прибавления очков к общему счету - в самом конце раунда)
//2 - а) сброс на 0 при +/- 555                (сразу после прибавления очков к общему счету - в самом конце раунда)
//    б) штраф при 3х болтах подряд (пока без этого)
//    в) штраф при третьем болте               (сразу после прибавления очков к общему счету - в самом конце раунда)
//    г) сброс на 0 после 3х бочек             (сразу после прибавления очков к общему счету - в самом конце раунда)
//    д) величина штрафа = 120
//3 - пересдачи: а) сумма прикупа < 5          (после открытия прикупа - перед взятием карт активным игроком)
//               б) на руках < 14 очков        (сразу после раздачи карт)
//               в) 4 девятки после раздачи    (до торгов)
//               г) 2 девятки в прикупе        (после открытия прикупа - перед взятием карт активным игроком)
//               д) 4 девятки на руках         (после сброса карт)
//4 - роспись по 60 очков оппонентам, у активного вычитается столько, сколько заявил на торгах


