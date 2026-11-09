package com.bhumaji.wordbuddy.data

/** A picture with a name, used by the thinking games. */
data class Pic(val label: String, val emoji: String)

/**
 * One question in a thinking game.
 * [prompt] is shown big above the choices (a picture, or a letter for Sound Detective).
 */
data class QuizRound(
    val question: String,
    val spoken: List<String>,
    val prompt: String?,
    val options: List<Pic>,
    val answer: Int,
    val success: String,
) {
    val correct: Pic get() = options[answer]
}

enum class Game(val id: String, val title: String, val emoji: String, val subtitle: String) {
    RHYME("rhyme", "Rhyme Time", "🎵", "Find words that sound alike"),
    SOUNDS("sounds", "Sound Detective", "🔤", "What sound does it start with?"),
    OPPOSITES("opposites", "Opposites", "↔️", "Hot and cold, big and small"),
    THINK("think", "Think & Find", "🧠", "Use your brain!"),
    SYLLABLES("syllables", "Syllable Clap", "👏", "How many claps?"),
}

object Games {

    fun next(game: Game): QuizRound = when (game) {
        Game.RHYME -> rhyme()
        Game.SOUNDS -> sound()
        Game.OPPOSITES -> opposite()
        Game.THINK -> think()
        Game.SYLLABLES -> syllables()
    }

    // ---------- Syllable Clap ----------
    private val clapNumbers = listOf(Pic("one", "1️⃣"), Pic("two", "2️⃣"), Pic("three", "3️⃣"), Pic("four", "4️⃣"))

    private fun syllables(): QuizRound {
        val word = WordBank.visibleWords
            .filter { it.color == null && Syllables.count(it.text) in 1..4 }
            .let { pool ->
                // more multi-syllable words: they are the interesting ones
                val multi = pool.filter { Syllables.count(it.text) > 1 }
                if (multi.isNotEmpty() && (0..3).random() > 0) multi.random() else pool.random()
            }
        val parts = Syllables.split(word.text)
        val answer = clapNumbers[parts.size - 1]
        val others = clapNumbers.filter { it != answer }.shuffled()
        return build(answer, others) { options, idx ->
            QuizRound(
                question = "How many claps in ${word.text}?",
                spoken = listOf("How many claps in", "${word.text}?", parts.joinToString(", ")),
                prompt = word.emoji,
                options = options,
                answer = idx,
                success = "${parts.joinToString(", ")}! ${answer.label.replaceFirstChar { it.uppercase() }} ${if (parts.size == 1) "clap" else "claps"}!",
            )
        }
    }

    private fun <T> build(answer: Pic, others: List<Pic>, make: (List<Pic>, Int) -> T): T {
        val options = (others.take(2) + answer).shuffled()
        return make(options, options.indexOf(answer))
    }

    // ---------- Rhyme Time ----------
    private val rhymes: List<List<Pic>> = listOf(
        listOf(Pic("cat", "🐱"), Pic("hat", "🎩"), Pic("bat", "🦇")),
        listOf(Pic("dog", "🐶"), Pic("frog", "🐸"), Pic("log", "🪵")),
        listOf(Pic("bee", "🐝"), Pic("tree", "🌳"), Pic("key", "🔑")),
        listOf(Pic("car", "🚗"), Pic("star", "⭐")),
        listOf(Pic("cake", "🎂"), Pic("snake", "🐍")),
        listOf(Pic("mouse", "🐭"), Pic("house", "🏠")),
        listOf(Pic("moon", "🌙"), Pic("spoon", "🥄"), Pic("balloon", "🎈")),
        listOf(Pic("boat", "⛵"), Pic("goat", "🐐"), Pic("coat", "🧥")),
        listOf(Pic("bear", "🐻"), Pic("pear", "🍐"), Pic("chair", "🪑")),
        listOf(Pic("ring", "💍"), Pic("king", "🤴")),
        listOf(Pic("fish", "🐟"), Pic("dish", "🍽️")),
        listOf(Pic("clock", "⏰"), Pic("sock", "🧦"), Pic("rock", "🪨")),
        listOf(Pic("fox", "🦊"), Pic("box", "📦")),
        listOf(Pic("bed", "🛏️"), Pic("red", "🔴")),
        listOf(Pic("sun", "☀️"), Pic("one", "1️⃣")),
        listOf(Pic("kite", "🪁"), Pic("light", "💡")),
        listOf(Pic("train", "🚆"), Pic("rain", "🌧️"), Pic("plane", "✈️")),
        listOf(Pic("bell", "🔔"), Pic("shell", "🐚")),
        listOf(Pic("whale", "🐋"), Pic("snail", "🐌")),
        listOf(Pic("bug", "🐛"), Pic("mug", "☕"), Pic("rug", "🧶")),
    )

    private fun rhyme(): QuizRound {
        val group = rhymes.random()
        val (target, answer) = group.shuffled().take(2)
        val others = rhymes.filter { it !== group }.shuffled().take(2).map { it.random() }
        return build(answer, others) { options, idx ->
            QuizRound(
                question = "What rhymes with ${target.label}?",
                spoken = listOf("What rhymes with", "${target.label}?"),
                prompt = target.emoji,
                options = options,
                answer = idx,
                success = "${target.label}, ${answer.label}! They rhyme!",
            )
        }
    }

    // ---------- Sound Detective ----------
    private val letterSounds = mapOf(
        'b' to "buh", 'c' to "kuh", 'd' to "duh", 'f' to "fuh", 'g' to "guh", 'h' to "huh",
        'j' to "juh", 'k' to "kuh", 'l' to "luh", 'm' to "muh", 'n' to "nuh", 'p' to "puh",
        'r' to "ruh", 's' to "sss", 't' to "tuh", 'v' to "vuh", 'w' to "wuh", 'y' to "yuh", 'z' to "zuh",
    )
    private val trickyStarts = listOf("ch", "sh", "th", "wh", "kn", "ph", "wr", "gn", "ce", "ci", "cy", "ge", "gi", "gy")

    /** Same first sound: c, k and q all say "kuh". */
    private fun soundGroup(c: Char): Char = if (c == 'k' || c == 'q') 'c' else c

    private val soundPool: List<Pic> by lazy {
        (WordBank.words.filter { it.color == null }.map { Pic(it.text, it.emoji) } + rhymes.flatten())
            .filter { p ->
                val t = p.label
                !t.contains(' ') && t.first() in letterSounds && trickyStarts.none { t.startsWith(it) }
            }
            .distinctBy { it.emoji }
    }

    private fun sound(): QuizRound {
        val answer = soundPool.random()
        val letter = answer.label.first()
        val sound = letterSounds.getValue(letter)
        val others = soundPool
            .filter { soundGroup(it.label.first()) != soundGroup(letter) }
            .shuffled()
            .distinctBy { soundGroup(it.label.first()) }
        return build(answer, others) { options, idx ->
            QuizRound(
                question = "Which one starts with ${letter.uppercaseChar()}?  “$sound”",
                spoken = listOf("${letter.uppercaseChar()}.", "$sound, $sound.", "Which one starts with $sound?"),
                prompt = "${letter.uppercaseChar()}${letter}",
                options = options,
                answer = idx,
                success = "${answer.label} starts with $sound!",
            )
        }
    }

    // ---------- Opposites ----------
    private val opposites: List<Pair<Pic, Pic>> = listOf(
        Pic("hot", "🔥") to Pic("cold", "🥶"),
        Pic("big", "🐘") to Pic("small", "🐭"),
        Pic("fast", "🐆") to Pic("slow", "🦥"),
        Pic("up", "⬆️") to Pic("down", "⬇️"),
        Pic("day", "🌞") to Pic("night", "🌙"),
        Pic("happy", "😀") to Pic("sad", "😢"),
        Pic("open", "🔓") to Pic("closed", "🔒"),
        Pic("wet", "💦") to Pic("dry", "🌵"),
        Pic("loud", "📢") to Pic("quiet", "🔇"),
        Pic("clean", "🛁") to Pic("dirty", "🐷"),
        Pic("laugh", "😂") to Pic("cry", "😭"),
        Pic("old", "👴") to Pic("young", "👶"),
        Pic("left", "⬅️") to Pic("right", "➡️"),
        Pic("stop", "🛑") to Pic("go", "🟢"),
        Pic("awake", "😃") to Pic("asleep", "😴"),
        Pic("tall", "🦒") to Pic("short", "🐕"),
    )

    private fun opposite(): QuizRound {
        val pair = opposites.random()
        val (from, answer) = if (listOf(true, false).random()) pair else pair.second to pair.first
        val others = opposites.filter { it !== pair }.shuffled().take(2).map { listOf(it.first, it.second).random() }
        return build(answer, others) { options, idx ->
            QuizRound(
                question = "What is the opposite of ${from.label}?",
                spoken = listOf("What is the opposite of", "${from.label}?"),
                prompt = from.emoji,
                options = options,
                answer = idx,
                success = "${from.label} and ${answer.label} are opposites!",
            )
        }
    }

    // ---------- Think & Find ----------
    private class Riddle(val question: String, val answer: Pic, val wrong: List<Pic>, val why: String)

    private val riddles = listOf(
        Riddle("Which one can fly?", Pic("butterfly", "🦋"), listOf(Pic("turtle", "🐢"), Pic("elephant", "🐘")), "The butterfly can fly!"),
        Riddle("Which one lives in water?", Pic("dolphin", "🐬"), listOf(Pic("giraffe", "🦒"), Pic("camel", "🐫")), "The dolphin lives in the sea!"),
        Riddle("Which one do we wear?", Pic("coat", "🧥"), listOf(Pic("banana", "🍌"), Pic("car", "🚗")), "We wear a coat!"),
        Riddle("Which one can we eat?", Pic("apple", "🍎"), listOf(Pic("shoe", "👟"), Pic("box", "📦")), "We can eat an apple!"),
        Riddle("Who helps sick people?", Pic("doctor", "🧑‍⚕️"), listOf(Pic("farmer", "🧑‍🌾"), Pic("pilot", "🧑‍✈️")), "The doctor helps sick people!"),
        Riddle("Who flies an aeroplane?", Pic("pilot", "🧑‍✈️"), listOf(Pic("chef", "🧑‍🍳"), Pic("teacher", "🧑‍🏫")), "The pilot flies the aeroplane!"),
        Riddle("Who puts out fires?", Pic("firefighter", "🧑‍🚒"), listOf(Pic("doctor", "🧑‍⚕️"), Pic("artist", "🧑‍🎨")), "The firefighter puts out fires!"),
        Riddle("Who grows our food?", Pic("farmer", "🧑‍🌾"), listOf(Pic("astronaut", "🧑‍🚀"), Pic("postman", "📮")), "The farmer grows our food!"),
        Riddle("Which one is cold?", Pic("ice cream", "🍦"), listOf(Pic("fire", "🔥"), Pic("tea", "☕")), "Ice cream is cold!"),
        Riddle("Which one gives us milk?", Pic("cow", "🐄"), listOf(Pic("dog", "🐶"), Pic("fish", "🐟")), "The cow gives us milk!"),
        Riddle("Which one lays eggs?", Pic("hen", "🐔"), listOf(Pic("cow", "🐄"), Pic("cat", "🐱")), "The hen lays eggs!"),
        Riddle("What do we use when it rains?", Pic("umbrella", "☂️"), listOf(Pic("sunglasses", "🕶️"), Pic("ball", "⚽")), "We use an umbrella in the rain!"),
        Riddle("What do we use to cut paper?", Pic("scissors", "✂️"), listOf(Pic("spoon", "🥄"), Pic("crayon", "🖍️")), "Scissors cut paper!"),
        Riddle("Which one makes music?", Pic("drum", "🥁"), listOf(Pic("chair", "🪑"), Pic("bread", "🍞")), "The drum makes music!"),
        Riddle("Which one has eight arms?", Pic("octopus", "🐙"), listOf(Pic("dog", "🐶"), Pic("bird", "🐦")), "The octopus has eight arms!"),
        Riddle("Which one shines at night?", Pic("moon", "🌙"), listOf(Pic("rainbow", "🌈"), Pic("umbrella", "☂️")), "The moon shines at night!"),
        Riddle("Where do we go when we are sick?", Pic("hospital", "🏥"), listOf(Pic("beach", "🏖️"), Pic("park", "🏞️")), "We go to the hospital!"),
        Riddle("Where do we learn to read?", Pic("school", "🏫"), listOf(Pic("forest", "🌲"), Pic("beach", "🏖️")), "We learn at school!"),
        Riddle("Which one goes to space?", Pic("rocket", "🚀"), listOf(Pic("ship", "🚢"), Pic("train", "🚂")), "The rocket goes to space!"),
        Riddle("Which animal is the tallest?", Pic("giraffe", "🦒"), listOf(Pic("mouse", "🐭"), Pic("turtle", "🐢")), "The giraffe is the tallest!"),
        Riddle("Which one is a fruit?", Pic("grapes", "🍇"), listOf(Pic("carrot", "🥕"), Pic("bread", "🍞")), "Grapes are a fruit!"),
        Riddle("Which one is a vegetable?", Pic("carrot", "🥕"), listOf(Pic("banana", "🍌"), Pic("biscuit", "🍪")), "A carrot is a vegetable!"),
        Riddle("Which one do we sleep in?", Pic("bed", "🛏️"), listOf(Pic("bath", "🛁"), Pic("car", "🚗")), "We sleep in a bed!"),
        Riddle("Which one tells the time?", Pic("clock", "⏰"), listOf(Pic("book", "📖"), Pic("bell", "🔔")), "The clock tells the time!"),
        Riddle("Which one has black and white stripes?", Pic("zebra", "🦓"), listOf(Pic("elephant", "🐘"), Pic("lion", "🦁")), "The zebra has stripes!"),
        Riddle("Which one is hot?", Pic("fire", "🔥"), listOf(Pic("snowflake", "❄️"), Pic("ice cream", "🍦")), "Fire is hot!"),
        Riddle("How do you feel at a birthday party?", Pic("excited", "🤩"), listOf(Pic("sad", "😢"), Pic("angry", "😠")), "We feel excited at a party!"),
        Riddle("Which one lives in a shell?", Pic("snail", "🐌"), listOf(Pic("bee", "🐝"), Pic("owl", "🦉")), "The snail lives in a shell!"),
        Riddle("Which one makes honey?", Pic("bee", "🐝"), listOf(Pic("ant", "🐜"), Pic("spider", "🕷️")), "The bee makes honey!"),
        Riddle("Which one do we use to see far stars?", Pic("telescope", "🔭"), listOf(Pic("spoon", "🥄"), Pic("guitar", "🎸")), "We use a telescope!"),
        Riddle("Which one is wet?", Pic("water", "💧"), listOf(Pic("sand", "🏜️"), Pic("bread", "🍞")), "Water is wet!"),
        Riddle("Which one has a long trunk?", Pic("elephant", "🐘"), listOf(Pic("zebra", "🦓"), Pic("penguin", "🐧")), "The elephant has a long trunk!"),
    )

    private var lastRiddle = -1

    private fun think(): QuizRound {
        var i = riddles.indices.random()
        if (i == lastRiddle) i = (i + 1) % riddles.size
        lastRiddle = i
        val r = riddles[i]
        return build(r.answer, r.wrong) { options, idx ->
            QuizRound(
                question = r.question,
                spoken = listOf(r.question),
                prompt = null,
                options = options,
                answer = idx,
                success = r.why,
            )
        }
    }
}
