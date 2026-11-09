package com.bhumaji.wordbuddy.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import com.bhumaji.wordbuddy.data.Game
import com.bhumaji.wordbuddy.data.ProfileStore
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.bhumaji.wordbuddy.data.Status
import com.bhumaji.wordbuddy.data.Word
import com.bhumaji.wordbuddy.data.WordBank
import com.bhumaji.wordbuddy.data.WordStats

@Composable
fun ParentScreen(app: AppServices, onBack: () -> Unit) {
    var unlocked by remember { mutableStateOf(false) }
    if (unlocked) ParentDashboard(app, onBack) else ParentGate(onBack) { unlocked = true }
}

/** A small times-table question keeps a 4-year-old out of the settings. */
@Composable
private fun ParentGate(onBack: () -> Unit, onPass: () -> Unit) {
    val a = remember { (3..9).random() }
    val b = remember { (3..9).random() }
    val answer = a * b
    val choices = remember { listOf(answer, answer + a, answer - b).shuffled() }
    var wrongTry by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopBar("Grown-ups", onBack)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("For grown-ups only 🙂", fontSize = 20.sp, color = Soft)
            Spacer(Modifier.height(12.dp))
            Text("What is $a × $b ?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                choices.forEach { c ->
                    Button(onClick = {
                        if (c == answer) {
                            onPass()
                        } else {
                            wrongTry = true
                        }
                    }) {
                        Text("$c", fontSize = 24.sp)
                    }
                }
            }
            if (wrongTry) {
                Spacer(Modifier.height(16.dp))
                Text("Not quite — try again.", color = Coral)
            }
        }
    }
}

@Composable
private fun ParentDashboard(app: AppServices, onBack: () -> Unit) {
    val speaker = app.speaker
    val store = app.store
    val version = store.version
    val stats = remember(version) { WordBank.visibleWords.associate { it.id to store.stats(it.id) } }
    val learned = stats.values.count { it.status == Status.LEARNED }
    val practising = stats.values.count { it.status == Status.PRACTISING }
    val notYet = stats.values.count { it.status == Status.NEW }
    val needsPractice = WordBank.visibleWords.filter { stats[it.id]?.needsPractice == true }
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopBar("Grown-ups", onBack, store.stars)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { BuddySetup(app) }

            item {
                Panel {
                    Text("Progress", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Count("✅", learned, "Learned")
                        Count("🔁", practising, "Practising")
                        Count("🆕", notYet, "Not yet")
                        Count("⭐", store.stars, "Stars")
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "A word counts as learned after 3 correct answers (saying it or finding it).",
                        fontSize = 13.sp,
                        color = Soft,
                    )
                }
            }

            item {
                Panel {
                    Text("Thinking games", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Game.entries.forEach { g ->
                        val (good, miss) = remember(version, g) { store.quizScore(g.id) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${g.emoji} ${g.title}", Modifier.weight(1f), fontSize = 17.sp)
                            Text(
                                if (good + miss == 0) "not played yet" else "$good right first time / ${good + miss}",
                                fontSize = 14.sp,
                                color = Soft,
                            )
                        }
                    }
                }
            }

            item {
                Panel {
                    Text("Needs practice", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    if (needsPractice.isEmpty()) {
                        Text("Nothing yet — keep playing! 🎉", color = Soft)
                    } else {
                        Text(
                            needsPractice.joinToString("   ") { "${it.emoji.ifEmpty { "🎨" }} ${it.text}" },
                            fontSize = 18.sp,
                        )
                        Text(
                            "Say It! and Find It! show these words first.",
                            fontSize = 13.sp,
                            color = Soft,
                        )
                    }
                }
            }

            item {
                Panel {
                    Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Slower voice", Modifier.weight(1f), fontSize = 18.sp)
                        Switch(
                            checked = store.slowVoice,
                            onCheckedChange = {
                                store.changeSlowVoice(it)
                                speaker.slow = it
                                speaker.say("This is how I sound.")
                            },
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Show easy words too", fontSize = 18.sp)
                            Text("Colours, fruits, body parts and other basic groups", fontSize = 13.sp, color = Soft)
                        }
                        Switch(
                            checked = store.includeEasy,
                            onCheckedChange = { store.changeIncludeEasy(it) },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { speaker.say("Hello! Can you say elephant?") }) {
                            Text("Test voice")
                        }
                        Button(
                            onClick = { confirmReset = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Coral),
                        ) { Text("Reset progress") }
                    }
                    if (speaker.failed) {
                        Text(
                            "No text-to-speech voice found. Install \"Speech Services by Google\" from the Play Store.",
                            color = Coral,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            WordBank.categories.forEach { cat ->
                item(key = "cat_${cat.id}") {
                    Text(
                        "${cat.emoji} ${cat.title}",
                        Modifier.padding(top = 12.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple,
                    )
                }
                items(WordBank.inCategory(cat.id), key = { it.id }) { w ->
                    WordRow(w, stats[w.id] ?: WordStats())
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("This clears all stars and word progress on this phone.") },
            confirmButton = {
                TextButton(onClick = {
                    store.reset()
                    confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) { content() }
}

@Composable
private fun Count(emoji: String, n: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$emoji $n", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 13.sp, color = Soft)
    }
}

@Composable
private fun WordRow(w: Word, s: WordStats) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WordPicture(w, 44.dp, animate = false)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(w.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Heard ${s.heard} · Said ${s.sayGood}/${s.sayTries} · Found ${s.findGood}/${s.findGood + s.findMiss}",
                fontSize = 13.sp,
                color = Soft,
            )
        }
        val (label, color) = when (s.status) {
            Status.LEARNED -> "Learned" to Grass
            Status.PRACTISING -> if (s.needsPractice) "Needs help" to Coral else "Practising" to Sun
            Status.NEW -> "New" to Color(0xFFBDBDBD)
        }
        Text(
            label,
            Modifier
                .clip(RoundedCornerShape(50))
                .background(color)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Her name and photos: used for the animated buddy, praise and stickers. Stays on this phone. */
@Composable
private fun BuddySetup(app: AppServices) {
    val profile = app.profile
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ProfileStore.MAX_PHOTOS)
    ) { uris ->
        if (uris.isNotEmpty()) {
            busy = true
            scope.launch {
                val added = profile.addPhotos(uris)
                busy = false
                note = if (added == 0) "Couldn't read that photo. Try another one." else null
                if (added > 0) {
                    app.sfx.tada()
                    app.speaker.say(app.cheer())
                }
            }
        }
    }

    Panel {
        Text("Her buddy", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Add her name and up to 4 clear photos of her face. The app crops around her face " +
                "and turns it into her animated buddy and dress-up stickers. Photos stay only on this phone.",
            fontSize = 13.sp,
            color = Soft,
        )
        OutlinedTextField(
            value = profile.name,
            onValueChange = { profile.changeName(it) },
            label = { Text("Her name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            profile.photos.forEachIndexed { i, photo ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        photo,
                        contentDescription = "Photo ${i + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                    )
                    Row {
                        Text(
                            "↻",
                            Modifier
                                .clickable { scope.launch { profile.rotate(i) } }
                                .padding(6.dp),
                            fontSize = 20.sp,
                        )
                        Text(
                            "✕",
                            Modifier
                                .clickable { scope.launch { profile.remove(i) } }
                                .padding(6.dp),
                            fontSize = 20.sp,
                            color = Coral,
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = profile.canAddMore && !busy,
            ) { Text(if (profile.photos.isEmpty()) "Add her photos" else "Add more photos") }
            if (busy) CircularProgressIndicator(Modifier.size(28.dp))
        }
        note?.let { Text(it, color = Coral, fontSize = 14.sp) }
    }
}
