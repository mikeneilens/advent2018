import java.io.File
import kotlin.collections.HashMap

val offsets = listOf( Position(0,-1), Position(-1,0), Position(1,0), Position(0,1) )


fun main(args: Array<String>) {

    val mapLines = readFile()
    val mapOfSquaresAndUnits = createMapOfSquaresAndUnits(mapLines)
    val mapOfSquares = mapOfSquaresAndUnits.first
    val listOfUnits = mapOfSquaresAndUnits.second
    printMapOfSquares(mapOfSquares, listOfUnits)

    val completedRounds = playGame(mapOfSquares,listOfUnits)

    val totalHitPoints = listOfUnits.fold(0){acc, unit -> acc + unit.hitPoints}
    val totalScore = completedRounds * totalHitPoints

    println("Total hitpoints is $totalHitPoints. Score is $totalScore")
    printMapOfSquares(mapOfSquares, listOfUnits)
}

fun playGame(mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):Int {
    var completedRounds = 0

    while (completedRounds < 10000) {
        val sortedListOfUnits = listOfUnits.sortedBy { unit -> unit.position.y * 10000 + unit.position.x }.filter { it.isAlive() }
        sortedListOfUnits.forEach { unit ->
            val resultOfMove = unit.move(sortedListOfUnits, mapOfSquares)

            when (resultOfMove) {
                is ResultOfMove.NoTargetsLeft -> return completedRounds
                is ResultOfMove.Attack -> resultOfMove.unitToAttack.attacked(unit.attackPower)
                is ResultOfMove.Moved, ResultOfMove.CannotMove -> {
                    val targets = unit.targets(listOfUnits)
                    val adjacentTarget = unit.adjacentTarget(targets)
                    adjacentTarget?.attacked(unit.attackPower)
                }
            }
        }
        completedRounds += 1
        println("After $completedRounds round${if (completedRounds==1) "" else "s"}:")
        printMapOfSquares(mapOfSquares, listOfUnits)
    }
    return completedRounds
}

fun getRoute(targetPosition:Position, step:Step, mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):List<Step> {

    if (targetPosition == step.position) return listOf(step)

    val shortestWayToThisLocation = HashMap<Position, Int>()

    fun getRoute(step: Step, validRoutes: MutableList<Step>): List<Step> {

        val positionsAdjacentToStep = step.position.emptyAdjacentPositions(mapOfSquares, listOfUnits)

        //For each of the positions adjacent to the current step
        //If the position is the target location then we have reached our destination so add this step to the final list of routes.
        //Otherwise, if we have taken the smallest no of steps to get to the new position so record that and repeat the process.
        positionsAdjacentToStep.forEach { position ->
            if (position == targetPosition) {
                shortestWayToThisLocation[position] = step.noOfPreviousSteps
                val newStep = Step(position, step,step.noOfPreviousSteps + 1)
                validRoutes.add(newStep)
            } else {
                val shortestWayToHere = shortestWayToThisLocation[position] ?: 99999
                if (step.noOfPreviousSteps < shortestWayToHere) {
                    shortestWayToThisLocation[position] = step.noOfPreviousSteps
                    val newStep = Step(position, step,step.noOfPreviousSteps + 1)
                    getRoute(newStep, validRoutes)
                }
            }
        }
        return validRoutes
    }

    return getRoute(step, mutableListOf())
}

fun List<Step>.shortestRoute():Step {
    var fewestSteps = 9999999
    var finalStep = Step(Position(0,0), null,0)
    //does this manually to keep to top to bottom rule in event of a draw
    this.forEach { step ->
        if (step.noOfPreviousSteps < fewestSteps) {
            fewestSteps = step.noOfPreviousSteps
            finalStep = step
        }
    }
    return finalStep
}

fun createMapOfSquaresAndUnits(mapLines:List<String>):Pair<HashMap<Position, Square>,List<Unit>> {
    val mapOfSquares = HashMap<Position, Square>()
    var listOfUnits = listOf<Unit>()

    mapLines.forEachIndexed {y, line ->
        val listOfStringChars = line.toCharArray().map{it.toString()}
        listOfStringChars.forEachIndexed{ x, stringChar ->
            when (stringChar.toSquare()) {
                Square.Goblin -> {  listOfUnits += Goblin(Position(x,y))
                                    mapOfSquares[Position(x,y)] = Square.Open
                                }
                Square.Elf ->   {   listOfUnits += Elf(Position(x, y))
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
    println()
}

fun List<Unit>.unitAt(position: Position):Unit? {
    this.forEach { unit ->
        if (unit.position == position && unit.isAlive()) return unit
    }
    return null
}
fun List<Unit>.scoresOnRow(y:Int):String{
    var text="   "
    var prefix = ""
    this.forEach{unit->
        if (unit.position.y == y && unit.isAlive()) {
            text += prefix + unit.scoreToString()
            prefix = ", "
        }
    }
    return text
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

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day15-test2.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

