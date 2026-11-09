package com.bhumaji.wordbuddy.data

import androidx.compose.ui.graphics.Color

/**
 * One word the child learns.
 *  - [emoji] is the picture (no image files needed). Colour words use [color] instead.
 *  - [alsoAccept] lists sound-alikes the speech recogniser often returns for small kids
 *    (e.g. "eye" is often heard as "I").
 *
 * To add a word, add one line to the right list below. Ids must be unique.
 */
data class Word(
    val id: String,
    val text: String,
    val emoji: String,
    val sentence: String,
    val category: String,
    val color: Color? = null,
    val alsoAccept: List<String> = emptyList(),
) {
    val targets: List<String> get() = listOf(text) + alsoAccept
}

data class Category(
    val id: String,
    val title: String,
    val emoji: String,
    val tint: Color,
    val easy: Boolean = false,
)

object WordBank {
    const val ALL = "all"

    val everything = Category(ALL, "Everything", "✨", Color(0xFF6C4AB6))

    private val easyCategories = listOf(
        Category("animals", "Animals", "🐾", Color(0xFFFF8A65), easy = true),
        Category("fruits", "Fruits", "🍎", Color(0xFFEF5350), easy = true),
        Category("colours", "Colours", "🎨", Color(0xFFAB47BC), easy = true),
        Category("body", "My Body", "🙂", Color(0xFFFFA726), easy = true),
        Category("vehicles", "Vehicles", "🚗", Color(0xFF42A5F5), easy = true),
        Category("food", "Food", "🍞", Color(0xFF8D6E63), easy = true),
        Category("home", "At Home", "🏠", Color(0xFF26A69A), easy = true),
        Category("nature", "Nature", "🌳", Color(0xFF66BB6A), easy = true),
    )

    /** Grown-ups setting. Off = only the advanced word groups are shown. */
    var includeEasy = false

    private val allCategories = AdvancedWords.categories + easyCategories

    val categories: List<Category>
        get() = allCategories.filter { includeEasy || !it.easy }

    private fun animal(id: String, e: String, s: String, alt: List<String> = emptyList()) =
        Word(id, id, e, s, "animals", alsoAccept = alt)

    private val animals = listOf(
        animal("dog", "🐶", "The dog says woof woof."),
        animal("cat", "🐱", "The cat says meow."),
        animal("cow", "🐮", "The cow gives us milk."),
        animal("duck", "🦆", "The duck says quack quack."),
        animal("fish", "🐟", "The fish swims in the water."),
        animal("lion", "🦁", "The lion says roar!"),
        animal("tiger", "🐯", "The tiger has stripes."),
        animal("elephant", "🐘", "The elephant has a long trunk."),
        animal("monkey", "🐒", "The monkey loves bananas."),
        animal("rabbit", "🐰", "The rabbit goes hop, hop, hop.", listOf("bunny")),
        animal("horse", "🐴", "The horse runs very fast."),
        animal("bird", "🐦", "The bird can fly."),
        animal("frog", "🐸", "The frog jumps high."),
        animal("butterfly", "🦋", "The butterfly has pretty wings."),
    )

    private fun fruit(id: String, e: String, s: String, alt: List<String> = emptyList()) =
        Word(id, id, e, s, "fruits", alsoAccept = alt)

    private val fruits = listOf(
        fruit("apple", "🍎", "An apple is red and sweet."),
        fruit("banana", "🍌", "A banana is long and yellow."),
        fruit("mango", "🥭", "Mango is juicy and yummy."),
        fruit("grapes", "🍇", "Grapes are small and round.", listOf("grape")),
        fruit("orange", "🍊", "We peel an orange."),
        fruit("watermelon", "🍉", "A watermelon is big and green."),
        fruit("strawberry", "🍓", "A strawberry is red."),
        fruit("pineapple", "🍍", "A pineapple is spiky outside."),
        fruit("cherry", "🍒", "Cherries are tiny and red.", listOf("cherries")),
        fruit("pear", "🍐", "A pear is green and sweet.", listOf("pair")),
    )

    private fun colour(name: String, c: Long, s: String, alt: List<String> = emptyList()) =
        Word("c_$name", name, "", s, "colours", color = Color(c), alsoAccept = alt)

    private val colours = listOf(
        colour("red", 0xFFE53935, "The apple is red.", listOf("read")),
        colour("blue", 0xFF1E88E5, "The sky is blue.", listOf("blew")),
        colour("green", 0xFF43A047, "The leaf is green."),
        colour("yellow", 0xFFFDD835, "The sun is yellow."),
        colour("orange", 0xFFFB8C00, "The carrot is orange."),
        colour("pink", 0xFFF06292, "The flower is pink."),
        colour("purple", 0xFF8E24AA, "Grapes can be purple."),
        colour("black", 0xFF212121, "The night is black."),
        colour("white", 0xFFFFFFFF, "Milk is white."),
        colour("brown", 0xFF6D4C41, "Chocolate is brown."),
    )

    private fun body(id: String, e: String, s: String, alt: List<String> = emptyList()) =
        Word(id, id, e, s, "body", alsoAccept = alt)

    private val bodyParts = listOf(
        body("eye", "👁️", "I see with my eyes.", listOf("eyes", "i", "aye")),
        body("ear", "👂", "I hear with my ears.", listOf("ears")),
        body("nose", "👃", "I smell with my nose.", listOf("knows", "noes")),
        body("mouth", "👄", "I eat with my mouth."),
        body("tongue", "👅", "My tongue can taste.", listOf("tung")),
        body("tooth", "🦷", "I brush my teeth.", listOf("teeth")),
        body("hand", "✋", "I wave my hand.", listOf("hands")),
        body("foot", "🦶", "I walk with my feet.", listOf("feet")),
    )

    private val vehicles = listOf(
        Word("car", "car", "🚗", "We go in the car.", "vehicles", alsoAccept = listOf("cars")),
        Word("bus", "bus", "🚌", "The bus takes us to school.", "vehicles"),
        Word("train", "train", "🚆", "The train goes choo choo.", "vehicles"),
        Word("aeroplane", "aeroplane", "✈️", "The aeroplane flies in the sky.", "vehicles",
            alsoAccept = listOf("airplane", "aero plane", "plane", "air plane")),
        Word("bicycle", "bicycle", "🚲", "I ride my bicycle.", "vehicles", alsoAccept = listOf("cycle", "bike")),
        Word("boat", "boat", "⛵", "The boat sails on water.", "vehicles"),
        Word("truck", "truck", "🚚", "The truck is big.", "vehicles", alsoAccept = listOf("lorry")),
        Word("auto", "auto", "🛺", "The auto has three wheels.", "vehicles", alsoAccept = listOf("auto rickshaw", "rickshaw")),
    )

    private fun food(id: String, text: String, e: String, s: String, alt: List<String> = emptyList()) =
        Word(id, text, e, s, "food", alsoAccept = alt)

    private val foods = listOf(
        food("milk", "milk", "🥛", "I drink milk."),
        food("water", "water", "💧", "Water keeps me strong."),
        food("bread", "bread", "🍞", "Bread is soft."),
        food("rice", "rice", "🍚", "We eat rice for lunch."),
        food("egg", "egg", "🥚", "The egg is round.", listOf("eggs")),
        food("cake", "cake", "🎂", "Cake for my birthday!"),
        food("icecream", "ice cream", "🍦", "Ice cream is cold.", listOf("icecream", "ice-cream")),
        food("biscuit", "biscuit", "🍪", "A crunchy biscuit.", listOf("cookie", "biscuits")),
    )

    private fun home(id: String, text: String, e: String, s: String, alt: List<String> = emptyList()) =
        Word(id, text, e, s, "home", alsoAccept = alt)

    private val homeThings = listOf(
        home("ball", "ball", "⚽", "I kick the ball.", listOf("bowl", "balls")),
        home("book", "book", "📖", "I read a book."),
        home("chair", "chair", "🪑", "I sit on the chair."),
        home("bed", "bed", "🛏️", "I sleep in my bed."),
        home("door", "door", "🚪", "Please open the door."),
        home("clock", "clock", "⏰", "The clock goes tick tock."),
        home("cup", "cup", "☕", "I drink from a cup."),
        home("spoon", "spoon", "🥄", "I eat with a spoon."),
        home("teddy", "teddy", "🧸", "I hug my teddy.", listOf("teddy bear", "teddybear")),
    )

    private fun nature(id: String, e: String, s: String, alt: List<String> = emptyList()) =
        Word(id, id, e, s, "nature", alsoAccept = alt)

    private val natureThings = listOf(
        nature("sun", "☀️", "The sun is hot.", listOf("son")),
        nature("moon", "🌙", "The moon comes at night."),
        nature("star", "⭐", "Twinkle twinkle little star.", listOf("stars")),
        nature("tree", "🌳", "The tree is tall.", listOf("three")),
        nature("flower", "🌸", "The flower smells nice.", listOf("flour", "flowers")),
        nature("rain", "🌧️", "Rain makes puddles.", listOf("rains")),
        nature("cloud", "☁️", "The cloud is white and fluffy.", listOf("clouds")),
        nature("rainbow", "🌈", "A rainbow has many colours."),
    )

    /** Every word, easy and advanced. */
    val words: List<Word> =
        AdvancedWords.words + animals + fruits + colours + bodyParts + vehicles + foods + homeThings + natureThings

    /** Words in the groups currently shown. */
    val visibleWords: List<Word>
        get() {
            val ids = categories.map { it.id }.toSet()
            return words.filter { it.category in ids }
        }

    fun inCategory(id: String): List<Word> =
        if (id == ALL) visibleWords else words.filter { it.category == id }

    fun category(id: String): Category =
        if (id == ALL) everything else allCategories.firstOrNull { it.id == id } ?: everything

    /** What the app says in the Find It game. */
    fun findPrompt(w: Word): String =
        if (w.color != null) "Find the colour ${w.text}!" else "Find the ${w.text}!"
}
