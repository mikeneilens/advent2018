import java.io.File

fun main(args: Array<String>) {
    val listOfObservations = readFile()
    val  observationsSorted = listOfObservations.sorted()

    val guardActivities = createGuardActivities(observationsSorted, listOf<GuardActivity>(),"")
    val mapOfActivities = createMapOfActivities(guardActivities,ActivityMap("","",listOf<String>()), listOf())
    val sortedMapOfActivities = mapOfActivities.sortedBy { it.id.removePrefix("#").toInt() }
    val laziestGuard = obtainLaziestGuard(sortedMapOfActivities,"","",0,0)
    println("Laziest Guard $laziestGuard")

    val laziestMapOfActivities = sortedMapOfActivities.filter { it.id == laziestGuard }
    val laziestMinuteAndTotal = obtainLaziestMinute(laziestMapOfActivities)
    println("Laziest minute $laziestMinuteAndTotal")
    val laziestGuardNo = laziestGuard.removePrefix("#").toInt()
    println("Laziest guard X laziest minute = ${laziestGuardNo * laziestMinuteAndTotal.first} ")

    var mostFrequentlyAsleepMinute = 0
    var mostFrequentlyAsleep = 0
    var guardMostAsleepAtThatMinute = ""
    sortedMapOfActivities.forEach{ activityMap ->
        val activitiesForId = sortedMapOfActivities.filter { it.id == activityMap.id }
        val laziestMinuteAndTotal = obtainLaziestMinute(activitiesForId)
        if (laziestMinuteAndTotal.second > mostFrequentlyAsleep) {
            mostFrequentlyAsleep       = laziestMinuteAndTotal.second
            mostFrequentlyAsleepMinute = laziestMinuteAndTotal.first
            guardMostAsleepAtThatMinute = activityMap.id
        }
    }
    println("MostFrequently asleep minute = $mostFrequentlyAsleepMinute guardMostAsleepAtThatMinute = $guardMostAsleepAtThatMinute")
    val guardNo = guardMostAsleepAtThatMinute.removePrefix("#").toInt()
    println("Most frewquently asleep minute X guardNo = ${mostFrequentlyAsleepMinute * guardNo} ")
}

class GuardActivity(val id:String, val date:String, val minute:Int, val event:String) {

    companion object {
        fun create(observation:String, nextObservation:String, prevGuard:String):GuardActivity {
            val splitData = observation.split(" ")
            var date = splitData[0].substring(1,11)
            val hour = splitData[1].substring(0,2).toInt()
            val minute = if (hour == 0) {
                            splitData[1].substring(3,5).toInt()
                         }
                         else {
                            date = nextObservation.split(" ")[0].substring(1,11)
                            0
                         }
            val event = splitData[2]
            return if (event == "Guard") {
                GuardActivity(splitData[3],date, minute,"Begins Shift")
            } else {
                GuardActivity(prevGuard,date, minute,event)
            }
        }
    }
}
class ActivityMap(val id:String, private val date:String, val activities:List<String>) {
    val minutesSleeping = activities.fold(0){ acc, element -> if (element=="#") acc +1 else acc }
    fun add(activity:String):ActivityMap {
        return ActivityMap(this.id,date,activities + activity)
    }
}

fun readFile():List<String> {
    val lineList = mutableListOf<String>()
    File("/Users/michaelneilens/day4.txt").useLines { lines -> lines.forEach { lineList.add(it) }}
    return lineList
}

fun createGuardActivities(observations:List<String>, guardActivities:List<GuardActivity>, prevGuard:String):List<GuardActivity> {
    if (observations.isEmpty()) return guardActivities
    val head = observations.first()
    val remainder = observations.drop(1)
    val guardActivity = if (remainder.size > 0 ) {
        GuardActivity.create(head,remainder.first(),prevGuard)
    } else {
        GuardActivity.create(head,head,prevGuard)
    }
    return createGuardActivities(remainder,guardActivities + guardActivity, guardActivity.id)
}
fun createMapOfActivities(guardActivities: List<GuardActivity>, activityMap:ActivityMap, listOfActivityMaps:List<ActivityMap>):List<ActivityMap> {

    fun addEvent(times:Int, activity:String, activityMap:ActivityMap):ActivityMap {
        return if (times == 0)
            activityMap
        else
            addEvent(times -1 , activity, activityMap.add(activity) )
    }

    val currentGuardActivity = guardActivities.first()
    val remainder = guardActivities.drop(1)

    if (remainder.isEmpty()) {
        val updatedActivityMap = if (currentGuardActivity.event == "falls") addEvent(59 - currentGuardActivity.minute + 1, "#", activityMap)
        else addEvent(59 - currentGuardActivity.minute + 1, ".", activityMap)
        return listOfActivityMaps + updatedActivityMap
    }

    val nextGuardActivity = remainder.first()
    val endMinute = if ((currentGuardActivity.date != nextGuardActivity.date)|| (currentGuardActivity.id != nextGuardActivity.id))  59
                    else nextGuardActivity.minute - 1
    val startMinute = if (currentGuardActivity.event == "Begins Shift") 0
                      else currentGuardActivity.minute
    val activityMapToUpdate =   if (currentGuardActivity.event == "Begins Shift") ActivityMap(currentGuardActivity.id, currentGuardActivity.date,listOf())
                                else activityMap
    val updatedActivityMap = if (currentGuardActivity.event == "falls") addEvent(endMinute - startMinute + 1, "#", activityMapToUpdate)
                             else addEvent(endMinute - startMinute + 1, ".", activityMapToUpdate)

    return if ((currentGuardActivity.event == "Begins Shift")&&(!activityMap.id.isEmpty()))
        createMapOfActivities(remainder,updatedActivityMap, listOfActivityMaps + activityMap)
    else
        createMapOfActivities(remainder,updatedActivityMap, listOfActivityMaps)
}
fun obtainLaziestGuard(mapOfActivities:List<ActivityMap>, laziestGuard:String, previousGuard:String, minutesSleeping:Int, mostMinutesSleeping:Int):String{
    if (mapOfActivities.isEmpty()) return laziestGuard

    val head = mapOfActivities.first()
    val remainder = mapOfActivities.drop(1)
    val totalMinutesSleeping =  if (head.id == previousGuard) minutesSleeping + head.minutesSleeping
                                else  head.minutesSleeping

    return  if ((totalMinutesSleeping) > mostMinutesSleeping) obtainLaziestGuard(remainder,head.id,head.id,totalMinutesSleeping,totalMinutesSleeping )
            else obtainLaziestGuard(remainder,laziestGuard, head.id,totalMinutesSleeping,mostMinutesSleeping )

}
fun obtainLaziestMinute(mapOfActivities: List<ActivityMap>):Pair<Int,Int> {
    var laziestMinute = 0
    var laziestTotal = 0
    for (i in 0..59) {
        val total = mapOfActivities.fold(0){acc, element -> if (element.activities[i] == "#") acc + 1 else acc  }
        if (total > laziestTotal) {
            laziestTotal = total
            laziestMinute = i
        }
    }
    return Pair (laziestMinute, laziestTotal)
}


