import java.io.File

fun main(args: Array<String>) {
    val listOfInstructions = readFile()
    val listOfInstructionsSorted = listOfInstructions.sortedBy { it.substring(36..36) + it.substring(5..5) }
    val listOfSteps = createSteps(listOfInstructionsSorted, listOf())
    val listOfAllSteps = addStepsWithNoDependencies(listOfInstructionsSorted,listOfSteps)

    val stepsTaken = processSteps(listOfAllSteps,"")
    println(stepsTaken)
}

class Step(val id:String, val dependencies:List<String>)

fun processSteps(listOfSteps:List<Step>, stepsTaken:String):String {
    if (listOfSteps.isEmpty()) {
        return stepsTaken
    }
    val listOfStepsSorted = listOfSteps.sortedBy { step -> step.dependencies.count().toString() + step.id }
    val idOfStep = listOfStepsSorted.first().id
    val remainingSteps = takeStep(idOfStep,listOfStepsSorted)
    return processSteps(remainingSteps, stepsTaken + idOfStep)
}

fun takeStep(id:String, listOfSteps:List<Step>):List<Step> {

    var newListOfSteps= listOf<Step>()
    listOfSteps.forEach{ step ->
        if (step.id != id) {
            var listOfDependencies = listOf<String>()
            step.dependencies.forEach{dependency ->
                if (dependency != id) {
                    listOfDependencies += dependency
                }
            }
            newListOfSteps += Step(step.id, listOfDependencies)
        }
    }
    return newListOfSteps
}

fun createSteps(listOfInstructions:List<String>, listOfSteps:List<Step>):List<Step> {
    return if (listOfInstructions.isEmpty()) {
        listOfSteps
    } else {
        val id = listOfInstructions.first().substring(36..36)
        val pair = createDependencies(id,listOfInstructions, listOf())
        val remainingInstructions = pair.first
        val dependencies = pair.second
        val step = Step(id,dependencies)
        createSteps(remainingInstructions,listOfSteps + step)
    }
}

fun createDependencies(id:String, listOfInstructions: List<String>,listOfDependencies:List<String>):Pair<List<String>,List<String>> {
    if (listOfInstructions.isEmpty()) return Pair(listOfInstructions,listOfDependencies)

    val first = listOfInstructions.first()
    val firstDependency = first.substring(5..5)
    val firstId = first.substring(36..36)
    return if (firstId != id) Pair(listOfInstructions,listOfDependencies)
    else createDependencies(id,listOfInstructions.drop(1), listOfDependencies + firstDependency)
}

fun addStepsWithNoDependencies(listOfInstructions: List<String>, listOfSteps: List<Step>):List<Step> {
    if (listOfInstructions.isEmpty()) return listOfSteps

    val id = listOfInstructions.first().substring(5..5)
    return if (listOfSteps.none { step -> (step.id == id) })
        addStepsWithNoDependencies(listOfInstructions.drop(1),listOfSteps + Step(id,listOf()) )
    else
        addStepsWithNoDependencies(listOfInstructions.drop(1),listOfSteps)
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day7.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
