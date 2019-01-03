import java.io.File

fun main(args: Array<String>) {
    val linesOfData = readFile()
    val listOfInstruction = processFile(linesOfData)

    //Part 1
    var countOfInstructions = 0
    listOfInstruction.forEach { instruction ->
        val opCodesWithSameResultAsTheInstruction = instruction.opCodesWithSameResultAsTheInstruction(OpCode.functions)
        if (opCodesWithSameResultAsTheInstruction.size >= 3) {
            countOfInstructions += 1
        }
    }

    println("$countOfInstructions instructions out of ${listOfInstruction.size} instructions behave like 3 or more OpCodes")


    //part 2

    val listOfDatab = readFileb()
    val listOfOperations = processFileb(listOfDatab)

    var register = listOf(0,0,0,0)
    listOfOperations.forEach {opCode ->
        register = opCode.executeFunction(register)
    }

    println("Content of register 0 is ${register[0]}")

}

class Instruction(val beforeRegisters:List<Int>, val afterRegisters:List<Int>, val opCode:OpCode) {
    override fun toString(): String {
        return "opcode: $opCode before: $beforeRegisters afrer: $afterRegisters"
    }

    fun opCodesWithSameResultAsTheInstruction(functions:List<(Int, Int, Int, List<Int>) -> List<Int>>):List<Int> {
        var listOfOpCodesNdx  = listOf<Int>()
        functions.forEachIndexed {ndx, function ->
            val result = function(opCode.A, opCode.B, opCode.C, beforeRegisters)
            if (result[0] == afterRegisters[0] && result[1] == afterRegisters[1] && result[2] == afterRegisters[2] && result[3] == afterRegisters[3]) {
                listOfOpCodesNdx += ndx
            }
        }
        return listOfOpCodesNdx
    }
}

class OpCode(val description:String, val number:Int, val A:Int, val B:Int, val C:Int ) {

    //val codeToFunction = listOf(3,6,11,14,12,13,1, ? ,2,0,7,?,9,15,?,10) //had to guess op 7, 11 and 14!
    val codeToFunction = listOf(3,6,11,14,12,13,1,8,2,0,7,4,9,15,5,10)

    fun executeFunction(register:List<Int>):List<Int> {
        val f = functions[codeToFunction[number]]
        return  f(A, B, C, register)
    }

    override fun toString(): String {
        return "$description $number $A $B $C"
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
        val setr = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
            val result = registers.toMutableList()
            result[C] =  registers[A]
            return result
        }
        val seti = fun(A:Int, B:Int, C:Int, registers: List<Int>):List<Int>{
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



fun processFile(linesOfData:List<String>):List<Instruction> {
    var listOfInstructions = listOf<Instruction>()
    (0..linesOfData.size - 3).forEach { ndx ->
        if (linesOfData[ndx].length > 5 && linesOfData[ndx].substring(0,6) == "Before" ) {
            val beforeRegister = linesOfData[ndx].toRegister()
            val opCode = linesOfData[ndx +1].toOpcode()
            val afterRegister = linesOfData[ndx +2].toRegister()
            val instruction = Instruction(beforeRegister, afterRegister, opCode)
            listOfInstructions += instruction
        }
    }
    return listOfInstructions
}

fun processFileb(linesOfData:List<String>):List<OpCode> {
    var listOfOpCodes = listOf<OpCode>()
    linesOfData.forEach { line ->
        val nums = line.split(" ").map{it.toInt()}
        val opCode = OpCode("",nums[0],nums[1],nums[2],nums[3])
        listOfOpCodes += opCode
    }
    return listOfOpCodes
}


fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day16.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
fun readFileb():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day16b.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}
fun String.toRegister():List<Int>{
    var register = listOf<Int>()
    val registersChars = this.split("[")[1].split(", ")
    registersChars.forEach { registerChar ->
        registerChar.removePrefix("[").removeSuffix("]").toInt()
        register += registerChar.removePrefix("[").removeSuffix("]").toInt()
    }
    return register
}
fun String.toOpcode():OpCode{
    val opCodeChars =  this.split(" ")
    return OpCode("", opCodeChars[0].toInt(), opCodeChars[1].toInt(), opCodeChars[2].toInt(), opCodeChars[3].toInt())

}

