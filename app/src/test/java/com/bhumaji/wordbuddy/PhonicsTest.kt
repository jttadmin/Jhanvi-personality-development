package com.bhumaji.wordbuddy

import com.bhumaji.wordbuddy.data.Alphabet
import com.bhumaji.wordbuddy.data.Syllables
import org.junit.Assert.assertEquals
import org.junit.Test

class PhonicsTest {
    @Test fun butterflyHasThreeClaps() = assertEquals(listOf("but", "ter", "fly"), Syllables.split("butterfly"))

    @Test fun twoWordJob() = assertEquals(5, Syllables.count("police officer"))

    @Test fun shortWordIsOneClap() = assertEquals(1, Syllables.count("dog"))

    @Test fun firstSoundOfBall() = assertEquals("buh", Alphabet.firstSound("ball"))

    @Test fun softG() = assertEquals("juh", Alphabet.firstSound("giraffe"))

    @Test fun chefStartsWithShh() = assertEquals("shh", Alphabet.firstSound("chef"))
}
