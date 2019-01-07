import java.io.File

fun main(args: Array<String>) {
    val data=readFile("/Users/michaelneilens/day20.txt")[0].replace("$","").replace("^","").toCharArray().map{it.toString()}

    val firstDataItem = convertTextToLinkedList(data,null,null)

    val finalNodes = mutableListOf<Node>()
    val mapOfBase = hashMapOf<Vector, String>()
    val mapOfDistance:HashMap<Vector, Int> = hashMapOf<Vector, Int>()
    mapOfBase[Vector(0,0)] = "X"

    processData(firstDataItem, Node(null,0,Vector(0,0)), finalNodes, mapOfBase, mapOfDistance)

    val locationAtEndOfLongsetRoute = finalNodes.sortedBy { it.noOfParents }.last().position
    val routesGoingToLocation = finalNodes.filter{it.position == locationAtEndOfLongsetRoute}
    val shortestRouteToLocation = routesGoingToLocation.sortedBy { it.noOfParents }.first()

    println("Shortest no of doors to location on longest routes ${shortestRouteToLocation.noOfParents}. Location is ${shortestRouteToLocation.position}")

    val noOfRoomsRequiring1000orMoreDoors = mapOfDistance.filterValues { it >= 1000 }.size
    println("Rooms requireing at least 1000 doors to reach = $noOfRoomsRequiring1000orMoreDoors")

    val output = mapOfBase.print("#")
    val testPassed = checkOutput(output,"/Users/michaelneilens/day20-test2-result.txt")

    println("finished. Result is: $testPassed ")
}

fun processData(initialData:DataItem?, initialNode:Node, finalNodes:MutableList<Node>, mapOfBase:HashMap<Vector, String>, mapOfDistance:HashMap<Vector, Int>) {

    var data = initialData
    var node = initialNode

    while (data != null) {
        val head = data.value.toRoute()
        val tail = data.getNext()

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

                val currentShortestDistance = mapOfDistance[newPosition] ?: 99999
                if (node.noOfParents < currentShortestDistance) mapOfDistance[newPosition] = node.noOfParents
            }
            Route.Branch -> {
                val branches = getBranches(data)
                branches.forEach{dataSet ->
                    if (dataSet != null) {
                        processData(dataSet, node, finalNodes, mapOfBase, mapOfDistance)
                    }
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
    val output= mutableListOf<String>()

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

class Node(val parent:Node?, val noOfParents:Int,val  position:Vector) {

}

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

fun getBranches(data:DataItem):List<DataItem?> {
    val dataBetweenBranchesAndAfter = getDataBetweenBracesAndAfter(data)
    val dataBetweenBraces = dataBetweenBranchesAndAfter.first
    val remainingData = dataBetweenBranchesAndAfter.second

    val blocks = getBlocks(dataBetweenBraces, listOf(), null,null,0)

    /* This is annoying as the  problem implies you need to look at every branch but any branches with a |) which you don't have to check to solve the problem. */
     if (blocks.last()==null) return listOf(remainingData) // or blocks + remainingData for part 2
     else return blocks

    //val blocksWithRemainingDataAppended = appendBranchesToRemainingData(blocks,remainingData)
    //return blocksWithRemainingDataAppended
}

//This is only useful if you want to draw an accurate map. Otherwise it creates loads more pointless routes to explore.
fun appendBranchesToRemainingData(branches:List<DataItem?>, remainingData:DataItem?):List<DataItem?> {
    if (remainingData == null) {
        return branches
    } else {
        val blocksWithRemainingDataAppended = branches.map{ branch ->
            if (branch == null) remainingData
            else {
                branch.getLast()?.append( remainingData)
                branch
            }
        }
        return blocksWithRemainingDataAppended
    }
}


fun getDataBetweenBracesAndAfter(firstDataItem:DataItem):Pair<DataItem?,DataItem?> {
    var bracesOpen = 1
    var dataItem = firstDataItem.getNext()

    var newFirstDataItem:DataItem? = null
    var lastDataItem = newFirstDataItem

    while ( dataItem != null && bracesOpen > 0) {

        val head = dataItem.value
        if (head == "(") bracesOpen += 1
        if (head == ")") bracesOpen -= 1

        if (bracesOpen > 0) {

            if (newFirstDataItem == null) {
                newFirstDataItem = DataItem.create(head)
                lastDataItem = newFirstDataItem
            } else {
                val newDataItem = lastDataItem?.add(head)
                lastDataItem = newDataItem

            }
            dataItem = dataItem.getNext()
        }
    }
    val dataBetweenBraces = newFirstDataItem
    val dataAfterBraces = dataItem?.getNext()
    return Pair(dataBetweenBraces,dataAfterBraces  )
}

tailrec fun getBlocks(data:DataItem?, blocks:List<DataItem?>, currentBlockFirst:DataItem?,currentBlockLast:DataItem?, bracesOpen:Int):List<DataItem?> {
    if (data == null) return  blocks.plusElement(currentBlockFirst)

    val head = data.value
    val tail = data.getNext()

    if (head == "|" && bracesOpen == 0) {
        val newBlocks = blocks.plusElement(currentBlockFirst)
        return getBlocks(tail, newBlocks, null, null, bracesOpen)
    }

    val newOpenBrace = if (head == "(") 1 else 0
    val newCloseBrace = if (head == ")") -1 else 0

    if (currentBlockFirst == null) {
        val newDataItem = DataItem.create(head)
        return getBlocks(tail, blocks, newDataItem, newDataItem, bracesOpen + newOpenBrace + newCloseBrace)
    } else {
        val newDataItem = currentBlockLast?.add(head) //DaataItem(head, null, null, null)
        return getBlocks(tail, blocks, currentBlockFirst, newDataItem, bracesOpen + newOpenBrace + newCloseBrace)
    }

}


class DataItem(val value:String, private var child:DataItem?, var root:DataItem?, var final:DataItem?  ) {

    fun getNext():DataItem? {
        return child
    }

    fun getLast():DataItem? {
        return root?.final
    }
    fun add(newValue:String):DataItem {
        val dataItem =   DataItem(newValue, null, this.root,null)
        this.child = dataItem
        this.root?.final = dataItem
        return dataItem
    }
    fun append(dataItem: DataItem?) {
        tailrec fun append(dataItem:DataItem, otherDataItem:DataItem?) {
            if (otherDataItem == null) return
            else {
                val newDataItem = dataItem.add(otherDataItem.value)
                return append(newDataItem, otherDataItem.child)
            }
         }
         append(this, dataItem)
    }
    override fun toString(): String {
        tailrec fun addString(dataItem:DataItem?, x:String):String {
            if (dataItem == null) return x
            else return addString(dataItem.child, x + dataItem.value)
        }
        return addString(this,"")
    }
    companion object {
        fun create(value:String):DataItem {
            val newDataItem =  DataItem(value,null,null,null)
            newDataItem.root = newDataItem
            newDataItem.root?.final = newDataItem
            return newDataItem
        }
    }
}

tailrec fun convertTextToLinkedList(data:List<String>, dataItem:DataItem?, firstDataItem:DataItem?):DataItem? {
    if (data.isEmpty()) return firstDataItem
    val head = data.first()
    val tail = data.drop(1)

    if (dataItem != null) {
        val newDataItem =  dataItem.add(head)
        return convertTextToLinkedList(tail,newDataItem,firstDataItem)
    }  else {
        val newDataItem = DataItem.create(head)
        return convertTextToLinkedList(tail,newDataItem,newDataItem)
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