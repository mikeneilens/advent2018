import java.io.File

fun main(args: Array<String>) {
    val numbers = readFile().map{it.toInt()}
    val total = numbers.fold(0,{total,element -> processor(total , element) })
    println("The total is $total")

    var listOfFrequencies = mutableListOf<Int>()
    var frequency = 0

    var mutableNumbers = numbers
    while (!listOfFrequencies.contains(frequency)) {
        listOfFrequencies.add(frequency)
        val head = mutableNumbers.first()
        frequency += head
        mutableNumbers = mutableNumbers.drop(1) + head
    }
    println("frequency is $frequency")

    //this runs out of stack!!!
    val frequency2 = getRepeatingFrequency(numbers, 0, listOf())
    println("frequency with recursion is $frequency2")
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day1.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun processor(total:Int, element:Int):Int {
    return element + total
}

fun getRepeatingFrequency(listOfInts:List<Int>, frequency:Int, listOfFrequencies:List<Int>   ):Int {
    val element = listOfInts.first()
    val remainder = listOfInts.drop(1) + element
    val newFrequency = frequency + element
    if (listOfFrequencies.contains(newFrequency)) return newFrequency
    return getRepeatingFrequency( remainder, newFrequency, listOfFrequencies + newFrequency)
}
