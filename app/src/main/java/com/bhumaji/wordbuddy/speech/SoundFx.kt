package com.bhumaji.wordbuddy.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.bhumaji.wordbuddy.R

/** Short, friendly sound effects bundled in res/raw. */
class SoundFx(context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val popId = pool.load(context, R.raw.pop, 1)
    private val chimeId = pool.load(context, R.raw.chime, 1)
    private val tadaId = pool.load(context, R.raw.tada, 1)
    private val boingId = pool.load(context, R.raw.boing, 1)

    fun pop() = play(popId, 0.5f)
    fun chime() = play(chimeId, 0.6f)
    fun tada() = play(tadaId, 0.7f)
    fun boing() = play(boingId, 0.5f)

    private fun play(id: Int, volume: Float) {
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() = pool.release()
}
