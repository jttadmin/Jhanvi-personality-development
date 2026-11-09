package com.bhumaji.wordbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.ProgressStore
import kotlinx.coroutines.delay

private data class StickerDef(val accessory: String, val title: String, val color: Color)

/** Dress-up stickers of her (her photo + a costume). One unlocks every 10 stars. */
private val stickers = listOf(
    StickerDef("👑", "Princess", Color(0xFFFFE082)),
    StickerDef("🎩", "Magician", Color(0xFFD1C4E9)),
    StickerDef("🕶️", "Cool Star", Color(0xFFB3E5FC)),
    StickerDef("🎀", "Pretty Bow", Color(0xFFF8BBD0)),
    StickerDef("🧢", "Sports Star", Color(0xFFC8E6C9)),
    StickerDef("🌟", "Superstar", Color(0xFFFFF59D)),
    StickerDef("🦄", "Unicorn", Color(0xFFE1BEE7)),
    StickerDef("🐱", "Kitty", Color(0xFFFFCCBC)),
    StickerDef("🌈", "Rainbow", Color(0xFFB2EBF2)),
    StickerDef("🎈", "Party Queen", Color(0xFFFFCDD2)),
    StickerDef("🚀", "Astronaut", Color(0xFFBBDEFB)),
    StickerDef("🏆", "Champion", Color(0xFFFFE0B2)),
)

@Composable
fun StickerScreen(app: AppServices, onBack: () -> Unit) {
    val stars = app.store.stars
    val unlocked = (stars / ProgressStore.STARS_PER_STICKER).coerceAtMost(stickers.size)
    val name = app.profile.name.trim()
    var celebrating by remember { mutableStateOf(-1) }
    var confetti by remember { mutableIntStateOf(0) }

    LaunchedEffect(celebrating) {
        if (celebrating >= 0) {
            delay(2500)
            celebrating = -1
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar("My Stickers", onBack, stars)
            Text(
                "Every 10 ⭐ unlocks a new sticker!  ($unlocked / ${stickers.size})",
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                color = Soft,
                fontSize = 16.sp,
            )
            if (app.profile.photos.isEmpty()) {
                Text(
                    "Grown-ups: add her photo in 🔒 Grown-ups to put her face on the stickers.",
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Coral,
                    fontSize = 14.sp,
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                itemsIndexed(stickers) { i, s ->
                    val open = i < unlocked
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (open) s.color else Color(0xFFEDE7F6))
                            .clickable {
                                if (open) {
                                    celebrating = i
                                    confetti++
                                    app.sfx.tada()
                                    val line = if (name.isBlank()) "${s.title}!" else "$name the ${s.title}!"
                                    app.speaker.say(line)
                                } else {
                                    app.sfx.boing()
                                    val more = (i + 1) * ProgressStore.STARS_PER_STICKER - stars
                                    app.speaker.say("Get $more more stars to open this one!")
                                }
                            }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (open) {
                            BuddyAvatar(
                                app.profile.photo(i),
                                if (celebrating == i) Mood.CHEER else Mood.IDLE,
                                96.dp,
                                accessory = s.accessory,
                            )
                            Text(s.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                        } else {
                            Text("🔒", fontSize = 48.sp)
                            Text(
                                "${(i + 1) * ProgressStore.STARS_PER_STICKER} ⭐",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Soft,
                            )
                        }
                    }
                }
            }
        }
        Confetti(confetti)
    }
}
