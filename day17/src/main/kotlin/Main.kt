import java.io.File
import kotlin.random.Random

fun main(args: Array<String>) {

    val listOfData = readFile()

    val mapOfTiles = createMap(listOfData)
    printMap(mapOfTiles)
    var x = 0
    var noOfWaterTiles = 0
    var noOfWetSandTiles = 0
    while (x < 100000) {
        pourWater(Position(500,0),Vector(0,1),mapOfTiles, x)
        if (x % 5000 == 0 ) { //check every 10000 drops
            val noOfNewWaterTiles = mapOfTiles.countTiles(Tile.Water)
            val noOfNewWetSandTiles = mapOfTiles.countTiles(Tile.WetSand)
            if (noOfNewWaterTiles == noOfWaterTiles || noOfNewWetSandTiles == noOfWetSandTiles ) {
                break
            } else {
                printMap(mapOfTiles)
                noOfWaterTiles = noOfNewWaterTiles
                noOfWetSandTiles = noOfNewWetSandTiles
                println("Wet sand tiles = $noOfWetSandTiles")
                println("Water tiles    = $noOfWaterTiles")
            }
        }
        if (x % 1000 == 0) {
            println(x)
        }
        x += 1
    }

    noOfWaterTiles = mapOfTiles.countTiles(Tile.Water)
    noOfWetSandTiles = mapOfTiles.countTiles(Tile.WetSand)

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
        var wetTiles = 0
        (minX..maxX).forEach{ x ->
            val tile = mapOfTiles[Position(x,y)]
            line += when (tile) {
                        null -> "."
                        else -> tile.toString()
                    }
            when (tile) {
                Tile.Water -> wetTiles += 1
                Tile.WetSand -> wetTiles += 1
            }
        }
        println(line + " $wetTiles")
    }
    println()
}


fun pourWater(position:Position, currentDirection:Vector,mapOfTiles:HashMap<Position, Tile>, dropNo:Int ){
    val minX = mapOfTiles.minX()
    val maxX = mapOfTiles.maxX()
    val maxY = mapOfTiles.maxY()
    val downDirection = Vector(0,1)
    val leftDirection = Vector(-1,0)
    val rightDirection = Vector(1,0)

    tailrec fun pourWaterBounded(position:Position, currentDirection:Vector,mapOfTiles:HashMap<Position, Tile> ){

        //See if water can drop
        if (!position.positionBelowIsBlocked(mapOfTiles)) {
            if (position.y == maxY) {
                return
            } else {
                val newPosition = position + downDirection
                mapOfTiles[newPosition] =  Tile.WetSand
                return pourWaterBounded(newPosition, downDirection, mapOfTiles)
            }
        } else {

        }

        //See if there are leaks to the left or right. Return left and right bounds of container if there are no leaks
        val leakToLeft = mapOfTiles.leakToLeft(position,position.x, minX)
        val leakToRight = mapOfTiles.leakToRight(position,position.x, maxX)

        if (!leakToLeft.first && !leakToRight.first ) {
            val freePositionToLeft = leakToLeft.second
            val freePositionToRight = leakToRight.second
            when {
                (freePositionToLeft.x != position.x ) -> mapOfTiles[freePositionToLeft] = Tile.Water
                (freePositionToRight.x != position.x ) -> mapOfTiles[freePositionToRight] = Tile.Water
                else -> mapOfTiles[position] = Tile.Water
            }
            return
        }

        //If I've fallen to here then set off in either left or right direction
        if (currentDirection == downDirection) {
            if (dropNo % 5 == 2 || dropNo % 5 == 3) {
                return pourWaterBounded(position, leftDirection, mapOfTiles)
            } else {
                return pourWaterBounded(position, rightDirection, mapOfTiles)
            }
        }

        val newPosition = position + currentDirection
        //If cannot go in current direction, go in the opposite direction
        if (newPosition.positionIsBlocked(mapOfTiles)) {
            if (currentDirection == rightDirection) {
                return pourWaterBounded(position, leftDirection, mapOfTiles)
            } else {
                return pourWaterBounded(position, rightDirection, mapOfTiles)
            }
        }

        //If have got here then am following a valid wet route so put down a wet sand marker
        mapOfTiles[newPosition] =  Tile.WetSand
        return pourWaterBounded(newPosition, currentDirection, mapOfTiles)
    }

    pourWaterBounded(position,currentDirection, mapOfTiles)
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
