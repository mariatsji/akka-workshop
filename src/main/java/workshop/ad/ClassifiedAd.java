package workshop.ad;

public class ClassifiedAd {

    public final Long adId;
    public final Long userId;
    public final String title;
    public final String description;

    public ClassifiedAd(Long adId, Long userId, String title, String description) {
        this.adId = adId;
        this.userId = userId;
        this.title = title;
        this.description = description;
    }
}
