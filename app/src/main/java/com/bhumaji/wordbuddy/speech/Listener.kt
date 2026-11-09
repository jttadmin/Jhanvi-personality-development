package com.bhumaji.wordbuddy.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Uses the phone's own speech recogniser (Google speech services).
 * It first asks for on-device recognition so audio stays on the phone; if the
 * offline English pack isn't installed it falls back to the default mode.
 * The app itself has no server and no INTERNET permission.
 */
class Listener(private val context: Context) {
    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(
        onLevel: (Float) -> Unit,
        onHeard: (List<String>) -> Unit,
        onFail: (Int) -> Unit,
    ) = start(preferOffline = true, onLevel, onHeard, onFail)

    private fun start(
        preferOffline: Boolean,
        onLevel: (Float) -> Unit,
        onHeard: (List<String>) -> Unit,
        onFail: (Int) -> Unit,
    ) {
        stop()
        val r = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) = onLevel(rmsdB)
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onError(error: Int) {
                release(r)
                // 12 = language not supported, 13 = language pack unavailable (offline)
                if (preferOffline && (error == 12 || error == 13)) {
                    start(preferOffline = false, onLevel, onHeard, onFail)
                } else {
                    onFail(error)
                }
            }

            override fun onResults(results: Bundle?) {
                release(r)
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                onHeard(list?.toList() ?: emptyList())
            }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            if (preferOffline) putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        r.startListening(intent)
    }

    /** Destroy a finished recogniser after the current callback returns. */
    private fun release(r: SpeechRecognizer) {
        if (recognizer === r) recognizer = null
        main.post { r.destroy() }
    }

    fun stop() {
        recognizer?.let {
            it.cancel()
            it.destroy()
        }
        recognizer = null
    }

    companion object {
        const val ERROR_SPEECH_TIMEOUT = 6
        const val ERROR_NO_MATCH = 7
        const val ERROR_PERMISSION = 9
    }
}
