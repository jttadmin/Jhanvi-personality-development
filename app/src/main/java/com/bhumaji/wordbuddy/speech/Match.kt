package com.bhumaji.wordbuddy.speech

/**
 * Decides how close what the recogniser heard is to the target word.
 * Deliberately lenient: small children's speech is hard to recognise.
 */
object Match {
    enum class Result { GREAT, CLOSE, MISS }

    private val nonLetters = Regex("[^a-z ]")
    private val spaces = Regex("\\s+")

    fun normalize(s: String): String =
        s.lowercase().replace(nonLetters, " ").replace(spaces, " ").trim()

    fun check(targets: List<String>, heard: List<String>): Result {
        val wanted = targets.map(::normalize).filter { it.isNotEmpty() }
        var best = 0.0
        for (raw in heard) {
            val h = normalize(raw)
            if (h.isEmpty()) continue
            val heardWords = h.split(" ")
            for (t in wanted) {
                if (isSaid(heardWords, t)) return Result.GREAT
                val n = t.split(" ").size
                val lastStart = maxOf(0, heardWords.size - n)
                for (i in 0..lastStart) {
                    val chunk = heardWords.subList(i, minOf(heardWords.size, i + n)).joinToString(" ")
                    best = maxOf(best, similarity(chunk, t))
                }
            }
        }
        return if (best >= 0.6) Result.CLOSE else Result.MISS
    }

    /**
     * For Sentence Star: what share of the sentence's words did she say?
     * Each word may be slightly off (≥ 75% similar). 75%+ = great, 45%+ = close.
     */
    fun checkSentence(sentence: String, heard: List<String>): Result {
        val want = normalize(sentence).split(" ").filter { it.isNotEmpty() }
        if (want.isEmpty()) return Result.MISS
        var best = 0.0
        for (raw in heard) {
            val got = normalize(raw).split(" ").filter { it.isNotEmpty() }
            if (got.isEmpty()) continue
            val hits = want.count { w -> got.any { g -> g == w || similarity(g, w) >= 0.75 } }
            best = maxOf(best, hits.toDouble() / want.size)
        }
        return when {
            best >= 0.75 -> Result.GREAT
            best >= 0.45 -> Result.CLOSE
            else -> Result.MISS
        }
    }

    /** Whole-word match, so a short target like "i" doesn't match inside "fish". */
    private fun isSaid(heardWords: List<String>, target: String): Boolean {
        val t = target.split(" ")
        // the target words appear in order, e.g. "a dog" or "teddy bear please"
        for (i in 0..heardWords.size - t.size) {
            if (heardWords.subList(i, i + t.size) == t) return true
        }
        val squashed = target.replace(" ", "")
        // "butter fly" -> "butterfly", "ice-cream" -> "icecream"
        if (squashed.length >= 4 && heardWords.joinToString("").contains(squashed)) return true
        // simple plurals / endings: "cats", "apples"
        return squashed.length >= 3 && heardWords.any { it.startsWith(squashed) && it.length <= squashed.length + 2 }
    }

    fun similarity(a: String, b: String): Double {
        val longest = maxOf(a.length, b.length)
        if (longest == 0) return 1.0
        return 1.0 - levenshtein(a, b).toDouble() / longest
    }

    private fun levenshtein(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
            }
            val tmp = prev
            prev = cur
            cur = tmp
        }
        return prev[b.length]
    }
}
