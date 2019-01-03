class Position(val x:Int, val y:Int) {
    override fun equals(other: Any?): Boolean {
        return other is Position && this.x == other.x  && this.y == other.y
    }
    override fun toString(): String {
        return "($x,$y)"
    }
    infix operator fun plus(other:Position):Position {
        return Position(x + other.x, y + other.y)
    }
    fun emptyAdjacentPositions(mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):List<Position> {
        return offsets.mapNotNull { offset ->
            val positionInRange = this + Position(offset.x,offset.y)
            if (!positionInRange.isBlocked(mapOfSquares,listOfUnits)) positionInRange
            else  null
        }
    }
    private fun isBlocked(mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):Boolean {
        return (mapOfSquares[this] != Square.Open || listOfUnits.unitAt(this) != null )
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}