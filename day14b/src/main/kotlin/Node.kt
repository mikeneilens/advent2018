class Node<T>(value: T){
    var value:T = value
    var next: Node<T>? = null
    var previous:Node<T>? = null

    fun nextCyclical() = if (next == null) first() else next
    fun previousCyclical() = if (previous == null) last() else previous

    fun first(): Node<T>? = getFirst(this)
    fun last(): Node<T>? = getLast(this)

    private tailrec fun getFirst(node:Node<T>?):Node<T>? {
        if (node?.previous == null) return node else return getFirst(node.previous)
    }
    private tailrec fun getLast(node:Node<T>?):Node<T>? {
        if (node?.next == null) return node else return getLast(node.next)
    }

    fun addNode(value:T):Node<T> {
        val nextNode = next
        val newNode = Node(value)
        newNode.next = nextNode
        newNode.previous = this
        nextNode?.previous = newNode
        this.next = newNode
        return newNode
    }
    fun removeNextNode() {
        if (this.next != null) {
            val nextNode = this.next
            this.next = nextNode?.next
            nextNode?.next?.previous = this
            nextNode?.previous = null
            nextNode?.next = null
        } else {
            val nextNode = first()
            nextNode?.next?.previous = this
            nextNode?.previous = null
            nextNode?.next = null
        }

    }
    fun subList(first:Int, last:Int):List<T> {
        var node = this
        (0..(first - 1)).forEach {
            if (node.next != null ) node = node.next!!
        }
        var list = listOf<T>()
        (first..last).forEach {
            list += node.value
            if (node.next != null ) node = node.next!!
        }
        return list
    }

    override fun toString():String {
        val opening = if (previous == null ) "[" else ""
        val closing = if (next == null ) "]" else "," + next.toString()
        return opening + value.toString() + closing
    }
}