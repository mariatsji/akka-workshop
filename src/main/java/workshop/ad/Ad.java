package workshop.ad;

import java.util.Random;

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

    @Override
    public String toString() {
        return "Ad{" +
                "adId=" + adId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
