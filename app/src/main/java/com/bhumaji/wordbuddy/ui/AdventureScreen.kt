package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.Game
import com.bhumaji.wordbuddy.data.WordBank
import kotlin.math.roundToInt

/** One stop on today's adventure. [share] = part of the session time (spec: 3/4/3/3/2 of 15 min). */
enum class Station(val title: String, val emoji: String, val share: Float, val teacher: Teacher) {
    SOUNDS("Letters & Sounds", "🔤", 3f / 15f, Teacher.BUNNY),
    NEW_WORDS("New Words", "📚", 4f / 15f, Teacher.BUNNY),
    SAY("Say Them", "🐼", 3f / 15f, Teacher.PANDA),
    SENTENCES("Build Sentences", "🧱", 3f / 15f, Teacher.KITTY),
    STORY("Story & Treasure", "📖", 2f / 15f, Teacher.COCO),
}

/** Where a station leads. */
fun Station.target(): Screen = when (this) {
    Station.SOUNDS -> Screen.Quiz(Game.SOUNDS)
    Station.NEW_WORDS -> Screen.Play(Mode.LEARN, WordBank.TODAY)
    Station.SAY -> Screen.CoachToday
    Station.SENTENCES -> Screen.Builder
    Station.STORY -> Screen.Stories
}

/**
 * Daily Adventure: a short guided path through five stations, then a glowing
 * treasure chest. Length follows the grown-up's session setting. No streaks,
 * no penalty for missed days — every day is a fresh, friendly start.
 */
@Composable
fun AdventureScreen(app: AppServices, onBack: () -> Unit, onStation: (Station) -> Unit) {
    val store = app.store
    val version = store.version
    val done = remember(version) { store.adventureDone() }
    val next = Station.entries.firstOrNull { it.name !in done }
    var chestOpen by remember { mutableStateOf(false) }
    var confetti by remember { mutableIntStateOf(0) }
    val minutes = store.sessionMinutes
    val name = app.profile.name.trim()

    LaunchedEffect(done, app.speaker.ready) {
        if (!app.speaker.ready) return@LaunchedEffect
        when {
            next == null -> app.speaker.say("You finished today's adventure!", "Tap the treasure chest!")
            done.isEmpty() -> app.speaker.say(
                if (name.isBlank()) "Hello, little superstar!" else "Hello, $name!",
                "Are you ready for today's adventure?",
            )
            else -> app.speaker.say("Great job! Next stop:", next.title)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar("Today's Adventure", onBack, store.stars)
            Text(
                "About $minutes minutes · ${done.size} of ${Station.entries.size} done",
                color = Soft,
                fontWeight = FontWeight.Bold,
            )
            Station.entries.forEachIndexed { i, st ->
                val isDone = st.name in done
                val isNext = st == next
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .shadow(if (isNext) 10.dp else 3.dp, RoundedCornerShape(26.dp))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            when {
                                isDone -> Color(0xFFE8F5E9)
                                isNext -> Color.White
                                else -> Color(0xFFF5F5F5)
                            }
                        )
                        .border(if (isNext) 4.dp else 0.dp, if (isNext) Sun else Color.Transparent, RoundedCornerShape(26.dp))
                        .clickable {
                            app.sfx.pop()
                            app.speaker.say(st.title)
                            onStation(st)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TeacherAvatar(st.teacher, if (isNext) Mood.TALK else Mood.IDLE, 64.dp, accessory = store.costumeEmoji)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    ) {
                        Text("${i + 1}. ${st.emoji} ${st.title}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text(
                            "${(st.share * minutes).roundToInt().coerceAtLeast(1)} min",
                            fontSize = 14.sp,
                            color = Soft,
                        )
                    }
                    Text(if (isDone) "✅" else if (isNext) "▶️" else "⭕", fontSize = 30.sp)
                }
            }

            if (next == null) {
                TreasureChest(isOpen = chestOpen, modifier = Modifier.padding(16.dp)) {
                    if (!chestOpen) {
                        chestOpen = true
                        confetti++
                        val first = store.claimAdventureReward()
                        if (first) store.addStars(20)
                        app.sfx.tada()
                        app.speaker.say(
                            if (first) "Twenty coins!" else "You opened it!",
                            app.cheer(),
                            "You learned so much today.",
                            "Time for a little break. Drink some water and stretch!",
                        )
                    }
                }
                if (chestOpen) {
                    Text(
                        "🌿 Time for a break! See you tomorrow.",
                        Modifier.padding(16.dp),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Grass,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                RoundButton("▶️", Grass, {
                    app.sfx.pop()
                    onStation(next)
                }, Modifier.padding(20.dp), size = 100.dp)
            }
        }
        Confetti(confetti)
    }
}

/** A glowing treasure chest drawn in code; the lid pops open and coins jump out. */
@Composable
fun TreasureChest(isOpen: Boolean, modifier: Modifier = Modifier, onTap: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "chest")
    val glow by inf.animateFloat(
        0.5f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow",
    )
    val lid = remember { Animatable(0f) }
    LaunchedEffect(isOpen) {
        if (isOpen) lid.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 120f))
    }
    Box(
        modifier
            .size(220.dp)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(210.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFFFF59D).copy(alpha = glow), Color.Transparent)))
        )
        Canvas(Modifier.size(170.dp)) {
            val w = size.width
            val h = size.height
            val brown = Color(0xFF8D6E63)
            val dark = Color(0xFF5D4037)
            val gold = Color(0xFFFFCA28)
            // coins inside, visible when open
            if (lid.value > 0.2f) {
                listOf(0.35f, 0.5f, 0.65f).forEachIndexed { i, fx ->
                    drawCircle(gold, w * 0.07f, Offset(w * fx, h * (0.48f - 0.25f * lid.value * (1 + i % 2))))
                }
            }
            // body
            drawRoundRect(brown, Offset(w * 0.12f, h * 0.48f), Size(w * 0.76f, h * 0.42f), CornerRadius(w * 0.05f))
            drawRect(gold, Offset(w * 0.12f, h * 0.6f), Size(w * 0.76f, h * 0.06f))
            drawRect(dark, Offset(w * 0.46f, h * 0.48f), Size(w * 0.08f, h * 0.42f))
            drawCircle(gold, w * 0.06f, Offset(w * 0.5f, h * 0.58f))
            // lid, hinged at the back
            rotate(-35f * lid.value, Offset(w * 0.12f, h * 0.48f)) {
                drawRoundRect(brown, Offset(w * 0.1f, h * 0.28f), Size(w * 0.8f, h * 0.22f), CornerRadius(w * 0.08f))
                drawRect(gold, Offset(w * 0.1f, h * 0.36f), Size(w * 0.8f, h * 0.05f))
            }
        }
        if (!isOpen) Text("Tap me!", Modifier.align(Alignment.BottomCenter), fontWeight = FontWeight.ExtraBold, color = Coral)
    }
}

/** Friendly break reminder after the session length the grown-up chose. */
@Composable
fun BreakReminder(app: AppServices, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        app.speaker.say(
            "You worked so hard!",
            "Time for a little break.",
            "Drink some water, stretch your arms, and rest your eyes.",
        )
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TeacherAvatar(Teacher.PANDA, Mood.TALK, 120.dp, accessory = app.store.costumeEmoji)
            Text("Break time! 🌿", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Grass)
            Text(
                "💧 Drink water   🙆 Stretch   👀 Rest your eyes",
                Modifier.padding(vertical = 12.dp),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = Ink,
            )
            RoundButton("👍", Grass, {
                app.sfx.pop()
                onDismiss()
            }, size = 80.dp)
        }
    }
}
