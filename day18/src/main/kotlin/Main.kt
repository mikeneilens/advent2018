import java.io.File

val adjacentVectors = listOf(Position(-1,-1),Position(0,-1),Position(+1,-1),Position(-1,0),Position(+1,0),Position(-1,+1),Position(0,+1),Position(+1,+1))

fun main(args: Array<String>) {
    val listOfData = readFile()
    var mapOfAcres = createMap(listOfData)
    mapOfAcres.print(0 )

    (1..10).forEachIndexed { ndx, _ ->
        val newMapOfAcres:Map<Position, AcreType> = mapOfAcres.mapValues{   val position = it.key; val acreType = it.value
                                                                            val surroundingAcres = position.adjacentAcres(mapOfAcres)
                                                                 acreType.transform(surroundingAcres)
                                                                        }
        mapOfAcres = newMapOfAcres
        mapOfAcres.print(ndx + 1)
    }

    //This is for part 2. You end up at this repeating pattern of 28 pairs which starts at ndx 2860
    val repeatingPatternAt2860 = listOf(Pair(641, 363), Pair(635, 363), Pair(629, 361), Pair(620, 363), Pair(614, 357), Pair(606, 358), Pair(601, 351), Pair(597, 346), Pair(595, 339),
        Pair(593, 337), Pair(595, 332), Pair(596, 331), Pair(600, 326), Pair(604, 328), Pair(610, 327), Pair(616, 328), Pair(624, 329), Pair(630, 334), Pair(637, 334), Pair(642, 339),
        Pair(648, 343), Pair(651, 348), Pair(653, 352), Pair(653, 358), Pair(654, 360), Pair(652, 361), Pair(651, 361), Pair(646, 365))

    val modOf1000000000 = (1000000000 - 2860) % repeatingPatternAt2860.size

    val treesAndLumberYards = repeatingPatternAt2860[modOf1000000000]
    println("value at position 1000000000 = ${treesAndLumberYards.first * treesAndLumberYards.second}")
}

fun createMap(listOfData:List<String>):Map<Position,AcreType> {
    val mapOfAcres = HashMap<Position,AcreType>()
    listOfData.forEachIndexed{y, line ->
        val listOfChars = line.toCharArray().map{it.toString()}
        listOfChars.forEachIndexed { x, char ->
            val position = Position(x,y)
            mapOfAcres[position] = char.toAcreType()
        }
    }
    return mapOfAcres
}
fun Map<Position,AcreType>.print(ndx:Int){
    val minX = this.keys.sortedBy { it.x }.first().x
    val minY = this.keys.sortedBy { it.y }.first().y
    val maxX = this.keys.sortedBy { it.x }.last().x
    val maxY = this.keys.sortedBy { it.y }.last().y
    (minY..maxY).forEach { y->
        var line = ""
        (minX..maxX).forEach { x->
            val image = this[Position(x,y)]?.image ?: " "
            line += image
        }
        //println(line)
    }
    val numberOfLumberYards = this.filter { it.value == AcreType.Lumberyard }.size
    val numberOfTrees = this.filter { it.value == AcreType.Trees }.size

    println("Number of Trees = $numberOfTrees, number of Lumberyards = $numberOfLumberYards Product is ${numberOfTrees * numberOfLumberYards}  $ndx")
    println()
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day18.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
class Position(val x:Int, val y:Int) {
    override fun equals(other: Any?): Boolean {
        return other is Position && this.x == other.x  && this.y == other.y
    }
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
    infix operator fun plus(other:Position):Position {
        return Position(x + other.x, y + other.y)
    }
    fun adjacentAcres(mapOfAcres:Map<Position, AcreType>):List<AcreType>{
        return adjacentVectors.mapNotNull {vector -> mapOfAcres[ this + vector] }
    }
}
enum class AcreType(val image:String) {
    OpenGround("."),
    Trees("|"),
    Lumberyard("#")
}

fun AcreType.transform(surroundingAcres:List<AcreType>):AcreType = when(this) {
        AcreType.OpenGround ->  if (surroundingAcres.threeOrMoreContainTrees()) AcreType.Trees else AcreType.OpenGround
        AcreType.Trees -> if (surroundingAcres.threeOrMoreContainLumberYards()) AcreType.Lumberyard else AcreType.Trees
        AcreType.Lumberyard -> if (surroundingAcres.oneOrMoreLumberYardsAndOneOrMoreTrees()) AcreType.Lumberyard else AcreType.OpenGround
}

fun List<AcreType>.threeOrMoreContainTrees():Boolean {
    return this.filter { it == AcreType.Trees }.size >= 3
}
fun List<AcreType>.threeOrMoreContainLumberYards():Boolean {
    return this.filter { it == AcreType.Lumberyard }.size >= 3
}
fun List<AcreType>.oneOrMoreLumberYardsAndOneOrMoreTrees():Boolean {
    return (this.any { it == AcreType.Lumberyard }) && (this.any { it == AcreType.Trees })
}

fun String.toAcreType() = when (this) {
    "." -> AcreType.OpenGround
    "#" -> AcreType.Lumberyard
    "|" -> AcreType.Trees
    else -> AcreType.OpenGround
}
