package vision.eyn.liveproof_sampleapp.api

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import vision.eyn.liveproof_sampleapp.BuildConfig

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Chain): Response = chain.proceed(
        chain.request().newBuilder().addHeader("Authorization", "Basic $API_SECRET").build()
    )

    companion object {
        private const val API_SECRET = BuildConfig.API_SECRET
    }
}