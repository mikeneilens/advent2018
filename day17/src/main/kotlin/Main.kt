import java.io.File
import kotlin.random.Random

fun main(args: Array<String>) {

    val listOfData = readFile()

    val mapOfTiles = createMap(listOfData)
    var x = 0
    var leaksFound = 0
    while (x < 100000 && leaksFound < 10) {
        leaksFound += pourWater(Position(500,0),mapOfTiles)
        x += 1
    }

    val noOfWaterTiles = mapOfTiles.countTiles(Tile.Water)
    val noOfWetSandTiles = mapOfTiles.countTiles(Tile.WetSand)

    printMap(mapOfTiles)
    println("Wet sand tiles = $noOfWetSandTiles")
    println("Water tiles    = $noOfWaterTiles")
    println("Total    = ${noOfWaterTiles + noOfWetSandTiles}")
}

fun createMap(listOfData:List<String>):HashMap<Position, Tile>{
    val mapOfTiles = HashMap<Position, Tile>()
    listOfData.forEach { line ->
        val rangeX = line.toFirst("x")..line.toSecond("x")
        val rangeY = line.toFirst("y")..line.toSecond("y")
        rangeY.forEach {y ->
            rangeX.forEach{ x->
                mapOfTiles[Position(x,y)] = Tile.Clay
            }
        }
    }
    return mapOfTiles
}

fun printMap(mapOfTiles:HashMap<Position, Tile>) {
    val minX = mapOfTiles.minX()
    val maxX = mapOfTiles.maxX()
    val minY = mapOfTiles.minY()
    val maxY = mapOfTiles.maxY()

    var line1 = ""
    var line2 = ""
    var line3 = ""
    (minX..maxX).forEach{ x ->
        line1 += x.toString().substring(0,1)
        line2 += x.toString().substring(1,2)
        line3 += x.toString().substring(2,3)
    }
    println(line1)
    println(line2)
    println(line3)

    (minY..maxY).forEach() { y ->
        var line = ""
        (minX..maxX).forEach{ x ->
            val tile = mapOfTiles[Position(x,y)]
            line += when (tile) {
                        null -> "."
                        else -> tile.toString()
                    }
        }
        println(line)
    }
    println()
}


fun pourWater(position:Position, mapOfTiles:HashMap<Position, Tile>):Int {

    val minX = mapOfTiles.minX()
    val maxX = mapOfTiles.maxX()
    val maxY = mapOfTiles.maxY()

    tailrec fun pourWaterBounded(position:Position):Int{

        if (position.y >= maxY) return 1

        if (position.positionIsBlocked(mapOfTiles)) return 0

        //See if water can drop
        if (!position.positionBelowIsBlocked(mapOfTiles)) {
            val newY = mapOfTiles.fillVerticalWithTilesUntilBlocked(position, maxY, Tile.WetSand)
            if (newY >= maxY) return 1
            else return pourWaterBounded(Position(position.x, newY))
        }

        //See if there are leaks to the left or right.
        val leakToLeft = mapOfTiles.leakToLeft(position,position.x, minX)
        val leakToRight = mapOfTiles.leakToRight(position,position.x, maxX)

        val positionOfLeakOrLeftBoundary = leakToLeft.second
        val positionOfLeakOrRightBoundary = leakToRight.second

        //if no leaks, fill this row with water, otherwise fill row with wet tiles
        val tile = if (!leakToLeft.first && !leakToRight.first ) Tile.Water else Tile.WetSand

        //fill this row with tiles
        mapOfTiles.fillHorizontalWithTiles(positionOfLeakOrLeftBoundary.x, positionOfLeakOrRightBoundary.x, position.y, tile)

        //if no leaks, return otherwise carry on dropping at position of the leaks
        when {
            (!leakToLeft.first && !leakToRight.first ) -> return 0
            (leakToLeft.first && leakToRight.first) ->
                return if (Random.nextBoolean()) pourWaterBounded(positionOfLeakOrLeftBoundary ) else pourWaterBounded(positionOfLeakOrRightBoundary )
            (leakToLeft.first) -> return pourWaterBounded(positionOfLeakOrLeftBoundary  )
            (leakToRight.first) -> return pourWaterBounded(positionOfLeakOrRightBoundary )
            else -> return 0
        }
    }

    return pourWaterBounded(position)
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day17.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

open class Vector(val x:Int, val y:Int) {
    override fun equals(other: Any?): Boolean {
        return other is Vector && this.x == other.x  && this.y == other.y
    }
    override fun toString(): String {
        return "($x,$y)"
    }
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}
class Position(x:Int, y:Int):Vector(x, y) {
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
    infix operator fun plus(other:Vector):Position {
        return Position(x + other.x, y + other.y)
    }
    fun positionBelowIsBlocked(mapOfTiles: HashMap<Position, Tile>):Boolean {
        return Position(x, y +1).positionIsBlocked(mapOfTiles)
    }
    fun positionIsBlocked(mapOfTiles: HashMap<Position, Tile>):Boolean {
        return mapOfTiles[this] == Tile.Clay || mapOfTiles[this] == Tile.Water
    }

}
enum class Tile {
    Clay,
    Sand,
    Water,
    WetSand;

    override fun toString():String = when(this) {
        Clay -> "#"
        Sand -> "."
        Water -> "~"
        WetSand -> "|"
    }

}

fun HashMap<Position,Tile>.minX():Int  {
    return this.keys.sortedBy { it.x }.first().x - 1
}
fun HashMap<Position,Tile>.maxX():Int  {
    return this.keys.sortedBy { it.x }.last().x + 1
}
fun HashMap<Position,Tile>.minY():Int  {
    return this.keys.sortedBy { it.y }.first().y
}
fun HashMap<Position,Tile>.maxY():Int  {
    return this.keys.sortedBy { it.y }.last().y
}
fun HashMap<Position,Tile>.countTiles(tile:Tile):Int {
    return this.values.filter{it == tile}.size
}
tailrec fun HashMap<Position,Tile>.fillVerticalWithTilesUntilBlocked(position:Position, maxY:Int, tile:Tile):Int {
    if (position.y >= maxY) return position.y
    if (position.positionBelowIsBlocked(this)) return position.y
    val newPosition = Position(position.x, position.y +1)
    this[newPosition] =  Tile.WetSand
    return fillVerticalWithTilesUntilBlocked(newPosition, maxY, tile)
}
tailrec fun HashMap<Position,Tile>.fillHorizontalWithTiles(firstX:Int, lastX:Int, y:Int, tile:Tile) {
    if (firstX > lastX) return
    this[Position(firstX, y)] = tile
    fillHorizontalWithTiles(firstX + 1,lastX, y, tile)
}

tailrec fun HashMap<Position, Tile>.leakToLeft(position:Position, x:Int, minX:Int):Pair<Boolean, Position> {
    if (x < minX) return Pair(true, Position(minX, position.y))
    else {
        if (Position(x, position.y).positionIsBlocked(this  ))  {
            return Pair(false, Position(x + 1, position.y))
        } else {
            if (!Position(x, position.y).positionBelowIsBlocked(this)) {
                return Pair(true, Position(x, position.y))
            } else {
                return leakToLeft(position, x -1, minX)
            }
        }
    }
}
tailrec fun HashMap<Position, Tile>.leakToRight(position:Position, x:Int, maxX:Int):Pair<Boolean, Position> {
    if (x > maxX) return Pair(true, Position(maxX, position.y))
    else {
        if (Position(x, position.y).positionIsBlocked(this)) {
            return Pair(false, Position(x - 1, position.y))
        }
        else {
            if (!Position(x, position.y).positionBelowIsBlocked(this)) {
                return Pair(true, Position(x, position.y) )
            } else {
                return leakToRight(position, x +1, maxX)
            }
        }
    }
}

fun String.toFirst(key:String):Int {
    val firstSplit = this.split("$key=")
    if (firstSplit[0]=="")
        return firstSplit[1].split(",")[0].toInt()
     else {
        return firstSplit[1].split("..")[0].toInt()
    }
}
fun String.toSecond(key:String):Int {
    val firstSplit = this.split("$key=")
    if (firstSplit[0]=="")
        return firstSplit[1].split(",")[0].toInt()
    else {
        return firstSplit[1].split("..")[1].toInt()
    }
}
