import java.io.File

fun main(args: Array<String>) {
    val listOfLetters = readFile()
    val listOfListOfLetters = listOfLetters.map{ string -> string.toCharArray().map{it.toString()}}

    val containsTwoOrThrees = listOfListOfLetters.map{getCount(it)}
    val twos = containsTwoOrThrees.fold(0) { acc, pair -> acc + pair.first}
    val threes = containsTwoOrThrees.fold(0) { acc, pair -> acc + pair.second}
    println("The answer is $twos twos X $threes threes =   ${twos * threes}")
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day2.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun getCount(letters:List<String>):Pair<Int, Int> {

    fun getSortedCount( count2:Int, count3:Int, letters:List<String>):Pair<Int, Int> {
        return if (letters.size < 2)  Pair(count2, count3)
        else
            if ((letters.size > 2 ) && (letters[0] == letters[1]) && (letters[1] == letters[2])) getSortedCount(  count2 , count3.or(1) ,letters.drop(3))
            else
                if ((letters.size > 1 ) && (letters[0] == letters[1]) ) getSortedCount(count2.or(1) , count3 ,letters.drop(2))
                else getSortedCount( count2, count3,letters.drop(1))
    }

    return getSortedCount(0,0,letters.sorted())
}

