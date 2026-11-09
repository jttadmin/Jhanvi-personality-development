package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.Motion
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sign
import kotlin.math.sin

private const val TAU = (2 * PI).toFloat()
private const val HALF = PI.toFloat()

/**
 * Makes whatever is inside act out a [motion]: jump, dance, swim, spin…
 * [speed] > 1 is faster, < 1 slower. Motion.NONE just shows the content still.
 */
@Composable
fun MotionBox(
    motion: Motion,
    size: Dp,
    modifier: Modifier = Modifier,
    speed: Float = 1f,
    content: @Composable () -> Unit,
) {
    if (motion == Motion.NONE) {
        Box(modifier.size(size), contentAlignment = Alignment.Center) { content() }
        return
    }
    val duration = (motion.durationMs / speed).toInt().coerceAtLeast(200)
    val inf = rememberInfiniteTransition(label = "motion")
    val t by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(duration, easing = LinearEasing), RepeatMode.Restart),
        label = "t",
    )
    val px = with(LocalDensity.current) { size.toPx() }

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { applyMotion(motion, t, px) },
            contentAlignment = Alignment.Center,
        ) { content() }
        MotionExtras(motion, t, px, size)
    }
}

private fun GraphicsLayerScope.applyMotion(m: Motion, t: Float, s: Float) {
    when (m) {
        Motion.JUMP, Motion.HOP -> {
            val up = abs(sin(HALF * t))
            val height = if (m == Motion.JUMP) 0.32f else 0.16f
            transformOrigin = TransformOrigin(0.5f, 1f)
            translationY = -up * s * height
            if (up < 0.18f) {
                scaleY = 0.86f + up
                scaleX = 1.14f - up
            }
        }

        Motion.DANCE -> {
            rotationZ = sin(TAU * t) * 15f
            translationX = sin(TAU * t) * s * 0.12f
            translationY = -abs(sin(2 * TAU * t)) * s * 0.08f
        }

        Motion.RUN -> {
            translationX = sin(TAU * t) * s * 0.28f
            scaleX = if (cos(TAU * t) >= 0f) 1f else -1f
            rotationZ = 8f * sign(cos(TAU * t))
            translationY = -abs(sin(4 * TAU * t)) * s * 0.05f
        }

        Motion.SWIM -> {
            translationX = sin(TAU * t) * s * 0.24f
            translationY = cos(2 * TAU * t) * s * 0.06f
            rotationZ = cos(TAU * t) * 12f
            scaleX = if (cos(TAU * t) >= 0f) 1f else -1f
        }

        Motion.SPIN -> rotationZ = 360f * t

        Motion.CLIMB -> {
            val step = floor(t * 4f) / 4f + (t * 4f - floor(t * 4f)) * 0.1f
            translationY = (0.28f - step * 0.56f) * s
            rotationZ = if ((t * 8f).toInt() % 2 == 0) -6f else 6f
        }

        Motion.SLEEP -> {
            val breathe = 1f + 0.05f * sin(TAU * t)
            scaleX = breathe
            scaleY = breathe
            rotationZ = -12f
        }

        Motion.CLAP -> {
            val pulse = 1f + 0.2f * abs(sin(2 * TAU * t))
            scaleX = pulse
            scaleY = pulse
        }

        Motion.WAVE -> {
            transformOrigin = TransformOrigin(0.5f, 0.9f)
            rotationZ = sin(2 * TAU * t) * 20f
        }

        Motion.FLY -> {
            translationX = sin(TAU * t) * s * 0.28f
            translationY = sin(2 * TAU * t) * s * 0.14f
            rotationZ = cos(TAU * t) * 12f
            scaleY = 1f - 0.12f * abs(sin(6 * TAU * t))
        }

        Motion.STOMP -> {
            transformOrigin = TransformOrigin(0.5f, 1f)
            val up = abs(sin(HALF * t))
            translationY = -up * s * 0.1f
            rotationZ = if (t < 0.5f) -7f else 7f
            if (up < 0.1f) translationX = sin(40f * t) * s * 0.02f
        }

        Motion.WIGGLE -> {
            rotationZ = sin(4 * TAU * t) * 12f
            scaleX = 1f + 0.08f * sin(4 * TAU * t)
        }

        Motion.WADDLE -> {
            transformOrigin = TransformOrigin(0.5f, 1f)
            rotationZ = sin(TAU * t) * 14f
            translationX = sin(TAU * t / 2f) * s * 0.12f
        }

        Motion.STRETCH -> {
            transformOrigin = TransformOrigin(0.5f, 1f)
            scaleY = 1f + 0.35f * abs(sin(HALF * t))
            scaleX = 1f - 0.08f * abs(sin(HALF * t))
        }

        Motion.ROLL -> {
            rotationZ = 360f * t
            translationX = sin(TAU * t) * s * 0.25f
        }

        Motion.TIPTOE -> {
            translationY = -abs(sin(2 * TAU * t)) * s * 0.05f
            translationX = sin(TAU * t) * s * 0.18f
            scaleY = 1.05f
        }

        Motion.SHAKE -> translationX = sin(TAU * t) * s * 0.05f

        Motion.CRAWL -> {
            translationX = sin(TAU * t) * s * 0.22f
            translationY = s * 0.12f + abs(sin(3 * TAU * t)) * s * -0.02f
            scaleX = if (cos(TAU * t) >= 0f) 1f else -1f
        }

        Motion.NOD -> {
            transformOrigin = TransformOrigin(0.5f, 1f)
            rotationZ = sin(TAU * t) * 8f
        }

        Motion.ROBOT -> {
            val quarter = (t * 4f).toInt()
            rotationZ = when (quarter) {
                0 -> -12f
                1 -> 0f
                2 -> 12f
                else -> 0f
            }
            translationX = (if (quarter < 2) -1f else 1f) * s * 0.06f
        }

        Motion.NONE -> Unit
    }
}

/** Little extras that make the action obvious: 💤 for sleep, 🎵 for dance, 🌊 for swim… */
@Composable
private fun MotionExtras(m: Motion, t: Float, px: Float, size: Dp) {
    val (emoji, x, y, alpha) = when (m) {
        Motion.SLEEP -> Extra("💤", 0.3f, -0.15f - 0.25f * t, 1f - t)
        Motion.DANCE -> Extra("🎵", 0.35f * cos(TAU * t), -0.35f + 0.05f * sin(2 * TAU * t), 1f)
        Motion.SWIM -> Extra("🌊", 0.25f * sin(TAU * t + 1f), 0.36f, 0.9f)
        Motion.CLAP -> Extra("✨", 0.3f, -0.3f, abs(sin(2 * TAU * t)))
        Motion.FLY -> Extra("☁️", -0.32f + 0.1f * t, -0.32f, 0.7f)
        Motion.STOMP -> Extra("💥", 0.3f, 0.36f, if (t < 0.12f || t > 0.88f) 1f else 0f)
        Motion.JUMP -> Extra("⬆️", 0.38f, -0.1f, abs(sin(HALF * t)) * 0.8f)
        else -> return
    }
    Text(
        emoji,
        Modifier
            .offset { IntOffset((x * px).toInt(), (y * px).toInt()) }
            .graphicsLayer { this.alpha = alpha.coerceIn(0f, 1f) },
        fontSize = (size.value * 0.18f).sp,
    )
}

private data class Extra(val emoji: String, val x: Float, val y: Float, val alpha: Float)
