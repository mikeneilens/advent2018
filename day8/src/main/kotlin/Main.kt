import java.io.File

fun main(args: Array<String>) {
    val listOfData = readFile()[0].split(" ").map{it.toInt()}

    val pair = createNode(listOfData)
    val root = pair.first
    println("sum of nodes is ${root.sum()}") //44838
    println("sum of nodes 2 is ${root.sum2()}")

}

class Node(val noOfChildNodes:Int, val noOfMetaDataEntries:Int, val childNodes:List<Node>, val metaDataEntries:List<Int>) {
    fun sum():Int = metaDataEntries.fold(0){acc, element -> acc + element} + this.childNodes.fold(0){acc,element -> acc + element.sum()}

    fun sum2():Int = if (noOfChildNodes == 0) metaDataEntries.fold(0){ acc, element -> acc + element}
                    else {
                        metaDataEntries.fold(0){acc, metaDataEntry ->
                            if ((metaDataEntry == 0) || metaDataEntry > noOfChildNodes) acc
                            else acc + childNodes[metaDataEntry - 1].sum2()
                        }
                    }
}

fun createNode(listOfInts:List<Int>):Pair<Node,List<Int>> {
    val noOfChildNodes = listOfInts[0]
    val noOfMetaDataEntries = listOfInts[1]
    if (noOfChildNodes == 0) {
        val metaDataEntries = listOfInts.subList(2,(2 + noOfMetaDataEntries))
        val node = Node(noOfChildNodes,noOfMetaDataEntries, listOf(),metaDataEntries)
        val remainingList = listOfInts.drop(2 + noOfMetaDataEntries)
        return Pair(node,remainingList)
    }

    var remainingList = listOfInts.drop(2)
    var listOfNodes = listOf<Node>()
    (1..noOfChildNodes).forEach {
        val pair = createNode(remainingList)
        listOfNodes += pair.first
        remainingList = pair.second
    }
    val metaDataEntries = remainingList.subList(0,noOfMetaDataEntries)
    remainingList = remainingList.drop(noOfMetaDataEntries)
    return Pair(Node(noOfChildNodes,noOfMetaDataEntries, listOfNodes, metaDataEntries ),remainingList)

}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day8.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
