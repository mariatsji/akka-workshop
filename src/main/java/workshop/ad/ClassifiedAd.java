package workshop.ad;

import java.util.Random;

public class ClassifiedAd {

    public final Integer adId;
    public final Integer userId;
    public final String title;
    public final String description;

    public ClassifiedAd(Integer userId, String title, String description) {
        this.adId = new Random().nextInt();
        this.userId = userId;
        this.title = title;
        this.description = description;
    }

    @Override
    public String toString() {
        return "ClassifiedAd{" +
                "adId=" + adId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
