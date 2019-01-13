import java.io.File

//val depth = 510
val depth = 8103
//val target = Position(10,10)
val target = Position(9,758)

fun main(args: Array<String>) {

    print(15,15)

    val totalRiskLevel = sumOfPositions(target)
    println("total risk level is: $totalRiskLevel")
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
//    checkResult(resultData)
}

fun checkResult(resultData:List<String>) {
    val expectedData = readFile("/Users/michaelneilens/day22-result.txt")

    resultData.forEachIndexed{ndx, data ->
        if (data != expectedData[ndx]) {
            println("Error at row $ndx")
            println("Expected data ${expectedData[ndx]}")
            println("Actual data   $data")
        }
    }
}

enum class RegionType(val riskLevel:Int) {
    Rocky (riskLevel = 0),
    Narrow(riskLevel = 2),
    Wet(riskLevel = 1)
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
        this == target -> 0
        else -> Position(x -1 , y).erosionLevel() * Position (x, y-1).erosionLevel()
    }
    fun toRegionString():String = when {
        x == 0 && y == 0 -> "M"
        this == target -> "T"
        else -> regionType().toString2()
    }
    companion object { //used as a datastore to prevent recursive calculation of erosionLevel
        val mapOfErosionLevels = HashMap<Position, Int>()
    }
}
fun readFile(inputFile:String):List<String> {
    val lineList = mutableListOf<String>()
    File(inputFile).useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
