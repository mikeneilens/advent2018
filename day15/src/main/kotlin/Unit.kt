open class Unit(var position:Position, val square:Square, val attackPower:Int = 3, var hitPoints:Int = 200   ) {
    fun isAlive() = (hitPoints > 0)
    fun scoreToString():String {
        return "${square.image}($hitPoints)"
    }

    fun move(listOfUnits:List<Unit>, mapOfSquares: HashMap<Position, Square>): ResultOfMove {
        val targets =  targets(listOfUnits)
        if (targets.isEmpty()) return ResultOfMove.NoTargetsLeft

        val adjacentTarget = this.adjacentTarget(targets)
        if (adjacentTarget != null) return ResultOfMove.Attack(adjacentTarget)

        val positionsInRangeOfTarget = targets.map{ target -> target.emptyAdjacentPositions(mapOfSquares,listOfUnits)}.flatten()
        if (positionsInRangeOfTarget.isEmpty()) return ResultOfMove.CannotMove

        var finalStepOfShortestRoute = Step(Position(0,0),null,9999)
        var routeFound = false

        positionsInRangeOfTarget.forEach { targetPosition ->
            val validRoutes = getRoute(targetPosition, Step(this.position, null,0),  mapOfSquares,listOfUnits)
            if (validRoutes.isNotEmpty()) {
                val finalStepforShortestRouteToThisTarget = validRoutes.shortestRoute()
                if (finalStepforShortestRouteToThisTarget.noOfPreviousSteps < finalStepOfShortestRoute.noOfPreviousSteps ) {
                    finalStepOfShortestRoute = finalStepforShortestRouteToThisTarget
                    routeFound = true
                }
            }
        }
        if (routeFound) {
            this.position = finalStepOfShortestRoute.firstStep().position
            return ResultOfMove.Moved
        } else {
            return ResultOfMove.CannotMove
        }
    }

    private fun emptyAdjacentPositions(mapOfSquares: HashMap<Position, Square>, listOfUnits: List<Unit>):List<Position> {
        return this.position.emptyAdjacentPositions(mapOfSquares,listOfUnits)
    }

    fun adjacentTarget(listOfUnits: List<Unit>):Unit? {
        var targetWithLowestHitPoints:Unit? = null
        listOfUnits.forEach { unit ->
            offsets.forEach { offset ->
                val positionInRange = unit.position + Position(offset.x,offset.y)
                if (positionInRange == this.position) {
                    if (unit.hitPoints < (targetWithLowestHitPoints?.hitPoints ?: 9999 )) {
                        targetWithLowestHitPoints = unit
                    }
                }
            }
        }
        return targetWithLowestHitPoints
    }

    fun attacked(attackPower:Int) {
        if (attackPower == 0 || !isAlive()) return
        else {
            //  println("$square at $position is being attacked!")
            hitPoints -= 1
            this.attacked(attackPower - 1)
        }
    }

    fun targets(listOfUnits:List<Unit>):List<Unit>{
        return listOfUnits.filter { unit ->  (unit::class != this::class && unit.isAlive()) }
    }
}


class Goblin( position:Position, square:Square = Square.Goblin ):Unit(position, square)
class Elf( position:Position, square:Square = Square.Elf ):Unit(position, square)

