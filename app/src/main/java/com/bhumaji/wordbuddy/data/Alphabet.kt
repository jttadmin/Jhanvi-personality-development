package com.bhumaji.wordbuddy.data

/** A for Apple… with the letter's sound for phonics. */
data class Letter(val upper: Char, val sound: String, val word: String, val emoji: String) {
    val lower: Char get() = upper.lowercaseChar()
}

object Alphabet {
    val letters = listOf(
        Letter('A', "ah", "apple", "🍎"),
        Letter('B', "buh", "ball", "⚽"),
        Letter('C', "kuh", "cat", "🐱"),
        Letter('D', "duh", "dog", "🐶"),
        Letter('E', "eh", "egg", "🥚"),
        Letter('F', "fuh", "fish", "🐟"),
        Letter('G', "guh", "goat", "🐐"),
        Letter('H', "huh", "hat", "🎩"),
        Letter('I', "ih", "insect", "🐞"),
        Letter('J', "juh", "juice", "🧃"),
        Letter('K', "kuh", "kite", "🪁"),
        Letter('L', "luh", "lion", "🦁"),
        Letter('M', "muh", "moon", "🌙"),
        Letter('N', "nuh", "nose", "👃"),
        Letter('O', "oh", "octopus", "🐙"),
        Letter('P', "puh", "pig", "🐷"),
        Letter('Q', "kwuh", "queen", "👸"),
        Letter('R', "ruh", "rabbit", "🐰"),
        Letter('S', "sss", "sun", "☀️"),
        Letter('T', "tuh", "tiger", "🐯"),
        Letter('U', "uh", "umbrella", "☂️"),
        Letter('V', "vuh", "van", "🚐"),
        Letter('W', "wuh", "watch", "⌚"),
        Letter('X', "ks", "fox", "🦊"),
        Letter('Y', "yuh", "yo-yo", "🪀"),
        Letter('Z', "zuh", "zebra", "🦓"),
    )

    /** How to say the letter's line, e.g. "B says buh. B is for ball." (X is at the end of fox.) */
    fun line(l: Letter): List<String> =
        if (l.upper == 'X') {
            listOf("${l.upper}.", "${l.upper} says ${l.sound}.", "You can hear it at the end of fox!")
        } else {
            listOf("${l.upper}.", "${l.upper} says ${l.sound}.", "${l.upper} is for ${l.word}!")
        }

    private val digraphs = mapOf("ch" to "ch", "sh" to "shh", "th" to "th", "wh" to "wuh", "ph" to "fuh", "kn" to "nuh", "wr" to "ruh")

    /** The beginning sound of a word, e.g. "butterfly" -> "buh", "chef" -> "shh". */
    fun firstSound(word: String): String {
        val w = word.lowercase()
        if (w == "chef") return "shh"
        digraphs.entries.firstOrNull { w.startsWith(it.key) }?.let { return it.value }
        val c = w.firstOrNull() ?: return ""
        val next = w.getOrNull(1)
        if (c == 'c' && next in listOf('e', 'i', 'y')) return "sss"
        if (c == 'g' && next in listOf('e', 'i', 'y')) return "juh"
        return letters.firstOrNull { it.lower == c }?.sound ?: c.toString()
    }
}
