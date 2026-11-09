package com.bhumaji.wordbuddy.data

/**
 * One question from Coco. [model] is a full-sentence answer to practise
 * ("My favourite colour is {x}."); {x} is filled from what she said
 * (first matching [keywords] entry) or [fallback]; {name} is her name.
 */
data class Turn(
    val ask: String,
    val model: String,
    val keywords: List<String> = emptyList(),
    val fallback: String = "",
    val react: String = "Wonderful!",
    val choices: List<Pic> = emptyList(),
)

data class Conversation(val id: String, val title: String, val emoji: String, val turns: List<Turn>, val bye: String)

object Conversations {
    private val colours = listOf("red", "blue", "green", "yellow", "pink", "purple", "orange", "black", "white", "brown")
    private val numbers = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    private fun c(label: String, emoji: String) = Pic(label, emoji)

    val all = listOf(
        Conversation(
            "hello", "Hello, Friend!", "👋",
            listOf(
                Turn("Hello! What is your name?", "My name is {name}.", react = "Nice to meet you, {name}!"),
                Turn(
                    "How old are you?", "I am {x} years old.", numbers, "four", "Wow, {x} years old! So big!",
                    listOf(c("three", "3️⃣"), c("four", "4️⃣"), c("five", "5️⃣"), c("six", "6️⃣")),
                ),
                Turn(
                    "What is your favourite colour?", "My favourite colour is {x}.", colours, "pink", "{x}! What a lovely colour!",
                    listOf(c("pink", "🌸"), c("blue", "🔵"), c("red", "🔴"), c("green", "🟢")),
                ),
                Turn(
                    "How are you today?", "I am {x} today.", listOf("happy", "good", "fine", "great", "sad", "tired", "sleepy", "excited"),
                    "happy", "I am happy too!",
                    listOf(c("happy", "😀"), c("excited", "🤩"), c("sleepy", "😪"), c("sad", "😢")),
                ),
            ),
            "It was so nice talking to you. Bye bye!",
        ),
        Conversation(
            "snack", "Snack Time", "🍪",
            listOf(
                Turn(
                    "Are you thirsty? Ask me for some water!", "Can I have some water, please?",
                    react = "Here you go! Great asking!",
                ),
                Turn(
                    "What would you like to eat?", "I would like {x}, please.",
                    listOf("an apple", "a banana", "a biscuit", "rice", "bread", "a mango", "apple", "banana", "biscuit", "mango", "cake"),
                    "an apple", "Yummy! {x}!",
                    listOf(c("an apple", "🍎"), c("a banana", "🍌"), c("a biscuit", "🍪"), c("a mango", "🥭")),
                ),
                Turn("I gave you a snack. What do you say?", "Thank you!", react = "You're welcome!"),
            ),
            "Snack time is over. See you soon!",
        ),
        Conversation(
            "toys", "My Toys", "🧸",
            listOf(
                Turn(
                    "What is your favourite toy?", "My favourite toy is my {x}.",
                    listOf("teddy", "doll", "ball", "car", "blocks", "kite", "train", "bicycle"), "teddy", "A {x}! How fun!",
                    listOf(c("teddy", "🧸"), c("doll", "🪆"), c("ball", "⚽"), c("car", "🚗")),
                ),
                Turn(
                    "Is it big or small?", "It is {x}.", listOf("big", "small", "little", "tiny"), "small", "I see, it is {x}!",
                    listOf(c("big", "🐘"), c("small", "🐭")),
                ),
                Turn(
                    "What colour is it?", "It is {x}.", colours, "brown", "{x}! Nice!",
                    listOf(c("brown", "🟤"), c("red", "🔴"), c("blue", "🔵"), c("pink", "🌸")),
                ),
                Turn("Can I play with you?", "Yes, let's play together!", react = "Yay! Let's play!"),
            ),
            "That was fun. Bye bye!",
        ),
        Conversation(
            "feelings", "My Feelings", "😊",
            listOf(
                Turn(
                    "How do you feel when you play?", "I feel {x} when I play.",
                    listOf("happy", "excited", "good", "great"), "happy", "Playing makes me {x} too!",
                    listOf(c("happy", "😀"), c("excited", "🤩")),
                ),
                Turn(
                    "How do you feel when you fall down?", "I feel {x}.", listOf("sad", "hurt", "angry", "scared"), "sad",
                    "Oh no! A big hug can help.",
                    listOf(c("sad", "😢"), c("scared", "😨"), c("angry", "😠")),
                ),
                Turn("What can you say when your friend is sad?", "Are you okay? Do you want a hug?", react = "That is so kind!"),
            ),
            "Thank you for sharing your feelings. Bye!",
        ),
        Conversation(
            "school", "At School", "🏫",
            listOf(
                Turn(
                    "What do you do at school?", "At school, I {x}.",
                    listOf("play", "learn", "draw", "sing", "read", "dance", "write", "paint"), "play", "You {x} at school? Wonderful!",
                    listOf(c("play", "⚽"), c("draw", "🎨"), c("sing", "🎶"), c("read", "📖")),
                ),
                Turn("What do you say to your teacher in the morning?", "Good morning, teacher!", react = "Good morning to you too!"),
                Turn("You need a crayon. What do you ask?", "Can I have the crayon, please?", react = "Here is the crayon. Lovely manners!"),
            ),
            "Have a great day at school. Bye!",
        ),
    )

    /** Picks the {x} value from what she said. */
    fun extract(turn: Turn, heard: String): String {
        val h = " " + heard.lowercase() + " "
        return turn.keywords.firstOrNull { h.contains(" ${it.lowercase()} ") } ?: turn.fallback
    }

    fun fill(template: String, x: String, name: String): String =
        template.replace("{x}", x).replace("{name}", name.ifBlank { "Superstar" })
}
