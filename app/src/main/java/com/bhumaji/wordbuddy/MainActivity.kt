package com.bhumaji.wordbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bhumaji.wordbuddy.data.ProfileStore
import com.bhumaji.wordbuddy.data.ProgressStore
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Listener
import com.bhumaji.wordbuddy.speech.SoundFx
import com.bhumaji.wordbuddy.speech.Speaker
import com.bhumaji.wordbuddy.ui.AppServices
import com.bhumaji.wordbuddy.ui.WordBuddyApp
import com.bhumaji.wordbuddy.ui.WordBuddyTheme

class MainActivity : ComponentActivity() {
    private lateinit var app: AppServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ProgressStore(this)
        WordBank.includeEasy = store.includeEasy
        app = AppServices(
            speaker = Speaker(this).also { it.slow = store.slowVoice },
            listener = Listener(this),
            store = store,
            profile = ProfileStore(this),
            sfx = SoundFx(this),
        )

        setContent {
            WordBuddyTheme {
                WordBuddyApp(app)
            }
        }
    }

    override fun onPause() {
        // Stop talking/listening when the app goes to the background.
        app.listener.stop()
        app.speaker.stop()
        super.onPause()
    }

    override fun onDestroy() {
        app.listener.stop()
        app.speaker.shutdown()
        app.sfx.release()
        super.onDestroy()
    }
}
