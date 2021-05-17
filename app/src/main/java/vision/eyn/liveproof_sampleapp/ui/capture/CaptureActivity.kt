package vision.eyn.liveproof_sampleapp.ui.capture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vision.eyn.liveproof_sampleapp.R
import vision.eyn.liveproof_sampleapp.ui.result.ResultActivity
import vision.eyn.liveproof_sdk.model.LiveproofCaptureSession
import vision.eyn.liveproof_sdk.ui.*

class CaptureActivity : FragmentActivity(), LiveproofCaptureListener {

    companion object {
        private const val OPTIONAL_ANALYTICS_TAG = "user@email.com"

        fun createIntent(context: Context) = Intent(context, CaptureActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }

    private lateinit var viewModel: CaptureViewModel
    private var stimulus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        viewModel = ViewModelProvider(this).get(CaptureViewModel::class.java)
        initViewModelObservers()
        viewModel.onPageLoaded()
    }

    private fun initViewModelObservers() {
        viewModel.navEvents.observe(this, {
            when (it) {
                is CaptureViewModel.NavEvent.NavigateLoadingSuccessful -> onStimulusLoaded(it.stimulus)
                is CaptureViewModel.NavEvent.NavigateLoadingFailed -> onError()
            }
        })
        viewModel.errorsReported.observe(this, { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onStimulusLoaded(stimulus: String) {
        this.stimulus = stimulus
        showFragment()
    }

    private fun onError() {
    }

    private fun setFragment(fragment: Fragment) {
        val fragTrans = supportFragmentManager.beginTransaction()
        fragTrans.replace(android.R.id.content, fragment)
        fragTrans.commit()
    }

    private fun showFragment() {
        val cameraFragment = LiveproofCaptureFragment().apply {
            setLiveproofCaptureListener(this@CaptureActivity)
        }
        setFragment(cameraFragment)
    }

    override fun onGetStimulus(): String? = stimulus

    override fun onCaptureSuccess(session: LiveproofCaptureSession) {
        lifecycleScope.launch(Dispatchers.Main) {
            val sessionWithAnalytics = session.copy(tag = OPTIONAL_ANALYTICS_TAG)
            val intent = ResultActivity.createIntent(this@CaptureActivity, sessionWithAnalytics)
            startActivity(intent)
        }
    }

    override fun onCaptureFail(error: CaptureError) {
        when (error) {
            CaptureError.FaceOffScreen -> showInfoDialog(
                getString(R.string.face_lost_message),
                getString(R.string.retry_button)
            ) { showFragment() }
            CaptureError.FaceExcessiveTilt -> showInfoDialog(
                getString(R.string.face_tilted_message),
                getString(R.string.retry_button)
            ) { showFragment() }
            CaptureError.FaceNoMovement -> showFragment()
            is CaptureError.CameraConfigFailed -> showInfoDialog(
                getString(R.string.error_camera_config),
                getString(R.string.close_button)
            ) { finish() }
        }
    }

    private fun showInfoDialog(message: String, buttonTitle: String, onDismiss: () -> Unit) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setPositiveButton(buttonTitle) { _, _ -> onDismiss() }
        alertDialog.setOnDismissListener { onDismiss() }
        alertDialog.setMessage(message)
        alertDialog.show()
    }
}