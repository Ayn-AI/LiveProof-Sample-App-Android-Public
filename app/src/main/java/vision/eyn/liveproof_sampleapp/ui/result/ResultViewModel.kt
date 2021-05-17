package vision.eyn.liveproof_sampleapp.ui.result

import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import retrofit2.HttpException
import vision.eyn.liveproof_sampleapp.api.LivenessApi
import vision.eyn.liveproof_sampleapp.api.LivenessRequest
import vision.eyn.liveproof_sampleapp.api.LivenessResponse
import vision.eyn.liveproof_sampleapp.api.NetworkManager
import vision.eyn.liveproof_sampleapp.utils.SingleLiveEvent
import vision.eyn.liveproof_sdk.model.LiveproofCaptureSession
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.zip.DataFormatException
import java.util.zip.Deflater

class ResultViewModel() : ViewModel() {

    val api: LivenessApi by lazy {
        NetworkManager.getInstance().livenessApi
    }

    private lateinit var session: LiveproofCaptureSession

    val navEvents = SingleLiveEvent<NavEvent>()

    sealed class NavEvent {
        data class NavigateSubmissionSuccessful(val response: LivenessResponse) : NavEvent()
        data class NavigateSubmissionFailed(val message: String) : NavEvent()
        object NavigateFinish : NavEvent()
    }

    private val _errorsReported = MutableLiveData<String>()
    val errorsReported: LiveData<String>
        get() = _errorsReported

    private val _isOperationInProgress = MutableLiveData(false)
    val isOperationInProgress: LiveData<Boolean>
        get() = _isOperationInProgress

    fun onPageLoaded(session: LiveproofCaptureSession) {
        this.session = session
        viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            navEvents.postValue(NavEvent.NavigateSubmissionFailed(""))
        }) {
            submitLiveproofSession()
        }
    }

    private suspend fun submitLiveproofSession() = coroutineScope {
        _isOperationInProgress.postValue(true)
        val livenessResponse = try {
            sendCaptureSession(session)
        } catch (error: Throwable) {
            _isOperationInProgress.postValue(false)
            val bodyContent = (error as HttpException).response()?.errorBody()?.charStream()?.readText()
            val message = when {
                bodyContent?.isNotEmpty() == true -> bodyContent
                error.message.toString().isNotEmpty() -> error.message.toString()
                else -> ""
            }
            _errorsReported.postValue("Submission failed: ${message}")
            navEvents.postValue(NavEvent.NavigateSubmissionFailed(message))
            return@coroutineScope
        }
        _isOperationInProgress.postValue(false)
        Log.d(TAG, "Liveness record ID: ${livenessResponse.recordId}")
        navEvents.postValue(NavEvent.NavigateSubmissionSuccessful(livenessResponse))
    }

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun sendCaptureSession(session: LiveproofCaptureSession): LivenessResponse {
        val audioBytes = File(session.recordedAudioFilePath).readBytes()
        val compressedAudio = Base64.encodeToString(
            audioBytes.packZLib(),
            Base64.DEFAULT
        )
//        val uncompressedAudio = Base64.encodeToString(
//            audioBytes,
//            Base64.DEFAULT
//        )
//        val encodedVideo = Base64.encodeToString(
//            File(session.recordedVideoFilePath).readBytes(),
//        )
        val deviceModel = getDeviceName()
        val request = LivenessRequest(
            audioBase64 = compressedAudio,
            isMovementBased = session.isMovementBased,
            device = deviceModel
        )
        return api.postLivenessRequest(request)
    }

    private fun ByteArray.packZLib(): ByteArray? = try {
        val deflater = Deflater()
        deflater.setInput(this, 0, this.size)
        val outputStream = ByteArrayOutputStream(this.size)
        deflater.finish()
        val buffer = ByteArray(4048)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer) // returns the generated code... index
            outputStream.write(buffer, 0, count)
        }
        outputStream.close()
        val output: ByteArray = outputStream.toByteArray()
        output
    } catch (ex: UnsupportedEncodingException) {
        // handle
        null
    } catch (ex: DataFormatException) {
        // handle
        null
    }

    /** Returns the consumer friendly device name  */
    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val locale = Locale.getDefault()
        val capitalizedModel =
            model.split(" ").joinToString(" ") { it.capitalize(locale) }
        return if (model.toLowerCase(locale).startsWith(manufacturer.toLowerCase(locale))) {
            capitalizedModel
        } else "$manufacturer $capitalizedModel"
    }

    fun onNextClicked() {
        navEvents.postValue(NavEvent.NavigateFinish)
    }

    companion object {
        private const val TAG = "RESULT"
    }
}