package vision.eyn.liveproof_sampleapp.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LivenessResponse(
    @Json(name = "record_id") val recordId: String,
    @Json(name = "liveness_score") val score: String, // "55.44"
    @Json(name = "liveness_result") val result: LivenessResult,
    @Json(name = "created_at") val createdAtTimestamp: Long,
    @Json(name = "is_optimized") val isOptimized: Boolean = false
)

enum class LivenessResult {
    @Json(name = "not_implemented")
    NOT_IMPLEMENTED,
    @Json(name = "genuine")
    GENUINE,
    @Json(name = "attack")
    ATTACK,
    @Json(name = "invalid_input")
    INVALID
}