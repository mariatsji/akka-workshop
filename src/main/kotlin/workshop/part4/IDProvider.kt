package workshop.part4

import java.util.concurrent.atomic.AtomicInteger

object IDProvider {

    private val counter = AtomicInteger(0)

    fun nextId(): String {
        return getCounter().toString()
    }

    private fun getCounter(): Int {
        return counter.incrementAndGet()
    }


}
