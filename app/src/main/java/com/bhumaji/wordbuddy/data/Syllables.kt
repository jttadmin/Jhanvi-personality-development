package com.bhumaji.wordbuddy.data

/**
 * Child-friendly syllable splits ("clap" parts) for the words in the app.
 * Words not listed here have one syllable.
 */
object Syllables {
    private val table: Map<String, String> = """
        aeroplane ae-ro-plane, airport air-port, alien a-li-en, angry an-gry, apple ap-ple,
        artist art-ist, astronaut as-tro-naut, auto au-to, banana ba-na-na, bicycle bi-cy-cle,
        biscuit bis-cuit, butterfly but-ter-fly, camel cam-el, caterpillar cat-er-pil-lar,
        cherry cher-ry, city cit-y, cloudy cloud-y, comet com-et, cricket crick-et,
        crocodile croc-o-dile, dentist den-tist, desert des-ert, dirty dirt-y, doctor doc-tor,
        dolphin dol-phin, elephant el-e-phant, excited ex-cit-ed, farmer farm-er,
        firefighter fire-fight-er, flamingo fla-min-go, flower flow-er, foggy fog-gy,
        forest for-est, galaxy gal-ax-y, gallop gal-lop, giraffe gi-raffe, gorilla go-ril-la,
        guitar gui-tar, happy hap-py, heavy heav-y, hippo hip-po, hospital hos-pi-tal,
        kangaroo kan-ga-roo, ladybird la-dy-bird, library li-brar-y, lightning light-ning,
        mango man-go, market mar-ket, monkey mon-key, mosquito mos-qui-to, mountain moun-tain,
        octopus oc-to-pus, officer of-fi-cer, orange or-ange, parrot par-rot, peacock pea-cock,
        penguin pen-guin, piano pi-an-o, pilot pi-lot, pineapple pine-ap-ple, planet plan-et,
        police po-lice, postman post-man, purple pur-ple, rabbit rab-bit, rainbow rain-bow,
        rainy rain-y, rhino rhi-no, rocket rock-et, satellite sat-el-lite, saxophone sax-o-phone,
        silly sil-ly, singer sing-er, sleepy sleep-y, snowy snow-y, spider spi-der,
        squirrel squir-rel, station sta-tion, stormy storm-y, strawberry straw-ber-ry,
        sunny sun-ny, surprised sur-prised, teacher teach-er, teddy ted-dy, telescope tel-e-scope,
        tiger ti-ger, tiny ti-ny, trumpet trum-pet, turtle tur-tle, umbrella um-brel-la,
        violin vi-o-lin, water wa-ter, watermelon wa-ter-mel-on, wiggle wig-gle, windy wind-y,
        worried wor-ried, yellow yel-low, zebra ze-bra, lion li-on, quiet qui-et,
        whisper whis-per, gallop gal-lop, tiptoe tip-toe, saxophone sax-o-phone
    """.trimIndent()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .associate { entry ->
            val (word, parts) = entry.split(" ")
            word to parts
        }

    /** "butterfly" -> [but, ter, fly]; "police officer" -> [po, lice, of, fi, cer]. */
    fun split(text: String): List<String> =
        text.lowercase().split(" ").filter { it.isNotEmpty() }.flatMap { w ->
            table[w]?.split("-") ?: listOf(w)
        }

    fun count(text: String): Int = split(text).size
}
