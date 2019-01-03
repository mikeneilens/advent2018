class Step(val position:Position, private val parent:Step?, val noOfPreviousSteps:Int) {
    fun firstStep():Step {
        return if (parent?.parent == null) this else this.parent.firstStep()
    }
    override fun toString(): String {
        return "Step at $position"
    }
}
