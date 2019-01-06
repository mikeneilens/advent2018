import java.io.File
import kotlin.math.abs

fun main(args: Array<String>) {
    val data=readFile("/Users/michaelneilens/day20.txt")[0].replace("$","").replace("^","").toCharArray().map{it.toString()}

    val finalNodes = mutableListOf<Node>()
    val mapOfBase = hashMapOf<Vector, String>()
    mapOfBase[Vector(0,0)] = "X"
    processData(data, Node(null,0,Vector(0,0)), finalNodes, mapOfBase)

    val locationAtEndOfLongsetRoute = finalNodes.sortedBy { it.noOfParents }.last().position
    val routesGoingToLocation = finalNodes.filter{it.position == locationAtEndOfLongsetRoute}
    val shortestRouteToLocation = routesGoingToLocation.sortedBy { it.noOfParents }.first()

    println("Shortest no of doors to location on longest routes ${shortestRouteToLocation.noOfParents}. Location is ${shortestRouteToLocation.position}")

    val output = mapOfBase.print("#")
    val testPassed = checkOutput(output,"/Users/michaelneilens/day20-test2-result.txt")

    println("finished. Result is: $testPassed ")
}

fun processData(initialData:List<String>, initialNode:Node, finalNodes:MutableList<Node>, mapOfBase:HashMap<Vector, String>) {


    var data = initialData
    var node = initialNode

    while (data.isNotEmpty()) {
        if (data.size % 1000 == 0) println(data.size)
        val head = data.first().toRoute()
        val tail = data.drop(1)

        when (head) {
            Route.North, Route.South, Route.East, Route.West -> {
                val doorPosition = node.position + head.move
                val newPosition = doorPosition + head.move
                mapOfBase[doorPosition ] = head.image
                mapOfBase[doorPosition + head.wall1] = "#"
                mapOfBase[doorPosition + head.wall2] = "#"
                mapOfBase[newPosition] = "."
                node = Node(node, node.noOfParents + 1, newPosition)
                data = tail
            }
            Route.Branch -> {
                val branches = getBranches(data)
                branches.forEach{dataSet ->
                    processData(dataSet, node, finalNodes, mapOfBase)
                }
                return
            }
        }
    }

    finalNodes.add(node)
    return
}

fun HashMap<Vector,String>.print(unkownSquare:String):List<String> {
    val minX = this.keys.sortedBy { it.x }.first().x
    val maxX = this.keys.sortedBy { it.x }.last().x
    val minY = this.keys.sortedBy { it.y }.first().y
    val maxY = this.keys.sortedBy { it.y }.last().y
    var output= mutableListOf<String>()

    (minY..maxY).forEach{y ->
        var line = ""
        (minX..maxX).forEach {x ->
            line += this[Vector(x,y)] ?: unkownSquare
        }
        println(line)
        output.add(line)
    }
    return output
}

class Node(val parent:Node?, val noOfParents:Int,val  position:Vector)

class Vector (val x:Int, val y:Int) {
    override fun toString(): String {
        return "($x, $y)"
    }
    override fun equals(other: Any?): Boolean {
        return other is Vector && other.x == x && other.y == y
    }
    infix operator fun plus(other:Vector):Vector {
        return Vector(x + other.x, y + other.y)
    }
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

enum class Route(val image:String, val move:Vector, val wall1:Vector, val wall2:Vector) {
    North("-",Vector(0,-1),Vector(1,0),Vector(-1,0)),
    South("-",Vector(0, +1),Vector(1,0),Vector(-1,0)),
    East("|",Vector(+1, 0),Vector(0,1),Vector(0,-1)),
    West("|", Vector(-1, 0),Vector(0,1),Vector(0,-1)),
    Branch("(",Vector(0,0),Vector(0,0),Vector(0,0))
}

fun String.toRoute():Route = when(this) {
    "N" -> Route.North
    "S" -> Route.South
    "E" -> Route.East
    "W" -> Route.West
    "(" -> Route.Branch
    else -> Route.North
}

fun getBranches(data:List<String>):List<List<String>> {
    val dataBetweenBranchesAndAfter = getDataBetweenBracesAndAfter(data)
    val dataBetweenBraces = dataBetweenBranchesAndAfter.first
    val remainingData = dataBetweenBranchesAndAfter.second

    val blocks = getBlocks(dataBetweenBraces, listOf(), listOf(),0)
    val blocksWithRemainingDataAppended = blocks.map{it + remainingData}

    return blocksWithRemainingDataAppended
}

fun getDataBetweenBracesAndAfter(data:List<String>):Pair<List<String>,List<String>> {
    val start = 1
    var end = 1
    var ndx = 1
    var bracesOpen = 1
    while (ndx < data.size && bracesOpen > 0) {
        if (data[ndx] == "(") bracesOpen += 1
        if (data[ndx] == ")") bracesOpen -= 1
        if (bracesOpen == 0)  end = ndx
        ndx +=1
    }
    val dataBetweenBraces = data.subList(start, end)
    val dataAfterBraces = data.subList(end + 1, data.size)
    return Pair(dataBetweenBraces,dataAfterBraces)
}

tailrec fun getBlocks(data:List<String>, blocks:List<List<String>>, currentBlock:List<String>, bracesOpen:Int):List<List<String>> {
    if (data.isEmpty()) return  blocks.plusElement(currentBlock)
    val head = data.first()
    val tail = data.drop(1)

    when (head) {
        "|" -> {
            if (bracesOpen == 0 ) {
                val newBlocks = blocks.plusElement(currentBlock)
                return getBlocks(tail, newBlocks, listOf(),bracesOpen)
            } else {
                return getBlocks(tail, blocks, currentBlock + head, bracesOpen)
            }
        }
        "(" ->  return getBlocks(tail, blocks, currentBlock + head, bracesOpen + 1)
        ")" ->  return getBlocks(tail, blocks, currentBlock + head, bracesOpen - 1)
        else -> return getBlocks(tail, blocks, currentBlock + head, bracesOpen)
    }
}


fun readFile(fileName:String):List<String> {
    val lineList = mutableListOf<String>()
    File(fileName).useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
fun checkOutput(output:List<String>, testFile:String):Boolean {
    val lineList = mutableListOf<String>()
    File(testFile).useLines { lines -> lines.forEach { lineList.add(it) }}

    lineList.forEachIndexed{ndx, line ->
        if (line != output[ndx]) {
            return false
        }
    }
    return true
}