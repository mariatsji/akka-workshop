package workshop.part4;

import java.util.concurrent.ConcurrentHashMap;

import javaslang.Tuple2;
import javaslang.control.Option;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

public class VettingRepository {

    private static final ConcurrentHashMap<String, Tuple2<Ad, Verdict>> verdicts = new ConcurrentHashMap<>();

    public static Option<Verdict> getVerdict(Ad ad) {
        return Option.ofOptional(verdicts.values().stream()
                .filter(t -> ad != null)
                .filter(t -> t._1.equals(ad))
                .map(t -> t._2)
                .findFirst());
    }

    public static Option<Ad> getAd(String verdictId) {
        return Option.of(verdicts.get(verdicts)._1);
    }

    public static Option<Verdict> getVerdict(String verdictId) {
        return Option.of(verdicts.get(verdictId)._2);
    }
}
