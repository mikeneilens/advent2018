fun main(args: Array<String>) {

    val maxPlayers = 424
    val maxMarble = 7114400
//    val maxPlayers = 9
//    val maxMarble = 25
//    val maxPlayers = 13
//    val maxMarble = 7999

    var node:Node<Int>? = Node(0)
    var player = 1
    val playerScores= MutableList<Long>(maxPlayers + 1){ 0 }
    (1..maxMarble).forEach { marble ->
        if (marble % 23 != 0) {
            val oneAfterCurrentNode = node?.nextCyclical()
            node = oneAfterCurrentNode?.addNode(marble)
        } else {
            val sevenBeforeCurrentNode = node?.previousCyclical()?.previousCyclical()?.previousCyclical()?.previousCyclical()?.previousCyclical()?.previousCyclical()?.previousCyclical()?.previousCyclical()
            val marbleBeingRemoved = sevenBeforeCurrentNode?.nextCyclical()
            playerScores[player] += marble.toLong() + ( marbleBeingRemoved?.value ?:0).toLong()
            sevenBeforeCurrentNode?.removeNextNode()
            node = sevenBeforeCurrentNode?.nextCyclical()
        }
        player = nextPlayer(player,maxPlayers)
    }
    //println(node?.first())

    val maxScoreAndIndex = maxValueAndInt(playerScores,0,0,0)
    println("Winner is player ${maxScoreAndIndex.second}, winning score is ${maxScoreAndIndex.first} ")

}

fun nextPlayer(player:Int, maxPlayers:Int):Int {
    return if (player == maxPlayers) 1 else player + 1
}
tailrec fun maxValueAndInt(list:List<Long>, maxValue:Long, ndx:Int, maxNdx:Int):Pair<Long,Int> {
    return when {
        list.isEmpty() -> Pair(maxValue,maxNdx)
        list.first() > maxValue -> maxValueAndInt(list.drop(1),list.first(),ndx + 1, ndx)
        else -> maxValueAndInt(list.drop(1),maxValue,ndx + 1, maxNdx)
    }
}
