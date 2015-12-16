/* Trick-taking game for three players "1000"
by Sokolova Polina & Kuzmina Liza */

package console

fun main(args : Array<String>) {
    Game.ComputerPlayer1.name = "Computer Lisa"
    Game.ComputerPlayer2.name = "Computer Polina"
    Game.HumanPlayer.name = "Player"
    Game.ComputerPlayer1.totalScore = 0
    Game.ComputerPlayer2.totalScore = 0
    Game.HumanPlayer.totalScore = 0
    Game.HumanPlayer.handCards = arrayListOf(Card('s', 2), Card('d', 4), Card('d', 3),
            Card('h', 11), Card('h', 10), Card('h', 3), Card('h', 2), Card('h', 0))
    Game.ComputerPlayer2.handCards = arrayListOf(Card('s', 11), Card('s', 0), Card('c', 4),
            Card('c', 2), Card('d', 11), Card('d', 10), Card('d', 2), Card('d', 0))
    Game.ComputerPlayer1.handCards = arrayListOf(Card('s', 10), Card('s', 4), Card('s', 3),
            Card('c', 11), Card('c', 10), Card('c', 3), Card('c', 0), Card('h', 4))
    Game.HumanPlayer.obligation = 105
    Game.activePlayer = Game.HumanPlayer
    Game.lastTrick = Game.HumanPlayer
    Game.ComputerPlayer2.cardAnalysis()
    Game.ComputerPlayer1.cardAnalysis()

    for (i in 1..8) {
        Game.comparison()
    }
    Game.conclusion()
    //Game.startGame()
}