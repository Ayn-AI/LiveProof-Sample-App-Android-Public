package vision.eyn.liveproof_sampleapp

import android.app.Application
import com.facebook.stetho.Stetho

class LiveproofApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }

    companion object {
        const val EXTRA_SESSION = "session"
    }
}