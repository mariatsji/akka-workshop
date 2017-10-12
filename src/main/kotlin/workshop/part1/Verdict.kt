package workshop.part1

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

enum class VerdictType {
    GOOD, BAD, PENDING, FAILURE
}

class Verdict @JsonCreator
constructor(@param:JsonProperty("id") val id: String,
            @JsonProperty("value") value: VerdictType) {
    val value: String

    init {
        this.value = value.name
    }


}

