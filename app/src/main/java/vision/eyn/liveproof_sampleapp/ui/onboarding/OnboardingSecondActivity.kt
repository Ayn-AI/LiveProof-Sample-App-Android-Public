package vision.eyn.liveproof_sampleapp.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_onboarding_textual.*
import vision.eyn.liveproof_sampleapp.R
import vision.eyn.liveproof_sampleapp.ui.capture.CaptureActivity

class OnboardingSecondActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_textual)

        button_next.setOnClickListener {
            val intent = CaptureActivity.createIntent(this)
            startActivity(intent)
        }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, OnboardingSecondActivity::class.java)
    }
}