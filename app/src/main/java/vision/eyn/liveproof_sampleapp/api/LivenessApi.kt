package vision.eyn.liveproof_sampleapp.api

import retrofit2.http.*

interface LivenessApi {

    @GET("/audio")
    suspend fun getAudioFile(@Query("movement") isMovementBased: Boolean): GetAudioResponse

    @POST("/liveness_receiver")
    suspend fun postLivenessRequest(@Body request: LivenessRequest): LivenessResponse
}