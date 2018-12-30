import java.io.File
import kotlin.collections.HashMap

val intersectionChoice = listOf(InteresctionChoice.Left,InteresctionChoice.Straight, InteresctionChoice.Right)

fun main(args: Array<String>) {
    val mapLines = readFile()
    val mapAndVehicles = createTrackMap(mapLines)

    val trackMap = mapAndVehicles.first
    val listOfVehicles = mapAndVehicles.second

    val maxX = mapLines[0].length - 1
    val maxY = mapLines.size - 1

    printMap(trackMap,listOfVehicles,maxX,maxY)

    moveCarsUntilCrash(trackMap, listOfVehicles)

    printMap(trackMap,listOfVehicles,maxX,maxY)

    while (listOfVehicles.filter{vehicle -> vehicle.isAlive}.size > 1) {
        moveCarsUntilCrash(trackMap, listOfVehicles)
    }
    val lastVehicleAlive = listOfVehicles.filter{vehicle -> vehicle.isAlive}
    println("Last vehicle alive is at ${lastVehicleAlive[0].position}")
}

fun moveCarsUntilCrash(trackMap:HashMap<String,Track>, listOfVehicles: List<Vehicle>):Vector {
    var noCollision = true
    var positionOfCollision = Vector(0,0)
    while (noCollision) {
        listOfVehicles.sortedBy{vehicle -> vehicle.position.y * 10000 + vehicle.position.x }.forEach { vehicle ->
            vehicle.moveOn(trackMap)
            if (vehicle.hasCollided(listOfVehicles)) {
                noCollision = false
                positionOfCollision = vehicle.position
            }
        }
    }
    return positionOfCollision
}

fun createTrackMap(mapLines:List<String>):Pair<HashMap<String,Track>,List<Vehicle> > {
    val trackMap = HashMap<String, Track>()
    var listOfVehicles = listOf<Vehicle>()

    (0..(mapLines.size - 1)).forEach { y ->
        val listOfMapSquares = mapLines[y].toCharArray().map{it.toString()}
        for (x in 0..listOfMapSquares.size - 1) {
            val mapSquare = listOfMapSquares[x]
            val position = Vector(x,y)
            if (mapSquare.isTrack() || mapSquare.isVehicle()) trackMap[position.toString()] = mapSquare.toTrack()
            if (mapSquare.isVehicle()) listOfVehicles += Vehicle.createNew(position,mapSquare.toDirection().toVector())
        }
    }
    return Pair(trackMap, listOfVehicles)
}


class Vehicle(var position:Vector, private var travel:Vector, private val id:Int, private var nextIntersectionNdx:Int = 0, var isAlive:Boolean=true) {
    fun image() = this.travel.toDirection().image

    override fun equals(other: Any?): Boolean {
        return (other is Vehicle) && (this.id == other.id)
    }

    fun moveOn(map:HashMap<String, Track>) {
        if (!isAlive) return

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
    private fun moveOnNWSEPath():Vector = when (travel.toDirection()) {
            Direction.Left -> Direction.Up.toVector()
            Direction.Right -> Direction.Down.toVector()
            Direction.Up -> Direction.Left.toVector()
            Direction.Down -> Direction.Right.toVector()
    }
    private fun moveOnSWNEPath():Vector = when (travel.toDirection()) {
        Direction.Left -> Direction.Down.toVector()
        Direction.Right -> Direction.Up.toVector()
        Direction.Up -> Direction.Right.toVector()
        Direction.Down -> Direction.Left.toVector()
    }
    private fun moveOnInteresection():Vector {
        val newTravel = when (intersectionChoice[nextIntersectionNdx]) {
            InteresctionChoice.Straight -> travel
            InteresctionChoice.Left -> when (travel.toDirection()) {
                                            Direction.Left -> Direction.Down.toVector()
                                            Direction.Right -> Direction.Up.toVector()
                                            Direction.Up -> Direction.Left.toVector()
                                            Direction.Down -> Direction.Right.toVector()
                                        }
            InteresctionChoice.Right -> when (travel.toDirection()) {
                                            Direction.Left -> Direction.Up.toVector()
                                            Direction.Right -> Direction.Down.toVector()
                                            Direction.Up -> Direction.Right.toVector()
                                            Direction.Down -> Direction.Left.toVector()
                                        }
        }
        nextIntersectionNdx = if (nextIntersectionNdx < (intersectionChoice.size - 1) ) nextIntersectionNdx + 1 else 0
        return newTravel
    }
    fun hasCollided(listOfVehicles:List<Vehicle>):Boolean {
        if (!isAlive) return false

        listOfVehicles.forEach { otherVehicle ->
            if (otherVehicle != this && otherVehicle.isAlive) {
                if (otherVehicle.position == this.position) {
                    println("Collision at $position")
                    otherVehicle.isAlive = false
                    this.isAlive = false
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private var nextVehicleId = 0
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

enum class Direction(val image:String) {
    Right(">"),
    Left("<"),
    Up("^"),
    Down("v")
}
fun String.isVehicle():Boolean {
    return (this == Direction.Right.image) || (this == Direction.Left.image) || (this == Direction.Up.image) || (this == Direction.Down.image)
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
enum class InteresctionChoice {
    Right,
    Left,
    Straight
}
fun Direction.toVector():Vector  = when(this) {
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
        println(line + y.toString())
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

