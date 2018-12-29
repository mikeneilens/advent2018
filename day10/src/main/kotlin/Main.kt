import java.io.File

fun main(args: Array<String>) {
    val listOfData = readFile()

    var listOfPoints = listOfData.map { line -> Point.createFrom(line) }

    (1..50000).forEach { seconds ->
        listOfPoints = listOfPoints.map{point -> point.move()}
        drawList(listOfPoints, seconds)
    }
}

class Point(val position:Position, private val velocity:Velocity) {
    fun move():Point{
        val newX = position.x + velocity.x
        val newY = position.y + velocity.y
        return Point(Position(newX, newY), velocity)
    }
    companion object {
        fun createFrom(line:String):Point {
            val firstSplit = line.split("<")
            val positionString = firstSplit[1].split(">")[0]
            val positionX = positionString.split(", ")[0].removePrefix(" ").toInt()
            val positionY = positionString.split(", ")[1].removePrefix(" ").toInt()
            val velocityString = firstSplit[2].split(">")[0]
            val velocityX = velocityString.split(", ")[0].removePrefix(" ").toInt()
            val velocityY = velocityString.split(", ")[1].removePrefix(" ").toInt()
            return Point(Position(positionX,positionY),Velocity(velocityX,velocityY))
        }

    }
}

class Position(val x:Int, val y:Int)
class Velocity(val x:Int, val y:Int)

fun drawList(list:List<Point>, seconds:Int) {
    if (listContainClusteredPoints(list)) {
        val sortedByXList = list.sortedBy { point -> point.position.x }
        val sortedByYList = list.sortedBy { point -> point.position.y }
        val minX = sortedByXList.first().position.x
        val maxX = sortedByXList.last().position.x
        val minY = sortedByYList.first().position.y
        val maxY = sortedByYList.last().position.y
        println("Image at $seconds seconds")
        (minY..maxY).forEach { y ->
            var output = ""
            (minX..maxX).forEach { x ->
                output += if (list.none { point -> point.position.x == x && point.position.y == y }) "." else "#"
            }
            println(output)
        }
    }
}

fun listContainClusteredPoints(list:List<Point>):Boolean {
    var mostPointsFound = 0
    list.forEach{point ->
        var pointsFound = 0
        ((point.position.y - 10)..(point.position.y + 10)).forEach { y ->
            ((point.position.x - 10)..(point.position.x + 10)).forEach { x ->
                if (!list.none{point-> point.position.x == x && point.position.y == y }) {
                    pointsFound += 1
                }
            }
        }
        if (pointsFound > mostPointsFound) mostPointsFound = pointsFound
        if (pointsFound >= 10) return true
    }
    return false
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day10.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}