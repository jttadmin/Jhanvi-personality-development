package com.bhumaji.wordbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bhumaji.wordbuddy.data.ProgressStore
import com.bhumaji.wordbuddy.speech.Listener
import com.bhumaji.wordbuddy.speech.Speaker
import com.bhumaji.wordbuddy.ui.WordBuddyApp
import com.bhumaji.wordbuddy.ui.WordBuddyTheme

class MainActivity : ComponentActivity() {
    private lateinit var speaker: Speaker
    private lateinit var listener: Listener
    private lateinit var store: ProgressStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProgressStore(this)
        speaker = Speaker(this).also { it.slow = store.slowVoice }
        listener = Listener(this)

        setContent {
            WordBuddyTheme {
                WordBuddyApp(speaker, listener, store)
            }
        }
    }

    override fun onPause() {
        // Stop talking/listening when the app goes to the background.
        listener.stop()
        speaker.stop()
        super.onPause()
    }

    override fun onDestroy() {
        listener.stop()
        speaker.shutdown()
        super.onDestroy()
    }
}
