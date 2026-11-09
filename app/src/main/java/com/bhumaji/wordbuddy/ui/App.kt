package com.bhumaji.wordbuddy.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.Game
import com.bhumaji.wordbuddy.data.ProfileStore
import com.bhumaji.wordbuddy.data.ProgressStore
import com.bhumaji.wordbuddy.data.Status
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Listener
import com.bhumaji.wordbuddy.speech.SoundFx
import com.bhumaji.wordbuddy.speech.Speaker

/** Everything the screens need, created once in MainActivity. */
class AppServices(
    val speaker: Speaker,
    val listener: Listener,
    val store: ProgressStore,
    val profile: ProfileStore,
    val sfx: SoundFx,
) {
    fun cheer(): String = praise(profile.name)
}

enum class Mode(val title: String, val emoji: String) {
    LEARN("Learn Words", "📚"),
    SAY("Say It!", "🎤"),
    FIND("Find It!", "🔍"),
    SENTENCE("Sentence Star", "🗣️"),
}

sealed interface Screen {
    data object Home : Screen
    data class Pick(val mode: Mode) : Screen
    data class Play(val mode: Mode, val category: String) : Screen
    data class Quiz(val game: Game) : Screen
    data object Stickers : Screen
    data object Parent : Screen
}

private fun parentOf(s: Screen): Screen = when (s) {
    is Screen.Play -> Screen.Pick(s.mode)
    else -> Screen.Home
}

@Composable
fun WordBuddyApp(app: AppServices) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var greeted by remember { mutableStateOf(false) }
    val back = { screen = parentOf(screen) }

    BackHandler(enabled = screen != Screen.Home) { back() }

    Box(
        Modifier
            .fillMaxSize()
            .background(PlayBackground)
    ) {
        when (val s = screen) {
            Screen.Home -> HomeScreen(
                app = app,
                greeted = greeted,
                onGreeted = { greeted = true },
                onPick = { screen = Screen.Pick(it) },
                onGame = { screen = Screen.Quiz(it) },
                onStickers = { screen = Screen.Stickers },
                onParent = { screen = Screen.Parent },
            )

            is Screen.Pick -> CategoryScreen(s.mode, app, onBack = back) { cat ->
                screen = Screen.Play(s.mode, cat)
            }

            is Screen.Play -> when (s.mode) {
                Mode.LEARN -> LearnScreen(s.category, app, onBack = back)
                Mode.SAY -> SayScreen(s.category, app, onBack = back)
                Mode.FIND -> FindScreen(s.category, app, onBack = back)
                Mode.SENTENCE -> SayScreen(s.category, app, onBack = back, sentenceMode = true)
            }

            is Screen.Quiz -> QuizScreen(s.game, app, onBack = back)

            Screen.Stickers -> StickerScreen(app, onBack = back)
            Screen.Parent -> ParentScreen(app, onBack = back)
        }
    }
}

@Composable
fun HomeScreen(
    app: AppServices,
    greeted: Boolean,
    onGreeted: () -> Unit,
    onPick: (Mode) -> Unit,
    onGame: (Game) -> Unit,
    onStickers: () -> Unit,
    onParent: () -> Unit,
) {
    val name = app.profile.name.trim()
    var talking by remember { mutableStateOf(false) }
    val hello = if (name.isBlank()) "Hello! Let's play!" else "Hi $name! Let's play!"

    LaunchedEffect(app.speaker.ready) {
        if (app.speaker.ready && !greeted) {
            onGreeted()
            talking = true
            app.speaker.say(hello, "What shall we learn today?", onDone = { talking = false })
        }
    }

    val modeColors = mapOf(
        Mode.LEARN to Purple, Mode.SAY to Coral, Mode.FIND to Grass, Mode.SENTENCE to Color(0xFF29B6F6),
    )
    val modeSubtitles = mapOf(
        Mode.LEARN to "Look and listen",
        Mode.SAY to "Say it like me",
        Mode.FIND to "Tap the picture",
        Mode.SENTENCE to "Say whole sentences",
    )
    val gameColors = mapOf(
        Game.RHYME to Color(0xFFEC407A), Game.SOUNDS to Color(0xFF5C6BC0),
        Game.OPPOSITES to Color(0xFFFF7043), Game.THINK to Color(0xFF26A69A),
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Word Buddy",
                Modifier.weight(1f),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple,
            )
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
                120.dp,
                Modifier.clickable {
                    app.sfx.pop()
                    talking = true
                    app.speaker.say(hello, onDone = { talking = false })
                },
            )
            SpeechBubble(hello, Modifier.weight(1f))
        }

        SectionTitle("Words")
        TilePairs(Mode.entries) { mode, m ->
            BigTile(mode.emoji, mode.title, modeColors.getValue(mode), m, modeSubtitles[mode]) {
                app.sfx.pop()
                app.speaker.say(mode.title)
                onPick(mode)
            }
        }
        SectionTitle("Thinking games")
        TilePairs(Game.entries) { game, m ->
            BigTile(game.emoji, game.title, gameColors.getValue(game), m, game.subtitle) {
                app.sfx.pop()
                app.speaker.say(game.title)
                onGame(game)
            }
        }
        Spacer(Modifier.height(14.dp))
        BigTile(
            "🏆",
            "My Stickers",
            Sun,
            Modifier
                .fillMaxWidth()
                .height(110.dp),
            "Collect them with stars",
        ) {
            app.sfx.pop()
            app.speaker.say("My stickers!")
            onStickers()
        }

        Spacer(Modifier.height(20.dp))
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
fun CategoryScreen(
    mode: Mode,
    app: AppServices,
    onBack: () -> Unit,
    onChoose: (String) -> Unit,
) {
    val version = app.store.version
    val all = listOf(WordBank.everything) + WordBank.categories
    Column(Modifier.fillMaxSize()) {
        TopBar(mode.title, onBack, app.store.stars)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(all, key = { it.id }) { cat ->
                val words = WordBank.inCategory(cat.id)
                val learned = remember(version, cat.id) {
                    words.count { app.store.stats(it.id).status == Status.LEARNED }
                }
                BigTile(
                    cat.emoji,
                    cat.title,
                    cat.tint,
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    "⭐ $learned / ${words.size}",
                ) {
                    app.sfx.pop()
                    app.speaker.say(cat.title)
                    onChoose(cat.id)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 8.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Ink,
    )
}

/** Lays tiles out two per row. */
@Composable
private fun <T> TilePairs(items: List<T>, tile: @Composable (T, Modifier) -> Unit) {
    items.chunked(2).forEach { pair ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            pair.forEach { item ->
                tile(
                    item,
                    Modifier
                        .weight(1f)
                        .height(150.dp),
                )
            }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}
