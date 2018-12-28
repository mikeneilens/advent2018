fun main(args: Array<String>) {

    val maxPlayers = 424
    val maxMarble = 71144
    var list = listOf(0)
    var ndx = 0
    var player = 1
    var playerScores = MutableList<Int>(maxPlayers + 1){0}
    (1..maxMarble).forEach{marble ->
        if (marble % 23 != 0) {
            ndx = ndxClockwiseOf(ndx,list)
            ndx = ndxClockwiseOf(ndx,list)
            list = addMarbleTo(list,ndx,marble, listOf())
        } else {
            ndx = ndxCounterClockwiseOf(ndx,list)
            ndx = ndxCounterClockwiseOf(ndx,list)
            ndx = ndxCounterClockwiseOf(ndx,list)
            ndx = ndxCounterClockwiseOf(ndx,list)
            ndx = ndxCounterClockwiseOf(ndx,list)
            ndx = ndxCounterClockwiseOf(ndx,list)
            val marbleRemoved = takeMarbleFrom(list,ndx,-1, listOf())
            list = marbleRemoved.second
            playerScores[player] += marble + marbleRemoved.first
            ndx = ndxCounterClockwiseOf(ndx,list)
        }
        player = nextPlayer(player,maxPlayers)
 //       println("[$player] $list")
    }
    val maxScoreAndIndex = maxValueAndInt(playerScores,0,0,0)
    println("Winner is player ${maxScoreAndIndex.second}, winning score is ${maxScoreAndIndex.first} ")
}

fun ndxClockwiseOf(ndx:Int, list:List<Int>):Int {
    return if ((ndx + 1)  > list.size -1) 0 else (ndx + 1)
}

fun ndxCounterClockwiseOf(ndx:Int, list:List<Int>):Int {
    return if ((ndx - 1)  < 0) (list.size - 1) else (ndx - 1)
}

fun nextPlayer(player:Int, maxPlayers:Int):Int {
    return if (player == maxPlayers) 1 else player + 1
}

tailrec fun addMarbleTo(list:List<Int>,ndxToAddAfter:Int,marble:Int, newList:List<Int>):List<Int> {
    return  if (list.isEmpty()) newList
            else
                if ((newList.size) == ndxToAddAfter)
                    addMarbleTo(list.drop(1), ndxToAddAfter,marble,newList + list.first() + marble)
                else
                    addMarbleTo(list.drop(1), ndxToAddAfter,marble,newList + list.first())
}
tailrec fun takeMarbleFrom(list:List<Int>,ndx:Int,marble:Int, newList:List<Int>):Pair<Int, List<Int>> {
    return  if (list.isEmpty()) Pair(marble,newList)
            else
                if (((newList.size) == ndx) && (marble < 0))
                    takeMarbleFrom(list.drop(1), ndx,list.first(),newList  )
                else
                    takeMarbleFrom(list.drop(1), ndx,marble,newList + list.first())
}

tailrec fun maxValueAndInt(list:List<Int>, maxValue:Int, ndx:Int, maxNdx:Int):Pair<Int,Int> {
    return  if (list.isEmpty()) Pair(maxValue,maxNdx)
            else
                if (list.first() > maxValue) maxValueAndInt(list.drop(1),list.first(),ndx + 1, ndx)
                else maxValueAndInt(list.drop(1),maxValue,ndx + 1, maxNdx)

}