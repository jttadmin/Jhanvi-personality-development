package com.bhumaji.wordbuddy.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Status { NEW, PRACTISING, LEARNED }

data class WordStats(
    val heard: Int = 0,
    val sayTries: Int = 0,
    val sayGood: Int = 0,
    val findGood: Int = 0,
    val findMiss: Int = 0,
) {
    val successes: Int get() = sayGood + findGood
    private val misses: Int get() = (sayTries - sayGood) + findMiss
    private val touched: Boolean get() = heard + sayTries + findGood + findMiss > 0

    val status: Status
        get() = when {
            successes >= 3 -> Status.LEARNED
            touched -> Status.PRACTISING
            else -> Status.NEW
        }

    /** Shown on the parent page: tried a few times and missed more than got right. */
    val needsPractice: Boolean
        get() = status != Status.LEARNED && misses >= 2 && misses > successes

    fun encode() = "$heard,$sayTries,$sayGood,$findGood,$findMiss"

    companion object {
        fun decode(raw: String?): WordStats {
            if (raw == null) return WordStats()
            val p = raw.split(",").map { it.toIntOrNull() ?: 0 }
            return WordStats(
                p.getOrElse(0) { 0 }, p.getOrElse(1) { 0 }, p.getOrElse(2) { 0 },
                p.getOrElse(3) { 0 }, p.getOrElse(4) { 0 },
            )
        }
    }
}

/** All progress lives in SharedPreferences on the phone. Nothing leaves the device. */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("word_buddy_progress", Context.MODE_PRIVATE)

    /** Coins she can spend (shown as 🪙). Stored under the old "stars" key. */
    var stars by mutableIntStateOf(prefs.getInt(KEY_STARS, 0))
        private set

    /** All coins ever earned (spending doesn't lower it). Unlocks stickers. */
    var earned by mutableIntStateOf(prefs.getInt(KEY_EARNED, prefs.getInt(KEY_STARS, 0)))
        private set

    /** Shop items she owns. */
    val owned = mutableStateListOf<String>().apply {
        addAll(prefs.getStringSet(KEY_OWNED, emptySet()).orEmpty())
    }

    var costume by mutableStateOf(prefs.getString(KEY_COSTUME, null))
        private set

    var theme by mutableStateOf(prefs.getString(KEY_THEME, "meadow") ?: "meadow")
        private set

    /** Emoji of the costume she is wearing, if any. */
    val costumeEmoji: String? get() = Shop.item(costume)?.emoji

    fun buy(item: ShopItem): Boolean {
        if (item.id in owned || stars < item.price) return false
        stars -= item.price
        owned.add(item.id)
        prefs.edit().putInt(KEY_STARS, stars).putStringSet(KEY_OWNED, owned.toSet()).apply()
        return true
    }

    fun wearCostume(id: String?) {
        costume = id
        prefs.edit().putString(KEY_COSTUME, id).apply()
    }

    fun useTheme(id: String) {
        theme = id
        prefs.edit().putString(KEY_THEME, id).apply()
    }

    /** Bumped on every change so screens showing progress recompose. */
    var version by mutableIntStateOf(0)
        private set

    var slowVoice by mutableStateOf(prefs.getBoolean(KEY_SLOW, false))
        private set

    /** Show the easy word groups (colours, fruits…) too. Off by default. */
    var includeEasy by mutableStateOf(prefs.getBoolean(KEY_EASY, false))
        private set

    fun changeIncludeEasy(on: Boolean) {
        includeEasy = on
        WordBank.includeEasy = on
        prefs.edit().putBoolean(KEY_EASY, on).apply()
        version++
    }

    /** Right / wrong answers per thinking game. */
    fun quizScore(gameId: String): Pair<Int, Int> {
        val p = (prefs.getString("q_$gameId", null) ?: "0,0").split(",").map { it.toIntOrNull() ?: 0 }
        return p.getOrElse(0) { 0 } to p.getOrElse(1) { 0 }
    }

    fun recordQuiz(gameId: String, correct: Boolean) {
        val (good, miss) = quizScore(gameId)
        val next = if (correct) "${good + 1},$miss" else "$good,${miss + 1}"
        prefs.edit().putString("q_$gameId", next).apply()
        version++
    }

    fun stats(id: String): WordStats = WordStats.decode(prefs.getString("w_$id", null))

    private fun update(word: Word, change: (WordStats) -> WordStats) {
        val s = change(stats(word.id))
        prefs.edit().putString("w_${word.id}", s.encode()).apply()
        version++
    }

    /** Returns true the first time she meets this word. */
    fun markHeard(word: Word): Boolean {
        val first = stats(word.id).heard == 0
        update(word) { it.copy(heard = it.heard + 1) }
        return first
    }

    fun recordSay(word: Word, success: Boolean) = update(word) {
        it.copy(sayTries = it.sayTries + 1, sayGood = it.sayGood + if (success) 1 else 0)
    }

    fun recordFind(word: Word, correct: Boolean) = update(word) {
        if (correct) it.copy(findGood = it.findGood + 1) else it.copy(findMiss = it.findMiss + 1)
    }

    /** Adds coins; returns true if this unlocked a new sticker. */
    fun addStars(n: Int): Boolean {
        val before = earned / STARS_PER_STICKER
        stars += n
        earned += n
        prefs.edit().putInt(KEY_STARS, stars).putInt(KEY_EARNED, earned).apply()
        return earned / STARS_PER_STICKER > before && before < STICKER_COUNT
    }

    fun changeSlowVoice(on: Boolean) {
        slowVoice = on
        prefs.edit().putBoolean(KEY_SLOW, on).apply()
    }

    fun reset() {
        prefs.edit().clear().putBoolean(KEY_SLOW, slowVoice).putBoolean(KEY_EASY, includeEasy).apply()
        stars = 0
        earned = 0
        owned.clear()
        costume = null
        theme = "meadow"
        version++
    }

    /** Shuffled, but words she knows least come first. */
    fun practiceOrder(words: List<Word>): List<Word> =
        words.shuffled().sortedBy { stats(it.id).successes.coerceAtMost(3) }

    companion object {
        const val STARS_PER_STICKER = 50
        const val STICKER_COUNT = 12
        private const val KEY_STARS = "stars"
        private const val KEY_SLOW = "slow_voice"
        private const val KEY_EASY = "include_easy"
        private const val KEY_EARNED = "earned"
        private const val KEY_OWNED = "owned"
        private const val KEY_COSTUME = "costume"
        private const val KEY_THEME = "theme"
    }
}
