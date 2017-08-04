package workshop.common.ad;

import java.util.Random;

import javaslang.collection.List;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.Verdict;
import workshop.part1.VerdictStatus;

public class Ad {

    public final Integer adId;
    public final Integer userId;
    public final String title;
    public final String description;

    public Ad(Integer userId, String title, String description) {
        this.adId = new Random().nextInt();
        this.userId = userId;
        this.title = title;
        this.description = description;
    }

    public List<String> toAdWords() {
        return List.of(title.split("\\W"))
            .push(description.split("\\W"));
    }


    public static Verdict toVerdictStatus(UserCriminalRecord record, List<FraudWord> fraudWords) {
        return new Verdict(fraudWords, toVerdictStatus(fraudWords, record));
    }

    public static VerdictStatus toVerdictStatus(List<FraudWord> fraudWords, UserCriminalRecord record) {
        if (record == UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            return VerdictStatus.GOOD;
        } else {
            return VerdictStatus.BAD;
        }
    }

    @Override
    public String toString() {
        return "Ad{" +
            "adId=" + adId +
            ", userId=" + userId +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ad ad = (Ad) o;

        return adId.equals(ad.adId);
    }

    @Override
    public int hashCode() {
        return adId.hashCode();
    }
}
