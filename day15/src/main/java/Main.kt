import java.io.File
import java.lang.Math.abs
import kotlin.collections.HashMap

val offsets = listOf( Position(0,-1), Position(-1,0), Position(1,0), Position(0,1) )

fun main(args: Array<String>) {

    val mapLines = readFile()
    val mapOfSquaresAndUnits = createMapOfSquaresAndUnits(mapLines)
    val mapOfSquares = mapOfSquaresAndUnits.first
    val listOfUnits = mapOfSquaresAndUnits.second
    printMapOfSquares(mapOfSquares, listOfUnits)

    val completedRounds = playGame(mapOfSquares,listOfUnits)

    val totalScore = completedRounds * listOfUnits.fold(0){acc, unit -> acc + unit.hitPoints}
    println("Final score is $totalScore")
}

fun playGame(mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):Int {
    var completedRounds = 0

    while (completedRounds < 10000) {
        val sortedListOfUnits = listOfUnits.sortedBy { unit -> unit.position.y * 10000 + unit.position.x }.filter { it.isAlive() }
        sortedListOfUnits.forEach { unit ->
            val moveOutcome = unit.move(sortedListOfUnits, mapOfSquares)

            when (moveOutcome) {
                is MoveOutcome.NoTargetsLeft -> return completedRounds
                is MoveOutcome.Attack -> moveOutcome.unitToAttack.attacked(unit.attackPower)
                is MoveOutcome.Moved, MoveOutcome.CannotMove -> {
                    val targets = unit.targets(listOfUnits)
                    val adjacentTarget = unit.adjacentTarget(targets)
                    adjacentTarget?.attacked(unit.attackPower)
                }
            }
        }
        completedRounds += 1
        println("Completed Round $completedRounds")
        printMapOfSquares(mapOfSquares, listOfUnits)
        if (completedRounds == 13) {
            println("stop!!")
        }
    }
    return completedRounds
}

fun getRoute(targetPosition:Position, node:Node, mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):List<Node> {

    if (targetPosition == node.position) return listOf(node)

    val shortestWayToThisLocation = HashMap<Position, Int>()

    fun getRoute( node: Node, validEndNodes: MutableList<Node>): List<Node> {

        val adjacentPositions = node.position.emptyAdjacentPositions(mapOfSquares, listOfUnits)

        adjacentPositions.forEach { position ->
            if (position == targetPosition) {
                shortestWayToThisLocation[position] = node.ancestors
                val newNode = Node(position, node,node.ancestors + 1)
                validEndNodes.add(newNode)
            } else {
                val shortestWayToHere = shortestWayToThisLocation[position] ?: 99999
                if (node.ancestors < shortestWayToHere) {
                    shortestWayToThisLocation[position] = node.ancestors
                    val newNode = Node(position, node,node.ancestors + 1)
                    getRoute(newNode, validEndNodes)
                }
            }
        }
        return validEndNodes
    }

    return getRoute(node, mutableListOf())
}

class Node(val position:Position, private val parent:Node?, val ancestors:Int) {
    fun rootNode():Node {
        return if (parent?.parent == null) this else this.parent.rootNode()
    }
    override fun toString(): String {
        return "Node at $position"
    }
}
fun List<Node>.shortestRoute():Node {
    var shortest = 9999999
    var nodeToReturn = Node(Position(0,0), null,0)
    //does this manually to keep to top to bottom rule in event of a draw
    this.forEach { node ->
        if (node.ancestors < shortest) {
            shortest = node.ancestors
            nodeToReturn = node
        }
    }
    return nodeToReturn
}

fun createMapOfSquaresAndUnits(mapLines:List<String>):Pair<HashMap<Position, Square>,List<Unit>> {
    val mapOfSquares = HashMap<Position, Square>()
    var listOfUnits = listOf<Unit>()

    mapLines.forEachIndexed {y, line ->
        val listOfStringChars = line.toCharArray().map{it.toString()}
        listOfStringChars.forEachIndexed{ x, stringChar ->
            when (stringChar.toSquare()) {
                Square.Goblin -> {
                                    listOfUnits += Goblin(Position(x,y))
                                    mapOfSquares[Position(x,y)] = Square.Open
                                }
                Square.Elf ->   {
                                    listOfUnits += Elf(Position(x, y))
                                    mapOfSquares[Position(x,y)] = Square.Open
                                }
                else -> mapOfSquares[Position(x,y)] = stringChar.toSquare()
            }
        }
    }
    return Pair(mapOfSquares, listOfUnits)
}

fun printMapOfSquares(mapOfSquares:HashMap<Position, Square>, listOfUnits:List<Unit>) {
    val maxY= mapOfSquares.maxY()
    val maxX = mapOfSquares.maxX()

    (0..maxY).forEach { y ->
        var line = ""
        (0..maxX).forEach {x ->
            val position = Position(x,y)
            val unit = listOfUnits.unitAt(position)
            line += if (unit != null && unit.isAlive()) unit.square.image
                    else mapOfSquares[position]?.image ?: Square.Open.image
        }
        line += listOfUnits.scoresOnRow(y)
        println(line)
    }
}

fun List<Unit>.scoresOnRow(y:Int):String{
    var text=""
    this.forEach{unit->
        if (unit.position.y == y) text += unit.scoreToString()
    }
    return text
}

open class Unit(var position:Position, val square:Square, val attackPower:Int = 3, var hitPoints:Int = 200   ) {
    fun isAlive() = (hitPoints > 0)
    fun scoreToString():String {
        return "${square.image}($hitPoints)"
    }

    fun move(listOfUnits:List<Unit>, mapOfSquares: HashMap<Position, Square>):MoveOutcome {
        val targets =  targets(listOfUnits)
        if (targets.isEmpty()) return MoveOutcome.NoTargetsLeft

        val adjacentTarget = this.adjacentTarget(targets)
        if (adjacentTarget != null) return MoveOutcome.Attack(adjacentTarget)

        val positionsInRangeOfTarget = targets.map{ target -> target.emptyAdjacentPositions(mapOfSquares,listOfUnits)}.flatten()
        val positionsInRangeSorted = positionsInRangeOfTarget.sortedBy { position -> (position distanceTo this.position) * 1000000 + position.y * 1000 + position.x   }
        if (positionsInRangeSorted.isEmpty()) return MoveOutcome.CannotMove

        positionsInRangeSorted.forEach { targetPosition ->
            val validRoutes = getRoute(targetPosition, Node(this.position, null,0),  mapOfSquares,listOfUnits)
            if (validRoutes.isNotEmpty()) {
                val bestNode = validRoutes.shortestRoute().rootNode()
                this.position = bestNode.position
                return MoveOutcome.Moved
            }
        }
        return MoveOutcome.CannotMove

    }

    private fun emptyAdjacentPositions(mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):List<Position> {
        return this.position.emptyAdjacentPositions(mapOfSquares,listOfUnits)
    }

    fun adjacentTarget(listOfUnits: List<Unit>):Unit? {
        var targetWithLowestHitPoints:Unit? = null
        listOfUnits.forEach { unit ->
            offsets.forEach { offset ->
                val positionInRange = unit.position + Position(offset.x,offset.y)
                if (positionInRange == this.position) {
                    if (unit.hitPoints < (targetWithLowestHitPoints?.hitPoints ?: 9999 )) {
                        targetWithLowestHitPoints = unit
                    }
                }
            }
        }
        return targetWithLowestHitPoints
    }

    fun attacked(attackPower:Int) {
        if (attackPower == 0 || !isAlive()) return
        else {
            //  println("$square at $position is being attacked!")
            hitPoints -= 1
            this.attacked(attackPower - 1)
        }
    }

    fun targets(listOfUnits:List<Unit>):List<Unit>{
        return listOfUnits.filter { unit ->  (unit::class != this::class && unit.isAlive()) }
    }
}


class Goblin( position:Position, square:Square = Square.Goblin ):Unit(position, square)
class Elf( position:Position, square:Square = Square.Elf ):Unit(position, square)

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day15-test2.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

enum class Square(val image:String) {
    Wall("#"),
    Open("."),
    Goblin("G"),
    Elf("E")
}

class Position(val x:Int, val y:Int) {
    infix fun distanceTo(other:Position):Int {
        return abs(this.x - other.x) + abs(this.y - other.y)
    }
    override fun equals(other: Any?): Boolean {
        return other is Position && this.x == other.x  && this.y == other.y
    }
    override fun toString(): String {
        return "($x,$y)"
    }
    infix operator fun plus(other:Position):Position {
        return Position(x + other.x, y + other.y)
    }
    infix operator fun minus(other:Position):Position {
        return Position(x - other.x, y - other.y)
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

sealed class MoveOutcome {
    object NoTargetsLeft:MoveOutcome()
    class Attack(val unitToAttack:Unit):MoveOutcome()
    object Moved:MoveOutcome()
    object CannotMove:MoveOutcome()

    override fun toString(): String =  when(this) {
        is MoveOutcome.NoTargetsLeft -> "No targets left"
        is MoveOutcome.Attack -> "Attack" + this.unitToAttack.square
        is MoveOutcome.Moved -> "Moved"
        is MoveOutcome.CannotMove -> "Cannot Move"
    }
}

fun String.toSquare():Square = when (this) {
    "#" -> Square.Wall
    "." -> Square.Open
    "G" -> Square.Goblin
    "E" -> Square.Elf
    else -> Square.Open
}

fun HashMap<Position, Square>.maxY():Int {
    val positions = this.keys.toList()
    return positions.sortedBy { it.y}.last().y
}
fun HashMap<Position, Square>.maxX():Int {
    val positions = this.keys.toList()
    return positions.sortedBy { it.x}.last().x
}
fun List<Unit>.unitAt(position: Position):Unit? {
    this.forEach { unit ->
        if (unit.position == position && unit.isAlive()) return unit
    }
    return null
}
