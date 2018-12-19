import java.io.File

fun main(args: Array<String>) {
    val listOfLetters = readFile()

    val size = listOfLetters[0].length

    for (i in 0..(size - 1)) {
        val maskedList = listOfLetters.map{it.replaceCharAt(i)}
        val key  = getMatchingAdjacentStrings(maskedList.sorted())
        if (!key.isEmpty()) {
            println(key.replace("*",""))
            break
        }
    }
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day2.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun String.replaceCharAt(position:Int):String {
    return "${this.take(position)}*${this.drop(position + 1)}"
}

fun getMatchingAdjacentStrings(  letters:List<String>):String {
    return  if (letters.size < 2) ""
            else
                if ((letters.size > 1) && (letters[0] == letters[1])) letters[0]
                else getMatchingAdjacentStrings(letters.drop(1))
}


