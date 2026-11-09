package com.bhumaji.wordbuddy.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.Alphabet
import com.bhumaji.wordbuddy.data.Letter

private val letterColors = listOf(Coral, Purple, Grass, Sky, Color(0xFFFFA726), Color(0xFFEC407A))

/** Alphabet Forest "ABC & Tracing": A–Z with phonics, then trace each letter with a finger. */
@Composable
fun AlphabetScreen(app: AppServices, onBack: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val current = selected
    if (current == null) {
        LetterGrid(app, onBack) { selected = it }
    } else {
        LetterDetail(app, current, onBack = { selected = null }, onMove = { selected = (it + 26) % 26 })
    }
}

@Composable
private fun LetterGrid(app: AppServices, onBack: () -> Unit, onPick: (Int) -> Unit) {
    LaunchedEffect(app.speaker.ready) {
        if (app.speaker.ready) app.speaker.say("Pick a letter!")
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("ABC & Tracing", onBack, app.store.stars)
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TeacherAvatar(Teacher.BUNNY, Mood.IDLE, 72.dp, accessory = app.store.costumeEmoji)
            SpeechBubble("Tap a letter to hear its sound!", Modifier.weight(1f))
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(84.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(Alphabet.letters) { i, l ->
                Column(
                    Modifier
                        .aspectRatio(1f)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(letterColors[i % letterColors.size])
                        .clickable {
                            app.sfx.pop()
                            onPick(i)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("${l.upper}${l.lower}", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(l.emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun LetterDetail(app: AppServices, index: Int, onBack: () -> Unit, onMove: (Int) -> Unit) {
    val l = Alphabet.letters[index]
    var tracing by remember(index) { mutableStateOf(false) }
    var upper by remember(index) { mutableStateOf(true) }
    var mood by remember { mutableStateOf(Mood.TALK) }
    var confetti by remember { mutableIntStateOf(0) }

    LaunchedEffect(index, app.speaker.ready) {
        if (app.speaker.ready) {
            mood = Mood.TALK
            app.speaker.say(*Alphabet.line(l).toTypedArray(), onDone = { mood = Mood.IDLE })
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar("Letter ${l.upper}", onBack, app.store.stars)
            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                TeacherAvatar(Teacher.BUNNY, mood, 72.dp, accessory = app.store.costumeEmoji)
                SpeechBubble(
                    if (tracing) "Trace the letter with your finger!" else "${l.upper} says “${l.sound}”",
                    Modifier.weight(1f),
                )
            }

            if (!tracing) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(8.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .clickable {
                            mood = Mood.TALK
                            app.speaker.say(*Alphabet.line(l).toTypedArray(), onDone = { mood = Mood.IDLE })
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "${l.upper} ${l.lower}",
                        fontSize = 110.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = letterColors[index % letterColors.size],
                    )
                    Text(l.emoji, fontSize = 80.sp)
                    Text(
                        buildAnnotatedString {
                            val i = l.word.indexOf(l.lower)
                            if (i >= 0) {
                                append(l.word.substring(0, i))
                                withStyle(SpanStyle(color = Coral, fontWeight = FontWeight.ExtraBold)) {
                                    append(l.word[i].toString())
                                }
                                append(l.word.substring(i + 1))
                            } else {
                                append(l.word)
                            }
                        },
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                }
            } else {
                TracePad(
                    letter = l,
                    upper = upper,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    onDone = { goodEnough ->
                        if (goodEnough) {
                            mood = Mood.CHEER
                            confetti++
                            val newSticker = app.store.addStars(5)
                            if (newSticker) app.sfx.tada() else app.sfx.chime()
                            app.speaker.say(app.cheer(), "Beautiful writing!", onDone = { mood = Mood.IDLE })
                        } else {
                            mood = Mood.TALK
                            app.speaker.say("Trace all of the letter!", onDone = { mood = Mood.IDLE })
                        }
                    },
                    onClear = { app.sfx.pop() },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(true to "${l.upper}", false to "${l.lower}").forEach { (u, label) ->
                        Text(
                            label,
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (upper == u) Purple else Color.White)
                                .clickable {
                                    app.sfx.pop()
                                    upper = u
                                }
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (upper == u) Color.White else Ink,
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton("⬅️", Color.White, {
                    app.sfx.pop()
                    onMove(index - 1)
                })
                RoundButton(if (tracing) "🔤" else "✏️", Sun, {
                    app.sfx.pop()
                    tracing = !tracing
                    if (tracing) app.speaker.say("Trace the letter ${l.upper} with your finger!")
                }, size = 88.dp)
                RoundButton("➡️", Color.White, {
                    app.sfx.pop()
                    onMove(index + 1)
                })
            }
        }
        Confetti(confetti)
    }
}

/** Faint letter guide; her finger draws thick colourful strokes over it. */
@Composable
private fun TracePad(
    letter: Letter,
    upper: Boolean,
    modifier: Modifier,
    onDone: (Boolean) -> Unit,
    onClear: () -> Unit,
) {
    val strokes = remember(letter, upper) { mutableStateListOf<List<Offset>>() }
    var current by remember(letter, upper) { mutableStateOf<List<Offset>>(emptyList()) }
    var padWidth by remember { mutableStateOf(1f) }

    fun inkLength(): Float = (strokes + listOf(current)).sumOf { s ->
        s.zipWithNext { a, b -> (a - b).getDistance().toDouble() }.sum()
    }.toFloat()

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (upper) "${letter.upper}" else "${letter.lower}",
                fontSize = 260.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0x22000000),
            )
            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(letter, upper) {
                        padWidth = size.width.toFloat()
                        detectDragGestures(
                            onDragStart = { current = listOf(it) },
                            onDragEnd = {
                                if (current.size > 1) strokes.add(current)
                                current = emptyList()
                            },
                        ) { change, _ ->
                            change.consume()
                            current = current + change.position
                        }
                    }
            ) {
                (strokes + listOf(current)).forEachIndexed { i, s ->
                    if (s.size < 2) return@forEachIndexed
                    val path = Path().apply {
                        moveTo(s.first().x, s.first().y)
                        s.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path,
                        letterColors[i % letterColors.size],
                        style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        }
        Row(
            Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RoundButton("🧽", Color(0xFFECEFF1), {
                strokes.clear()
                current = emptyList()
                onClear()
            }, size = 64.dp)
            RoundButton("✅", Grass, {
                onDone(inkLength() > padWidth * 0.8f)
            }, size = 64.dp)
        }
    }
}
