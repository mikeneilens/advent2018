import java.io.File

fun main(args: Array<String>) {
    val linesOfData = readFile()

    //need to put some empty pots before and after the line of pots
    var stringOfPots = "........." + linesOfData[0].split(": ")[1] + "........................................................................................................................................................................"
    val listOfRules = linesOfData.drop(2).map{ Rule.createFrom(it)}

    println("   ---------0--------1---------2---------3---------4---------5---------6---------7---------8---------9---------0---------1---------2")
    (1..20).forEach {
        stringOfPots = createANewGenerationOfPots(stringOfPots, listOfRules)
        if (it >= 10) println("$it $stringOfPots") else println(" $it $stringOfPots")
    }
    val total = calcTotalOfPots(stringOfPots)
    println("print total after 20 cyles = $total")

    (21..120).forEach {
        stringOfPots = createANewGenerationOfPots(stringOfPots, listOfRules)
        if (it >= 10) println("$it $stringOfPots") else println(" $it $stringOfPots")
    }
    val totalAfter120 = calcTotalOfPots(stringOfPots)
    println("print total after 120 cyles = $totalAfter120")

    println("Total for 50000000000 is total for 120 cycles + (50000000000 - 120) * 23 = ${totalAfter120 + (50000000000 - 120) * 23}")
}

fun createANewGenerationOfPots(stringOfPots:String, listOfRules: List<Rule>):String {
    var newGeneration = ""
    (0..stringOfPots.length - 1).forEach{ offset ->
        val pots =  when{
            (offset + 1 >= stringOfPots.length ) ->  stringOfPots.substring((offset -2)..offset) + ".."
            (offset + 2 >= stringOfPots.length )  -> stringOfPots.substring((offset -2)..(offset + 1)) + "."
            (offset >= 2) -> stringOfPots.substring((offset - 2)..(offset + 2))
            (offset >= 1) -> "." + stringOfPots.substring((offset - 1)..(offset + 2))
            else -> ".." + stringOfPots.substring(0..(offset + 2))
        }
        newGeneration += checkAllRules(listOfRules,pots).second
    }
    return newGeneration
}
fun calcTotalOfPots(stringOfPots: String):Int {
    var total = 0
    (-10..stringOfPots.length-11).forEach {
        total += if (stringOfPots.substring(it+10..it+10) == "#") it + 1 else 0
    }
    return total
}

fun checkAllRules(listOfRules:List<Rule>,pots:String):Pair<Boolean,String> {
    listOfRules.forEach { rule ->
        val resultOfRule = rule.applyTo(pots)
        if (resultOfRule.first) return resultOfRule
    }
    return Pair(false,".") //spec doesn't say, but not rules triggered, empty the pot
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day12.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

class Rule(private val pattern:String, private val result:String) {

    fun applyTo(pots:String):Pair<Boolean, String> {
        return if (pattern == pots) Pair(true, result) else Pair(false, pots.substring(2..2))
    }

    companion object {
        fun createFrom(lineOfData:String):Rule {
            return Rule(lineOfData.substringBefore(" => "),lineOfData.substringAfter(" => "))
        }
    }
}

