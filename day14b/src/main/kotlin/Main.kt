
fun main(args: Array<String>) {
    val firstNode = Node(3)
    var lastNode = firstNode.addNode(7)

    var elfOneNode  = firstNode
    var elfTwoNode = lastNode
    var listSize = 2

    val valueToFind = "580741"

    (0..100000000).forEach {
        val combinedScore = elfOneNode.value + elfTwoNode.value
        if (combinedScore < 10) {
            lastNode = lastNode.addNode(combinedScore)
            listSize += 1
        }
        else {
            lastNode = lastNode.addNode(combinedScore / 10)
            lastNode = lastNode.addNode(combinedScore % 10)
            listSize += 2
        }

        if (containsSequence(lastNode, valueToFind)) {
            println("value $valueToFind found after ${listSize - valueToFind.length } recipes. $it")
        }
        val elfOneSteps = 1 + elfOneNode.value
        val elfTwoSteps = 1 + elfTwoNode.value

        (1..elfOneSteps).forEach {
            elfOneNode = elfOneNode.nextCyclical() ?: Node(0)
        }
        (1..elfTwoSteps).forEach {
            elfTwoNode = elfTwoNode.nextCyclical() ?: Node(0)
        }
    }
    println(firstNode.subList(9,9 + (10 -1)))
    println(firstNode.subList(5,5 + (10 -1)))
    println(firstNode.subList(18,18 + (10 -1)))
    println(firstNode.subList(2018,2018 + (10 -1)))
    //println(firstNode.subList(580741,580741 + (10 -1)))
}

fun containsSequence(lastNode:Node<Int>,sequence:String):Boolean {
    val chars = sequence.toString().toCharArray().map{it.toString()}
    var nodeToCheck = lastNode
    (0..chars.size - 1 ).forEach {
        val ndx = chars.size - 1  - it
        if (nodeToCheck.value.toString() != chars[ndx]) return false
        if (nodeToCheck.previous == null && it > 0 ) return false
        nodeToCheck = nodeToCheck.previous!!
    }
    return true
}
