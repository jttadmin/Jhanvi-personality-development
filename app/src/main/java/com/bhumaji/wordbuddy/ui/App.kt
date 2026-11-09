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
    data class InWorld(val world: World) : Screen
    data class Pick(val mode: Mode) : Screen
    data class Play(val mode: Mode, val category: String) : Screen
    data class Quiz(val game: Game) : Screen
    data object Move : Screen
    data object Builder : Screen
    data object Coach : Screen
    data object Abc : Screen
    data object Freeze : Screen
    data object Shop : Screen
    data object Stickers : Screen
    data object Parent : Screen
}

@Composable
fun WordBuddyApp(app: AppServices) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var world by remember { mutableStateOf<World?>(null) }
    var greeted by remember { mutableStateOf(false) }

    fun up(s: Screen): Screen = when (s) {
        is Screen.Play -> Screen.Pick(s.mode)
        is Screen.Pick, is Screen.Quiz, Screen.Move, Screen.Freeze, Screen.Builder, Screen.Coach, Screen.Abc -> world?.let { Screen.InWorld(it) } ?: Screen.Home
        else -> Screen.Home
    }
    val back = { screen = up(screen) }

    BackHandler(enabled = screen != Screen.Home) { back() }

    ThemedBackground(com.bhumaji.wordbuddy.data.Shop.theme(app.store.theme)) {
        when (val s = screen) {
            Screen.Home -> MapScreen(
                app = app,
                greeted = greeted,
                onGreeted = { greeted = true },
                onWorld = {
                    world = it
                    screen = Screen.InWorld(it)
                },
                onShop = { screen = Screen.Shop },
                onStickers = { screen = Screen.Stickers },
                onParent = { screen = Screen.Parent },
            )

            is Screen.InWorld -> WorldScreen(s.world, app, onBack = back) { act ->
                screen = when (act) {
                    is Activity.Words -> Screen.Pick(act.mode)
                    is Activity.Quiz -> Screen.Quiz(act.game)
                    Activity.MoveLikeMe -> Screen.Move
                    Activity.FreezeDance -> Screen.Freeze
                    Activity.Builder -> Screen.Builder
                    Activity.Coach -> Screen.Coach
                    Activity.Abc -> Screen.Abc
                    is Activity.Soon -> s
                }
            }

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
            Screen.Move -> MoveScreen(app, onBack = back)
            Screen.Freeze -> FreezeScreen(app, onBack = back)
            Screen.Builder -> SentenceBuilderScreen(app, onBack = back)
            Screen.Coach -> WordCoachScreen(app, onBack = back)
            Screen.Abc -> AlphabetScreen(app, onBack = back)
            Screen.Shop -> ShopScreen(app, onBack = back)
            Screen.Stickers -> StickerScreen(app, onBack = back)
            Screen.Parent -> ParentScreen(app, onBack = back)
        }
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

