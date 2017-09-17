package workshop.part3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Verdict {

    private final String id;
    private final String value;

    @JsonCreator
    public Verdict(@JsonProperty("id") String id,
            @JsonProperty("value") VerdictType value) {
        this.id = id;
        this.value = value.name();
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    enum VerdictType {
        GOOD, BAD, PENDING
    }
}
