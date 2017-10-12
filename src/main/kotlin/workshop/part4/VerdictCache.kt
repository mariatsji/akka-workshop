package workshop.part4

import javaslang.collection.Stream
import javaslang.control.Option
import workshop.common.ad.Ad
import workshop.part1.Verdict
import java.util.concurrent.ConcurrentHashMap

open class VerdictCache {

    private val cache = ConcurrentHashMap<Ad, Verdict>()

    operator fun get(ad: Ad): Option<Verdict> {
        return Option.of(cache[ad])
    }

    open operator fun get(adId: Int?): Option<Verdict> {
        return Stream.ofAll(cache.keys)
                .find { a -> a.adId == adId }
                .map({ cache[it] })
    }

    fun put(ad: Ad, verdict: Verdict) {
        cache.put(ad, verdict)
    }
}
