import java.io.File
import java.lang.Math.abs

fun main(args: Array<String>) {
    val listOfPlaceCoOrdinates = readFile()
//    val listOfPlaceCoOrdinates = listOf("1, 1" , "1, 6", "8, 3", "3, 4", "5, 5", "8, 9")
    val listOfPlaces = listOfPlaceCoOrdinates.map { Place.create(it) }

    val minX = listOfPlaces.sortedBy { it.x }.first().x - 1
    val maxX = listOfPlaces.sortedBy { it.x }.last().x + 1
    val minY = listOfPlaces.sortedBy { it.y }.first().y - 1
    val maxY = listOfPlaces.sortedBy { it.y }.last().y + 1

    val map = HashMap<String,Int>()

    map.addCoordinates(listOfPlaces,minX,maxX,minY,maxY)
    map.print(minX,maxX,minY,maxY)

    val mapContainingOnes =  map.filterValues { value -> value == 1 }
    println("safe area has size of ${mapContainingOnes.size}")
}

fun HashMap<String,Int>.addCoordinates(listOfPlaces:List<Place>,minX:Int, maxX:Int, minY:Int, maxY:Int) {
    (minY..maxY).forEach { y ->
        (minX..maxX).forEach { x ->
            val distanceToAlPlaces = listOfPlaces.fold(0){acc, place -> acc + place.distanceToLocation(x,y) }
            if (distanceToAlPlaces < 10000) {
                this[key(x,y)] = 1
            } else {
                this[key(x,y)] = 0
            }
        }
    }
}

fun HashMap<String,Int>.print(minX:Int, maxX:Int, minY:Int, maxY:Int) {
    (minY..maxY).forEach { y ->
        var line = ""
        (minX..maxX).forEach { x ->
            val value =  this[key(x,y)] ?: -1
            line += if (value == 1) "#" else "."
        }
        println(line)
    }
}

fun key(x:Int, y:Int):String {
    return (x * 10000 + y).toString()
}

class Place(val id:Int, val x:Int, val y:Int) {
    var text = "id:${id} x:$x y:$y"

    fun distanceToLocation(otherX:Int, otherY:Int ):Int {
        return abs( x - otherX) + abs(y - otherY)
    }

    companion object {
        private var lastId = -1
        fun create(fromPlaceCoordinate:String):Place {
            val split = fromPlaceCoordinate.split(", ")
            val x = split[0].toInt()
            val y = split[1].toInt()
            lastId += 1
            return Place(lastId, x, y)
        }
    }
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day6.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
