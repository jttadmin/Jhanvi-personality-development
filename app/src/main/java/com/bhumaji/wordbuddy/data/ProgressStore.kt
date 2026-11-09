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
    /** Spaced-repetition box 0–5 and the day (epoch day) it is next due. */
    val box: Int = 0,
    val due: Int = 0,
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

    fun encode() = "$heard,$sayTries,$sayGood,$findGood,$findMiss,$box,$due"

    companion object {
        fun decode(raw: String?): WordStats {
            if (raw == null) return WordStats()
            val p = raw.split(",").map { it.toIntOrNull() ?: 0 }
            return WordStats(
                p.getOrElse(0) { 0 }, p.getOrElse(1) { 0 }, p.getOrElse(2) { 0 },
                p.getOrElse(3) { 0 }, p.getOrElse(4) { 0 }, p.getOrElse(5) { 0 }, p.getOrElse(6) { 0 },
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
        leitner(it.copy(sayTries = it.sayTries + 1, sayGood = it.sayGood + if (success) 1 else 0), success)
    }

    fun recordFind(word: Word, correct: Boolean) = update(word) {
        leitner(if (correct) it.copy(findGood = it.findGood + 1) else it.copy(findMiss = it.findMiss + 1), correct)
    }

    // ---------- Spaced repetition (Leitner boxes) ----------

    fun today(): Int = java.time.LocalDate.now().toEpochDay().toInt()

    /** Right answer: move up a box and wait longer. Wrong: back to box 1, see it again tomorrow. */
    private fun leitner(s: WordStats, success: Boolean): WordStats {
        val box = if (success) (s.box + 1).coerceAtMost(5) else 1
        return s.copy(box = box, due = today() + INTERVALS[box])
    }

    /** Words she has met that are due for revision today, hardest first. */
    fun dueWords(pool: List<Word>): List<Word> {
        val t = today()
        return pool.map { it to stats(it.id) }
            .filter { (_, s) -> (s.heard > 0 || s.sayTries > 0) && s.due <= t }
            .sortedBy { (_, s) -> s.box }
            .map { it.first }
    }

    /** Today's new words (same list all day), chosen from words she hasn't met yet. */
    fun todaysNewWords(pool: List<Word>, count: Int = 6): List<Word> {
        val t = today()
        if (prefs.getInt(KEY_DAILY_DAY, -1) == t) {
            val ids = prefs.getString(KEY_DAILY_WORDS, "").orEmpty().split(",").toSet()
            val saved = pool.filter { it.id in ids }
            if (saved.isNotEmpty()) return saved
        }
        val fresh = pool.filter { stats(it.id).heard == 0 && it.color == null }.shuffled()
        val picked = (fresh.ifEmpty { practiceOrder(pool) }).take(count)
        prefs.edit()
            .putInt(KEY_DAILY_DAY, t)
            .putString(KEY_DAILY_WORDS, picked.joinToString(",") { it.id })
            .apply()
        return picked
    }

    // ---------- Daily adventure & session length ----------

    var sessionMinutes by mutableIntStateOf(prefs.getInt(KEY_SESSION, 15))
        private set

    fun changeSessionMinutes(m: Int) {
        sessionMinutes = m
        prefs.edit().putInt(KEY_SESSION, m).apply()
    }

    fun adventureDone(): Set<String> =
        if (prefs.getInt(KEY_ADV_DAY, -1) == today()) {
            prefs.getString(KEY_ADV_DONE, "").orEmpty().split(",").filter { it.isNotEmpty() }.toSet()
        } else {
            emptySet()
        }

    fun markStation(id: String) {
        val done = adventureDone() + id
        prefs.edit().putInt(KEY_ADV_DAY, today()).putString(KEY_ADV_DONE, done.joinToString(",")).apply()
        version++
    }

    /** True once per day: the treasure chest can be opened. */
    fun claimAdventureReward(): Boolean {
        if (prefs.getInt(KEY_ADV_REWARD, -1) == today()) return false
        prefs.edit().putInt(KEY_ADV_REWARD, today()).apply()
        return true
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
        prefs.edit().clear().putBoolean(KEY_SLOW, slowVoice).putBoolean(KEY_EASY, includeEasy)
            .putInt(KEY_SESSION, sessionMinutes).apply()
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
        private const val KEY_DAILY_DAY = "daily_day"
        private const val KEY_DAILY_WORDS = "daily_words"
        private const val KEY_SESSION = "session_minutes"
        private const val KEY_ADV_DAY = "adv_day"
        private const val KEY_ADV_DONE = "adv_done"
        private const val KEY_ADV_REWARD = "adv_reward"
        private val INTERVALS = intArrayOf(0, 1, 2, 4, 7, 14)
    }
}
