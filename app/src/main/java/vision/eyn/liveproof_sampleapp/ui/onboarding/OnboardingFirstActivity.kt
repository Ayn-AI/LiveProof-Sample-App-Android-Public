package vision.eyn.liveproof_sampleapp.ui.onboarding

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_onboarding_video.*
import vision.eyn.liveproof_sampleapp.R
import kotlin.math.min

class OnboardingFirstActivity : FragmentActivity() {

    var mMediaPlayer: MediaPlayer? = null

    var areaWidth = 0
    var areaHeight = 0
    var videoWidth = 0
    var videoHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_video)
        val holder = surface_view.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                mMediaPlayer?.apply {
                    setDisplay(holder)
                    isLooping = true
                    start()
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                mMediaPlayer?.apply {
                    setDisplay(holder)
                    isLooping = true
                    start()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })

        val vto = container_view.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                container_view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                areaHeight = container_view.measuredHeight
                areaWidth = container_view.measuredWidth
                updateVideoScreen()
            }
        })
        button_next.setOnClickListener {
            val intent = OnboardingSecondActivity.createIntent(this)
            startActivity(intent)
        }
    }

    override fun onResume() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.instructions).apply {
            setOnVideoSizeChangedListener(this@OnboardingFirstActivity::setFitToFillAspectRatio)
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (null != mMediaPlayer) {
            mMediaPlayer!!.release()
        }
        mMediaPlayer = null
    }

    private fun setFitToFillAspectRatio(mp: MediaPlayer, videoWidth: Int, videoHeight: Int) {
        this.videoWidth = videoWidth
        this.videoHeight = videoHeight
        updateVideoScreen()
    }

    fun updateVideoScreen() {
        if (mMediaPlayer != null && videoHeight > 0 && videoWidth > 0 && areaHeight > 0 && areaWidth > 0) {
            val scaleX = areaWidth.toFloat() / videoWidth.toFloat()
            val scaleY = areaHeight.toFloat() / videoHeight.toFloat()
            val scale = min(scaleX, scaleY)
            val videoParams = surface_view.layoutParams.apply {
                width = (videoWidth * scale).toInt()
                height = (videoHeight * scale).toInt()
            }
            surface_view.layoutParams = videoParams
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, OnboardingFirstActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
}