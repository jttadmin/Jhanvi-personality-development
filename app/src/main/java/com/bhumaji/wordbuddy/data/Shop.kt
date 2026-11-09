package com.bhumaji.wordbuddy.data

import androidx.compose.ui.graphics.Color

enum class ItemKind(val title: String) {
    COSTUME("Costumes"),
    BACKGROUND("Backgrounds"),
    TOY("Toy Box"),
    BADGE("Badges"),
}

/** Things she can buy with coins she earned. Coins have no real-money value; nothing is ever sold. */
data class ShopItem(val id: String, val kind: ItemKind, val name: String, val emoji: String, val price: Int)

/** App backgrounds: soft gradients with floating decorations. */
data class BgTheme(val id: String, val name: String, val emoji: String, val colors: List<Color>, val decor: List<String>, val price: Int)

object Shop {
    val themes = listOf(
        BgTheme("meadow", "Sunny Meadow", "🌼", listOf(Color(0xFFFFF1D0), Color(0xFFFFF8E7), Color(0xFFE1F5FE)), listOf("🌼", "☁️", "🦋"), 0),
        BgTheme("ocean", "Under the Sea", "🐠", listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF80DEEA)), listOf("🐠", "🐚", "💧"), 60),
        BgTheme("space", "Starry Space", "🪐", listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFFC5CAE9)), listOf("⭐", "🪐", "✨"), 80),
        BgTheme("candy", "Candy Land", "🍭", listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF3E5F5)), listOf("🍭", "🍬", "🧁"), 80),
        BgTheme("jungle", "Jungle Fun", "🌴", listOf(Color(0xFFE8F5E9), Color(0xFFDCEDC8), Color(0xFFFFFDE7)), listOf("🌴", "🦜", "🍃"), 70),
        BgTheme("snow", "Snowy Land", "⛄", listOf(Color(0xFFE3F2FD), Color(0xFFFFFFFF), Color(0xFFE1F5FE)), listOf("❄️", "⛄", "✨"), 70),
        BgTheme("garden", "Magical Garden", "🌷", listOf(Color(0xFFF1F8E9), Color(0xFFFCE4EC), Color(0xFFFFF8E1)), listOf("🌷", "🌸", "🍄"), 100),
    )

    fun theme(id: String): BgTheme = themes.firstOrNull { it.id == id } ?: themes.first()

    val items: List<ShopItem> = listOf(
        ShopItem("c_flower", ItemKind.COSTUME, "Flower", "🌸", 25),
        ShopItem("c_bow", ItemKind.COSTUME, "Pretty Bow", "🎀", 30),
        ShopItem("c_cap", ItemKind.COSTUME, "Cool Cap", "🧢", 30),
        ShopItem("c_sunhat", ItemKind.COSTUME, "Sun Hat", "👒", 40),
        ShopItem("c_tophat", ItemKind.COSTUME, "Magic Hat", "🎩", 40),
        ShopItem("c_shades", ItemKind.COSTUME, "Sunglasses", "🕶️", 50),
        ShopItem("c_crown", ItemKind.COSTUME, "Crown", "👑", 60),
        ShopItem("c_star", ItemKind.COSTUME, "Star Clip", "🌟", 70),
        ShopItem("c_grad", ItemKind.COSTUME, "Clever Cap", "🎓", 80),
        ShopItem("c_unicorn", ItemKind.COSTUME, "Unicorn Horn", "🦄", 100),
    ) + themes.filter { it.price > 0 }.map {
        ShopItem("bg_${it.id}", ItemKind.BACKGROUND, it.name, it.emoji, it.price)
    } + listOf(
        ShopItem("t_balloon", ItemKind.TOY, "Balloon", "🎈", 20),
        ShopItem("t_yoyo", ItemKind.TOY, "Yo-yo", "🪀", 25),
        ShopItem("t_teddy", ItemKind.TOY, "Teddy", "🧸", 30),
        ShopItem("t_paints", ItemKind.TOY, "Paints", "🎨", 30),
        ShopItem("t_puzzle", ItemKind.TOY, "Puzzle", "🧩", 35),
        ShopItem("t_kite", ItemKind.TOY, "Kite", "🪁", 40),
        ShopItem("t_train", ItemKind.TOY, "Toy Train", "🚂", 50),
        ShopItem("t_skate", ItemKind.TOY, "Skateboard", "🛹", 60),
        ShopItem("t_heli", ItemKind.TOY, "Helicopter", "🚁", 60),
        ShopItem("t_dino", ItemKind.TOY, "Dino", "🦖", 70),
        ShopItem("t_carousel", ItemKind.TOY, "Carousel", "🎠", 80),
        ShopItem("b_bronze", ItemKind.BADGE, "Bronze Learner", "🥉", 50),
        ShopItem("b_silver", ItemKind.BADGE, "Silver Speaker", "🥈", 120),
        ShopItem("b_gold", ItemKind.BADGE, "Gold Star", "🥇", 250),
        ShopItem("b_hero", ItemKind.BADGE, "Word Hero", "🎖️", 300),
        ShopItem("b_champ", ItemKind.BADGE, "Champion", "🏆", 500),
        ShopItem("b_diamond", ItemKind.BADGE, "Diamond Mind", "💎", 800),
    )

    fun item(id: String?): ShopItem? = items.firstOrNull { it.id == id }
}
