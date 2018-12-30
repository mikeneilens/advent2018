fun main(args: Array<String>) {
    val firstNode = Node(3)
    var lastNode = firstNode.addNode(7)

    var elfOneNode  = firstNode
    var elfTwoNode = lastNode

    (0..25).forEach {
        val combinedScore = elfOneNode.value + elfTwoNode.value
        if (combinedScore < 10) {
            lastNode = lastNode.addNode(combinedScore)
        }
        else {
            lastNode = lastNode.addNode(combinedScore / 10)
            lastNode = lastNode.addNode(combinedScore % 10)
        }

        val elfOneSteps = 1 + elfOneNode.value
        val elfTwoSteps = 1 + elfTwoNode.value

        (1..elfOneSteps).forEach {
            elfOneNode = elfOneNode.nextCyclical() ?: Node(0)
        }
        (1..elfTwoSteps).forEach {
            elfTwoNode = elfTwoNode.nextCyclical() ?: Node(0)
        }
        println("$it " + firstNode.toString())
    }
    println(firstNode.subList(9,9 + (10 -1)))
    println(firstNode.subList(5,5 + (10 -1)))
    println(firstNode.subList(18,18 + (10 -1)))
    println(firstNode.subList(2018,2018 + (10 -1)))
    //println(firstNode.subList(580741,580741 + (10 -1)))
}
