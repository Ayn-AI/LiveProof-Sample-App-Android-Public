package vision.eyn.liveproof_sampleapp.ui.capture

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import vision.eyn.liveproof_sampleapp.api.LivenessApi
import vision.eyn.liveproof_sampleapp.api.NetworkManager
import vision.eyn.liveproof_sampleapp.utils.SingleLiveEvent
import java.util.*

class CaptureViewModel() : ViewModel() {

    val api: LivenessApi by lazy {
        NetworkManager.getInstance().livenessApi
    }

    val navEvents = SingleLiveEvent<NavEvent>()

    sealed class NavEvent {
        data class NavigateLoadingSuccessful(val stimulus: String) : NavEvent()
        object NavigateLoadingFailed : NavEvent()
    }

    private val _errorsReported = MutableLiveData<String>()
    val errorsReported: LiveData<String>
        get() = _errorsReported

    fun onPageLoaded() {
        viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            navEvents.postValue(NavEvent.NavigateLoadingFailed)
        }) {
            loadAudioFile()
        }
    }

    private suspend fun loadAudioFile() = coroutineScope {
        val stimulus = try {
            val response = api.getAudioFile(true)
            response.audioBase64
        } catch (error: Throwable) {
            Log.e(TAG, error.message.toString())
            _errorsReported.postValue("Submission failed: ${error.message.toString()}")
            return@coroutineScope
        }
        navEvents.postValue(NavEvent.NavigateLoadingSuccessful(stimulus))
    }

    companion object {
        private const val TAG = "CAPTURE"
    }
}