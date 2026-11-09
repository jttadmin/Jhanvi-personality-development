package com.bhumaji.wordbuddy.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bhumaji.wordbuddy.data.Role
import com.bhumaji.wordbuddy.data.Sentences
import com.bhumaji.wordbuddy.speech.Match
import kotlinx.coroutines.launch
import kotlin.math.sin

private fun roleColor(r: Role): Color = when (r) {
    Role.WHO -> Color(0xFF42A5F5)
    Role.DOES -> Color(0xFFEF5350)
    Role.WHAT -> Color(0xFF66BB6A)
    Role.DESCRIBE -> Color(0xFFFFA726)
    Role.LINK -> Color(0xFF90A4AE)
    Role.ASK -> Color(0xFFAB47BC)
}

/**
 * Sentence City "Word Builder": tap word cards to put them in order
 * (I | like | apples), then say the whole sentence. Cards are coloured by
 * job: who, does, what, describe, little words, question words.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderScreen(app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var level by remember { mutableIntStateOf(1) }
    var round by remember { mutableIntStateOf(0) }
    val sentence = remember(round, level) { Sentences.pick(level) }
    val order = remember(round, level) { sentence.cards.indices.shuffled() }
    val placed = remember(round, level) { mutableStateListOf<Int>() }
    var solved by remember(round, level) { mutableStateOf(false) }
    var mood by remember { mutableStateOf(Mood.TALK) }
    var message by remember { mutableStateOf("") }
    var confetti by remember { mutableIntStateOf(0) }
    var listening by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }

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

    LaunchedEffect(round, level, speaker.ready) {
        if (!speaker.ready) return@LaunchedEffect
        mood = Mood.TALK
        message = "Put the words in order!"
        speaker.say("Let's build a sentence!", "Put the words in order.", onDone = { mood = Mood.IDLE })
    }

    fun check() {
        val built = placed.map { sentence.cards[it].text }
        val want = sentence.cards.map { it.text }
        if (built == want) {
            solved = true
            mood = Mood.CHEER
            confetti++
            val newSticker = app.store.addStars(10)
            if (newSticker) app.sfx.tada() else app.sfx.chime()
            message = "${app.cheer()} +10 🪙"
            val lines = mutableListOf(app.cheer(), sentence.spoken)
            if (newSticker) lines.add("You got a new sticker!")
            lines.add("Can you say the full sentence?")
            speaker.say(*lines.toTypedArray(), onDone = { mood = Mood.IDLE })
        } else {
            // keep the correct start, send the rest back
            val firstWrong = built.indices.first { built[it] != want[it] }
            while (placed.size > firstWrong) placed.removeAt(placed.lastIndex)
            mood = Mood.OOPS
            app.sfx.boing()
            message = "Almost! Try again."
            scope.launch {
                shake.snapTo(1f)
                shake.animateTo(0f, tween(500))
            }
            speaker.say("Almost!", "Listen.", sentence.spoken, onDone = { mood = Mood.IDLE })
        }
    }

    fun place(i: Int) {
        if (solved || i in placed) return
        app.sfx.pop()
        speaker.say(sentence.cards[i].text)
        placed.add(i)
        if (placed.size == sentence.cards.size) check()
    }

    fun unplace(i: Int) {
        if (solved) return
        app.sfx.pop()
        val at = placed.indexOf(i)
        while (placed.size > at) placed.removeAt(placed.lastIndex)
    }

    fun sayIt() {
        if (!canRecognize) return
        if (!hasMic) {
            askMic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        speaker.stop()
        listening = true
        mood = Mood.LISTEN
        message = "I'm listening… 👂"
        app.listener.listen(
            onLevel = {},
            onHeard = { heard ->
                listening = false
                when (Match.checkSentence(sentence.spoken, heard)) {
                    Match.Result.GREAT -> {
                        mood = Mood.CHEER
                        confetti++
                        app.store.addStars(5)
                        app.sfx.chime()
                        message = "Super speaking! +5 🪙"
                        speaker.say("Wonderful!", "You said the whole sentence!")
                    }

                    else -> {
                        mood = Mood.TALK
                        message = "Let's say it together!"
                        speaker.say("Let's say it one more time together.", sentence.spoken, onDone = { mood = Mood.IDLE })
                    }
                }
            },
            onFail = {
                listening = false
                mood = Mood.IDLE
                message = "Tap 🎤 and say it!"
            },
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar("Word Builder", onBack, app.store.stars)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { lv ->
                    Text(
                        "$lv · ${Sentences.levelNames[lv]}",
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (level == lv) Purple else Color.White)
                            .clickable {
                                app.sfx.pop()
                                level = lv
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (level == lv) Color.White else Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeacherAvatar(Teacher.KITTY, mood, 90.dp, accessory = app.store.costumeEmoji)
                SpeechBubble(message.ifBlank { "Let's build!" }, Modifier.weight(1f))
            }

            Text(
                sentence.picture,
                Modifier
                    .shadow(6.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .clickable { speaker.say(sentence.spoken) }
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                fontSize = 80.sp,
            )

            // The sentence being built
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .graphicsLayer { translationX = sin(shake.value * 30f) * 24f * shake.value }
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(3.dp, if (solved) Grass else Color(0x22000000), RoundedCornerShape(24.dp))
                    .padding(12.dp)
                    .heightIn(min = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sentence.cards.indices.forEach { slot ->
                    val cardIndex = placed.getOrNull(slot)
                    if (cardIndex != null) {
                        val card = sentence.cards[cardIndex]
                        val text = if (slot == 0) card.text.replaceFirstChar { it.uppercase() } else card.text
                        WordCard(text + if (slot == sentence.cards.lastIndex) sentence.end else "", roleColor(card.role)) {
                            unplace(cardIndex)
                        }
                    } else {
                        Box(
                            Modifier
                                .widthIn(min = 64.dp)
                                .heightIn(min = 52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, Color(0x33000000), RoundedCornerShape(16.dp)),
                        )
                    }
                }
            }

            // Cards to choose from
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                order.forEach { i ->
                    if (i !in placed) {
                        val card = sentence.cards[i]
                        WordCard(card.text, roleColor(card.role), big = true) { place(i) }
                    }
                }
            }

            Spacer(Modifier.heightIn(min = 12.dp))
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Role.entries.forEach { r ->
                    Text(
                        r.label,
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(roleColor(r).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = roleColor(r),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton("🔊", Sky, { speaker.say(sentence.spoken) })
                if (solved && canRecognize) {
                    RoundButton("🎤", if (listening) Coral else Purple, { sayIt() }, size = 92.dp, enabled = !listening)
                }
                RoundButton("➡️", Color.White, {
                    app.sfx.pop()
                    app.listener.stop()
                    round++
                })
            }
        }
        Confetti(confetti)
    }
}

@Composable
private fun WordCard(text: String, color: Color, big: Boolean = false, onClick: () -> Unit) {
    Text(
        text,
        Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = if (big) 20.dp else 14.dp, vertical = if (big) 14.dp else 10.dp),
        fontSize = if (big) 26.sp else 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        textAlign = TextAlign.Center,
    )
}

