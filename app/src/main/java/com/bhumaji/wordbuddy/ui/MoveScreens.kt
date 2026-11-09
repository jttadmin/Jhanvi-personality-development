package com.bhumaji.wordbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.DanceStyle
import com.bhumaji.wordbuddy.data.Motion
import com.bhumaji.wordbuddy.data.Moves
import com.bhumaji.wordbuddy.speech.DanceMusic
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class MovePhase { SHOW, GO, DONE }

/**
 * "Move Like Me": her buddy acts out a move ("Hop like a bunny!"), then she copies it.
 * Level 1 = one move, 2 = counting / slow / fast, 3 = remember 2–3 moves in order.
 */
@Composable
fun MoveScreen(app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    var level by remember { mutableIntStateOf(1) }
    var round by remember { mutableIntStateOf(0) }
    var replay by remember { mutableIntStateOf(0) }
    val move = remember(round, level) { Moves.next(level) }
    var phase by remember { mutableStateOf(MovePhase.SHOW) }
    var stepIndex by remember { mutableIntStateOf(0) }
    var countdown by remember { mutableIntStateOf(0) }
    var counted by remember { mutableIntStateOf(0) }
    var confetti by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) { onDispose { speaker.stop() } }

    LaunchedEffect(round, level, replay, speaker.ready) {
        if (!speaker.ready) return@LaunchedEffect
        phase = MovePhase.SHOW
        stepIndex = 0
        counted = 0
        countdown = 0
        speaker.sayAndWait(*move.spoken.toTypedArray())
        phase = MovePhase.GO
        if (move.countTo > 0) {
            val beat = (move.steps[0].motion.durationMs).coerceIn(900, 1600).toLong()
            for (n in 1..move.countTo) {
                counted = n
                app.sfx.pop()
                speaker.say(Moves.numberWord(n) + "!")
                delay(beat)
            }
        } else {
            move.steps.forEachIndexed { i, step ->
                stepIndex = i
                if (move.steps.size > 1) {
                    app.sfx.pop()
                    speaker.sayAndWait(if (i == 0) "First, ${step.label}!" else "Now ${step.label}!")
                }
                for (c in 5 downTo 1) {
                    countdown = c
                    delay(900)
                }
            }
        }
        countdown = 0
        phase = MovePhase.DONE
        confetti++
        val newSticker = app.store.addStars(2)
        if (newSticker) app.sfx.tada() else app.sfx.chime()
        val lines = mutableListOf(app.cheer(), move.praise)
        if (newSticker) lines.add("You got a new sticker!")
        speaker.sayAndWait(*lines.toTypedArray())
        delay(1500)
        round++
    }

    val step = move.steps.getOrElse(stepIndex) { move.steps.first() }
    val motion = when (phase) {
        MovePhase.DONE -> Motion.DANCE
        else -> step.motion
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar("Move Like Me", onBack, app.store.stars)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(1 to "Easy ⭐", 2 to "Count ⭐⭐", 3 to "Order ⭐⭐⭐").forEach { (lv, label) ->
                    Text(
                        label,
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (level == lv) Purple else Color.White)
                            .clickable {
                                app.sfx.pop()
                                level = lv
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        color = if (level == lv) Color.White else Ink,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                move.title,
                Modifier.padding(horizontal = 20.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple,
                textAlign = TextAlign.Center,
            )
            if (move.steps.size > 1) {
                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    move.steps.forEachIndexed { i, s ->
                        val active = phase == MovePhase.GO && i == stepIndex
                        Text(
                            "${i + 1}. ${s.emoji} ${s.label}",
                            Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (active) Sun else Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 18.sp,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal,
                        )
                    }
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MotionBox(motion, 210.dp, speed = step.speed) {
                        BuddyAvatar(
                            app.profile.photo(round),
                            if (phase == MovePhase.DONE) Mood.CHEER else Mood.IDLE,
                            140.dp,
                        )
                    }
                    MotionBox(motion, 120.dp, speed = step.speed) {
                        Text(step.emoji, fontSize = 72.sp)
                    }
                }
                val badge = when {
                    move.countTo > 0 && phase == MovePhase.GO -> "$counted"
                    countdown > 0 -> "$countdown"
                    else -> null
                }
                if (badge != null) {
                    Text(
                        badge,
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Coral)
                            .padding(horizontal = 22.dp, vertical = 6.dp),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
            }

            Text(
                when (phase) {
                    MovePhase.SHOW -> "Watch me…"
                    MovePhase.GO -> "Your turn! Move with me!"
                    MovePhase.DONE -> "Yay! 🎉"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Coral,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RoundButton("🔁", Sky, {
                    app.sfx.pop()
                    replay++
                })
                RoundButton("➡️", Color.White, {
                    app.sfx.pop()
                    round++
                })
            }
        }
        Confetti(confetti)
    }
}

/**
 * Freeze Dance: music plays and her buddy dances; when the music stops, FREEZE!
 * Then a new way to dance: slowly, on tiptoes, like a robot…
 */
@Composable
fun FreezeScreen(app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    val context = LocalContext.current
    val music = remember { DanceMusic(context) }
    var session by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var frozen by remember { mutableStateOf(false) }
    var style by remember { mutableStateOf(DanceStyle.NORMAL) }
    var freezes by remember { mutableIntStateOf(0) }
    var confetti by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            music.release()
            speaker.stop()
        }
    }

    LaunchedEffect(session) {
        if (session == 0) return@LaunchedEffect
        try {
            style = DanceStyle.NORMAL
            frozen = false
            speaker.sayAndWait("Let's dance!", "When the music stops, freeze like a statue!")
            while (true) {
                frozen = false
                music.play(style.speed)
                delay(Random.nextLong(4000, 9000))
                music.pause()
                frozen = true
                app.sfx.pop()
                speaker.say("Freeze!")
                delay(3000)
                freezes++
                confetti++
                val newSticker = app.store.addStars(1)
                if (newSticker) app.sfx.tada()
                val next = DanceStyle.entries.filter { it != style }.random()
                style = next
                val lines = mutableListOf(app.cheer())
                if (newSticker) lines.add("You got a new sticker!")
                lines.add(if (next.instruction.isBlank()) "Now dance!" else "Now dance ${next.instruction}!")
                speaker.sayAndWait(*lines.toTypedArray())
            }
        } finally {
            music.pause()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar("Freeze Dance", onBack, app.store.stars)
            Text(
                when {
                    !running -> "Press ▶ to start the music!"
                    frozen -> "❄️ FREEZE! ❄️"
                    style.instruction.isBlank() -> "💃 Dance!"
                    else -> "💃 Dance ${style.instruction}!"
                },
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (frozen) Sky else Purple,
                textAlign = TextAlign.Center,
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                MotionBox(
                    if (running && !frozen) style.motion else Motion.NONE,
                    240.dp,
                    speed = style.speed,
                ) {
                    BuddyAvatar(app.profile.photo(freezes), Mood.IDLE, 170.dp)
                }
                if (frozen) Text("🧊", Modifier.align(Alignment.TopEnd).padding(24.dp), fontSize = 64.sp)
            }
            Text("Freezes: $freezes", fontSize = 18.sp, color = Soft)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                RoundButton(
                    if (running) "⏹️" else "▶️",
                    if (running) Coral else Grass,
                    {
                        app.sfx.pop()
                        if (running) {
                            running = false
                            frozen = false
                            session = 0
                            music.pause()
                            speaker.say("Great dancing!")
                        } else {
                            running = true
                            session++
                        }
                    },
                    size = 100.dp,
                )
            }
        }
        Confetti(confetti)
    }
}
