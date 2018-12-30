import java.io.File
import kotlin.collections.HashMap

val intersectionChoice = listOf(InteresctionChoice.Left,InteresctionChoice.Straight, InteresctionChoice.Right)

fun main(args: Array<String>) {
    val mapLines = readFile()
    val mapAndVehicles = createTrackMap(mapLines)
    val trackMap = mapAndVehicles.first
    var listOfVehicles = mapAndVehicles.second

    val maxX = mapLines[0].length - 1
    val maxY = mapLines.size - 1

    printMap(trackMap,listOfVehicles,maxX,maxY)
    var noCollision = true

    while (noCollision) {
        listOfVehicles.sortedBy{vehicle -> vehicle.position.y * 10000 + vehicle.position.x }.forEach { vehicle ->
            vehicle.moveOn(trackMap)
            if (vehicle.hasCollided(listOfVehicles)) {
                noCollision = false
            }
        }
        printMap(trackMap,listOfVehicles,maxX,maxY)
    }
}

fun createTrackMap(mapLines:List<String>):Pair<HashMap<String,Track>,List<Vehicle> > {
    val maxY = mapLines.size - 1
    val trackMap = HashMap<String, Track>()
    var listOfVehicles = listOf<Vehicle>()

    (0..maxY).forEach { y ->
        val listOfMapSquares = mapLines[y].toCharArray().map{it.toString()}
        for (x in 0..listOfMapSquares.size -1) {
            val mapSquare = listOfMapSquares[x]
            val position = Vector(x,y)
            if (mapSquare.isTrack() || mapSquare.isVehicle()) trackMap[position.toString()] = mapSquare.toTrack()
            if (mapSquare.isVehicle()) listOfVehicles += Vehicle.createNew(position,mapSquare.toDirection().toTravel())
        }
    }
    return Pair(trackMap, listOfVehicles)
}


class Vehicle(var position:Vector, var travel:Vector, private val id:Int, private var nextIntersectionNdx:Int = 0) {
    fun image() = this.travel.toDirection().image

    override fun equals(other: Any?): Boolean {
        return (other is Vehicle) && (this.id == other.id)
    }

    fun moveOn(map:HashMap<String, Track>) {
        val newPosition = position + travel
        val track= map[newPosition.toString()]
        val newTravel = when (track) {
            Track.VerticalPath -> Vector(0,travel.y)
            Track.HorizontalPath -> Vector(travel.x, 0)
            Track.CurveNWSEPath -> moveOnNWSEPath()
            Track.CurveSWNEPath ->  moveOnSWNEPath()
            Track.Intersection ->  moveOnInteresection()
            else -> { travel
            }
        }
        position = newPosition
        travel = newTravel
    }
    fun moveOnNWSEPath():Vector = when (travel.toDirection()) {
            Direction.Left -> Direction.Up.toTravel()
            Direction.Right -> Direction.Down.toTravel()
            Direction.Up -> Direction.Left.toTravel()
            Direction.Down -> Direction.Right.toTravel()
    }
    fun moveOnSWNEPath():Vector = when (travel.toDirection()) {
        Direction.Left -> Direction.Down.toTravel()
        Direction.Right -> Direction.Up.toTravel()
        Direction.Up -> Direction.Right.toTravel()
        Direction.Down -> Direction.Left.toTravel()
    }
    fun moveOnInteresection():Vector {
        val newTravel = when (intersectionChoice[nextIntersectionNdx]) {
            InteresctionChoice.Straight -> travel
            InteresctionChoice.Left -> when (travel.toDirection()) {
                                            Direction.Left -> Direction.Down.toTravel()
                                            Direction.Right -> Direction.Up.toTravel()
                                            Direction.Up -> Direction.Left.toTravel()
                                            Direction.Down -> Direction.Right.toTravel()
                                        }
            InteresctionChoice.Right -> when (travel.toDirection()) {
                                            Direction.Left -> Direction.Up.toTravel()
                                            Direction.Right -> Direction.Down.toTravel()
                                            Direction.Up -> Direction.Right.toTravel()
                                            Direction.Down -> Direction.Left.toTravel()
                                        }
        }
        nextIntersectionNdx = if (nextIntersectionNdx < (intersectionChoice.size - 1) ) nextIntersectionNdx + 1 else 0
        return newTravel
    }
    fun hasCollided(listOfVehicles:List<Vehicle>):Boolean {
        listOfVehicles.forEach { otherVehicle ->
            if (otherVehicle != this) {
                if (otherVehicle.position == this.position) {
                    println("Collision at $position")
                    return true
                }
            }
        }
        return false
    }
    companion object {
        var nextVehicleId = 0
        fun createNew(position: Vector, travel: Vector):Vehicle {
            nextVehicleId += 1
            return Vehicle(position, travel, nextVehicleId)
        }
    }
}

class Vector(val x:Int, val y:Int) {
    infix operator fun plus(other:Vector):Vector {
        return Vector(x + other.x, y + other.y)
    }

    override fun equals(other: Any?): Boolean {
        return other is Vector && this.x == other.x  && this.y == other.y
    }
    override fun toString(): String {
        return "($x, $y)"
    }
}

enum class Track(val image:String){
    VerticalPath("|"),
    HorizontalPath("-"),
    CurveNWSEPath("\\"),
    CurveSWNEPath("/"),
    Intersection("+")
}
fun String.isTrack():Boolean {
    return (this == Track.VerticalPath.image) || (this == Track.HorizontalPath.image) || (this == Track.CurveSWNEPath.image) || (this == Track.CurveNWSEPath.image)|| (this == Track.Intersection.image)
}
fun String.isVehicle():Boolean {
    return (this == Direction.Right.image) || (this == Direction.Left.image) || (this == Direction.Up.image) || (this == Direction.Down.image)
}

enum class Direction(val image:String) {
    Right(">"),
    Left("<"),
    Up("^"),
    Down("v")
}
fun String.toDirection():Direction  = when(this) {
    Direction.Right.image -> Direction.Right
    Direction.Left.image -> Direction.Left
    Direction.Up.image -> Direction.Up
    Direction.Down.image -> Direction.Down
    else -> Direction.Right
}
fun String.toTrack():Track  = when(this) {
    Track.VerticalPath.image -> Track.VerticalPath
    Track.HorizontalPath.image -> Track.HorizontalPath
    Track.CurveSWNEPath.image -> Track.CurveSWNEPath
    Track.CurveNWSEPath.image -> Track.CurveNWSEPath
    Track.Intersection.image -> Track.Intersection
    Direction.Right.image -> Track.HorizontalPath
    Direction.Left.image -> Track.HorizontalPath
    Direction.Up.image -> Track.VerticalPath
    Direction.Down.image -> Track.VerticalPath
    else -> Track.HorizontalPath
}
enum class InteresctionChoice() {
    Right,
    Left,
    Straight
}
fun Direction.toTravel():Vector  = when(this) {
    Direction.Right -> Vector(1,0)
    Direction.Left -> Vector(-1,0)
    Direction.Down -> Vector(0,1)
    Direction.Up -> Vector(0,-1)
}
fun Vector.toDirection():Direction  {
    return when {
        (this.x > 0) -> Direction.Right
        (this.x < 0) -> Direction.Left
        (this.y > 0) -> Direction.Down
        (this.y < 0) -> Direction.Up
        else -> Direction.Right
    }
}

fun printMap(trackMap:HashMap<String, Track>, vehicles:List<Vehicle>, maxX:Int,maxY:Int) {
    val mapOfVehicles = vehicles.toMap()
    (0..maxY).forEach { y ->
        var line = ""
        (0..maxX).forEach{ x ->
            val positionKey = Vector(x, y).toString()
            val vehicleAtPosotion = mapOfVehicles[positionKey]
            val trackAtPosition = trackMap[positionKey]
            line += when {
                vehicleAtPosotion != null -> vehicleAtPosotion.image()
                trackAtPosition != null -> trackAtPosition.image
                else -> " "
            }
        }
        println(line)
    }
}
fun List<Vehicle>.toMap():HashMap<String, Vehicle>{
    val mapOfVehicles = HashMap<String, Vehicle>()
    this.forEach {vehicle ->
        mapOfVehicles[vehicle.position.toString()] = vehicle
    }
    return mapOfVehicles
}
fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day13.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

