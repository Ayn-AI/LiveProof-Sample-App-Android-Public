package vision.eyn.liveproof_sampleapp.ui.result

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_result.*
import vision.eyn.liveproof_sdk.model.LiveproofCaptureSession
import vision.eyn.liveproof_sampleapp.LiveproofApp.Companion.EXTRA_SESSION
import vision.eyn.liveproof_sampleapp.R
import vision.eyn.liveproof_sampleapp.api.LivenessResponse
import vision.eyn.liveproof_sampleapp.ui.capture.CaptureActivity


class ResultActivity : FragmentActivity() {

    private lateinit var viewModel: ResultViewModel

    companion object {
        fun createIntent(context: Context, session: LiveproofCaptureSession) =
            Intent(context, ResultActivity::class.java).apply { putExtra(EXTRA_SESSION, session) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session: LiveproofCaptureSession? = intent.getParcelableExtra(EXTRA_SESSION)
        if (session == null) {
            finish()
            return
        }
        setContentView(R.layout.activity_result)
        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)
        initViewModelObservers()
        viewModel.onPageLoaded(session)
    }

    private fun initViewModelObservers() {
        finish_button.setOnClickListener {
            viewModel.onNextClicked()
        }
        viewModel.navEvents.observe(this, {
            when (it) {
                is ResultViewModel.NavEvent.NavigateFinish -> closeCaptureAndRestart()
                is ResultViewModel.NavEvent.NavigateSubmissionSuccessful -> onSubmissionSuccess(it.response)
                is ResultViewModel.NavEvent.NavigateSubmissionFailed -> onSubmissionFail(it.message)
            }
        })
        viewModel.errorsReported.observe(this, { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })
        viewModel.isOperationInProgress.observe(this, { inProgress ->
            operation_progressbar.visibility = if (inProgress) View.VISIBLE else View.INVISIBLE
        })
    }

    private fun onSubmissionSuccess(response: LivenessResponse) {
        operation_status_text.setText(R.string.operation_success)
        results_layout.visibility = View.VISIBLE
        score_text.text = getString(R.string.score, response.score)
        result_text.text = getString(R.string.result, response.result)
        finish_button.visibility = View.VISIBLE
        unoptimized_label.visibility = if (response.isOptimized) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    private fun onSubmissionFail(message: String) {
        operation_status_text.text = getString(R.string.operation_fail, message)
        finish_button.visibility = View.VISIBLE
    }

    private fun closeCaptureAndRestart() {
        val intent = CaptureActivity.createIntent(this)
        startActivity(intent)
    }
}