package com.bhumaji.wordbuddy

import com.bhumaji.wordbuddy.speech.Match
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchTest {
    @Test fun exactWord() =
        assertEquals(Match.Result.GREAT, Match.check(listOf("dog"), listOf("Dog")))

    @Test fun wordInsideSentence() =
        assertEquals(Match.Result.GREAT, Match.check(listOf("dog"), listOf("a dog!")))

    @Test fun soundAlikeAccepted() =
        assertEquals(Match.Result.GREAT, Match.check(listOf("aeroplane", "airplane"), listOf("airplane")))

    @Test fun splitWordAccepted() =
        assertEquals(Match.Result.GREAT, Match.check(listOf("butterfly"), listOf("butter fly")))

    @Test fun shortTargetNeedsWholeWord() =
        assertEquals(Match.Result.MISS, Match.check(listOf("i"), listOf("fish")))

    @Test fun pluralAccepted() =
        assertEquals(Match.Result.GREAT, Match.check(listOf("cat"), listOf("cats")))

    @Test fun nearMissIsClose() =
        assertEquals(Match.Result.CLOSE, Match.check(listOf("elephant"), listOf("elefan")))

    @Test fun oneLetterOffIsClose() =
        assertEquals(Match.Result.CLOSE, Match.check(listOf("cat"), listOf("cap")))

    @Test fun differentWordIsMiss() =
        assertEquals(Match.Result.MISS, Match.check(listOf("dog"), listOf("banana")))

    @Test fun nothingHeardIsMiss() =
        assertEquals(Match.Result.MISS, Match.check(listOf("dog"), emptyList()))
}
