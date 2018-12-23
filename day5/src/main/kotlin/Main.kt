import java.io.File

fun main(args: Array<String>) {
    val listOfLetters = readFile().first().toCharArray().map{it.toString()}
    val newListOfLetters = process(listOfLetters, listOf(),false)
    println("The size of the new list is ${newListOfLetters.size}")
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day5.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun shouldRemoveLetters(firstCharacter:String, secondCharacter:String):Boolean {
    return (firstCharacter != secondCharacter) && (firstCharacter.toLowerCase() == secondCharacter.toLowerCase())
}

tailrec fun process(listOfLetters:List<String>, newListOfLetters:List<String>, lettersRemoved:Boolean ):List<String>{
    return if (listOfLetters.size <= 1)  {
        if (!lettersRemoved) {
            newListOfLetters + listOfLetters
        } else {
            process(newListOfLetters + listOfLetters,listOf(),false)
        }
    } else {
        val firstLetter = listOfLetters[0]
        val secondLetter = listOfLetters[1]
        if (shouldRemoveLetters(firstLetter, secondLetter)) {
            process(listOfLetters.drop(2), newListOfLetters, lettersRemoved.or(true))
        } else {
            process(listOfLetters.drop(1), newListOfLetters + firstLetter, lettersRemoved.or(false))
        }
    }
}