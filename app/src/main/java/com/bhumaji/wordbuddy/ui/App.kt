package com.bhumaji.wordbuddy.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
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
import com.bhumaji.wordbuddy.data.ProgressStore
import com.bhumaji.wordbuddy.data.Status
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Listener
import com.bhumaji.wordbuddy.speech.Speaker

enum class Mode(val title: String, val emoji: String) {
    LEARN("Learn Words", "📚"),
    SAY("Say It!", "🎤"),
    FIND("Find It!", "🔍"),
}

sealed interface Screen {
    data object Home : Screen
    data class Pick(val mode: Mode) : Screen
    data class Play(val mode: Mode, val category: String) : Screen
    data object Parent : Screen
}

private fun parentOf(s: Screen): Screen = when (s) {
    is Screen.Play -> Screen.Pick(s.mode)
    else -> Screen.Home
}

@Composable
fun WordBuddyApp(speaker: Speaker, listener: Listener, store: ProgressStore) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var greeted by remember { mutableStateOf(false) }
    val back = { screen = parentOf(screen) }

    BackHandler(enabled = screen != Screen.Home) { back() }

    Surface(Modifier.fillMaxSize(), color = Cream) {
        when (val s = screen) {
            Screen.Home -> HomeScreen(
                speaker = speaker,
                store = store,
                greeted = greeted,
                onGreeted = { greeted = true },
                onPick = { screen = Screen.Pick(it) },
                onParent = { screen = Screen.Parent },
            )

            is Screen.Pick -> CategoryScreen(s.mode, speaker, store, onBack = back) { cat ->
                screen = Screen.Play(s.mode, cat)
            }

            is Screen.Play -> when (s.mode) {
                Mode.LEARN -> LearnScreen(s.category, speaker, store, onBack = back)
                Mode.SAY -> SayScreen(s.category, speaker, listener, store, onBack = back)
                Mode.FIND -> FindScreen(s.category, speaker, store, onBack = back)
            }

            Screen.Parent -> ParentScreen(speaker, store, onBack = back)
        }
    }
}

@Composable
fun HomeScreen(
    speaker: Speaker,
    store: ProgressStore,
    greeted: Boolean,
    onGreeted: () -> Unit,
    onPick: (Mode) -> Unit,
    onParent: () -> Unit,
) {
    LaunchedEffect(speaker.ready) {
        if (speaker.ready && !greeted) {
            onGreeted()
            speaker.say("Hello!", "Let's learn some words!")
        }
    }
    val colors = mapOf(Mode.LEARN to Purple, Mode.SAY to Coral, Mode.FIND to Grass)
    val subtitles = mapOf(
        Mode.LEARN to "Tap a picture and listen",
        Mode.SAY to "Say the word like me",
        Mode.FIND to "Tap the right picture",
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
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple,
            )
            StarBadge(store.stars)
        }
        Spacer(Modifier.height(16.dp))
        Mode.entries.forEach { mode ->
            BigTile(
                mode.emoji,
                mode.title,
                colors.getValue(mode),
                Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                subtitles[mode],
            ) {
                speaker.say(mode.title)
                onPick(mode)
            }
            Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(8.dp))
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
    speaker: Speaker,
    store: ProgressStore,
    onBack: () -> Unit,
    onChoose: (String) -> Unit,
) {
    val version = store.version
    val all = listOf(WordBank.everything) + WordBank.categories
    Column(Modifier.fillMaxSize()) {
        TopBar(mode.title, onBack, store.stars)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(all, key = { it.id }) { cat ->
                val words = WordBank.inCategory(cat.id)
                val learned = remember(version, cat.id) {
                    words.count { store.stats(it.id).status == Status.LEARNED }
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
                    speaker.say(cat.title)
                    onChoose(cat.id)
                }
            }
        }
    }
}
