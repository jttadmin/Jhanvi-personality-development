package com.bhumaji.wordbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.ProgressStore
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.speech.Speaker

/** Flashcards: picture + word. Tap to hear it, swipe or use arrows to move. */
@Composable
fun LearnScreen(category: String, speaker: Speaker, store: ProgressStore, onBack: () -> Unit) {
    val words = remember(category) { WordBank.inCategory(category) }
    var index by remember(category) { mutableIntStateOf(0) }
    val word = words[index % words.size]

    val next = { index = (index + 1) % words.size }
    val prev = { index = (index - 1 + words.size) % words.size }
    fun speak(extraSlow: Boolean = false) = speaker.say(word.text, word.sentence, extraSlow = extraSlow)

    LaunchedEffect(word.id, speaker.ready) {
        if (speaker.ready) {
            store.markHeard(word)
            speaker.say(word.text, word.sentence)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(WordBank.category(category).title, onBack, store.stars)
        Text(
            "${index + 1} / ${words.size}",
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Soft,
            fontSize = 16.sp,
        )

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            val pictureSize = minOf(maxWidth, maxHeight) * 0.55f
            Column(
                Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(36.dp))
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White)
                    .clickable { speak() }
                    .pointerInput(word.id) {
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onDragEnd = {
                                when {
                                    total < -120f -> next()
                                    total > 120f -> prev()
                                    else -> {}
                                }
                            },
                        ) { change, dx ->
                            change.consume()
                            total += dx
                        }
                    }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                WordPicture(word, pictureSize)
                WordText(word.text)
                Text(
                    word.sentence,
                    fontSize = 20.sp,
                    color = Soft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundButton("⬅️", Color.White, prev)
            RoundButton("🐢", Sun, { speak(extraSlow = true) })
            RoundButton("🔊", Sky, { speak() }, size = 88.dp)
            RoundButton("➡️", Color.White, next)
        }
    }
}
