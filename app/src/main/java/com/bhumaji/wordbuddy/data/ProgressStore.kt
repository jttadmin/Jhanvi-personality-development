package com.bhumaji.wordbuddy.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    var stars by mutableIntStateOf(prefs.getInt(KEY_STARS, 0))
        private set

    /** Bumped on every change so screens showing progress recompose. */
    var version by mutableIntStateOf(0)
        private set

    var slowVoice by mutableStateOf(prefs.getBoolean(KEY_SLOW, false))
        private set

    fun stats(id: String): WordStats = WordStats.decode(prefs.getString("w_$id", null))

    private fun update(word: Word, change: (WordStats) -> WordStats) {
        val s = change(stats(word.id))
        prefs.edit().putString("w_${word.id}", s.encode()).apply()
        version++
    }

    fun markHeard(word: Word) = update(word) { it.copy(heard = it.heard + 1) }

    fun recordSay(word: Word, success: Boolean) = update(word) {
        it.copy(sayTries = it.sayTries + 1, sayGood = it.sayGood + if (success) 1 else 0)
    }

    fun recordFind(word: Word, correct: Boolean) = update(word) {
        if (correct) it.copy(findGood = it.findGood + 1) else it.copy(findMiss = it.findMiss + 1)
    }

    fun addStars(n: Int) {
        stars += n
        prefs.edit().putInt(KEY_STARS, stars).apply()
    }

    fun changeSlowVoice(on: Boolean) {
        slowVoice = on
        prefs.edit().putBoolean(KEY_SLOW, on).apply()
    }

    fun reset() {
        prefs.edit().clear().putBoolean(KEY_SLOW, slowVoice).apply()
        stars = 0
        version++
    }

    /** Shuffled, but words she knows least come first. */
    fun practiceOrder(words: List<Word>): List<Word> =
        words.shuffled().sortedBy { stats(it.id).successes.coerceAtMost(3) }

    private companion object {
        const val KEY_STARS = "stars"
        const val KEY_SLOW = "slow_voice"
    }
}
