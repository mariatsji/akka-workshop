package workshop.part1;

import javaslang.collection.List;
import workshop.common.fraudwordsservice.FraudWord;

public class Verdict {

    public final List<FraudWord> fraudWords;
    public final VerdictStatus verdictStatus;

    public Verdict(List<FraudWord> fraudWords, VerdictStatus verdictStatus) {
        this.fraudWords = fraudWords;
        this.verdictStatus = verdictStatus;
    }

    @Override
    public String toString() {
        return "Verdict{" +
                ", fraudWords=" + fraudWords +
                ", verdictStatus=" + verdictStatus +
                '}';
    }
}
