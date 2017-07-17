package workshop.part1;

import javaslang.collection.List;
import workshop.fraudwordsservice.FraudWord;

public class Verdict {

    public final Integer adId;
    public final List<FraudWord> fraudWords;
    public final VerdictStatus verdictStatus;

    public Verdict(Integer adId, List<FraudWord> fraudWords, VerdictStatus verdictStatus) {
        this.adId = adId;
        this.fraudWords = fraudWords;
        this.verdictStatus = verdictStatus;
    }

    @Override
    public String toString() {
        return "Verdict{" +
                "adId=" + adId +
                ", fraudWords=" + fraudWords +
                ", verdictStatus=" + verdictStatus +
                '}';
    }
}
