sealed class ResultOfMove {
    object NoTargetsLeft:ResultOfMove()
    class Attack(val unitToAttack:Unit):ResultOfMove()
    object Moved:ResultOfMove()
    object CannotMove:ResultOfMove()

    override fun toString(): String =  when(this) {
        is ResultOfMove.NoTargetsLeft -> "No targets left"
        is ResultOfMove.Attack -> "Attack" + this.unitToAttack.square
        is ResultOfMove.Moved -> "Moved"
        is ResultOfMove.CannotMove -> "Cannot Move"
    }
}

