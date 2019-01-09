import java.io.File

fun main(args: Array<String>) {
    val linesOfData = readFile("/Users/michaelneilens/day21.txt")
    val program = Program.createFrom(linesOfData)
    var registers = program.execute(0)

    println(registers)
    println("finished")
}

class Program(private val instructionPointerRegister:Int, private val listOfOpCodes:List<OpCode>  ) {


    fun execute(instructionPointer: Int): List<Int> {

        var registersFound = 0
        var bestRegister = listOf<Int>(0,0,0,0,999999999,0)
        var bestInstructionsExectured = 0
        var bestRegister2 = listOf<Int>(0,0,0,0,999999999,0)
        var instructionExecuted = MutableList(listOfOpCodes.size){false}

        tailrec fun execute(instructionPointer: Int, listOfRegisters: List<Int>): List<Int> {

            instructionExecuted[instructionPointer] = true
            if (instructionPointer > listOfOpCodes.size - 1) return listOfRegisters

            if (instructionPointer == 28) {
                if (listOfRegisters[4] < bestRegister[4]) {
                    bestRegister = listOfRegisters
                    registersFound += 1
//                    println("new best $bestRegister $registersFound")
                    if (registersFound > 10000) return bestRegister
                }
                if (instructionExecuted.filter { it == true }.size >= bestInstructionsExectured) {
                    bestInstructionsExectured = instructionExecuted.filter { it == true }.size
                    if (listOfRegisters[4] < bestRegister2[4]) {
                        bestRegister2 = listOfRegisters
                        println("new best part2 $bestRegister2 $bestInstructionsExectured")
                    }
                }
            }
            val registersWithIPUpdated = listOfRegisters.toMutableList()
            registersWithIPUpdated[instructionPointerRegister] = instructionPointer

            val opCode = listOfOpCodes[instructionPointer]
            val updatedRegisters = opCode.executeFunction(registersWithIPUpdated)

//            println("ip=$instructionPointer $registersWithIPUpdated $opCode $updatedRegisters ")
            return execute(updatedRegisters[instructionPointerRegister] + 1, updatedRegisters)

        }

        val registers = execute(instructionPointer, listOf( 0,0, 0, 0, 0, 0))

        return execute(instructionPointer, listOf( registers[0],0, 0, 0, 0, 0))
        //return execute(instructionPointer, listOf( 1,0, 0, 0, 0, 0))
    }

    companion object {
        fun createFrom(listOfData:List<String>):Program {
            var listOfOpCodes = listOf<OpCode>()
            val instructionPointerRegister = listOfData[0].split(" ")[1].toInt()
            listOfData.forEachIndexed { ndx, line ->
                if (ndx > 0 ) {
                    val instruction = line.split(" ")[0]
                    val paramA = line.split(" ")[1].toInt()
                    val paramB = line.split(" ")[2].toInt()
                    val paramC = line.split(" ")[3].toInt()
                    val opCode = OpCode(instruction,0,paramA,paramB,paramC)
                    listOfOpCodes += opCode
                }
            }
            return Program(instructionPointerRegister,listOfOpCodes)
        }
    }
}

class OpCode(private val description:String, private val number:Int, private val paramA:Int, private val paramB:Int, private val paramC:Int ) {

    private val codeToFunction = listOf(3,6,11,14,12,13,1,8,2,0,7,4,9,15,5,10)

    fun executeFunction(register:List<Int>):List<Int> {
        val f = if (description.isEmpty()) {
            functions[codeToFunction[number]]
        } else {
            description.toFunction()
        }
        return  f(paramA, paramB, paramC, register)
    }

    override fun toString(): String {
        return "$description $paramA $paramB $paramC"
    }

    companion object {
        val addr = fun (A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A] + registers[B]
            return result
        }
        val addi = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A] + B
            return result
        }
        val mulr = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A] * registers[B]
            return result
        }
        val muli = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A] * B
            return result
        }
        val banr = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A].and(registers[B])
            return result
        }
        val bani = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A].and(B)
            return result
        }
        val borr = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A].or(registers[B])
            return result
        }
        val bori = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A].or(B)
            return result
        }
        val setr = fun(A:Int, _:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A]
            return result
        }
        val seti = fun(A:Int, _:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  A
            return result
        }
        val gtir = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  if (A > (registers[B])) 1 else 0
            return result
        }
        val gtri = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  if (registers[A] > B) 1 else 0
            return result
        }
        val gtrr = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  if (registers[A] > registers[B]) 1 else 0
            return result
        }
        val eqir = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  if (A == (registers[B])) 1 else 0
            return result
        }
        val eqri = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  if (registers[A] == B) 1 else 0
            return result
        }
        val eqrr = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  if (registers[A] == registers[B]) 1 else 0
            return result
        }
        val functions = listOf(addr, addi, mulr,muli,bani,banr,borr,bori,setr,seti,gtir,gtri,gtrr, eqir,eqri,eqrr)
    }
}

fun readFile(inputFile:String):List<String> {
    val lineList = mutableListOf<String>()
    File(inputFile).useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun String.toFunction():(Int, Int, Int, List<Int>)->List<Int> = when(this) {
    "addr" -> OpCode.addr
    "addi" -> OpCode.addi
    "mulr" -> OpCode.mulr
    "muli" -> OpCode.muli
    "bani" -> OpCode.bani
    "banr" -> OpCode.banr
    "borr" -> OpCode.borr
    "bori" -> OpCode.bori
    "setr" -> OpCode.setr
    "seti" -> OpCode.seti
    "gtir" -> OpCode.gtir
    "gtri" -> OpCode.gtri
    "gtrr" -> OpCode.gtrr
    "eqir" -> OpCode.eqir
    "eqri" -> OpCode.eqri
    "eqrr" -> OpCode.eqrr
    else   -> { println("error $this")
        OpCode.eqrr
    }
}

