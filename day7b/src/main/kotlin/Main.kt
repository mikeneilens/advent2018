import java.io.File

val mapLetters = mapOf(Pair("A",1),Pair("B",2),Pair("C",3),Pair("D",4),Pair("E",5),Pair("F",6),Pair("G",7),Pair("H",8),Pair("I",9),
    Pair("J",10),Pair("K",11),Pair("L",12),Pair("M",13),Pair("N",14),Pair("O",15),Pair("P",16),Pair("Q",17),Pair("R",18),
    Pair("S",19),Pair("T",20),Pair("U",21),Pair("V",22),Pair("W",23),Pair("X",24),Pair("Y",25),Pair("Y",26))

fun main(args: Array<String>) {
    val listOfInstructions = readFile()
    val listOfInstructionsSorted = listOfInstructions.sortedBy { it.substring(36..36) + it.substring(5..5) }
    val listOfSteps = createSteps(listOfInstructionsSorted, listOf())
    val listOfAllSteps = addStepsWithNoDependencies(listOfInstructionsSorted,listOfSteps)

    val worker1 = Worker(1,null)
    val worker2 = Worker(2,null)
    val worker3 = Worker(3,null)
    val worker4 = Worker(4,null)
    val worker5 = Worker(5,null)

    val workers = listOf(worker1, worker2, worker3, worker4, worker5)

    var minutes = 0
    var completed = ""
    while (!listOfAllSteps.allStepsComplete()) {
        workers.assignStepToIdleWorkers(listOfAllSteps)

        workers.print(minutes)

        workers.forEach { worker ->
            worker.doWork(listOfAllSteps)
        }
        minutes +=1
    }
    println("Minutes taken = $minutes")
}

class Step(val id:String, var dependencies:List<String>, var timeleft:Int = 60, var worker:Int ) {

    companion object {
        val minTime = 60
        fun create(id:String, dependencies:List<String>):Step {
            return Step(id, dependencies, minTime + (mapLetters[id] ?: 0),0)
        }
    }

    override fun toString(): String {
        return "$id dependencies:$dependencies timeleft:$timeleft"
    }

    fun removeDependency(id:String) {
        fun removeDependency(id:String, dependencies: List<String>, newDependencies:List<String>):List<String> {
            if (dependencies.isEmpty()) return newDependencies
            val head = dependencies.first()
            val tail = dependencies.drop(1)
            if (head == id) {
                return removeDependency(id, tail, newDependencies)
            }  else {
                return removeDependency(id, tail, newDependencies + head)
            }
        }
        dependencies = removeDependency(id, this.dependencies, listOf())
    }
    fun assignTo(worker:Worker) {
        this.worker = worker.id
        worker.currentStep = this
    }
}
fun List<Step>.removeDependency(id:String){
    this.forEach { step ->
        step.removeDependency(id)
    }
}

class Worker(val id:Int, var currentStep:Step?) {


    fun doWork(listOfSteps:List<Step>) {
        val step = currentStep
        if (step != null) {
            step.timeleft -= 1
            if (step.timeleft == 0) {
                currentStep = null
                listOfSteps.removeDependency(step.id)
            }
        }
    }

    override fun toString(): String {
        return "Worker $id current step:$currentStep"
    }
}

fun List<Step>.findNextStep():Step? {
    this.sortedBy { it.id }.forEach {step ->
        if (step.dependencies.isEmpty() && step.timeleft > 0 && step.worker==0) {
            return step
        }
    }
    return null
}
fun List<Worker>.assignStepToIdleWorkers(listOfSteps:List<Step>) {
    this.filter { it.currentStep == null }.forEach { idleWorker ->
        val nextStep = listOfSteps.findNextStep()
        nextStep?.assignTo(idleWorker)
        idleWorker.currentStep = nextStep
    }
}
fun List<Worker>.print(minute:Int) {

    var line = "$minute "
    this.forEach { worker ->
        line += worker.currentStep?.id ?: " "
        line += "   "
    }
    println(line)
}


fun List<Step>.allStepsComplete():Boolean {
    return this.filter { it.timeleft > 0 }.isEmpty()
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
            newListOfSteps += Step.create(step.id, listOfDependencies)
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
