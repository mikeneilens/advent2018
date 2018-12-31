import javafx.geometry.Pos
import java.io.File
import java.lang.Math.abs
import kotlin.collections.HashMap

val offsets = listOf( Position(0,-1), Position(-1,0), Position(1,0), Position(0,1) )

fun main(args: Array<String>) {
    val mapLines = readFile()
    val mapOfSquaresAndUnits = createMapOfSquaresAndUnits(mapLines)
    val mapOfSquares = mapOfSquaresAndUnits.first
    var listOfUnits = mapOfSquaresAndUnits.second
    printMapOfSquares(mapOfSquares, listOfUnits)

    val completedRounds = playGame(mapOfSquares,listOfUnits)

    val totalScore = completedRounds * listOfUnits.fold(0){acc, unit -> acc + unit.hitPoints}
    println("Final score is $totalScore")
}

fun playGame(mapOfSquares: HashMap<String, Square>, listOfUnits: List<Unit>):Int {
    var completedRounds = 0

    while (completedRounds < 10000) {
        val sortedListOfUnits = listOfUnits.sortedBy { unit -> unit.position.y * 10000 + unit.position.x }.filter { it.isAlive() }
        sortedListOfUnits.forEach { unit ->
            val moveOutcome = unit.move(sortedListOfUnits, mapOfSquares)

            when (moveOutcome) {
                is MoveOutcome.NoTargetsLeft -> return completedRounds
                is MoveOutcome.Attack -> moveOutcome.unit.attacked(unit.attackPower)
                is MoveOutcome.Moved, MoveOutcome.CannotMove -> {
                    val targets = listOfUnits.filter { target -> (target::class != unit::class && target.isAlive()) }
                    val adjacentTarget = unit.adjacentTarget(targets)
                    if (adjacentTarget != null) {
                        println("${unit.position} is attacking ${adjacentTarget.position}")
                        adjacentTarget?.attacked(unit.attackPower)
                    }
                }
            }
        }
        completedRounds += 1
        println("Completed Round $completedRounds")
        printMapOfSquares(mapOfSquares, listOfUnits)
        if (completedRounds == 23) {
            println("stop!!")
        }
    }
    return completedRounds
}

fun Unit.move(listOfUnits:List<Unit>, mapOfSquares: HashMap<String, Square>):MoveOutcome {
    val targets = listOfUnits.filter { unit -> (unit::class != this::class && unit.isAlive()) }
    if (targets.size == 0) return MoveOutcome.NoTargetsLeft

    val adjacentTarget = this.adjacentTarget(targets)
    if (adjacentTarget != null) return MoveOutcome.Attack(adjacentTarget)

    val positionsInRangeOfTarget = targets.map{ target -> target.emptyAdjacentSpaces(mapOfSquares,listOfUnits)}.flatten()
    val positionsInRangeSorted = positionsInRangeOfTarget.sortedBy { position -> (position distanceTo this.position) * 1000000 + position.y * 1000 + position.x   }
    if (positionsInRangeSorted.size == 0) return MoveOutcome.CannotMove
    val closestPositionInRange = positionsInRangeSorted.first()

    val potentialSteps = this.emptyAdjacentSpaces(mapOfSquares, listOfUnits)
    if (potentialSteps.size == 0) return MoveOutcome.CannotMove

    val stepsNotOnBlockedPath = potentialSteps.filter { position -> pathIsClear(mapOfSquares, listOfUnits, position - this.position ,closestPositionInRange)   }

    val closestStepsToTarget = stepsNotOnBlockedPath.sortedBy{position -> (position distanceTo closestPositionInRange) * 1000000 + position.y + position.x}
    if (closestStepsToTarget.size == 0) return MoveOutcome.CannotMove
    val newPosition = closestStepsToTarget.first()
    this.position = newPosition
    return MoveOutcome.Moved
}

fun Unit.pathIsClear(mapOfSquares: HashMap<String, Square>, listOfUnits: List<Unit>, offset:Position,  targetPosition:Position):Boolean {
    var path = this.position + Position(offset.x,offset.y)
    while (((offset.x == 0 && path.y != targetPosition.y ) ||(offset.y == 0 && path.x != targetPosition.x) ) && (!positionIsBlocked(path, mapOfSquares, listOfUnits))) {
        path +=  Position(offset.x,offset.y)
    }
    return !positionIsBlocked(path, mapOfSquares, listOfUnits)
}

fun Unit.emptyAdjacentSpaces(mapOfSquares: HashMap<String, Square>, listOfUnits: List<Unit>):List<Position> {
    return  offsets.map{ offset ->
        val positionInRange = this.position + Position(offset.x,offset.y)
        if (!positionIsBlocked(positionInRange, mapOfSquares,listOfUnits)) positionInRange
        else  null
    }.filterNotNull()

}
fun positionIsBlocked(position:Position, mapOfSquares: HashMap<String, Square>, listOfUnits: List<Unit>):Boolean {
    return (mapOfSquares[position.toString()] != Square.Open || listOfUnits.unitAt(position) != null )
}

fun Unit.adjacentTarget(targets: List<Unit>):Unit? {
    var nearestTarget:Unit? = null
    targets.forEach { target ->
        offsets.forEach { offset ->
            val positionInRange = target.position + Position(offset.x,offset.y)
            if (positionInRange == this.position) {
                if (target.hitPoints < (nearestTarget?.hitPoints ?: 9999 )) {
                    nearestTarget = target
                }
            }
        }
    }
    return nearestTarget
}

fun Unit.attacked(attackPower:Int) {
    if (attackPower == 0 || !isAlive()) return
    else {
      //  println("$square at $position is being attacked!")
        hitPoints = hitPoints - 1
        this.attacked(attackPower - 1)
    }
}

fun createMapOfSquaresAndUnits(mapLines:List<String>):Pair<HashMap<String, Square>,List<Unit>> {
    val mapOfSquares = HashMap<String, Square>()
    var listOfUnits = listOf<Unit>()

    mapLines.forEachIndexed {y, line ->
        val listOfStringChars = line.toCharArray().map{it.toString()}
        listOfStringChars.forEachIndexed{ x, stringChar ->
            when (stringChar.toSquare()) {
                Square.Goblin -> {
                                    listOfUnits += Goblin(Position(x,y))
                                    mapOfSquares[Position(x,y).toString()] = Square.Open
                                }
                Square.Elf ->   {
                                    listOfUnits += Elf(Position(x, y))
                                    mapOfSquares[Position(x,y).toString()] = Square.Open
                                }
                else -> mapOfSquares[Position(x,y).toString()] = stringChar.toSquare()
            }
        }
    }
    return Pair(mapOfSquares, listOfUnits)
}

fun printMapOfSquares(mapOfSquares:HashMap<String, Square>, listOfUnits:List<Unit>) {
    val maxY= mapOfSquares.maxY()
    val maxX = mapOfSquares.maxX()

    var line = ""
    (0..maxY).forEach { y ->
        var line = ""
        (0..maxX).forEach {x ->
            val position = Position(x,y)
            val unit = listOfUnits.unitAt(position)
            if (unit != null && unit.isAlive())
                line += unit.square.image
            else
                line += mapOfSquares[position.toString()]?.image ?: Square.Open.image
        }
        line += listOfUnits.scoresOnRow(y)
        println(line)
    }
}

open class Unit(var position:Position, val square:Square, val attackPower:Int = 3, var hitPoints:Int = 200   ) {
    fun isAlive() = (hitPoints > 0)
    fun scoreToString():String {
        return "${square.image}($hitPoints)"
    }
}
fun List<Unit>.scoresOnRow(y:Int):String{
    var text=""
    this.forEach{unit->
        if (unit.position.y == y) text += unit.scoreToString()
    }
    return text
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
open class Vector(val x:Int, val y:Int) {
    infix operator fun plus(other:Vector):Vector {
        return Vector(x + other.x, y + other.y)
    }
    override fun equals(other: Any?): Boolean {
        return other is Vector && this.x == other.x  && this.y == other.y
    }
    override fun toString(): String {
        return "($x,$y)"
    }
}
class Position(x:Int, y:Int):Vector(x,y) {
    infix fun distanceTo(other:Position):Int {
        return abs(this.x - other.x) + abs(this.y - other.y)
    }
    infix operator fun plus(other:Position):Position {
        return Position(x + other.x, y + other.y)
    }
    infix operator fun minus(other:Position):Position {
        return Position(x - other.x, y - other.y)
    }
}
sealed class MoveOutcome {
    object NoTargetsLeft:MoveOutcome()
    class Attack(val unit:Unit):MoveOutcome()
    object Moved:MoveOutcome()
    object CannotMove:MoveOutcome()

    override fun toString(): String =  when(this) {
        is MoveOutcome.NoTargetsLeft -> "No targets left"
        is MoveOutcome.Attack -> "Attack" + this.unit.square
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
fun String.toPosition():Position {
    val values = this.removePrefix("(").removeSuffix(")").split(",")
    return Position(values[0].toInt(),values[1].toInt())
}
fun HashMap<String, Square>.maxY():Int {
    val positions = this.keys.toList()
    return positions.sortedBy { it.toPosition().y}.last().toPosition().y
}
fun HashMap<String, Square>.maxX():Int {
    val positions = this.keys.toList()
    return positions.sortedBy { it.toPosition().x}.last().toPosition().x
}
fun List<Unit>.unitAt(position: Position):Unit? {
    this.forEach { unit ->
        if (unit.position == position && unit.isAlive()) return unit
    }
    return null
}
