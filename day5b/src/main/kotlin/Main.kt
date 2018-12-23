import java.io.File

val unitTypes = listOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")

fun main(args: Array<String>) {
    val listOfLetters = readFile().first().toCharArray().map{it.toString()}
    //val listOfLetters = "dabAcCaCBAcCcaDA".toCharArray().map{it.toString()}

    val results = unitTypes.map{
        val unitType = it
        val listOfLettersFiltered = listOfLetters.filter { (it.toLowerCase() != unitType) }
        process(listOfLettersFiltered, listOf(),false).size
    }
    println("The min size of the new list is ${results.min()}")
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day5.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun shouldRemoveLetters(firstCharacter:String, secondCharacter:String):Boolean {
    return (firstCharacter != secondCharacter) && (firstCharacter.toLowerCase() == secondCharacter.toLowerCase())
}

tailrec fun process(listOfLetters:List<String>, newListOfLetters:List<String>, lettersRemoved:Boolean):List<String>{
    if (listOfLetters.size <= 1)  {
        if (!lettersRemoved) {
            println( (newListOfLetters + listOfLetters).size)
            return newListOfLetters + listOfLetters
        } else {
            return process(newListOfLetters + listOfLetters,listOf(),false)
        }
    } else {
        val firstLetter = listOfLetters[0]
        val secondLetter = listOfLetters[1]
        if (shouldRemoveLetters(firstLetter, secondLetter)) {
            return process(listOfLetters.drop(2), newListOfLetters, lettersRemoved.or(true))
        } else {
            return process(listOfLetters.drop(1), newListOfLetters + firstLetter, lettersRemoved.or(false))
        }
    }
}