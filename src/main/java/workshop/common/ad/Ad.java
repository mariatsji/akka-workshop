package workshop.common.ad;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.List;

public class Ad {

    public final Integer adId;
    public final Integer userId;
    public final String title;
    public final String description;

    @JsonCreator
    public Ad(@JsonProperty(value = "userId") Integer userId,
              @JsonProperty(value = "title") String title,
              @JsonProperty(value = "description") String description) {
        this.adId = new Random().nextInt();
        this.userId = userId;
        this.title = title;
        this.description = description;
    }

    public List<String> toAdWords() {
        return List.of(title.split("\\W"))
            .push(description.split("\\W"));
    }

    public Integer getAdId() {
        return adId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ad ad = (Ad) o;

        if (!adId.equals(ad.adId)) return false;
        if (!userId.equals(ad.userId)) return false;
        if (!title.equals(ad.title)) return false;
        return description.equals(ad.description);
    }

    @Override
    public int hashCode() {
        int result = adId.hashCode();
        result = 31 * result + userId.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
