package workshop.part1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// This class is used as http response in the akka-http service in part 4
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

    // This enum is returned from actors as a vetting result
    public enum VerdictType {
        GOOD, BAD, PENDING, FAILURE
    }
}

