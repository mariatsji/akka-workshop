package workshop.part4;

import java.util.concurrent.ConcurrentHashMap;

import javaslang.collection.Stream;
import javaslang.control.Option;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

public class VerdictCache {

    private ConcurrentHashMap<Ad, Verdict> cache = new ConcurrentHashMap<>();

    public Option<Verdict> get(Ad ad) {
        return Option.of(cache.get(ad));
    }

    public Option<Verdict> get(Integer adId) {
        return Stream.ofAll(cache.keySet())
                .find(a -> a.getAdId().equals(adId))
                .map(cache::get);
    }

    public void put(Ad ad, Verdict verdict) {
        cache.put(ad, verdict);
    }
}
