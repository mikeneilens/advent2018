import java.io.File

val mapLetters = mapOf(Pair("A",1),Pair("B",2),Pair("C",3),Pair("D",4),Pair("E",5),Pair("F",6),Pair("G",7),Pair("H",8),Pair("I",9),
    Pair("J",10),Pair("K",11),Pair("L",12),Pair("M",13),Pair("N",14),Pair("O",15),Pair("P",16),Pair("Q",17),Pair("R",18),
    Pair("S",19),Pair("T",20),Pair("U",21),Pair("V",22),Pair("W",23),Pair("X",24),Pair("Y",25),Pair("Z",26))

fun main(args: Array<String>) {
    val listOfInstructions = readFile()
    val listOfInstructionsSorted = listOfInstructions.sortedBy { it.substring(36..36) + it.substring(5..5) }
    val listOfSteps = createSteps(listOfInstructionsSorted, listOf())
    val listOfAllSteps = addStepsWithNoDependencies(listOfInstructionsSorted,listOfSteps)

    val worker1 = Worker(1,WorkStatus.Idle)
    val worker2 = Worker(2,WorkStatus.Idle)
    val worker3 = Worker(3,WorkStatus.Idle)
    val worker4 = Worker(4,WorkStatus.Idle)
    val worker5 = Worker(5,WorkStatus.Idle)

    val workers = listOf(worker1, worker2, worker3, worker4, worker5)

    var minutes = 0
    var completed = ""
    while (!listOfAllSteps.allStepsComplete()) {
        workers.assignStepToIdleWorkers(listOfAllSteps)

        workers.print(minutes, completed)

        workers.forEach { worker ->
            completed = worker.doWork(listOfAllSteps, completed)
        }
        minutes +=1
    }
    println("Minutes taken = $minutes, $completed ")
}

class Step(val id:String, var dependencies:List<String>, var timeLeft:Int = 60, var waitingToBeProcessed:Boolean = true ) {

    companion object {
        private const val minTime = 60
        fun create(id:String, dependencies:List<String>):Step {
            return Step(id, dependencies, minTime + (mapLetters[id] ?: 0),true)
        }
    }

    override fun toString(): String {
        return "$id dependencies:$dependencies dependencies:${dependencies.size} time left:$timeLeft"
    }

    fun removeDependency(id:String) {
        tailrec fun removeDependency(id:String, dependencies: List<String>, newDependencies:List<String>):List<String> {
            if (dependencies.isEmpty()) return newDependencies
            val head = dependencies.first()
            return if (head == id) {
                removeDependency(id, dependencies.drop(1), newDependencies)
            }  else {
                removeDependency(id, dependencies.drop(1), newDependencies + head)
            }
        }
        dependencies = removeDependency(id, this.dependencies, listOf())
    }
}
fun List<Step>.removeDependency(id:String){
    this.forEach { step ->
        step.removeDependency(id)
    }
}

fun List<Step>.findNextStep():Step? {
    this.sortedBy { it.id }.forEach {step ->
        if (step.dependencies.isEmpty() && step.timeLeft > 0 && step.waitingToBeProcessed) {
            return step
        }
    }
    return null
}
fun List<Step>.allStepsComplete():Boolean {
    return this.none { it.timeLeft > 0 }
}

class Worker(private val id:Int, var workStatus:WorkStatus) {

    fun doWork(listOfSteps:List<Step>, completed:String):String {

        return  if (workStatus is WorkStatus.Working) {
                    val step = (workStatus as WorkStatus.Working).step
                    step.timeLeft -= 1
                    if (step.timeLeft == 0) {
                        workStatus = WorkStatus.Idle
                        listOfSteps.removeDependency(step.id)
                        completed + step.id
                    } else {
                        completed
                    }
                } else {
                    completed
                }
    }

    override fun toString(): String = "Worker $id $workStatus"
}

sealed class WorkStatus {
    object Idle : WorkStatus()
    class Working(val step:Step):WorkStatus()

    override fun toString():String = when(this) {
        is WorkStatus.Idle ->  "Idle"
        is WorkStatus.Working ->  "${step.id}(${step.timeLeft})"
    }
}

fun List<Worker>.assignStepToIdleWorkers(listOfSteps:List<Step>) {
    this.filter { it.workStatus is WorkStatus.Idle }.forEach { idleWorker ->
        val nextStep = listOfSteps.findNextStep()
        if (nextStep != null) {
            nextStep.waitingToBeProcessed = false
            idleWorker.workStatus = WorkStatus.Working(nextStep)
        }
    }
}

fun List<Worker>.print(minute:Int, complete:String) {
    var line = "$minute "
    this.forEach { worker ->
        line += worker.toString()
        line += "   "
    }
    println("$line $complete")
}

fun createSteps(listOfInstructions:List<String>, listOfSteps:List<Step>):List<Step> {
    return if (listOfInstructions.isEmpty()) {
        listOfSteps
    } else {
        val id = listOfInstructions.first().substring(36..36)
        val pair = createDependencies(id,listOfInstructions, listOf())
        val remainingInstructions = pair.first
        val dependencies = pair.second
        val step = Step.create(id,dependencies)
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
        addStepsWithNoDependencies(listOfInstructions.drop(1),listOfSteps + Step.create(id,listOf()) )
    else
        addStepsWithNoDependencies(listOfInstructions.drop(1),listOfSteps)
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day7.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
