package com.bhumaji.wordbuddy.data

/** How a picture (or her buddy) moves. [durationMs] is one full loop. */
enum class Motion(val durationMs: Int) {
    NONE(1000),
    JUMP(900),
    HOP(600),
    DANCE(1600),
    RUN(1400),
    SWIM(2600),
    SPIN(1500),
    CLIMB(2400),
    SLEEP(3000),
    CLAP(800),
    WAVE(1200),
    FLY(3000),
    STOMP(900),
    WIGGLE(900),
    WADDLE(1000),
    STRETCH(2000),
    ROLL(2400),
    TIPTOE(2400),
    SHAKE(500),
    CRAWL(2600),
    NOD(1400),
    ROBOT(1600),
}

/** One move in Move Like Me, e.g. "Hop like a bunny!". */
data class AnimalMove(
    val verb: String,
    val ing: String,
    val motion: Motion,
    val emoji: String,
    val phrase: String,
)

data class MoveStep(val label: String, val motion: Motion, val emoji: String, val speed: Float = 1f)

data class MoveRound(
    val title: String,
    val spoken: List<String>,
    val steps: List<MoveStep>,
    val praise: String,
    val countTo: Int = 0,
)

object Moves {
    val all = listOf(
        AnimalMove("hop", "hopping", Motion.HOP, "🐰", "Hop like a bunny!"),
        AnimalMove("stomp", "stomping", Motion.STOMP, "🐘", "Stomp like an elephant!"),
        AnimalMove("flap", "flapping", Motion.FLY, "🐦", "Flap your wings like a bird!"),
        AnimalMove("swim", "swimming", Motion.SWIM, "🐟", "Swim like a fish!"),
        AnimalMove("wiggle", "wiggling", Motion.WIGGLE, "🐍", "Wiggle like a snake!"),
        AnimalMove("waddle", "waddling", Motion.WADDLE, "🐧", "Waddle like a penguin!"),
        AnimalMove("jump", "jumping", Motion.JUMP, "🐸", "Jump like a frog!"),
        AnimalMove("tiptoe", "tiptoeing", Motion.TIPTOE, "🐭", "Tiptoe like a little mouse!"),
        AnimalMove("gallop", "galloping", Motion.RUN, "🐴", "Gallop like a horse!"),
        AnimalMove("spin", "spinning", Motion.SPIN, "🌀", "Spin around like a top!"),
        AnimalMove("stretch", "stretching", Motion.STRETCH, "🦒", "Stretch up tall like a giraffe!"),
        AnimalMove("crawl", "crawling", Motion.CRAWL, "🐢", "Crawl slowly like a turtle!"),
        AnimalMove("dance", "dancing", Motion.DANCE, "💃", "Dance like a superstar!"),
        AnimalMove("climb", "climbing", Motion.CLIMB, "🐒", "Climb like a monkey!"),
        AnimalMove("sleep", "sleeping", Motion.SLEEP, "🐻", "Curl up and sleep like a bear. Shhh!"),
        AnimalMove("shake", "shaking", Motion.SHAKE, "🐶", "Shake like a wet dog!"),
        AnimalMove("clap", "clapping", Motion.CLAP, "👏", "Clap your hands!"),
        AnimalMove("wave", "waving", Motion.WAVE, "👋", "Wave hello to everyone!"),
        AnimalMove("march", "marching", Motion.STOMP, "💂", "March like a soldier!"),
        AnimalMove("roar", "roaring", Motion.NOD, "🦁", "Roar like a lion! Roooar!"),
        AnimalMove("fly", "flying", Motion.FLY, "🦋", "Fly like a butterfly!"),
        AnimalMove("roll", "rolling", Motion.ROLL, "⚽", "Roll your arms like a ball!"),
        AnimalMove("balance", "balancing", Motion.NOD, "🦩", "Balance on one leg like a flamingo!"),
        AnimalMove("walk", "walking", Motion.ROBOT, "🤖", "Walk like a robot!"),
    )

    private val numbers = listOf("one", "two", "three", "four", "five")

    fun numberWord(n: Int): String = numbers.getOrElse(n - 1) { n.toString() }.replaceFirstChar { it.uppercase() }

    private fun step(m: AnimalMove, speed: Float = 1f) = MoveStep(m.verb, m.motion, m.emoji, speed)

    private fun cap(s: String) = s.replaceFirstChar { it.uppercase() }

    /** Level 1: one move. Level 2: count or speed. Level 3: 2–3 moves in order. */
    fun next(level: Int): MoveRound {
        val a = all.random()
        return when (level) {
            1 -> MoveRound(
                title = a.phrase,
                spoken = listOf(a.phrase, "Your turn!"),
                steps = listOf(step(a)),
                praise = "Great ${a.ing}!",
            )

            2 -> when ((0..2).random()) {
                0 -> {
                    val n = (2..4).random()
                    MoveRound(
                        title = "${cap(a.verb)} ${numberWord(n).lowercase()} times!",
                        spoken = listOf("${cap(a.verb)} ${numberWord(n).lowercase()} times!", "Ready? Let's count!"),
                        steps = listOf(step(a)),
                        praise = "You did it ${numberWord(n).lowercase()} times!",
                        countTo = n,
                    )
                }

                1 -> MoveRound(
                    title = "${cap(a.verb)} very slowly…",
                    spoken = listOf("${cap(a.verb)}…", "very… slowly…"),
                    steps = listOf(step(a, speed = 0.45f)),
                    praise = "Lovely slow ${a.ing}!",
                )

                else -> MoveRound(
                    title = "${cap(a.verb)} super fast!",
                    spoken = listOf("${cap(a.verb)}", "super fast!"),
                    steps = listOf(step(a, speed = 1.9f)),
                    praise = "Wow, fast ${a.ing}!",
                )
            }

            else -> {
                val picks = all.shuffled().distinctBy { it.motion }.take(if ((0..2).random() == 0) 3 else 2)
                val words = picks.map { it.verb }
                val title = when (picks.size) {
                    2 -> "First ${words[0]}, then ${words[1]}!"
                    else -> "First ${words[0]}, then ${words[1]}, then ${words[2]}!"
                }
                MoveRound(
                    title = title,
                    spoken = listOf(title, "Remember the order!", "Ready? Go!"),
                    steps = picks.map { step(it) },
                    praise = "You remembered all ${numberWord(picks.size).lowercase()}! Super listener!",
                )
            }
        }
    }
}

/** Freeze Dance styles: each is a new way to dance after a freeze. */
enum class DanceStyle(val instruction: String, val motion: Motion, val speed: Float) {
    NORMAL("", Motion.DANCE, 1f),
    SLOW("slowly", Motion.DANCE, 0.7f),
    FAST("super fast", Motion.DANCE, 1.4f),
    TIPTOE("on your tiptoes", Motion.TIPTOE, 1f),
    ROBOT("like a robot", Motion.ROBOT, 1f),
    ARMS("with your arms up high", Motion.STRETCH, 1f),
    WIGGLY("wiggly like jelly", Motion.WIGGLE, 1f),
    SPIN("and spin around", Motion.SPIN, 1f),
    HOPPY("like a hopping bunny", Motion.HOP, 1f),
}
