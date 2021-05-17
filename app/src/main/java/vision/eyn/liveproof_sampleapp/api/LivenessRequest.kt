package vision.eyn.liveproof_sampleapp.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LivenessRequest(
    @Json(name = "audio") val audioBase64: String,
    @Json(name = "video") val videoBase64: String = "",
    @Json(name = "movement") val isMovementBased: Boolean = true,
    @Json(name = "ref_image") val refImageBase64: String? = null,
    @Json(name = "model") val device: String,
    @Json(name = "app_version") val appVersion: String? = null,
    @Json(name = "tag") val tag: String? = null // can be used to lookup data on Backend
)
