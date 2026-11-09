package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.Game
import com.bhumaji.wordbuddy.data.Games
import com.bhumaji.wordbuddy.data.Pic
import kotlinx.coroutines.delay

/**
 * Shared screen for the thinking games (Rhyme Time, Sound Detective,
 * Opposites, Think & Find): a spoken question, an optional big clue,
 * and 3 pictures to choose from.
 */
@Composable
fun QuizScreen(game: Game, app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    val store = app.store
    var round by remember { mutableIntStateOf(0) }
    val quiz = remember(round) { Games.next(game) }
    var wrong by remember(round) { mutableStateOf(setOf<Int>()) }
    var solved by remember(round) { mutableStateOf(false) }
    var mood by remember { mutableStateOf(Mood.TALK) }
    var advance by remember { mutableIntStateOf(0) }
    var confetti by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }

    fun ask() {
        mood = Mood.TALK
        speaker.say(*quiz.spoken.toTypedArray(), onDone = { mood = Mood.IDLE })
    }

    LaunchedEffect(round, speaker.ready) { if (speaker.ready) ask() }
    LaunchedEffect(advance) {
        if (advance > 0) {
            delay(700)
            round++
        }
    }

    fun choose(i: Int, pic: Pic) {
        if (solved || i in wrong) return
        if (i == quiz.answer) {
            solved = true
            val firstTry = wrong.isEmpty()
            store.recordQuiz(game.id, correct = firstTry)
            streak = if (firstTry) streak + 1 else 0
            mood = Mood.CHEER
            confetti++
            val newSticker = store.addStars(if (firstTry) 5 else 2)
            if (newSticker) app.sfx.tada() else app.sfx.chime()
            val lines = mutableListOf(app.cheer(), quiz.success)
            if (streak > 0 && streak % 5 == 0) lines.add("$streak in a row! Amazing!")
            if (newSticker) lines.add("You got a new sticker!")
            speaker.say(*lines.toTypedArray(), onDone = { advance++ })
        } else {
            if (wrong.isEmpty()) store.recordQuiz(game.id, correct = false)
            wrong = wrong + i
            streak = 0
            mood = Mood.OOPS
            app.sfx.boing()
            speaker.say("That's ${pic.label}.", "Try again!", onDone = { mood = Mood.IDLE })
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(game.title, onBack, store.stars)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BuddyAvatar(app.profile.photo(round), mood, 80.dp, accessory = app.store.costumeEmoji)
                SpeechBubble(quiz.question, Modifier.weight(1f))
                RoundButton("🔊", Sky, { ask() }, Modifier.padding(start = 8.dp), size = 56.dp)
            }

            quiz.prompt?.let { clue ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        clue,
                        Modifier
                            .shadow(6.dp, RoundedCornerShape(28.dp))
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White)
                            .clickable { ask() }
                            .padding(horizontal = 28.dp, vertical = 6.dp),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Purple,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val landscape = maxWidth > maxHeight
                val emojiSize = if (landscape) {
                    minOf(maxWidth / 3, maxHeight) * 0.5f
                } else {
                    minOf(maxWidth, maxHeight / 3) * 0.55f
                }
                if (landscape) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        quiz.options.forEachIndexed { i, pic ->
                            QuizCard(
                                pic, emojiSize, i == quiz.answer, solved, i in wrong,
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                            ) { choose(i, pic) }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        quiz.options.forEachIndexed { i, pic ->
                            QuizCard(
                                pic, emojiSize, i == quiz.answer, solved, i in wrong,
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                            ) { choose(i, pic) }
                        }
                    }
                }
            }
        }
        Confetti(confetti)
    }
}

@Composable
private fun QuizCard(
    pic: Pic,
    emojiSize: Dp,
    isAnswer: Boolean,
    solved: Boolean,
    isWrong: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val celebrate = solved && isAnswer
    val scale by animateFloatAsState(
        if (celebrate) 1.08f else 1f,
        spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pop",
    )
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .scale(scale)
            .alpha(if (isWrong || (solved && !isAnswer)) 0.35f else 1f)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(if (celebrate) 6.dp else 0.dp, if (celebrate) Grass else Color.Transparent, shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(emojiSize), contentAlignment = Alignment.Center) {
            Text(pic.emoji, fontSize = (emojiSize.value * 0.62f).sp)
        }
        if (celebrate || isWrong) {
            Text(pic.label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (celebrate) Grass else Soft)
        }
    }
}
