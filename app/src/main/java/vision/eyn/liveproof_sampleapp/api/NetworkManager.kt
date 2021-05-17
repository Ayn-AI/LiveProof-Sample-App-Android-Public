package vision.eyn.liveproof_sampleapp.api

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import vision.eyn.liveproof_sampleapp.BuildConfig
import java.util.concurrent.TimeUnit

class NetworkManager private constructor() {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder().addInterceptor(authInterceptor).apply {
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(StethoInterceptor())
            }
        }.writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BuildConfig.API_URL).client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
    }

    val livenessApi: LivenessApi by lazy {
        retrofit.create(LivenessApi::class.java)
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: NetworkManager? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: NetworkManager().also { instance = it }
            }
    }
}