package com.bhumaji.wordbuddy.speech

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import java.util.concurrent.ConcurrentHashMap

/**
 * Wraps the phone's built-in Text-to-Speech engine (works offline once the
 * English voice is installed). Speaks a few phrases with short pauses and
 * can call back on the main thread when finished.
 */
class Speaker(context: Context) {
    private val main = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, () -> Unit>()
    private var pending: (() -> Unit)? = null
    private var counter = 0

    var ready by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
        private set

    /** Parent setting: speak a bit slower everywhere. */
    var slow = false

    private val tts: TextToSpeech =
        TextToSpeech(context.applicationContext) { status -> main.post { onInit(status) } }

    private fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            failed = true
            return
        }
        // Indian English voice if installed, otherwise US English.
        val r = tts.setLanguage(Locale("en", "IN"))
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.US)
        }
        tts.setPitch(1.1f)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = finish(utteranceId)

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finish(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)

            // Interrupted by a newer phrase: drop the callback, don't run it.
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                if (utteranceId != null) callbacks.remove(utteranceId)
            }
        })
        ready = true
        pending?.invoke()
        pending = null
    }

    private fun finish(id: String?) {
        if (id == null) return
        callbacks.remove(id)?.let { main.post(it) }
    }

    /**
     * Speak [parts] one after another with a short pause between them.
     * Replaces whatever is currently being spoken.
     */
    fun say(vararg parts: String, extraSlow: Boolean = false, onDone: (() -> Unit)? = null) {
        if (!ready) {
            if (failed) {
                onDone?.let { main.post(it) }
            } else {
                pending = { say(*parts, extraSlow = extraSlow, onDone = onDone) }
            }
            return
        }
        callbacks.clear()
        tts.setSpeechRate(
            when {
                extraSlow -> 0.5f
                slow -> 0.7f
                else -> 0.9f
            }
        )
        parts.forEachIndexed { i, text ->
            val id = "u${counter++}"
            if (i == parts.lastIndex && onDone != null) callbacks[id] = onDone
            if (i > 0) tts.playSilentUtterance(350, TextToSpeech.QUEUE_ADD, "s$id")
            tts.speak(text, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, id)
        }
    }

    /** Speak and wait until finished (gives up after 20 s so a game never gets stuck). */
    suspend fun sayAndWait(vararg parts: String, extraSlow: Boolean = false) {
        withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine<Unit> { cont ->
                say(*parts, extraSlow = extraSlow, onDone = { if (cont.isActive) cont.resume(Unit) })
            }
        }
    }

    fun stop() {
        callbacks.clear()
        pending = null
        if (ready) tts.stop()
    }

    fun shutdown() {
        stop()
        tts.shutdown()
    }
}
