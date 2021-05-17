package vision.eyn.liveproof_sampleapp.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetAudioResponse(
    @Json(name = "audio") val audioBase64: String
)