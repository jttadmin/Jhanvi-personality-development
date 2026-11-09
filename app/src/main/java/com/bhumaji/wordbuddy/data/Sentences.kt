package com.bhumaji.wordbuddy.data

/** Word roles, shown as card colours so she starts to feel sentence structure. */
enum class Role(val label: String) {
    WHO("Who"),
    DOES("Does"),
    WHAT("What"),
    DESCRIBE("Describe"),
    LINK("Little word"),
    ASK("Ask"),
}

data class Card(val text: String, val role: Role)

data class BuildSentence(val picture: String, val cards: List<Card>, val end: String) {
    val spoken: String get() = cards.joinToString(" ") { it.text } + end
}

/**
 * Sentence City sentences, written as "word:role" (who, does, what, describe, link, ask).
 * Level 1 simple, 2 "-ing", 3 past / describing / where, 4 questions.
 */
object Sentences {
    private fun s(picture: String, spec: String, end: String = "."): BuildSentence {
        val cards = spec.split(" ").map { token ->
            val (word, role) = token.split(":")
            Card(
                word.replace('_', ' '),
                when (role) {
                    "who" -> Role.WHO
                    "does" -> Role.DOES
                    "what" -> Role.WHAT
                    "desc" -> Role.DESCRIBE
                    "ask" -> Role.ASK
                    else -> Role.LINK
                },
            )
        }
        return BuildSentence(picture, cards, end)
    }

    val levels: Map<Int, List<BuildSentence>> = mapOf(
        1 to listOf(
            s("😀", "I:who am:does happy:desc"),
            s("🐱", "This:who is:does a:link cat:what"),
            s("🍎", "I:who like:does apples:what"),
            s("🐶", "I:who see:does a:link dog:what"),
            s("⚽", "I:who have:does a:link ball:what"),
            s("🥛", "I:who drink:does milk:what"),
            s("🌞", "The:link sun:who is:does hot:desc"),
            s("🐘", "The:link elephant:who is:does big:desc"),
            s("📖", "I:who love:does books:what"),
            s("🐟", "Fish:who can:does swim:does"),
        ),
        2 to listOf(
            s("🍎", "I:who am:does eating:does an:link apple:what"),
            s("🐕", "The:link dog:who is:does running:does"),
            s("🍳", "My:link mother:who is:does cooking:does"),
            s("😴", "The:link baby:who is:does sleeping:does"),
            s("🐦", "The:link bird:who is:does flying:does"),
            s("🎨", "I:who am:does painting:does a:link picture:what"),
            s("🧸", "She:who is:does hugging:does her:link teddy:what"),
            s("🚴", "Papa:who is:does riding:does a:link bicycle:what"),
            s("💃", "We:who are:does dancing:does together:desc"),
            s("🐟", "The:link fish:who are:does swimming:does"),
        ),
        3 to listOf(
            s("🏞️", "I:who went:does to:link the:link park:what"),
            s("🚲", "I:who have:does a:link red:desc bicycle:what"),
            s("🐦", "The:link little:desc bird:who is:does sitting:does on:link the:link tree:what"),
            s("🐱", "The:link cat:who is:does under:link the:link table:what"),
            s("📦", "The:link ball:who is:does in:link the:link box:what"),
            s("🎂", "I:who ate:does a:link big:desc cake:what"),
            s("🌈", "We:who saw:does a:link beautiful:desc rainbow:what"),
            s("🦒", "The:link tall:desc giraffe:who eats:does green:desc leaves:what"),
            s("🏖️", "We:who played:does at:link the:link beach:what"),
            s("🐶", "My:link small:desc dog:who is:does very:link happy:desc"),
        ),
        4 to listOf(
            s("🤔", "What:ask are:does you:who doing:does", "?"),
            s("💧", "Can:ask I:who have:does some:link water:what", "?"),
            s("🧸", "May:ask I:who play:does with:link you:who", "?"),
            s("🧸", "Where:ask is:does my:link teddy:what", "?"),
            s("😊", "How:ask are:does you:who today:desc", "?"),
            s("🍪", "Can:ask I:who have:does a:link biscuit:what", "?"),
            s("🎨", "What:ask is:does your:link favourite:desc colour:what", "?"),
            s("🐾", "Do:ask you:who like:does animals:what", "?"),
            s("🏫", "Can:ask we:who go:does to:link school:what", "?"),
            s("🙋", "What:ask is:does your:link name:what", "?"),
        ),
    )

    val levelNames = mapOf(1 to "Simple", 2 to "Doing", 3 to "Describe", 4 to "Questions")

    fun pick(level: Int, notThis: BuildSentence? = null): BuildSentence {
        val list = levels.getValue(level)
        return list.filter { it != notThis }.random()
    }
}
