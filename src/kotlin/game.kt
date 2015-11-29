package kotlin

import java.util.*

internal object Game {
    internal val suits = arrayOf("spades", "clubs", "diamonds", "hearts") // пики, крести, бубны, черви
    private val ranks = arrayOf(11, 10, 4, 3, 2, 0)
    private var cardArray : Array<Array<Card>> = Array(4,
            { i -> Array(6, { j -> Card(suits[i], ranks[j]) }) })

    internal val HumanPlayer = Human()
    internal val ComputerPlayer1 = Computer()
    internal val ComputerPlayer2 = Computer()

    internal var talon : Array<Card> = Array(3, { Card("", 0) })

    internal var firstHand : Player = ComputerPlayer1
    internal var activePlayer : Player = firstHand
    internal var trump : String? = null
    internal var activeSuit = ""

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
        activePlayer.obligation = bid
        printCards(HumanPlayer.handCards)
        activePlayer = leftPlayer()
        while (count < 2) {
            if (!activePlayer.pass) {
                var newBid = activePlayer.askObligation(bid)
                if (newBid > bid) {
                    bid = newBid
                    activePlayer.obligation = bid
                }
                else {
                    count++;
                    activePlayer.pass = true
                    println("${activePlayer.name}: Пас")
                }
            }
            activePlayer = leftPlayer()
        }
        if (activePlayer.pass) { activePlayer = leftPlayer() }

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

    private fun clearAll() {
        activePlayer.obligation = 0
        activePlayer.currentScore  = 0
        leftPlayer().currentScore  = 0
        rightPlayer().currentScore = 0
        activePlayer.firstCardNumber = 0
        activePlayer.secondCardNumber = 1
        Computer().inaccessibleCards = ArrayList()
        activePlayer.handCards  = ArrayList()
        leftPlayer().handCards  = ArrayList()
        rightPlayer().handCards = ArrayList()
        activePlayer.pass  = false
        leftPlayer().pass  = false
        rightPlayer().pass = false

    }

    public fun meeting() {
        ComputerPlayer1.name =  "Компьютер Лиза"
        ComputerPlayer2.name =  "Компьютер Полина"
        println("Введите ваше имя")
        HumanPlayer.name =  readLine() ?: "Игрок"
    }

    public fun start() {
        //обнуление текущего счета
        clearAll()
        //переход хода по часовой стрелке
        activePlayer = firstHand
        //раздача карт
        correctShuffle()
        //торговля
        bidding()
        //показывается прикуп
        showTalon()
        ///анализ карт в прикупе
        talonChecking()
        //активный игрок получает прикуп
        getTalon()
        val opponent1 = leftPlayer()
        val opponent2 = rightPlayer()
        activePlayer.giveCards(opponent1, opponent2)
        if (activePlayer == HumanPlayer) { printCards(HumanPlayer.handCards) }
        if (pointsDivision()) { ///кнопка "расписать" активна до тех пор, пока активный не нажмет "играть"
            firstHand = leftPlayer() //с кнопками условий не будет - будет ожидание нажатия + активность кнопки
            start()
        }
        else {
            HumanPlayer.finalObligation()
            if (!firstRetakeChecking()) {
                activePlayer.activeClick()
                opponent1.passiveClick()
                opponent2.passiveClick()
                //выводить карты перед сравнением
            } else {
                start()
            }
        }
    }

    private fun shuffle() : Array<Card> {
        var shuffledCards : Array<Card> = Array(24, { Card("", 0) })
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
                "spades"   -> spades.add(handC[i])
                "clubs"    -> clubs.add(handC[i])
                "diamonds" -> diamonds.add(handC[i])
                "hearts"   -> hearts.add(handC[i])
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

    private fun showTalon() {
        val showTalon : ArrayList<Card> = ArrayList()
        print("Прикуп: ")
        for (i in 0..talon.size - 1) {
            showTalon.add(talon[i])
        }
        printCards(showTalon)
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
            printCards(HumanPlayer.handCards)
            println ("У вас на руках четыре девятки. Хотите пересдать карты? Д/Н")
            return HumanPlayer.humanInput()
        }
        if (review14(HumanPlayer)) {
            printCards(HumanPlayer.handCards)
            println ("У вас на руках сумма карт меньше 14. Хотите пересдать карты? Д/Н")
            return HumanPlayer.humanInput()
        }
        var player : Player? = null
        if (reviewNines(ComputerPlayer1)) {
            player = ComputerPlayer1
        }
        if (reviewNines(ComputerPlayer2)) {
            player = ComputerPlayer2
        }
        if (player != null) {
            println ("${player.name}: У меня на руках четыре девятки. Карты будут пересданы")
            printCards(player.handCards)
            return true
        }
        if (review14(ComputerPlayer1)) {
            player = ComputerPlayer1
        }
        if (review14(ComputerPlayer2)) {
            player = ComputerPlayer2
        }
        if (player != null) {
            println ("${player.name}: У меня на руках сумма карт меньше 14. Карты будут пересданы")
            printCards(player.handCards)
            return true
        }
        return false
        // если у компьютера есть возможность пересдать карты - он обязательно это делает
        // если кто-то захотел пересдать - показать его карты и написать причину
    }

    private fun talonChecking() : Boolean {
        var sum = 0
        var counter9 = 0
        for (i in 0..talon.size - 1) {
            if (talon[i].rank == 0) { counter9++ }
            else { sum += talon[i].rank }
        }
        if (activePlayer == HumanPlayer) {
            if (sum < 5) {
                println ("Сумма карт в прикупе меньше 5. Хотите пересдать карты? Д/Н")
                return HumanPlayer.humanInput()
            }
            if (counter9 > 1) {
                println ("Две девятки в прикупе. Хотите пересдать карты? Д/Н")
                return HumanPlayer.humanInput()
            }
        }
        else {
            if (sum < 5) {
                println("${activePlayer.name}: Сумма карт в прикупе меньше 5. Карты будут пересданы")
                return true
            }
            if (counter9 > 1) {
                println("${activePlayer.name}: Две девятки в прикупе. Карты будут пересданы")
                return true
            }
        }
        return false
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
//3 - пересдачи: а) сумма прикупа < 5 !         (после открытия прикупа - перед взятием карт активным игроком)
//               б) на руках < 14 очков!        (сразу после раздачи карт)
//               в) 4 девятки после раздачи!    (до торгов)
//               г) 2 девятки в прикупе!        (после открытия прикупа - перед взятием карт активным игроком)
//               д) 4 девятки на руках         (после сброса карт)
//4 - роспись по 60 очков оппонентам, у активного вычитается столько, сколько заявил на торгах