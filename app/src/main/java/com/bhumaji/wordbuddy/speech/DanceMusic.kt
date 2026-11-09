package com.bhumaji.wordbuddy.speech

import android.content.Context
import android.media.MediaPlayer
import com.bhumaji.wordbuddy.R

/** The looping Freeze Dance tune (res/raw/dance_loop.wav), with adjustable speed. */
class DanceMusic(context: Context) {
    private val player: MediaPlayer? = MediaPlayer.create(context, R.raw.dance_loop)?.apply {
        isLooping = true
        setVolume(0.7f, 0.7f)
    }

    fun play(speed: Float = 1f) {
        val p = player ?: return
        try {
            p.playbackParams = p.playbackParams.setSpeed(speed)
        } catch (_: Exception) {
            // some phones don't support speed changes; play at normal speed
        }
        if (!p.isPlaying) p.start()
    }

    fun pause() {
        player?.let { if (it.isPlaying) it.pause() }
    }

    fun release() {
        player?.release()
    }
}
