package com.bhumaji.wordbuddy.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bhumaji.wordbuddy.data.Conversation
import com.bhumaji.wordbuddy.data.Conversations
import com.bhumaji.wordbuddy.data.Pic
import com.bhumaji.wordbuddy.data.Stories
import com.bhumaji.wordbuddy.data.Story
import com.bhumaji.wordbuddy.data.StoryQuestion
import com.bhumaji.wordbuddy.speech.Match

// ---------------------------------------------------------------------------
// Story Kingdom
// ---------------------------------------------------------------------------

/** Story Kingdom: pick a story; Coco reads it with you. */
@Composable
fun StoryShelfScreen(app: AppServices, onBack: () -> Unit) {
    var chosen by remember { mutableStateOf<Story?>(null) }
    val story = chosen
    if (story != null) {
        StoryReader(story, app, onClose = { chosen = null })
        return
    }
    LaunchedEffect(app.speaker.ready) {
        if (app.speaker.ready) app.speaker.say("Which story shall we read?")
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("Picture Stories", onBack, app.store.stars)
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TeacherAvatar(Teacher.COCO, Mood.IDLE, 80.dp, accessory = app.store.costumeEmoji)
            SpeechBubble("Pick a story!", Modifier.weight(1f))
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Stories.all, key = { it.id }) { s ->
                val read = app.store.quizScore("story_${s.id}").first > 0
                BigTile(
                    s.cover,
                    s.title,
                    Color(0xFFFFA726),
                    Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    if (read) "✅ Read" else "${s.pages.size} pages",
                ) {
                    app.sfx.pop()
                    chosen = s
                }
            }
        }
    }
}

private sealed interface StoryStep {
    data object Cover : StoryStep
    data class PageStep(val index: Int) : StoryStep
    data class Question(val index: Int) : StoryStep
    data object Predict : StoryStep
    data object End : StoryStep
}

@Composable
private fun StoryReader(story: Story, app: AppServices, onClose: () -> Unit) {
    val speaker = app.speaker
    var step by remember(story) { mutableStateOf<StoryStep>(StoryStep.Cover) }
    var mood by remember { mutableStateOf(Mood.TALK) }
    var filled by remember(step) { mutableStateOf(false) }
    var wrong by remember(step) { mutableStateOf(setOf<Int>()) }
    var confetti by remember { mutableIntStateOf(0) }
    var listening by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            speaker.stop()
            app.listener.stop()
        }
    }

    fun next() {
        app.sfx.pop()
        app.listener.stop()
        listening = false
        step = when (val s = step) {
            StoryStep.Cover -> StoryStep.PageStep(0)
            is StoryStep.PageStep ->
                if (s.index < story.pages.lastIndex) StoryStep.PageStep(s.index + 1) else StoryStep.Question(0)
            is StoryStep.Question ->
                if (s.index < story.questions.lastIndex) StoryStep.Question(s.index + 1) else StoryStep.Predict
            StoryStep.Predict -> StoryStep.End
            StoryStep.End -> StoryStep.End
        }
    }

    fun read(vararg lines: String) {
        mood = Mood.TALK
        speaker.say(*lines, onDone = { mood = Mood.IDLE })
    }

    LaunchedEffect(step, speaker.ready) {
        if (!speaker.ready) return@LaunchedEffect
        when (val s = step) {
            StoryStep.Cover -> read("Story time!", story.title)
            is StoryStep.PageStep -> {
                val page = story.pages[s.index]
                if (page.fill == null) read(page.text) else read(page.text.replace("___", "hmm"), "Which word fits?")
            }
            is StoryStep.Question -> read(story.questions[s.index].ask)
            StoryStep.Predict -> read("What do you think?", story.predict.ask)
            StoryStep.End -> {
                confetti++
                app.store.recordQuiz("story_${story.id}", correct = true)
                val newSticker = app.store.addStars(15)
                if (newSticker) app.sfx.tada() else app.sfx.chime()
                mood = Mood.CHEER
                val lines = mutableListOf("The end!", story.lesson, app.cheer(), "You finished the story!")
                if (newSticker) lines.add("You got a new sticker!")
                speaker.say(*lines.toTypedArray())
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(story.title, onClose, app.store.stars)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeacherAvatar(Teacher.COCO, mood, 76.dp, accessory = app.store.costumeEmoji)
                val progress = when (val s = step) {
                    is StoryStep.PageStep -> "Page ${s.index + 1} of ${story.pages.size}"
                    is StoryStep.Question -> "Question ${s.index + 1}"
                    StoryStep.Predict -> "What happens next?"
                    else -> ""
                }
                Text(progress, Modifier.weight(1f), color = Soft, fontWeight = FontWeight.Bold)
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                when (val s = step) {
                    StoryStep.Cover -> Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        MotionBox(com.bhumaji.wordbuddy.data.Motion.NOD, 180.dp) { Text(story.cover, fontSize = 120.sp) }
                        Text(story.title, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Purple, textAlign = TextAlign.Center)
                    }

                    is StoryStep.PageStep -> {
                        val page = story.pages[s.index]
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(32.dp))
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFFE1F5FE), Color(0xFFF1F8E9)))),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                page.actors.forEach { actor ->
                                    MotionBox(actor.motion, if (page.actors.size > 3) 80.dp else 110.dp) {
                                        Text(actor.emoji, fontSize = if (page.actors.size > 3) 52.sp else 72.sp)
                                    }
                                }
                            }
                            Text(
                                if (page.fill != null && filled) page.fullText else page.text,
                                Modifier
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .clickable { read(if (filled || page.fill == null) page.fullText else page.text.replace("___", "hmm")) }
                                    .padding(16.dp),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp,
                            )
                            page.fill?.let { fill ->
                                if (!filled) {
                                    PicChoices(fill.options, wrong) { i, pic ->
                                        if (i == fill.answer) {
                                            filled = true
                                            confetti++
                                            app.store.addStars(3)
                                            app.sfx.chime()
                                            read(app.cheer(), page.fullText)
                                        } else {
                                            wrong = wrong + i
                                            app.sfx.boing()
                                            read("${pic.label}? Hmm, not quite.", "Try again!")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is StoryStep.Question -> QuestionView(story.questions[s.index], app, onAnswered = {
                        mood = Mood.CHEER
                        confetti++
                    })

                    StoryStep.Predict -> Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(story.predict.ask, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Purple, textAlign = TextAlign.Center)
                        PicChoices(story.predict.options, emptySet()) { _, pic ->
                            app.sfx.chime()
                            app.store.addStars(2)
                            read("Good thinking!", "Maybe they will ${pic.label}!")
                            next()
                        }
                    }

                    StoryStep.End -> Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🎉", fontSize = 100.sp)
                        Text("The End!", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Purple)
                        Text(story.lesson, fontSize = 22.sp, color = Ink, textAlign = TextAlign.Center)
                        Text("+15 🪙", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Coral)
                    }
                }
            }

            val page = (step as? StoryStep.PageStep)?.let { story.pages[it.index] }
            val canGoOn = when (step) {
                is StoryStep.PageStep -> page?.fill == null || filled
                is StoryStep.Question -> false
                StoryStep.Predict -> false
                else -> true
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step is StoryStep.PageStep && page != null) {
                    RoundButton("🔁", Sky, {
                        read(if (filled || page.fill == null) page.fullText else page.text.replace("___", "hmm"))
                    })
                    if (app.listener.isAvailable && (page.fill == null || filled)) {
                        RoundButton("🗣️", if (listening) Coral else Purple, {
                            sayAlong(app, page.fullText, onStart = {
                                listening = true
                                mood = Mood.LISTEN
                            }) { ok ->
                                listening = false
                                if (ok) {
                                    confetti++
                                    read("Wonderful reading!")
                                } else {
                                    read("Good try! Listen.", page.fullText)
                                }
                            }
                        }, enabled = !listening)
                    }
                }
                if (step == StoryStep.End) {
                    RoundButton("📚", Sun, onClose, size = 88.dp)
                } else {
                    RoundButton("➡️", Color.White, { next() }, size = 88.dp, enabled = canGoOn)
                }
            }
        }
        Confetti(confetti)
    }

    // Questions move on by themselves once answered
    // Story questions move on by themselves a moment after a right answer.
    if (step is StoryStep.Question) {
        LaunchedEffect(confetti) {
            if (mood == Mood.CHEER) {
                kotlinx.coroutines.delay(2500)
                mood = Mood.IDLE
                next()
            }
        }
    }
}

@Composable
private fun QuestionView(q: StoryQuestion, app: AppServices, onAnswered: () -> Unit) {
    var wrong by remember(q) { mutableStateOf(setOf<Int>()) }
    var done by remember(q) { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(q.ask, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Purple, textAlign = TextAlign.Center)
        PicChoices(q.options, wrong, highlight = if (done) q.answer else -1) { i, pic ->
            if (done) return@PicChoices
            if (i == q.answer) {
                done = true
                app.sfx.chime()
                app.store.addStars(if (wrong.isEmpty()) 3 else 1)
                app.speaker.say(app.cheer(), q.why)
                onAnswered()
            } else {
                wrong = wrong + i
                app.sfx.boing()
                app.speaker.say("${pic.label}? Let's think again.")
            }
        }
    }
}

@Composable
private fun PicChoices(options: List<Pic>, wrong: Set<Int>, highlight: Int = -1, onPick: (Int, Pic) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        options.forEachIndexed { i, pic ->
            Column(
                Modifier
                    .alpha(if (i in wrong) 0.35f else 1f)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(if (i == highlight) 4.dp else 0.dp, if (i == highlight) Grass else Color.Transparent, RoundedCornerShape(20.dp))
                    .clickable { onPick(i, pic) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(pic.emoji, fontSize = 48.sp)
                Text(pic.label, fontSize = 14.sp, color = Soft, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Listen to her read a sentence aloud; ok = most of the words were heard. */
private fun sayAlong(app: AppServices, sentence: String, onStart: () -> Unit, onResult: (Boolean) -> Unit) {
    app.speaker.stop()
    onStart()
    app.listener.listen(
        onLevel = {},
        onHeard = { heard ->
            val ok = Match.checkSentence(sentence, heard) != Match.Result.MISS
            if (ok) app.store.addStars(2)
            onResult(ok)
        },
        onFail = { onResult(false) },
    )
}

// ---------------------------------------------------------------------------
// Conversation Planet
// ---------------------------------------------------------------------------

/** Conversation Planet: pick a topic and chat with Coco. */
@Composable
fun ConversationScreen(app: AppServices, onBack: () -> Unit) {
    var chosen by remember { mutableStateOf<Conversation?>(null) }
    val convo = chosen
    if (convo != null) {
        Chat(convo, app, onClose = { chosen = null })
        return
    }
    LaunchedEffect(app.speaker.ready) {
        if (app.speaker.ready) app.speaker.say("What shall we talk about?")
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("Talk with Coco", onBack, app.store.stars)
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TeacherAvatar(Teacher.COCO, Mood.IDLE, 80.dp, accessory = app.store.costumeEmoji)
            SpeechBubble("Let's chat!", Modifier.weight(1f))
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Conversations.all, key = { it.id }) { c ->
                BigTile(
                    c.emoji,
                    c.title,
                    Color(0xFF5C6BC0),
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    "${c.turns.size} questions",
                ) {
                    app.sfx.pop()
                    chosen = c
                }
            }
        }
    }
}

private data class Line(val fromCoco: Boolean, val text: String)

@Composable
private fun Chat(convo: Conversation, app: AppServices, onClose: () -> Unit) {
    val speaker = app.speaker
    val context = LocalContext.current
    var turnIndex by remember(convo) { mutableIntStateOf(0) }
    var lines by remember(convo) { mutableStateOf(listOf<Line>()) }
    var mood by remember { mutableStateOf(Mood.TALK) }
    var listening by remember { mutableStateOf(false) }
    var retry by remember(turnIndex) { mutableStateOf(false) }
    var finished by remember(convo) { mutableStateOf(false) }
    var confetti by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(app.profile.name.trim()) }

    val canRecognize = remember { app.listener.isAvailable }
    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }

    DisposableEffect(Unit) {
        onDispose {
            speaker.stop()
            app.listener.stop()
        }
    }

    val turn = convo.turns.getOrNull(turnIndex)

    fun coco(text: String, then: (() -> Unit)? = null) {
        lines = lines + Line(true, text)
        mood = Mood.TALK
        speaker.say(text, onDone = {
            mood = Mood.IDLE
            then?.invoke()
        })
    }

    fun advance() {
        if (turnIndex < convo.turns.lastIndex) {
            turnIndex++
        } else {
            finished = true
            confetti++
            mood = Mood.CHEER
            val newSticker = app.store.addStars(10)
            if (newSticker) app.sfx.tada() else app.sfx.chime()
            coco(convo.bye + if (newSticker) " You got a new sticker!" else "")
        }
    }

    /** She answered (by voice, or by tapping a picture and repeating). */
    fun answered(heard: String, viaTap: Boolean) {
        val t = turn ?: return
        if (name.isBlank() && t.model.contains("{name}")) {
            val words = heard.trim().split(" ").filter { it.isNotBlank() }
            val guess = words.lastOrNull()?.replaceFirstChar { it.uppercase() }
            if (!guess.isNullOrBlank()) name = guess
        }
        val x = Conversations.extract(t, heard)
        val model = Conversations.fill(t.model, x, name)
        lines = lines + Line(false, if (viaTap) model else heard.ifBlank { "…" })
        val good = !viaTap && Match.checkSentence(model, listOf(heard)) != Match.Result.MISS
        val react = Conversations.fill(t.react, x.replaceFirstChar { it.uppercase() }, name)
        if (good || retry) {
            app.store.addStars(if (good) 5 else 2)
            app.sfx.chime()
            mood = Mood.CHEER
            coco(if (good) "$react Great full sentence!" else react) { advance() }
        } else {
            retry = true
            coco("$react Can you say it in a full sentence? Say: $model")
        }
    }

    fun listen() {
        if (!canRecognize) return
        if (!hasMic) {
            askMic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        speaker.stop()
        listening = true
        mood = Mood.LISTEN
        app.listener.listen(
            onLevel = {},
            onHeard = { heard ->
                listening = false
                answered(heard.firstOrNull().orEmpty(), viaTap = false)
            },
            onFail = {
                listening = false
                mood = Mood.IDLE
                coco("I couldn't hear you. Tap the microphone and try again!")
            },
        )
    }

    LaunchedEffect(turnIndex, speaker.ready) {
        if (!speaker.ready || finished) return@LaunchedEffect
        val t = turn ?: return@LaunchedEffect
        coco(Conversations.fill(t.ask, "", name)) { if (canRecognize && hasMic) listen() }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(convo.title, onClose, app.store.stars)
            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                TeacherAvatar(Teacher.COCO, mood, 96.dp, accessory = app.store.costumeEmoji)
                BuddyAvatar(app.profile.photo(0), if (listening) Mood.LISTEN else Mood.IDLE, 70.dp, accessory = app.store.costumeEmoji)
            }
            androidx.compose.foundation.lazy.LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true,
            ) {
                items(lines.reversed().size) { i ->
                    val line = lines.reversed()[i]
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (line.fromCoco) Arrangement.Start else Arrangement.End,
                    ) {
                        Text(
                            (if (line.fromCoco) "🦜 " else "🧒 ") + line.text,
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (line.fromCoco) Color.White else Color(0xFFD1C4E9))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 20.sp,
                            color = Ink,
                        )
                    }
                }
            }

            if (!finished && turn != null) {
                if (turn.choices.isNotEmpty()) {
                    PicChoices(turn.choices, emptySet()) { _, pic ->
                        val model = Conversations.fill(turn.model, pic.label, name)
                        app.sfx.pop()
                        lines = lines + Line(false, model)
                        retry = true
                        mood = Mood.TALK
                        speaker.say("You can say:", model, "Now you say it!", onDone = {
                            mood = Mood.IDLE
                            if (canRecognize && hasMic) listen()
                        })
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundButton("💡", Sun, {
                        val model = Conversations.fill(turn.model, turn.fallback, name)
                        retry = true
                        coco("You can say: $model")
                    })
                    if (canRecognize) {
                        RoundButton("🎤", if (listening) Coral else Purple, { listen() }, size = 96.dp, enabled = !listening)
                    }
                    RoundButton("➡️", Color.White, {
                        app.listener.stop()
                        listening = false
                        advance()
                    })
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    RoundButton("💬", Sun, onClose, size = 88.dp)
                }
            }
        }
        Confetti(confetti)
    }
}
