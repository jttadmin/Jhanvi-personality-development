package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.BgTheme
import com.bhumaji.wordbuddy.data.Game
import kotlin.math.sin

/** Something to do inside a world. */
sealed interface Activity {
    val title: String
    val emoji: String
    val subtitle: String

    data class Words(val mode: Mode, override val subtitle: String) : Activity {
        override val title get() = mode.title
        override val emoji get() = mode.emoji
    }

    data class Quiz(val game: Game) : Activity {
        override val title get() = game.title
        override val emoji get() = game.emoji
        override val subtitle get() = game.subtitle
    }

    data object MoveLikeMe : Activity {
        override val title = "Move Like Me"
        override val emoji = "🕺"
        override val subtitle = "Hop, stomp, spin!"
    }

    data object Coach : Activity {
        override val title = "Word Coach"
        override val emoji = "🐼"
        override val subtitle = "Slow, clap, sound, say!"
    }

    data object Abc : Activity {
        override val title = "ABC & Tracing"
        override val emoji = "✏️"
        override val subtitle = "Letters, sounds, writing"
    }

    data object Builder : Activity {
        override val title = "Word Builder"
        override val emoji = "🧱"
        override val subtitle = "Put words in order"
    }

    data object FreezeDance : Activity {
        override val title = "Freeze Dance"
        override val emoji = "🧊"
        override val subtitle = "Dance… and FREEZE!"
    }

    data class Soon(override val title: String, override val emoji: String) : Activity {
        override val subtitle get() = "Coming soon"
    }
}

/** The worlds on the adventure map, each with its teacher. */
enum class World(
    val title: String,
    val emoji: String,
    val teacher: Teacher,
    val color: Color,
    val greeting: String,
) {
    ALPHABET(
        "Alphabet Forest", "🌳", Teacher.BUNNY, Color(0xFF66BB6A),
        "Welcome to Alphabet Forest! Let's play with letters and sounds.",
    ),
    VOCAB(
        "Vocabulary Kingdom", "🏰", Teacher.BUNNY, Color(0xFF7E57C2),
        "Hello, little superstar! Are you ready to learn new words today?",
    ),
    PRONOUNCE(
        "Pronunciation Palace", "🎤", Teacher.PANDA, Color(0xFFEF5350),
        "Hello! I'm Panda. Let's say words clearly together!",
    ),
    SENTENCE(
        "Sentence City", "🏙️", Teacher.KITTY, Color(0xFF29B6F6),
        "Meow! I'm Kitty. Let's build sentences together!",
    ),
    STORY(
        "Story Kingdom", "📖", Teacher.COCO, Color(0xFFFFA726),
        "Hi, I'm Coco! Do you love stories? Me too!",
    ),
    CONVO(
        "Conversation Planet", "🪐", Teacher.COCO, Color(0xFF5C6BC0),
        "Hello! Let's talk together, just like friends!",
    ),
    PLAY(
        "Move & Play", "🎪", Teacher.KITTY, Color(0xFFEC407A),
        "Time to move your body! Ready, steady, go!",
    );

    val activities: List<Activity>
        get() = when (this) {
            ALPHABET -> listOf(Activity.Abc, Activity.Quiz(Game.SOUNDS))
            VOCAB -> listOf(
                Activity.Words(Mode.LEARN, "Look and listen"),
                Activity.Words(Mode.FIND, "Tap the picture"),
                Activity.Quiz(Game.OPPOSITES),
                Activity.Quiz(Game.THINK),
            )
            PRONOUNCE -> listOf(
                Activity.Coach,
                Activity.Words(Mode.SAY, "Say it like me"),
                Activity.Quiz(Game.SYLLABLES),
                Activity.Quiz(Game.RHYME),
            )
            SENTENCE -> listOf(
                Activity.Builder,
                Activity.Words(Mode.SENTENCE, "Say whole sentences"),
            )
            STORY -> listOf(Activity.Soon("Picture Stories", "📖"))
            CONVO -> listOf(Activity.Soon("Talk with Coco", "💬"))
            PLAY -> listOf(Activity.MoveLikeMe, Activity.FreezeDance)
        }

    val isOpen: Boolean get() = activities.any { it !is Activity.Soon }
}

/** Soft gradient with slowly floating decorations; changes with the background bought in the shop. */
@Composable
fun ThemedBackground(theme: BgTheme, content: @Composable () -> Unit) {
    val inf = rememberInfiniteTransition(label = "bg")
    val drift by inf.animateFloat(
        0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart), label = "drift",
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.colors))
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = with(LocalDensity.current) { maxWidth.toPx() }
            val h = with(LocalDensity.current) { maxHeight.toPx() }
            spots.forEachIndexed { i, (fx, fy) ->
                val phase = drift * 6.283f + i
                Text(
                    theme.decor[i % theme.decor.size],
                    Modifier
                        .offset {
                            IntOffset(
                                (fx * w + sin(phase) * 18f).toInt(),
                                (fy * h + sin(phase * 0.7f) * 24f).toInt(),
                            )
                        }
                        .alpha(0.28f),
                    fontSize = 30.sp,
                )
            }
        }
        content()
    }
}

private val spots = listOf(
    0.05f to 0.12f, 0.82f to 0.08f, 0.15f to 0.45f, 0.88f to 0.38f,
    0.45f to 0.7f, 0.08f to 0.85f, 0.75f to 0.9f, 0.55f to 0.25f,
)

/** Home: an adventure map with a winding path through the worlds. */
@Composable
fun MapScreen(
    app: AppServices,
    greeted: Boolean,
    onGreeted: () -> Unit,
    onWorld: (World) -> Unit,
    onShop: () -> Unit,
    onStickers: () -> Unit,
    onParent: () -> Unit,
) {
    val name = app.profile.name.trim()
    var talking by remember { mutableStateOf(false) }
    val hello = if (name.isBlank()) "Hello, little superstar!" else "Hello, $name!"

    LaunchedEffect(app.speaker.ready) {
        if (app.speaker.ready && !greeted) {
            onGreeted()
            talking = true
            app.speaker.say(hello, "Where shall we go today?", onDone = { talking = false })
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("WordWonder Kids", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Purple)
                Text("Play. Discover. Speak. Shine!", fontSize = 14.sp, color = Soft, fontWeight = FontWeight.Bold)
            }
            StarBadge(app.store.stars)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BuddyAvatar(
                app.profile.photo(0),
                if (talking) Mood.TALK else Mood.IDLE,
                100.dp,
                Modifier.clickable {
                    app.sfx.pop()
                    talking = true
                    app.speaker.say(hello, "Where shall we go today?", onDone = { talking = false })
                },
                accessory = app.store.costumeEmoji,
            )
            SpeechBubble("$hello Where shall we go today?", Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigTile("🛍️", "Reward Shop", Sun, Modifier.weight(1f).height(110.dp), "Spend your coins") {
                app.sfx.pop()
                app.speaker.say("Reward shop!")
                onShop()
            }
            BigTile("🏆", "My Stickers", Color(0xFFFFA726), Modifier.weight(1f).height(110.dp), "Collect them all") {
                app.sfx.pop()
                app.speaker.say("My stickers!")
                onStickers()
            }
        }

        Spacer(Modifier.height(12.dp))
        World.entries.forEachIndexed { i, world ->
            if (i > 0) PathConnector(leftToRight = i % 2 == 1)
            WorldNode(world, alignRight = i % 2 == 1, costume = app.store.costumeEmoji) {
                app.sfx.pop()
                app.speaker.say(world.title)
                onWorld(world)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "🔒 Grown-ups",
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0x14000000))
                .clickable(onClick = onParent)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            fontSize = 16.sp,
            color = Soft,
        )
    }
}

@Composable
private fun PathConnector(leftToRight: Boolean) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val startX = if (leftToRight) size.width * 0.25f else size.width * 0.75f
        val endX = if (leftToRight) size.width * 0.75f else size.width * 0.25f
        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(startX, size.height * 0.7f, endX, size.height * 0.3f, endX, size.height)
        }
        drawPath(
            path,
            Color(0xFFBCAAA4),
            style = Stroke(
                width = 10.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            ),
        )
        drawCircle(Sun, 7.dp.toPx(), Offset((startX + endX) / 2f, size.height / 2f))
    }
}

@Composable
private fun WorldNode(world: World, alignRight: Boolean, costume: String?, onClick: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "node")
    val bob by inf.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(1800 + world.ordinal * 150, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (alignRight) TeacherBeside(world, costume)
        Column(
            Modifier
                .graphicsLayer { translationY = bob * 6f }
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(124.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(world.color.copy(alpha = 0.75f), world.color)))
                    .alpha(if (world.isOpen) 1f else 0.6f),
                contentAlignment = Alignment.Center,
            ) {
                Text(world.emoji, fontSize = 56.sp)
                if (!world.isOpen) {
                    Text("🔒", Modifier.align(Alignment.BottomEnd).padding(10.dp), fontSize = 22.sp)
                }
            }
            Text(
                world.title,
                Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = world.color,
                textAlign = TextAlign.Center,
            )
        }
        if (!alignRight) TeacherBeside(world, costume)
    }
}

@Composable
private fun TeacherBeside(world: World, costume: String?) {
    TeacherAvatar(world.teacher, Mood.IDLE, 90.dp, Modifier.padding(horizontal = 8.dp), accessory = costume)
}

/** Inside a world: the teacher greets her and shows the activities. */
@Composable
fun WorldScreen(
    world: World,
    app: AppServices,
    onBack: () -> Unit,
    onActivity: (Activity) -> Unit,
) {
    var talking by remember { mutableStateOf(false) }
    LaunchedEffect(world, app.speaker.ready) {
        if (app.speaker.ready) {
            talking = true
            app.speaker.say(world.greeting, onDone = { talking = false })
        }
    }
    Column(Modifier.fillMaxSize()) {
        TopBar(world.title, onBack, app.store.stars)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeacherAvatar(
                world.teacher,
                if (talking) Mood.TALK else Mood.IDLE,
                120.dp,
                Modifier.clickable {
                    talking = true
                    app.speaker.say(world.greeting, onDone = { talking = false })
                },
                accessory = app.store.costumeEmoji,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "${world.teacher.title} · ${world.teacher.role}",
                    fontSize = 14.sp,
                    color = Soft,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )
                SpeechBubble(world.greeting)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            world.activities.chunked(2).forEach { pair ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pair.forEach { act ->
                        val soon = act is Activity.Soon
                        BigTile(
                            act.emoji,
                            act.title,
                            if (soon) Color(0xFFB0BEC5) else world.color,
                            Modifier
                                .weight(1f)
                                .height(150.dp),
                            act.subtitle,
                        ) {
                            app.sfx.pop()
                            if (soon) {
                                app.speaker.say("${act.title} is coming soon!")
                            } else {
                                app.speaker.say(act.title)
                                onActivity(act)
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
