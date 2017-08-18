package workshop.part3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Ad {

    private final Long adId;
    private final String title;
    private final String description;

    @JsonCreator
    public Ad(@JsonProperty(value = "adId") Long adId,
              @JsonProperty(value = "title") String title,
              @JsonProperty(value = "description") String description) {
        this.adId = adId;
        this.title = title;
        this.description = description;
    }

    public Long getAdId() {
        return adId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
