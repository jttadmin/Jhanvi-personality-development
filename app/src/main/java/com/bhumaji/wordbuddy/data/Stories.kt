package com.bhumaji.wordbuddy.data

/** Someone or something on a story page, acting out a motion. */
data class Actor(val emoji: String, val motion: Motion = Motion.NONE)

/** "She found a big, round ___." with picture choices; [answer] is the index of the right one. */
data class Fill(val options: List<Pic>, val answer: Int)

data class Page(val actors: List<Actor>, val text: String, val fill: Fill? = null) {
    /** The sentence with the blank filled in. */
    val fullText: String get() = if (fill == null) text else text.replace("___", fill.options[fill.answer].label)
}

data class StoryQuestion(val ask: String, val options: List<Pic>, val answer: Int, val why: String)

data class Story(
    val id: String,
    val title: String,
    val cover: String,
    val pages: List<Page>,
    val questions: List<StoryQuestion>,
    val predict: StoryQuestion,
    val lesson: String,
)

/** Original short picture stories for Story Kingdom. */
object Stories {
    private fun a(e: String, m: Motion = Motion.NONE) = Actor(e, m)
    private fun p(label: String, emoji: String) = Pic(label, emoji)

    val all = listOf(
        Story(
            id = "ant",
            title = "Tiny Ant's Big Cookie",
            cover = "🐜",
            pages = listOf(
                Page(listOf(a("🐜", Motion.CRAWL), a("🌷"), a("🌼")), "Tiny the ant was walking in the garden."),
                Page(
                    listOf(a("🐜", Motion.NOD), a("🍪", Motion.JUMP)),
                    "She found a big, round ___.",
                    Fill(listOf(p("cookie", "🍪"), p("hat", "🎩"), p("shoe", "👟")), 0),
                ),
                Page(listOf(a("🐜", Motion.STOMP), a("🍪")), "The cookie was too heavy for Tiny!"),
                Page(listOf(a("🐝", Motion.FLY), a("🐞", Motion.FLY)), "Bee and Ladybird came to help."),
                Page(
                    listOf(a("🐜", Motion.WIGGLE), a("🐝", Motion.WIGGLE), a("🐞", Motion.WIGGLE), a("🍪", Motion.SHAKE)),
                    "Together they pushed and pulled the ___.",
                    Fill(listOf(p("car", "🚗"), p("cookie", "🍪"), p("tree", "🌳")), 1),
                ),
                Page(listOf(a("🐜", Motion.DANCE), a("🐝", Motion.DANCE), a("🐞", Motion.DANCE)), "Then they shared the cookie. Yum, yum!"),
            ),
            questions = listOf(
                StoryQuestion("Who found the cookie?", listOf(p("ant", "🐜"), p("bee", "🐝"), p("elephant", "🐘")), 0, "Tiny the ant found it!"),
                StoryQuestion("Who came to help?", listOf(p("a lion", "🦁"), p("bee and ladybird", "🐝"), p("a fish", "🐟")), 1, "Bee and Ladybird helped!"),
            ),
            predict = StoryQuestion(
                "What will they do tomorrow?",
                listOf(p("have a picnic", "🧺"), p("play ball", "⚽"), p("go to sleep", "😴")), -1, "",
            ),
            lesson = "Friends help each other!",
        ),
        Story(
            id = "rain",
            title = "Riya and the Rain",
            cover = "🌧️",
            pages = listOf(
                Page(listOf(a("👧", Motion.HOP), a("☀️", Motion.SPIN)), "Riya was playing outside in the sun."),
                Page(
                    listOf(a("☁️", Motion.FLY), a("🌧️", Motion.SHAKE)),
                    "Suddenly, dark clouds came and it started to ___.",
                    Fill(listOf(p("dance", "💃"), p("sing", "🎶"), p("rain", "🌧️")), 2),
                ),
                Page(listOf(a("👧"), a("☂️", Motion.WAVE)), "Riya opened her yellow umbrella."),
                Page(listOf(a("👧", Motion.JUMP), a("💦", Motion.SHAKE)), "Splash! She jumped in a big puddle."),
                Page(
                    listOf(a("🌈", Motion.NOD), a("☀️", Motion.SPIN)),
                    "The rain stopped, and she saw a beautiful ___.",
                    Fill(listOf(p("rainbow", "🌈"), p("rocket", "🚀"), p("cake", "🎂")), 0),
                ),
            ),
            questions = listOf(
                StoryQuestion("What did Riya open?", listOf(p("a door", "🚪"), p("an umbrella", "☂️"), p("a book", "📖")), 1, "She opened her umbrella!"),
                StoryQuestion("What did she jump in?", listOf(p("a bed", "🛏️"), p("a box", "📦"), p("a puddle", "💦")), 2, "Splash! A puddle!"),
            ),
            predict = StoryQuestion(
                "What will Riya do next?",
                listOf(p("go home", "🏠"), p("draw the rainbow", "🎨"), p("take a bath", "🛁")), -1, "",
            ),
            lesson = "Rainy days can be fun too!",
        ),
        Story(
            id = "leo",
            title = "Where Is Leo's Ball?",
            cover = "🦁",
            pages = listOf(
                Page(listOf(a("🦁", Motion.NOD), a("🔴", Motion.JUMP)), "Leo the lion had a red ball."),
                Page(
                    listOf(a("🦁", Motion.SHAKE), a("❓", Motion.NOD)),
                    "One day, Leo could not find his ball. He felt ___.",
                    Fill(listOf(p("happy", "😀"), p("sad", "😢"), p("sleepy", "😪")), 1),
                ),
                Page(listOf(a("🐒", Motion.CLIMB), a("🌳")), "He asked Monkey. Is it up in the tree? No!"),
                Page(listOf(a("🐟", Motion.SWIM), a("🌊")), "He asked Fish. Is it in the water? No!"),
                Page(listOf(a("🐢", Motion.CRAWL), a("🌿", Motion.SHAKE)), "Turtle said, look under the bush!"),
                Page(
                    listOf(a("🦁", Motion.JUMP), a("🔴", Motion.JUMP)),
                    "The ball was under the bush! Leo felt ___.",
                    Fill(listOf(p("angry", "😠"), p("happy", "😀"), p("scared", "😨")), 1),
                ),
            ),
            questions = listOf(
                StoryQuestion("What colour was the ball?", listOf(p("red", "🔴"), p("blue", "🔵"), p("green", "🟢")), 0, "It was a red ball!"),
                StoryQuestion("Where was the ball?", listOf(p("in the tree", "🌳"), p("under the bush", "🌿"), p("in the water", "🌊")), 1, "It was under the bush!"),
            ),
            predict = StoryQuestion(
                "What will Leo say to Turtle?",
                listOf(p("thank you", "🙏"), p("goodbye", "👋"), p("let's play", "⚽")), -1, "",
            ),
            lesson = "Always say thank you when someone helps!",
        ),
        Story(
            id = "moon",
            title = "Zoom to the Moon",
            cover = "🚀",
            pages = listOf(
                Page(listOf(a("🧑‍🚀", Motion.WAVE), a("🚀")), "Asha is an astronaut. She has a big rocket."),
                Page(
                    listOf(a("🚀", Motion.FLY), a("⭐", Motion.SPIN)),
                    "Three, two, one, blast off! The rocket flew up to the ___.",
                    Fill(listOf(p("shop", "🛒"), p("stars", "⭐"), p("sea", "🌊")), 1),
                ),
                Page(listOf(a("🚀"), a("🌙", Motion.NOD)), "Asha landed on the moon. It was very quiet."),
                Page(listOf(a("👽", Motion.WAVE)), "A little green alien waved hello!"),
                Page(listOf(a("🧑‍🚀", Motion.DANCE), a("👽", Motion.DANCE)), "They played and danced together."),
                Page(
                    listOf(a("🚀", Motion.FLY), a("🌍", Motion.SPIN)),
                    "Then Asha flew home to planet ___.",
                    Fill(listOf(p("pizza", "🍕"), p("zebra", "🦓"), p("Earth", "🌍")), 2),
                ),
            ),
            questions = listOf(
                StoryQuestion("Where did Asha land?", listOf(p("the beach", "🏖️"), p("the moon", "🌙"), p("school", "🏫")), 1, "She landed on the moon!"),
                StoryQuestion("Who waved hello?", listOf(p("a dog", "🐶"), p("a firefighter", "🧑‍🚒"), p("an alien", "👽")), 2, "The little alien waved!"),
            ),
            predict = StoryQuestion(
                "Where should Asha fly next?",
                listOf(p("to a planet", "🪐"), p("to the sea", "🌊"), p("to the zoo", "🦒")), -1, "",
            ),
            lesson = "New friends can be anywhere!",
        ),
        Story(
            id = "birthday",
            title = "Kitty's Birthday",
            cover = "🎂",
            pages = listOf(
                Page(listOf(a("🐱", Motion.HOP), a("🎂")), "Today is Kitty's birthday!"),
                Page(
                    listOf(a("🐱", Motion.JUMP), a("🎈", Motion.FLY)),
                    "Kitty is so ___!",
                    Fill(listOf(p("excited", "🤩"), p("angry", "😠"), p("bored", "🥱")), 0),
                ),
                Page(listOf(a("🐶", Motion.WIGGLE), a("🎁", Motion.JUMP)), "Dog brought a present."),
                Page(listOf(a("🐰", Motion.HOP), a("🎈", Motion.FLY), a("🎈", Motion.FLY)), "Bunny brought red balloons."),
                Page(
                    listOf(a("🎂", Motion.NOD), a("🕯️", Motion.SHAKE)),
                    "They sang Happy Birthday, and Kitty blew out the ___.",
                    Fill(listOf(p("shoes", "👟"), p("clouds", "☁️"), p("candles", "🕯️")), 2),
                ),
                Page(listOf(a("🐱", Motion.DANCE), a("🐶", Motion.DANCE), a("🐰", Motion.DANCE)), "What a happy party!"),
            ),
            questions = listOf(
                StoryQuestion("Whose birthday was it?", listOf(p("Dog", "🐶"), p("Kitty", "🐱"), p("Bunny", "🐰")), 1, "It was Kitty's birthday!"),
                StoryQuestion("What did Bunny bring?", listOf(p("balloons", "🎈"), p("pizza", "🍕"), p("a book", "📖")), 0, "Bunny brought balloons!"),
            ),
            predict = StoryQuestion(
                "What will they eat now?",
                listOf(p("cake", "🎂"), p("ice cream", "🍦"), p("watermelon", "🍉")), -1, "",
            ),
            lesson = "Sharing makes parties even better!",
        ),
    )
}
