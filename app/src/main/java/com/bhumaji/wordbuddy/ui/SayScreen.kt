package com.bhumaji.wordbuddy.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Listener
import com.bhumaji.wordbuddy.speech.Match
import kotlinx.coroutines.delay

private enum class Phase { WAITING, PROMPTING, LISTENING, RESULT }

/**
 * "Say It!" — her buddy says a word, she repeats it, the phone's speech
 * recogniser checks it. Always encouraging: close tries get a star and a
 * replay; after 3 tries it moves on with "good trying!".
 * If the phone has no speech recogniser, a grown-up taps 👍 instead.
 */
@Composable
fun SayScreen(category: String, app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    val listener = app.listener
    val store = app.store
    val context = LocalContext.current
    val words = remember(category) { store.practiceOrder(WordBank.inCategory(category)) }
    var index by remember { mutableIntStateOf(0) }
    val word = words[index % words.size]

    var phase by remember { mutableStateOf(Phase.WAITING) }
    var mood by remember { mutableStateOf(Mood.IDLE) }
    var level by remember { mutableFloatStateOf(0f) }
    var heard by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var starsWon by remember { mutableIntStateOf(0) }
    var tries by remember { mutableIntStateOf(0) }
    var relisten by remember { mutableIntStateOf(0) }
    var advance by remember { mutableIntStateOf(0) }
    var confetti by remember { mutableIntStateOf(0) }

    val canRecognize = remember { listener.isAvailable }
    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMic = granted
        if (granted) {
            relisten++
        } else {
            message = "A grown-up needs to allow the microphone."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            listener.stop()
            speaker.stop()
        }
    }

    fun celebrate(stars: Int, extra: String) {
        val p = app.cheer()
        val newSticker = store.addStars(stars)
        starsWon = stars
        message = "$p ⭐⭐⭐"
        mood = Mood.CHEER
        phase = Phase.RESULT
        confetti++
        if (newSticker) app.sfx.tada() else app.sfx.chime()
        val lines = listOf(p, extra, if (newSticker) "You got a new sticker!" else "").filter { it.isNotBlank() }
        speaker.say(*lines.toTypedArray(), onDone = { advance++ })
    }

    fun moveOn(spoken: String) {
        phase = Phase.RESULT
        mood = Mood.TALK
        speaker.say(spoken, onDone = { advance++ })
    }

    fun handle(results: List<String>) {
        level = 0f
        heard = results.firstOrNull()
        tries++
        when (Match.check(word.targets, results)) {
            Match.Result.GREAT -> {
                store.recordSay(word, success = true)
                celebrate(3, "You said ${word.text}!")
            }

            Match.Result.CLOSE -> {
                starsWon = maxOf(starsWon, 1)
                store.recordSay(word, success = false)
                store.addStars(1)
                app.sfx.pop()
                if (tries >= 3) {
                    message = "Nice try! ⭐"
                    moveOn("Nice try! Let's try a new word.")
                } else {
                    message = "Almost! Listen again… ⭐"
                    phase = Phase.RESULT
                    mood = Mood.TALK
                    speaker.say("Almost!", "Listen.", word.text, "Now you!", onDone = { relisten++ })
                }
            }

            Match.Result.MISS -> {
                store.recordSay(word, success = false)
                app.sfx.boing()
                mood = Mood.OOPS
                if (tries >= 3) {
                    message = "Good trying! 💪"
                    moveOn("Good trying! Let's try a new word.")
                } else {
                    message = "Let's try again!"
                    phase = Phase.RESULT
                    speaker.say("Let's try again.", "Listen.", word.text, onDone = { relisten++ })
                }
            }
        }
    }

    fun startListening() {
        if (!canRecognize) {
            phase = Phase.WAITING
            mood = Mood.IDLE
            return
        }
        if (!hasMic) {
            askMic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        speaker.stop()
        heard = null
        message = "I'm listening… 👂"
        phase = Phase.LISTENING
        mood = Mood.LISTEN
        listener.listen(
            onLevel = { level = it },
            onHeard = { handle(it) },
            onFail = { code ->
                level = 0f
                phase = Phase.WAITING
                mood = Mood.IDLE
                when (code) {
                    Listener.ERROR_NO_MATCH, Listener.ERROR_SPEECH_TIMEOUT -> {
                        message = "I didn't hear you. Tap the 🎤!"
                        speaker.say("I didn't hear you.", "Say", word.text)
                    }

                    Listener.ERROR_PERMISSION -> {
                        hasMic = false
                        message = "A grown-up needs to allow the microphone."
                    }

                    else -> {
                        message = "Tap the 🎤 to try again."
                    }
                }
            },
        )
    }

    fun prompt() {
        listener.stop()
        phase = Phase.PROMPTING
        mood = Mood.TALK
        heard = null
        starsWon = 0
        tries = 0
        message = "Can you say…"
        speaker.say("Can you say", word.text, onDone = {
            if (canRecognize && hasMic) {
                startListening()
            } else {
                phase = Phase.WAITING
                mood = Mood.IDLE
                message = if (canRecognize) "Tap the 🎤 and say it!" else "Say it out loud!"
            }
        })
    }

    LaunchedEffect(index, speaker.ready) { if (speaker.ready) prompt() }
    LaunchedEffect(relisten) { if (relisten > 0) startListening() }
    LaunchedEffect(advance) {
        if (advance > 0) {
            delay(700)
            index++
        }
    }

    val micScale by animateFloatAsState(
        if (phase == Phase.LISTENING) 1.05f + level.coerceIn(0f, 10f) / 25f else 1f,
        label = "mic",
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(WordBank.category(category).title, onBack, store.stars)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BuddyAvatar(app.profile.photo(index), mood, 84.dp)
                SpeechBubble(message.ifBlank { "Let's talk!" }, Modifier.weight(1f))
            }

            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                val pictureSize = minOf(maxWidth, maxHeight) * 0.5f
                Column(
                    Modifier
                        .fillMaxSize()
                        .shadow(8.dp, RoundedCornerShape(36.dp))
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White)
                        .clickable { speaker.say(word.text) }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    WordPicture(word, pictureSize)
                    WordText(word.text)
                    Text(
                        (1..3).joinToString(" ") { if (it <= starsWon) "⭐" else "☆" },
                        fontSize = 34.sp,
                        color = if (starsWon > 0) Ink else Color(0x33000000),
                    )
                    heard?.let {
                        Text(
                            "I heard: “$it”",
                            fontSize = 16.sp,
                            color = Soft,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            if (!canRecognize) {
                Text(
                    "Grown-up: tap 👍 when she says it",
                    Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Soft,
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton("🐢", Sun, { speaker.say(word.text, extraSlow = true) })
                if (canRecognize) {
                    RoundButton(
                        "🎤",
                        if (phase == Phase.LISTENING) Coral else Purple,
                        { startListening() },
                        modifier = Modifier.scale(micScale),
                        size = 100.dp,
                        enabled = phase != Phase.LISTENING,
                    )
                } else {
                    RoundButton(
                        "👍",
                        Grass,
                        {
                            if (phase != Phase.RESULT) {
                                store.recordSay(word, success = true)
                                celebrate(3, "")
                            }
                        },
                        size = 100.dp,
                    )
                }
                RoundButton("➡️", Color.White, {
                    listener.stop()
                    app.sfx.pop()
                    index++
                })
            }
        }
        Confetti(confetti)
    }
}
