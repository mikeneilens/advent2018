import java.io.File
import java.lang.Math.abs

val code = listOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",".")

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

    val placesThatAreFinite =  listOfPlaces.filter{place -> place.areaIsFinite(map,minX,maxX,minY,maxY)}
    val largestPlace= placesThatAreFinite.sortedBy { place -> map.filterValues { value -> value == place.id }.size  }.last()
    val sizeOfLargestPlace = map.filterValues { value ->  value == largestPlace.id }.size
    println("largestPlace is ${largestPlace.text} size is $sizeOfLargestPlace")
}

fun HashMap<String,Int>.addCoordinates(listOfPlaces:List<Place>,minX:Int, maxX:Int, minY:Int, maxY:Int) {
    (minY..maxY).forEach { y ->
        (minX..maxX).forEach { x ->
            val closestPlaces = listOfPlaces.sortedBy {it.distanceToLocation(x,y) }
            if (closestPlaces[0].distanceToLocation(x,y) != closestPlaces[1].distanceToLocation(x,y)) {
                this[key(x,y)] = closestPlaces[0].id
            } else {
                this[key(x,y)] = 52
            }
        }
    }
}
fun HashMap<String,Int>.print(minX:Int, maxX:Int, minY:Int, maxY:Int) {
    (minY..maxY).forEach { y ->
        var line = ""
        (minX..maxX).forEach { x ->
            val value =  this[key(x,y)] ?: -1
            line += if (value == -1) " " else code[value]
        }
        println(line)
    }
}

fun key(x:Int, y:Int):String {
    return (x * 10000 + y).toString()
}

class Place(val id:Int, val x:Int, val y:Int) {
    var text = "id:${code[id]} x:$x y:$y"

    fun distanceToLocation(otherX:Int, otherY:Int ):Int {
        return abs( x - otherX) + abs(y - otherY)
    }

    fun areaIsFinite(map:HashMap<String,Int>, minX:Int, maxX:Int, minY:Int, maxY:Int):Boolean {
        val westContainsOtherPlace =  (minX..x).fold(false){ acc, element ->
            if (map[key(element, y)] != id)
                acc.or(true)
            else
                acc.or(false)
        }
        val eastContainsOtherPlace =  (x..maxX).fold(false){ acc, element ->
            if (map[key(element, y)] != id)
                acc.or(true)
            else
                acc.or(false)
        }
        val northContainsOtherPlace =  (minY..y).fold(false){ acc, element ->
            if (map[key(x,element)] != id)
                acc.or(true )
            else
                acc.or(false)
        }
        val southContainsOtherPlace =  (y..maxY).fold(false){ acc, element ->
            if (map[key(x,element)] != id)
                acc.or(true )
            else
                acc.or(false)
        }
        return westContainsOtherPlace && eastContainsOtherPlace && northContainsOtherPlace && southContainsOtherPlace
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
