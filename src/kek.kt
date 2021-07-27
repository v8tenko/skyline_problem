fun getSkyline(buildings: Array<IntArray>): List<List<Int>> {
    val n = buildings.size
    val events = mutableListOf<Event>()
    repeat(n) {
        val (x, y, h) = buildings[it]
        events.add(Event(x, h, Type.Open))
        events.add(Event(y, h, Type.Close))
    }
    events.sortWith(compareBy({ it.x }, { it.type.value() }, { -it.height }))
    val arr = mutableListOf<List<Int>>()
    val heights = SegmentTree()
    events.forEach {
        if (it.type == Type.Open) {
            heights.add(it.height)
        } else {
            heights.remove(it.height)
        }
        val maxHeight = heights.max().toInt()
        if (it.type == Type.Open && it.height == maxHeight && heights.count(maxHeight) == 1L) {
            arr.add(listOf(it.x, it.height))
        } else if (it.type == Type.Close && it.height > maxHeight) {
            arr.add(listOf(it.x, maxHeight))
        }
    }
    val ans = mutableListOf<List<Int>>()
    var i = 0
    while (i < arr.size) {
        var j = i + 1
        while (j < arr.size && arr[i][0] == arr[j][0]) {
            j++
        }
        ans.add(arr[j - 1])
        i = j
    }
    return ans.toList()
}

class SegmentTree(maxElement: Long = 2L shl 31 - 1) {
    private val MAX_ELEMENT: Long = maxElement + 1
    private var root: Node = Node(0, null, null)

    val size: Long
        get() = root.value

    fun add(value: Int) {
        root.update(1, value.toLong(), 0, MAX_ELEMENT)
    }

    fun remove(value: Int) {
        root.update(-1, value.toLong())
    }

    fun count(value: Int) = root.sum(value.toLong(), value.toLong())

    fun maxInRange(l: Long, r: Long) = root.query(l, r)
    fun maxInPrefix(p: Long) = root.query(0, p)
    fun max() = root.query(0, MAX_ELEMENT - 1)

    fun toList(): List<Long> {
        val res = mutableListOf<Long>()
        (0 until MAX_ELEMENT).forEach {
            (0 until root[it]).forEach { _ ->
                res.add(it)
            }
        }
        return res.toList()
    }


    private inner class Node(
        var value: Long,
        var left: Node?,
        var right: Node?,
    ) {

        fun leaf(v: Long) = Node(v, null, null)

        fun query(l: Long, r: Long) = query(l, r, 0, MAX_ELEMENT)
        fun sum(l: Long, r: Long) = sum(l, r, 0, MAX_ELEMENT)
        fun sum(p: Long) = sum(p, p)
        operator fun get(p: Long) = sum(p, p, 0, MAX_ELEMENT)

        fun update(l: Long, r: Long) = update(l, r, 0, MAX_ELEMENT)

        fun sum(l: Long, r: Long, start: Long, end: Long): Long {
            if (l >= end || r < start) {
                return 0
            }
            if (start >= l && end - 1 <= r) {
                return value
            }
            val middle = (start + end) / 2
            return (left?.sum(l, r, start, middle) ?: 0) + (right?.sum(l, r, middle, end) ?: 0)
        }

        fun query(l: Long, r: Long, start: Long, end: Long): Long {
            if (l >= end || r < start) {
                return 0
            }
            if (end - start == 1L) {
                return start
            }
            val middle = (start + end) / 2
            val hashRight = right?.sum(l, r, middle, end)?.let { it > 0 } ?: false
            if (hashRight) {
                return right!!.query(l, r, middle, end)
            }
            return left?.query(l, r, start, middle) ?: 0
        }

        fun update(value: Long, position: Long, start: Long, end: Long) {
            if (position >= MAX_ELEMENT) {
                throw IndexOutOfBoundsException("max element is $MAX_ELEMENT, but query is $position")
            }
            if (end - start == 1L) {
                this.value += value
                if (this.value < 0) {
                    throw NoSuchElementException("there is no element $position")
                }
                return
            }
            val middle = (start + end) / 2
            if (position < middle) {
                if (left == null) {
                    left = leaf(0)
                }
                left!!.update(value, position, start, middle)
            } else {
                if (right == null) {
                    right = leaf(0)
                }
                right!!.update(value, position, middle, end)
            }
            this.value = (left?.value ?: 0) + (right?.value ?: 0)
        }
    }
}

data class Event(
    val x: Int,
    val height: Int,
    val type: Type
)

enum class Type {
    Open, Close;

    fun value() = when (this) {
        Open -> -1
        Close -> 1
    }
}
