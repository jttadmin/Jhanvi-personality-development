package com.bhumaji.wordbuddy.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bhumaji.wordbuddy.data.Alphabet
import com.bhumaji.wordbuddy.data.Syllables
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Match
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pronunciation Palace "Word Coach" with Panda. For each word:
 * picture → slow → normal → clap the syllables → beginning sound → sentence →
 * "Can you say …?" → listen → friendly feedback. Unlimited replays, never a penalty,
 * and an Indian English accent is fine (the check only looks for an understandable word).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordCoachScreen(app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    val store = app.store
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val words = remember { store.practiceOrder(WordBank.visibleWords) }
    var index by remember { mutableIntStateOf(0) }
    val word = words[index % words.size]
    val parts = remember(word) { Syllables.split(word.text) }
    val firstSound = remember(word) { Alphabet.firstSound(word.text) }

    var mood by remember { mutableStateOf(Mood.TALK) }
    var message by remember { mutableStateOf("") }
    var activeSyllable by remember { mutableIntStateOf(-1) }
    var showSound by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var heard by remember { mutableStateOf<String?>(null) }
    var confetti by remember { mutableIntStateOf(0) }
    var tries by remember { mutableIntStateOf(0) }
    var job by remember { mutableStateOf<Job?>(null) }

    val canRecognize = remember { app.listener.isAvailable }
    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }

    DisposableEffect(Unit) {
        onDispose {
            app.listener.stop()
            speaker.stop()
        }
    }

    /** Run one teaching step, cancelling whatever was playing. */
    fun play(block: suspend () -> Unit) {
        job?.cancel()
        app.listener.stop()
        listening = false
        job = scope.launch {
            block()
            activeSyllable = -1
            if (mood == Mood.TALK) mood = Mood.IDLE
        }
    }

    suspend fun clap() {
        mood = Mood.TALK
        message = parts.joinToString(" - ")
        parts.forEachIndexed { i, p ->
            activeSyllable = i
            app.sfx.pop()
            speaker.sayAndWait(p)
            delay(150)
        }
        activeSyllable = -1
    }

    suspend fun beginning() {
        mood = Mood.TALK
        showSound = true
        message = "${word.text.first().uppercaseChar()} says “$firstSound”"
        speaker.sayAndWait("${word.text} starts with", "$firstSound.", "$firstSound… ${word.text}.")
    }

    fun listen() {
        if (!canRecognize) {
            message = "Say it out loud!"
            return
        }
        if (!hasMic) {
            askMic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        job?.cancel()
        speaker.stop()
        listening = true
        heard = null
        mood = Mood.LISTEN
        message = "I'm listening… 👂"
        app.listener.listen(
            onLevel = {},
            onHeard = { results ->
                listening = false
                heard = results.firstOrNull()
                tries++
                when (Match.check(word.targets, results)) {
                    Match.Result.GREAT -> {
                        store.recordSay(word, success = true)
                        val newSticker = store.addStars(5)
                        mood = Mood.CHEER
                        confetti++
                        if (newSticker) app.sfx.tada() else app.sfx.chime()
                        message = "Wonderful! +5 🪙"
                        play {
                            speaker.sayAndWait("Wonderful!", "Let's say it one more time together.")
                            speaker.sayAndWait(word.text)
                            if (newSticker) speaker.sayAndWait("You got a new sticker!")
                        }
                    }

                    Match.Result.CLOSE -> {
                        store.recordSay(word, success = false)
                        store.addStars(1)
                        message = "So close! +1 🪙"
                        play {
                            mood = Mood.TALK
                            speaker.sayAndWait("So close!", "Let's clap it.")
                            clap()
                            speaker.sayAndWait(word.text, "Now you!")
                            listen()
                        }
                    }

                    Match.Result.MISS -> {
                        store.recordSay(word, success = false)
                        store.addStars(1)
                        message = "Good try! +1 🪙"
                        play {
                            mood = Mood.TALK
                            speaker.sayAndWait("Good try!", "Listen slowly.")
                            speaker.sayAndWait(word.text, extraSlow = true)
                            if (tries < 3) {
                                speaker.sayAndWait("Your turn!")
                                listen()
                            } else {
                                speaker.sayAndWait("Great practice! Tap the arrow for a new word.")
                            }
                        }
                    }
                }
            },
            onFail = {
                listening = false
                mood = Mood.IDLE
                message = "I couldn't hear clearly. Tap 🎤 to try again!"
            },
        )
    }

    // The full lesson for each new word
    LaunchedEffect(word, speaker.ready) {
        if (!speaker.ready) return@LaunchedEffect
        tries = 0
        heard = null
        showSound = false
        play {
            mood = Mood.TALK
            if (store.markHeard(word)) store.addStars(5)
            message = "Listen…"
            speaker.sayAndWait("This is", word.text, extraSlow = true)
            speaker.sayAndWait(word.text)
            if (parts.size > 1) clap()
            beginning()
            message = word.sentence
            speaker.sayAndWait(word.sentence)
            message = "Can you say ${word.text}?"
            speaker.sayAndWait("Can you say", "${word.text}?")
            if (canRecognize && hasMic) {
                listen()
            } else {
                mood = Mood.IDLE
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar("Word Coach", onBack, store.stars)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeacherAvatar(Teacher.PANDA, mood, 84.dp, accessory = store.costumeEmoji)
                SpeechBubble(message.ifBlank { "Let's practise!" }, Modifier.weight(1f))
            }

            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val pic = minOf(maxWidth, maxHeight) * 0.42f
                Column(
                    Modifier
                        .fillMaxSize()
                        .shadow(8.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    WordPicture(word, pic)
                    Text(
                        word.text.uppercase(),
                        fontSize = if (word.text.length > 8) 36.sp else 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Purple,
                        textAlign = TextAlign.Center,
                    )
                    if (parts.size > 1) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            parts.forEachIndexed { i, p ->
                                Text(
                                    p.uppercase(),
                                    Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (i == activeSyllable) Sun else Color(0xFFEDE7F6))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = if (i == activeSyllable) 26.sp else 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Ink,
                                )
                            }
                        }
                    }
                    if (showSound) {
                        Text(
                            "Starts with “$firstSound”",
                            Modifier.padding(top = 6.dp),
                            fontSize = 18.sp,
                            color = Coral,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    heard?.let { Text("I heard: “$it”", fontSize = 15.sp, color = Soft) }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RoundButton("🐢", Sun, { play { speaker.sayAndWait(word.text, extraSlow = true) } }, size = 64.dp)
                RoundButton("🔊", Sky, { play { speaker.sayAndWait(word.text) } }, size = 64.dp)
                RoundButton("👏", Color(0xFFFFCC80), { play { clap() } }, size = 64.dp)
                RoundButton("🔤", Color(0xFFCE93D8), { play { beginning() } }, size = 64.dp)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canRecognize) {
                    RoundButton("🎤", if (listening) Coral else Purple, { listen() }, size = 96.dp, enabled = !listening)
                }
                RoundButton("➡️", Color.White, {
                    app.sfx.pop()
                    job?.cancel()
                    app.listener.stop()
                    index++
                })
            }
        }
        Confetti(confetti)
    }
}
