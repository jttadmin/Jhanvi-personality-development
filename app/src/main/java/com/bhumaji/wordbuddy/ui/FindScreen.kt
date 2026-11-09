package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.bhumaji.wordbuddy.data.ProgressStore
import com.bhumaji.wordbuddy.data.Word
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Speaker
import kotlinx.coroutines.delay

/** "Find It!" — the app says a word, she taps the matching picture out of 3. */
@Composable
fun FindScreen(category: String, speaker: Speaker, store: ProgressStore, onBack: () -> Unit) {
    val pool = remember(category) { WordBank.inCategory(category) }
    val order = remember(category) { store.practiceOrder(pool) }
    var round by remember { mutableIntStateOf(0) }
    val target = order[round % order.size]

    val options = remember(round) {
        val source = if (pool.size >= 3) pool else WordBank.words
        (source.filter { it.id != target.id }.shuffled().take(2) + target).shuffled()
    }
    var wrong by remember(round) { mutableStateOf(setOf<String>()) }
    var solved by remember(round) { mutableStateOf(false) }
    var advance by remember { mutableIntStateOf(0) }

    LaunchedEffect(round, speaker.ready) {
        if (speaker.ready) speaker.say(WordBank.findPrompt(target))
    }
    LaunchedEffect(advance) {
        if (advance > 0) {
            delay(600)
            round++
        }
    }

    fun choose(w: Word) {
        if (solved || w.id in wrong) return
        if (w.id == target.id) {
            solved = true
            if (wrong.isEmpty()) store.recordFind(target, correct = true)
            store.addStars(1)
            speaker.say(praise(), "That's the ${target.text}!", onDone = { advance++ })
        } else {
            if (wrong.isEmpty()) store.recordFind(target, correct = false)
            wrong = wrong + w.id
            speaker.say("That's the ${w.text}.", WordBank.findPrompt(target))
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(WordBank.category(category).title, onBack, store.stars)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                WordBank.findPrompt(target),
                Modifier
                    .weight(1f, fill = false)
                    .padding(end = 12.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple,
                textAlign = TextAlign.Center,
            )
            RoundButton("🔊", Sky, { speaker.say(WordBank.findPrompt(target)) }, size = 60.dp)
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val landscape = maxWidth > maxHeight
            val cardPicture = if (landscape) minOf(maxWidth / 3, maxHeight) * 0.55f
            else minOf(maxWidth, maxHeight / 3) * 0.6f

            if (landscape) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    options.forEach { w ->
                        OptionCard(
                            w, cardPicture, w.id == target.id, solved, w.id in wrong,
                            Modifier
                                .weight(1f)
                                .fillMaxSize(),
                        ) { choose(w) }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    options.forEach { w ->
                        OptionCard(
                            w, cardPicture, w.id == target.id, solved, w.id in wrong,
                            Modifier
                                .weight(1f)
                                .fillMaxSize(),
                        ) { choose(w) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    word: Word,
    picture: Dp,
    isTarget: Boolean,
    solved: Boolean,
    isWrong: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val celebrate = solved && isTarget
    val scale by animateFloatAsState(
        if (celebrate) 1.08f else 1f,
        spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pop",
    )
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .scale(scale)
            .alpha(if (isWrong || (solved && !isTarget)) 0.35f else 1f)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(if (celebrate) 6.dp else 0.dp, if (celebrate) Grass else Color.Transparent, shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WordPicture(word, picture)
        // Names stay hidden until tapped, so she chooses by listening, not reading.
        if (celebrate || isWrong) {
            Text(word.text, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (celebrate) Grass else Soft)
        }
    }
}
