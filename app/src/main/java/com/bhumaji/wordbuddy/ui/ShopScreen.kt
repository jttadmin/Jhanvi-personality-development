package com.bhumaji.wordbuddy.ui

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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.ItemKind
import com.bhumaji.wordbuddy.data.Shop
import com.bhumaji.wordbuddy.data.ShopItem

/**
 * Reward Shop: spend earned coins on costumes, backgrounds, toys and badges.
 * Coins have no money value and nothing can be bought with real money.
 */
@Composable
fun ShopScreen(app: AppServices, onBack: () -> Unit) {
    val store = app.store
    var tab by remember { mutableStateOf(ItemKind.COSTUME) }
    var asking by remember { mutableStateOf<ShopItem?>(null) }
    var confetti by remember { mutableIntStateOf(0) }

    fun tap(item: ShopItem) {
        app.sfx.pop()
        val owned = item.id in store.owned || (item.kind == ItemKind.BACKGROUND && item.price == 0)
        when {
            owned && item.kind == ItemKind.COSTUME -> {
                val wearing = store.costume == item.id
                store.wearCostume(if (wearing) null else item.id)
                app.speaker.say(if (wearing) "Costume off!" else "You look wonderful!")
            }

            owned && item.kind == ItemKind.BACKGROUND -> {
                store.useTheme(item.id.removePrefix("bg_"))
                app.speaker.say("${item.name}! So pretty!")
            }

            owned -> app.speaker.say("That's your ${item.name}!")
            store.stars >= item.price -> {
                asking = item
                app.speaker.say("Buy the ${item.name} for ${item.price} coins?")
            }

            else -> {
                val more = item.price - store.stars
                app.speaker.say("You need $more more coins. Keep learning!")
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar("Reward Shop", onBack, store.stars)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ItemKind.entries.forEach { kind ->
                    Text(
                        kind.title,
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (tab == kind) Purple else Color.White)
                            .clickable {
                                app.sfx.pop()
                                tab = kind
                            }
                            .padding(vertical = 10.dp),
                        color = if (tab == kind) Color.White else Ink,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                    )
                }
            }

            val items = remember(tab) {
                if (tab == ItemKind.BACKGROUND) {
                    listOf(ShopItem("bg_meadow", ItemKind.BACKGROUND, "Sunny Meadow", "🌼", 0)) +
                        Shop.items.filter { it.kind == tab }
                } else {
                    Shop.items.filter { it.kind == tab }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(130.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeacherAvatar(Teacher.BUNNY, Mood.IDLE, 80.dp, accessory = store.costumeEmoji)
                        SpeechBubble(
                            when (tab) {
                                ItemKind.COSTUME -> "Tap a costume you own to wear it!"
                                ItemKind.BACKGROUND -> "Change how the whole app looks!"
                                ItemKind.TOY -> "Fill your toy box!"
                                ItemKind.BADGE -> "Collect shiny badges!"
                            },
                            Modifier.weight(1f),
                        )
                    }
                }
                items(items, key = { it.id }) { item ->
                    val owned = item.id in store.owned || item.price == 0
                    val inUse = (item.kind == ItemKind.COSTUME && store.costume == item.id) ||
                        (item.kind == ItemKind.BACKGROUND && "bg_${store.theme}" == item.id)
                    val affordable = store.stars >= item.price
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (owned) Color(0xFFFFF8E1) else Color.White)
                            .border(if (inUse) 4.dp else 0.dp, if (inUse) Grass else Color.Transparent, RoundedCornerShape(24.dp))
                            .clickable { tap(item) }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            item.emoji,
                            Modifier.alpha(if (owned || affordable) 1f else 0.5f),
                            fontSize = 52.sp,
                        )
                        Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(
                            when {
                                inUse -> "✅ Using"
                                owned -> "Yours!"
                                else -> "🪙 ${item.price}"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                inUse || owned -> Grass
                                affordable -> Purple
                                else -> Soft
                            },
                        )
                    }
                }
            }
        }

        asking?.let { item ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { asking = null },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier
                        .padding(32.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(item.emoji, fontSize = 80.sp)
                    Text(item.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Purple)
                    Text("🪙 ${item.price}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Row(
                        Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        RoundButton("✖️", Color(0xFFECEFF1), {
                            app.sfx.pop()
                            asking = null
                        }, size = 80.dp)
                        RoundButton("✔️", Grass, {
                            if (store.buy(item)) {
                                app.sfx.tada()
                                confetti++
                                if (item.kind == ItemKind.COSTUME) store.wearCostume(item.id)
                                if (item.kind == ItemKind.BACKGROUND) store.useTheme(item.id.removePrefix("bg_"))
                                app.speaker.say("Yay! The ${item.name} is yours!")
                            }
                            asking = null
                        }, size = 80.dp)
                    }
                }
            }
        }
        Confetti(confetti)
    }
}
