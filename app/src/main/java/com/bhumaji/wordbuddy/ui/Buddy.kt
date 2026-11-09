package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class Mood { IDLE, TALK, LISTEN, CHEER, OOPS }

private val rainbow = Brush.sweepGradient(listOf(Coral, Sun, Grass, Sky, Purple, Coral))

/**
 * Her animated buddy: her own photo in a rainbow ring (or a smiley mascot if
 * no photo was added yet). It bobs when idle, wiggles when talking, pulses
 * rings when listening and jumps with sparkles when cheering.
 */
@Composable
fun BuddyAvatar(
    photo: ImageBitmap?,
    mood: Mood,
    size: Dp,
    modifier: Modifier = Modifier,
    accessory: String? = null,
) {
    val inf = rememberInfiniteTransition(label = "buddy")
    val bob by inf.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob",
    )
    val fast by inf.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(220, easing = LinearEasing), RepeatMode.Reverse), label = "fast",
    )
    val pulse by inf.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart), label = "pulse",
    )
    val spin by inf.animateFloat(
        0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "spin",
    )
    val pop by animateFloatAsState(
        if (mood == Mood.CHEER) 1.12f else 1f, spring(dampingRatio = 0.3f, stiffness = 250f), label = "pop",
    )
    val sizePx = with(LocalDensity.current) { size.toPx() }
    val ringPx = with(LocalDensity.current) { 5.dp.toPx() }

    Box(modifier.size(size * 1.35f), contentAlignment = Alignment.Center) {
        if (mood == Mood.LISTEN) {
            Canvas(Modifier.fillMaxSize()) {
                for (k in 0..1) {
                    val p = (pulse + k * 0.5f) % 1f
                    drawCircle(
                        Coral.copy(alpha = (1f - p) * 0.6f),
                        radius = sizePx / 2f * (1f + p * 0.3f),
                        style = Stroke(width = ringPx),
                    )
                }
            }
        }

        Box(
            Modifier
                .size(size)
                .graphicsLayer {
                    translationY = when (mood) {
                        Mood.CHEER -> -abs(fast) * sizePx * 0.10f
                        else -> bob * sizePx * 0.03f
                    }
                    rotationZ = when (mood) {
                        Mood.CHEER -> fast * 9f
                        Mood.OOPS -> fast * 5f
                        Mood.TALK -> bob * 5f
                        else -> bob * 3f
                    }
                    scaleX = pop
                    scaleY = pop
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(size * 0.06f, rainbow, CircleShape)
                    .padding(size * 0.06f)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (photo != null) {
                    Image(
                        photo,
                        contentDescription = "Buddy",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    MascotFace(mood, Modifier.fillMaxSize())
                }
            }
            if (accessory != null) {
                Text(
                    accessory,
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = -(size * 0.24f))
                        .rotate(-12f),
                    fontSize = (size.value * 0.34f).sp,
                )
            }
        }

        if (mood == Mood.CHEER) {
            listOf(0f, 120f, 240f).forEach { start ->
                val rad = Math.toRadians((start + spin).toDouble())
                Text(
                    "✨",
                    Modifier.offset {
                        IntOffset((cos(rad) * sizePx * 0.58).toInt(), (sin(rad) * sizePx * 0.58).toInt())
                    },
                    fontSize = (size.value * 0.2f).sp,
                )
            }
        }
    }
}

/** A friendly drawn face used until a grown-up adds her photo. It blinks and talks. */
@Composable
private fun MascotFace(mood: Mood, modifier: Modifier) {
    val inf = rememberInfiniteTransition(label = "face")
    val blink by inf.animateFloat(
        1f, 1f,
        infiniteRepeatable(
            keyframes {
                durationMillis = 3200
                1f at 2900
                0.1f at 3000
                1f at 3100
            }
        ),
        label = "blink",
    )
    val talk by inf.animateFloat(
        0.2f, 1f, infiniteRepeatable(tween(180), RepeatMode.Reverse), label = "talk",
    )
    Canvas(modifier.background(Sun)) {
        val w = size.width
        val h = size.height
        val eyeR = w * 0.07f
        val eyeY = h * 0.42f
        listOf(0.35f, 0.65f).forEach { ex ->
            drawOval(
                Ink,
                topLeft = Offset(w * ex - eyeR, eyeY - eyeR * blink),
                size = Size(eyeR * 2, eyeR * 2 * blink),
            )
        }
        drawCircle(Coral.copy(alpha = 0.5f), w * 0.07f, Offset(w * 0.24f, h * 0.6f))
        drawCircle(Coral.copy(alpha = 0.5f), w * 0.07f, Offset(w * 0.76f, h * 0.6f))
        if (mood == Mood.OOPS) {
            drawArc(
                Ink, 200f, 140f, useCenter = false,
                topLeft = Offset(w * 0.38f, h * 0.66f), size = Size(w * 0.24f, h * 0.14f),
                style = Stroke(width = w * 0.03f, cap = StrokeCap.Round),
            )
        } else {
            val open = when (mood) {
                Mood.TALK -> talk
                Mood.CHEER -> 1f
                Mood.LISTEN -> 0.35f
                else -> 0.2f
            }
            drawArc(
                Ink, 0f, 180f, useCenter = true,
                topLeft = Offset(w * 0.34f, h * 0.55f), size = Size(w * 0.32f, h * (0.08f + 0.2f * open)),
            )
        }
    }
}

/** White speech bubble next to the buddy. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Purple,
    )
}

private class Piece(
    val x: Float, val vx: Float, val vy: Float, val color: Color, val size: Float, val spin: Float,
)

private val confettiColors = listOf(Coral, Sun, Grass, Sky, Purple, Color(0xFFF06292))

/** A burst of confetti every time [trigger] changes (0 = nothing). Doesn't block touches. */
@Composable
fun Confetti(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger == 0) return
    val pieces = remember(trigger) {
        List(80) {
            Piece(
                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                vx = (Random.nextFloat() - 0.5f) * 1.2f,
                vy = -(0.5f + Random.nextFloat() * 0.7f),
                color = confettiColors.random(),
                size = 6f + Random.nextFloat() * 8f,
                spin = Random.nextFloat() * 360f,
            )
        }
    }
    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { progress.animateTo(1f, tween(2000, easing = LinearEasing)) }

    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        pieces.forEach { p ->
            val x = (p.x + p.vx * t) * size.width
            val y = size.height * 0.45f + (p.vy * t + 1.3f * t * t) * size.height
            val s = p.size * density
            rotate(p.spin + t * 720f, Offset(x, y)) {
                drawRect(
                    p.color.copy(alpha = 1f - t * t),
                    topLeft = Offset(x - s / 2, y - s / 4),
                    size = Size(s, s / 2),
                )
            }
        }
    }
}
