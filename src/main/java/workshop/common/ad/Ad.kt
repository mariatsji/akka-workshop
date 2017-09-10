package workshop.common.ad

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class Ad @JsonCreator
constructor(@param:JsonProperty(value = "userId") val userId: Int?,
            @param:JsonProperty(value = "title") val title: String,
            @param:JsonProperty(value = "description") val description: String) {

    val adId: Int?

    init {
        this.adId = Random().nextInt()
    }

    fun toAdWords(): List<String> {
        return listOf(title.split("\\W"), description.split("\\W")).flatten()
    }
}
