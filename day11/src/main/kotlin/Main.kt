val GRID_SERIAL_NUMBER = 5791
val MIN = 1
val MAX = 300
fun main(args: Array<String>) {
    var maxPower = 0
    var cellForMaxPower= FuelCell(0,0)
    var sizeOfCellsForMaxPower = 0
    (1..300).forEach {size ->
        println("Size $size")
        (MIN..(MAX - size + 1) ).forEach {y ->
            (MIN..(MAX -size + 1)).forEach{x ->
                val power = calcPowerOfBlockOfCells(x,y,size)
                if (power > maxPower) {
                    maxPower = power
                    cellForMaxPower = FuelCell(x,y)
                    sizeOfCellsForMaxPower = size
                }
            }
        }
    }
    println("Max power is $maxPower, cell is $cellForMaxPower, size is $sizeOfCellsForMaxPower")
}

fun calcPowerOfBlockOfCells(x:Int, y:Int, size:Int):Int {
    var power = 0
    (0..(size - 1)).forEach { xOffset ->
        (0..(size - 1)).forEach { yOffset ->
            power += calcPowerOfCellAt(x + xOffset, y + yOffset)
        }
    }
    return power
}

fun calcPowerOfCellAt(x:Int, y:Int):Int {
    val rackId = x + 10
    val initialPowerLevel = rackId * y
    val powerPlusSerialNumber = initialPowerLevel + GRID_SERIAL_NUMBER
    val multiplyByRackId = powerPlusSerialNumber * rackId
    val hundredsDigit = (multiplyByRackId / 100) % 10
    return   hundredsDigit - 5
}

class FuelCell(val x:Int, val y:Int) {
    override fun toString(): String {
        return "($x,$y)"
    }
}