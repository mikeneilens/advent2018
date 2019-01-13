import java.io.File

//val depth = 510
val depth = 8103
//val target = Position(10,10)
val target = Position(9,758)
val validMoves=listOf(Position(-1,0),Position(+1,0),Position(0,-1),Position(0,+1))


fun main(args: Array<String>) {

    print(15,15)

    val totalRiskLevel = sumOfPositions(target)
    println("total risk level is: $totalRiskLevel")

    val firstStep = Step(Position(0,0),null,0,Tool.Torch)
    val routeToTarget = firstStep.getFastestTimeToTarget()
    println("Fastest time to target $routeToTarget")
}

fun sumOfPositions(target:Position):Int {
    var total = 0
    (0..target.y).forEach { y ->
        (0..target.x).forEach { x ->
            total += Position(x,y).regionType().riskLevel
        }
    }
    return total
}
fun print(maxX:Int, maxY:Int) {
    val resultData = mutableListOf<String>()
    (0..maxY).forEach { y->
        var line = ""
        (0..maxX).forEach { x ->
            val position = Position(x, y)
            line += position.toRegionString()
        }
        resultData += line
        println(line)
    }
}

class Step (val position:Position, val parent:Step?, val timeToReach:Int, val tool:Tool) {

    fun getFastestTimeToTarget():Int {
        var mapOfShortestTimeToPositionWithTorch = hashMapOf<Position, Int>()
        var mapOfShortestTimeToPositionWithClimbingGear = hashMapOf<Position, Int>()
        var mapOfShortestTimeToPositionWithNoTools = hashMapOf<Position, Int>()

        fun isShortestTimeToPosition(newStep:Step):Boolean {

            fun isShortestTimeToPosition(newStep:Step, mapOfShortestTimeToPosition:HashMap<Position, Int>):Boolean {

                val shortestTimeToPosition = mapOfShortestTimeToPosition[newStep.position] ?: 9999999

                if (newStep.timeToReach < shortestTimeToPosition ) {
                    mapOfShortestTimeToPosition[newStep.position] = newStep.timeToReach
                    return true
                } else {
                    return false
                }
            }

            return when (newStep.tool) {
                Tool.Torch -> isShortestTimeToPosition(newStep, mapOfShortestTimeToPositionWithTorch)
                Tool.ClimbingGear -> isShortestTimeToPosition(newStep, mapOfShortestTimeToPositionWithClimbingGear)
                Tool.None -> isShortestTimeToPosition(newStep, mapOfShortestTimeToPositionWithNoTools)
            }
        }

        fun isShorterTimeThanShortestTimeToTarget(time:Int):Boolean {
            val timeToTarget = mapOfShortestTimeToPositionWithTorch[target]
            if (timeToTarget == null) return true
            if (time < timeToTarget) return true
            else return false
        }
        //This returns an array of the next steps that can be reached from this position.
        // It doesn't look for new positions of the steps is already at the target.
        //It only returns steps that if the time taken to the new position is:
        //     shorter than the fastest time to the target
        //     and the previous fastest time taken to reach the new positon
        fun nextSteps(step:Step):List<Step> {
            if (step.position.isAtTarget()) return listOf<Step>()

            var steps = listOf<Step>()
            validMoves.forEach {validMove ->
                val newPosition = step.position + validMove
                if (!newPosition.isOutOfBounds()) {
                    val regionType = newPosition.regionType()
                    //must use torch at target regardless of region type
                    val toolsAllowedAtTheNewPosition = if (newPosition == target) listOf<Tool>(Tool.Torch) else regionType.validTools
                    //if the tool needs to be changed then try all valid tools or the region
                    val tools = if (toolsAllowedAtTheNewPosition.none{tool -> tool == step.tool}) toolsAllowedAtTheNewPosition else listOf(step.tool)

                    tools.forEach { validTool ->
                        val newTimeToReach = step.timeToReach + 1 + if (validTool == step.tool) 0 else 7
                        val newStep = Step(newPosition, step, newTimeToReach, validTool)
                        //only continue to this position, if its the quickest so far to this position and its still possible to get to the target and beat the beat time.
                        if ( isShorterTimeThanShortestTimeToTarget(newTimeToReach) && isShortestTimeToPosition(newStep))  {
                            steps  += newStep
                        }
                    }
                }
            }
            return steps
        }

        mapOfShortestTimeToPositionWithTorch[this.position] = 0
        mapOfShortestTimeToPositionWithNoTools[this.position] = 0
        mapOfShortestTimeToPositionWithClimbingGear[this.position] = 0

        var stepsToProcess = nextSteps(this)
        while (stepsToProcess.isNotEmpty()) {
            stepsToProcess = stepsToProcess.map{step -> nextSteps(step)}.flatten()
        }
        val timeToTarget = mapOfShortestTimeToPositionWithTorch[target] ?: 9999999
        return timeToTarget
    }

    override fun toString(): String {
        return "$position, time to reach $timeToReach, tool $tool"
    }
}

enum class RegionType(val riskLevel:Int, val validTools:List<Tool>) {
    Rocky (riskLevel = 0, validTools = listOf(Tool.ClimbingGear, Tool.Torch)),
    Narrow(riskLevel = 2, validTools = listOf(Tool.None, Tool.Torch)),
    Wet(riskLevel = 1, validTools = listOf(Tool.ClimbingGear, Tool.None))
}
enum class Tool {
    Torch,
    ClimbingGear,
    None
}

fun RegionType.toString2():String = when(this) {
    RegionType.Rocky -> "."
    RegionType.Narrow -> "|"
    RegionType.Wet -> "="
}

class Position(val x:Int, val y:Int) {
    override fun equals(other: Any?): Boolean {
        return other is Position && x == other.x && y == other.y
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
    private fun erosionLevel():Int {
        val levelFromMap = mapOfErosionLevels[this]
        return if (levelFromMap == null ) {
            val level = (geologicIndex() + depth) % 20183
            mapOfErosionLevels[this] = level
            level
        } else {
            levelFromMap
        }
    }
    fun regionType():RegionType = when (erosionLevel() % 3) {
        0 -> RegionType.Rocky
        1 -> RegionType.Wet
        2 -> RegionType.Narrow
        else -> RegionType.Narrow
    }
    private fun geologicIndex():Int = when {
        x == 0 && y == 0 -> 0
        y == 0 && x != 0 -> x * 16807
        x == 0 && y != 0 -> y * 48271
        isAtTarget() -> 0
        else -> Position(x -1 , y).erosionLevel() * Position (x, y-1).erosionLevel()
    }
    fun isAtTarget():Boolean = this == target

    fun isOutOfBounds():Boolean = (this.x < 0) or (this.y < 0 ) || (this.x > 20 * target.x) || (this.y > depth)

    fun toRegionString():String = when {
        x == 0 && y == 0 -> "M"
        this == target -> "T"
        else -> regionType().toString2()
    }

    infix operator fun plus(other:Position):Position {
        return Position(x + other.x, y + other.y)
    }
    companion object {
        val mapOfErosionLevels = HashMap<Position, Int>()
    }
}
fun readFile(inputFile:String):List<String> {
    val lineList = mutableListOf<String>()
    File(inputFile).useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
