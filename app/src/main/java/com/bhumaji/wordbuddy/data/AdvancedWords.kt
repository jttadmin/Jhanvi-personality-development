package com.bhumaji.wordbuddy.data

import androidx.compose.ui.graphics.Color

/**
 * Harder vocabulary for a child who already knows ABCs, numbers and colours.
 * Every sentence is used by Learn Words and by the Sentence Star game.
 */
internal object AdvancedWords {

    val categories = listOf(
        Category("wild", "Wild Animals", "🦒", Color(0xFFFF7043)),
        Category("actions", "Action Words", "🏃", Color(0xFF29B6F6)),
        Category("feelings", "Feelings", "😊", Color(0xFFEC407A)),
        Category("jobs", "Jobs", "🧑‍🚒", Color(0xFF5C6BC0)),
        Category("places", "Places", "🏫", Color(0xFF26A69A)),
        Category("describe", "Describing Words", "📏", Color(0xFFFFA726)),
        Category("weather", "Weather", "⛅", Color(0xFF42A5F5)),
        Category("space", "Space", "🚀", Color(0xFF3949AB)),
        Category("bugs", "Bugs", "🐞", Color(0xFF7CB342)),
        Category("music", "Music", "🎸", Color(0xFFAB47BC)),
    )

    private fun w(cat: String, text: String, emoji: String, sentence: String, vararg alt: String) =
        Word(text.replace(" ", "_"), text, emoji, sentence, cat, alsoAccept = alt.toList())

    /** Words that act out their meaning (see MotionBox). */
    private val motions = mapOf(
        "run" to Motion.RUN, "swim" to Motion.SWIM, "climb" to Motion.CLIMB, "dance" to Motion.DANCE,
        "sleep" to Motion.SLEEP, "eat" to Motion.NOD, "drink" to Motion.NOD, "sing" to Motion.DANCE,
        "cook" to Motion.SHAKE, "paint" to Motion.WAVE, "kick" to Motion.STOMP, "wash" to Motion.SHAKE,
        "clap" to Motion.CLAP, "wave" to Motion.WAVE, "hug" to Motion.CLAP, "laugh" to Motion.SHAKE,
        "think" to Motion.NOD, "ride" to Motion.RUN, "jump" to Motion.JUMP, "hop" to Motion.HOP,
        "spin" to Motion.SPIN, "march" to Motion.STOMP, "stomp" to Motion.STOMP, "stretch" to Motion.STRETCH,
        "wiggle" to Motion.WIGGLE, "tiptoe" to Motion.TIPTOE, "fly" to Motion.FLY, "crawl" to Motion.CRAWL,
        "bounce" to Motion.JUMP, "shake" to Motion.SHAKE, "roll" to Motion.ROLL, "gallop" to Motion.RUN,
        "kangaroo" to Motion.JUMP, "dolphin" to Motion.SWIM, "whale" to Motion.SWIM, "penguin" to Motion.WADDLE,
        "octopus" to Motion.WIGGLE, "parrot" to Motion.FLY, "flamingo" to Motion.NOD, "crocodile" to Motion.CRAWL,
        "turtle" to Motion.CRAWL, "monkey" to Motion.CLIMB, "squirrel" to Motion.CLIMB, "gorilla" to Motion.STOMP,
        "bee" to Motion.FLY, "ladybird" to Motion.FLY, "mosquito" to Motion.FLY, "snail" to Motion.CRAWL,
        "caterpillar" to Motion.CRAWL, "worm" to Motion.WIGGLE, "spider" to Motion.CLIMB, "cricket" to Motion.HOP,
        "rocket" to Motion.FLY, "comet" to Motion.FLY, "planet" to Motion.SPIN, "satellite" to Motion.SPIN,
        "earth" to Motion.SPIN, "windy" to Motion.SHAKE, "stormy" to Motion.SHAKE, "lightning" to Motion.SHAKE,
        "happy" to Motion.JUMP, "excited" to Motion.HOP, "angry" to Motion.SHAKE, "scared" to Motion.SHAKE,
        "sleepy" to Motion.SLEEP, "silly" to Motion.WIGGLE, "shy" to Motion.NOD, "fast" to Motion.RUN,
        "slow" to Motion.CRAWL, "drum" to Motion.CLAP, "guitar" to Motion.DANCE, "bell" to Motion.WAVE,
        "trumpet" to Motion.NOD, "bounce" to Motion.JUMP,
    )

    val words: List<Word> = rawWords.map { it.copy(motion = motions[it.id] ?: Motion.NONE) }

    private val rawWords: List<Word> get() = listOf(
        // Wild animals
        w("wild", "giraffe", "🦒", "The giraffe has a very long neck."),
        w("wild", "kangaroo", "🦘", "The kangaroo carries her baby in a pouch."),
        w("wild", "zebra", "🦓", "The zebra has black and white stripes."),
        w("wild", "penguin", "🐧", "The penguin cannot fly, but it can swim."),
        w("wild", "octopus", "🐙", "The octopus has eight long arms."),
        w("wild", "dolphin", "🐬", "The dolphin is a clever swimmer."),
        w("wild", "crocodile", "🐊", "The crocodile has big sharp teeth."),
        w("wild", "peacock", "🦚", "The peacock spreads its beautiful feathers."),
        w("wild", "owl", "🦉", "The owl stays awake at night."),
        w("wild", "camel", "🐫", "The camel walks in the hot desert."),
        w("wild", "squirrel", "🐿️", "The squirrel climbs trees and eats nuts."),
        w("wild", "turtle", "🐢", "The turtle carries its house on its back.", "tortoise"),
        w("wild", "hippo", "🦛", "The hippo loves the muddy river.", "hippopotamus"),
        w("wild", "rhino", "🦏", "The rhino has a horn on its nose.", "rhinoceros"),
        w("wild", "gorilla", "🦍", "The gorilla is very strong."),
        w("wild", "flamingo", "🦩", "The flamingo stands on one leg."),
        w("wild", "parrot", "🦜", "The parrot can copy our words."),
        w("wild", "whale", "🐋", "The whale is the biggest animal in the sea."),

        // Action words
        w("actions", "run", "🏃", "I run fast in the park.", "running"),
        w("actions", "swim", "🏊", "Fish swim in the water.", "swimming"),
        w("actions", "climb", "🧗", "I climb up the ladder.", "climbing"),
        w("actions", "dance", "💃", "We dance to happy music.", "dancing"),
        w("actions", "sleep", "😴", "I sleep in my cosy bed.", "sleeping"),
        w("actions", "eat", "😋", "I eat my lunch slowly.", "eating"),
        w("actions", "drink", "🥤", "I drink water when I am thirsty.", "drinking"),
        w("actions", "read", "📖", "Papa and I read a story.", "reading", "red"),
        w("actions", "write", "✍️", "I write my name with a pencil.", "writing", "right"),
        w("actions", "sing", "🎶", "I sing a song for mummy.", "singing"),
        w("actions", "cook", "🍳", "Mummy can cook tasty food.", "cooking"),
        w("actions", "paint", "🖌️", "I paint a big rainbow.", "painting"),
        w("actions", "kick", "🥅", "I kick the ball into the goal.", "kicking"),
        w("actions", "wash", "🧼", "I wash my hands with soap.", "washing"),
        w("actions", "clap", "👏", "Clap your hands two times.", "clapping"),
        w("actions", "wave", "👋", "I wave goodbye to my friends.", "waving"),
        w("actions", "hug", "🤗", "I give my family a big hug.", "hugging"),
        w("actions", "whisper", "🤫", "Whisper softly, the baby is sleeping.", "whispering"),
        w("actions", "laugh", "😂", "We laugh at a funny joke.", "laughing"),
        w("actions", "think", "🤔", "I think very hard.", "thinking"),
        w("actions", "ride", "🚴", "I ride my bicycle to the park.", "riding"),
        w("actions", "jump", "🤾", "I jump high like a frog.", "jumping"),
        w("actions", "hop", "🐇", "Hop, hop, hop on one foot.", "hopping"),
        w("actions", "spin", "🌀", "Spin round and round.", "spinning"),
        w("actions", "march", "💂", "March like a soldier: left, right!", "marching"),
        w("actions", "stomp", "🦖", "Stomp like a big dinosaur!", "stomping"),
        w("actions", "stretch", "🙆", "Stretch up to the sky.", "stretching"),
        w("actions", "wiggle", "🍮", "The jelly wiggles and wobbles.", "wiggling"),
        w("actions", "tiptoe", "🩰", "Tiptoe quietly like a mouse.", "tiptoeing", "tip toe"),
        w("actions", "fly", "🕊️", "Birds fly high in the sky.", "flying"),
        w("actions", "crawl", "👶", "Babies crawl on the floor.", "crawling"),
        w("actions", "bounce", "🏀", "The ball bounces up and down.", "bouncing"),
        w("actions", "shake", "🍃", "The leaves shake in the wind.", "shaking"),
        w("actions", "roll", "🎳", "The ball rolls down the lane.", "rolling"),
        w("actions", "gallop", "🏇", "The horse gallops very fast.", "galloping"),

        // Feelings
        w("feelings", "happy", "😀", "I feel happy when I play with friends."),
        w("feelings", "sad", "😢", "I feel sad when my toy breaks."),
        w("feelings", "angry", "😠", "Take a deep breath when you feel angry."),
        w("feelings", "scared", "😨", "I feel scared in the dark.", "afraid"),
        w("feelings", "surprised", "😲", "Wow! I am so surprised!", "surprise"),
        w("feelings", "excited", "🤩", "I am excited for my birthday party.", "exciting"),
        w("feelings", "sleepy", "😪", "I feel sleepy at bedtime."),
        w("feelings", "shy", "🙈", "I feel shy when I meet new people."),
        w("feelings", "bored", "🥱", "I feel bored when there is nothing to do.", "board"),
        w("feelings", "worried", "😟", "Mummy hugs me when I feel worried.", "worry"),
        w("feelings", "silly", "🤪", "Let's make a silly face!"),
        w("feelings", "proud", "🏅", "I feel proud when I try my best."),

        // Jobs
        w("jobs", "doctor", "🧑‍⚕️", "The doctor helps sick people get better."),
        w("jobs", "teacher", "🧑‍🏫", "The teacher helps us learn new things."),
        w("jobs", "farmer", "🧑‍🌾", "The farmer grows our food."),
        w("jobs", "chef", "🧑‍🍳", "The chef cooks yummy food.", "shef", "cook"),
        w("jobs", "pilot", "🧑‍✈️", "The pilot flies the aeroplane."),
        w("jobs", "firefighter", "🧑‍🚒", "The firefighter puts out fires.", "fireman", "fire fighter"),
        w("jobs", "police officer", "👮", "The police officer keeps us safe.", "police", "policeman", "policewoman"),
        w("jobs", "astronaut", "🧑‍🚀", "The astronaut flies to space."),
        w("jobs", "artist", "🧑‍🎨", "The artist paints lovely pictures."),
        w("jobs", "dentist", "🦷", "The dentist checks our teeth."),
        w("jobs", "postman", "📮", "The postman brings us letters.", "postwoman", "mailman"),
        w("jobs", "singer", "🧑‍🎤", "The singer sings on the stage."),

        // Places
        w("places", "school", "🏫", "We learn and play at school."),
        w("places", "hospital", "🏥", "Doctors work in the hospital."),
        w("places", "park", "🏞️", "We play on the swings in the park."),
        w("places", "beach", "🏖️", "We build sandcastles at the beach."),
        w("places", "market", "🛒", "We buy vegetables at the market.", "supermarket", "shop"),
        w("places", "library", "📚", "We borrow books from the library."),
        w("places", "airport", "🛫", "Aeroplanes take off at the airport."),
        w("places", "station", "🚉", "We catch the train at the station.", "railway station"),
        w("places", "mountain", "⛰️", "The mountain is very, very high."),
        w("places", "sea", "🌊", "Big waves splash in the sea.", "ocean", "see"),
        w("places", "desert", "🏜️", "The desert is hot and sandy.", "dessert"),
        w("places", "forest", "🌲", "Many tall trees grow in the forest."),
        w("places", "city", "🏙️", "The city has tall buildings."),
        w("places", "farm", "🚜", "Cows and goats live on the farm."),

        // Describing words
        w("describe", "big", "🐘", "The elephant is big."),
        w("describe", "tiny", "🤏", "The ant is tiny."),
        w("describe", "tall", "🗼", "The tower is very tall."),
        w("describe", "hot", "🔥", "Fire is hot, don't touch it!"),
        w("describe", "cold", "🥶", "Brrr! The snow is cold."),
        w("describe", "fast", "🐆", "The cheetah runs fast."),
        w("describe", "slow", "🦥", "The sloth moves very slowly.", "slowly"),
        w("describe", "heavy", "🏋️", "This box is too heavy to lift."),
        w("describe", "wet", "💦", "I get wet in the rain."),
        w("describe", "dry", "🌵", "The cactus grows where it is dry."),
        w("describe", "loud", "📢", "The drum is very loud."),
        w("describe", "quiet", "🔇", "Be quiet in the library.", "quite"),
        w("describe", "bright", "🌞", "The sun is bright today."),
        w("describe", "dark", "🌑", "It is dark at night."),
        w("describe", "sharp", "✂️", "Scissors are sharp, be careful."),
        w("describe", "long", "🐍", "The snake is long."),
        w("describe", "dirty", "🐷", "The pig is dirty from the mud."),
        w("describe", "clean", "🛁", "I am clean after my bath."),
        w("describe", "sweet", "🍭", "The lollipop is sweet."),
        w("describe", "sour", "🍋", "The lemon tastes sour.", "power"),

        // Weather
        w("weather", "sunny", "☀️", "It is a bright sunny day.", "sunday"),
        w("weather", "cloudy", "⛅", "The sky is cloudy today."),
        w("weather", "rainy", "🌧️", "On a rainy day I wear my raincoat.", "raining"),
        w("weather", "windy", "🌬️", "It is windy, hold your hat!"),
        w("weather", "stormy", "⛈️", "Stay inside when it is stormy.", "storm"),
        w("weather", "snowy", "❄️", "On a snowy day we build a snowman.", "snow"),
        w("weather", "foggy", "🌫️", "It is foggy, I can't see far.", "fog"),
        w("weather", "lightning", "⚡", "Lightning flashes in the sky.", "lighting"),
        w("weather", "umbrella", "☂️", "Take an umbrella when it rains."),

        // Space
        w("space", "planet", "🪐", "Saturn is a planet with rings."),
        w("space", "rocket", "🚀", "The rocket zooms into space."),
        w("space", "earth", "🌍", "We live on planet Earth."),
        w("space", "comet", "☄️", "A comet has a long shiny tail."),
        w("space", "telescope", "🔭", "We look at stars with a telescope."),
        w("space", "alien", "👽", "Is there an alien up there?"),
        w("space", "satellite", "🛰️", "The satellite goes round the Earth."),
        w("space", "galaxy", "🌌", "A galaxy has millions of stars."),

        // Bugs
        w("bugs", "ant", "🐜", "Ants work hard together.", "aunt"),
        w("bugs", "bee", "🐝", "The bee makes sweet honey.", "be"),
        w("bugs", "ladybird", "🐞", "The ladybird has red wings with spots.", "ladybug", "lady bird"),
        w("bugs", "spider", "🕷️", "The spider spins a web."),
        w("bugs", "snail", "🐌", "The snail carries its shell."),
        w("bugs", "caterpillar", "🐛", "The caterpillar becomes a butterfly."),
        w("bugs", "mosquito", "🦟", "The mosquito buzzes in my ear."),
        w("bugs", "cricket", "🦗", "The cricket chirps at night."),
        w("bugs", "worm", "🪱", "The worm wiggles in the soil."),

        // Music
        w("music", "guitar", "🎸", "I strum the guitar."),
        w("music", "drum", "🥁", "Bang, bang goes the drum.", "drums"),
        w("music", "piano", "🎹", "I press the piano keys."),
        w("music", "violin", "🎻", "The violin plays a sweet tune."),
        w("music", "trumpet", "🎺", "Toot toot goes the trumpet."),
        w("music", "saxophone", "🎷", "The saxophone plays jazz music.", "sax"),
        w("music", "bell", "🔔", "Ring ring goes the bell.", "bells"),
    )
}
