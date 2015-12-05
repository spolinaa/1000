/* Trick-taking game for three players "1000"
by Sokolova Polina & Kuzmina Liza */

package kotlin

import java.util.*

class Card(suit : Char, rank : Int) {
    internal var suit = suit
    internal var rank = rank
    internal var name = rankToName()
    private fun rankToName() : String {
        when (rank) {
            11 -> { return "A"  }
            10 -> { return "10" }
            4  -> { return "K"  }
            3  -> { return "Q"  }
            2  -> { return "J"  }
            0  -> { return "9"  }
            else -> { return "" }
        }
    }
    internal fun containsIn(array : ArrayList<Card>) : Boolean {
        val size = array.size
        for (i in array) {
            if (i.suit == this.suit && i.rank == this.rank ) {
                return true
            }
        }
        return false
    }

    internal fun removeFrom(array : ArrayList<Card>) : ArrayList<Card> {
        var res : ArrayList<Card> = ArrayList()
        for (i in array) {
            if (i.suit != this.suit || i.rank != this.rank ) {
                res.add(i)
            }
        }
        res = Game.sortBySuits(res)
        return res
    }
}