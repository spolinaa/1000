package kotlin

class Card(suit : Char, rank : Int) {
    internal var suit = suit
    internal var rank = rank
    internal var isTrump = false
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

}