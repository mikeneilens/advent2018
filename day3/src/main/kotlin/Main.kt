import java.io.File

fun main(args: Array<String>) {
    val records = readFile()
    var map = HashMap<Int, String>()
    records.forEach{
        val claim = Claim.createFrom(it)
        map = overlay(map, claim.getMap())
    }
    val overlaps = map.map{ if (it.value == "X") 1 else 0  }
    val number = overlaps.fold(0,{acc, i -> acc + i})
    println("nunber of overlaps:$number")

    records.forEach{
        val claim = Claim.createFrom(it)
        if (claim.isIntactOn(map)) {
            println("${claim.text()} does not overlap")
        }
    }


}

fun overlay(currentMap:HashMap<Int, String>, newMap:HashMap<Int, String>):HashMap<Int, String> {
    var map = HashMap<Int, String>()
    newMap.forEach { key, value -> if (currentMap.containsKey(key)) map.put(key,"X") else map.put(key, value)   }
    currentMap.forEach { key, value -> if (!newMap.containsKey(key)) map.put(key, value)   }
    return map
}

fun createKey( x:Int,  y:Int):Int {
    return  10000 * y + x
}

class Claim(val pattern:String, val x:Int, val y:Int, val width:Int, val length:Int){
    fun text():String  {
        return "#$pattern @ $x,$y : ${width}x$length"
    }
    
    fun getMap():HashMap<Int, String> {
        var map = HashMap<Int, String>()
        for (i in 0..(width -1)) {
            for (j in 0..(length -1)) {
                map.put(createKey(x + i,y + j),pattern)
            }
        }
        return map
    }

    fun isIntactOn(map:HashMap<Int, String>):Boolean {
        val mapForClaim = getMap()
        val patternIsOnMapAsArray = mapForClaim.map{map[it.key] == it.value }
        return patternIsOnMapAsArray.fold(true){acc ,i -> acc.and(i) }
    }

    companion object {
        fun createFrom(patternData:String):Claim{
            val splitPatternData = patternData.split("#","@")
            val pattern = splitPatternData[1].removeSuffix(" ")
            val origin = splitPatternData[2].split(":")[0]
            val size = splitPatternData[2].split(":")[1]
            val x = origin.split(",")[0].removePrefix(" ").toInt()
            val y = origin.split(",")[1].removeSuffix(" ").toInt()
            val width = size.split("x")[0].removePrefix(" ").toInt()
            val length = size.split("x")[1].removeSuffix(" ").toInt()

            return Claim(pattern, x,y,width,length)
        }
    }
}


fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day3.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}


