package workshop.part4;

import java.util.concurrent.ConcurrentHashMap;

import javaslang.collection.Stream;
import javaslang.control.Option;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

public class VerdictCache {

    private static final ConcurrentHashMap<Ad, Verdict> cache = new ConcurrentHashMap<>();

    public static Option<Verdict> get(Ad ad) {
        return Option.of(cache.get(ad));
    }

    public static Option<Verdict> get(Integer adId) {
        return Stream.ofAll(cache.keySet())
                .find(a -> a.getAdId().equals(adId))
                .map(cache::get);
    }

    public static void put(Ad ad, Verdict verdict) {
        cache.put(ad, verdict);
    }
}
