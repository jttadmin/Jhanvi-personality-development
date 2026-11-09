package com.bhumaji.wordbuddy.ui

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The four WordWonder teachers. */
enum class Teacher(val title: String, val role: String, val body: Color, val accent: Color) {
    BUNNY("Bunny", "Word teacher", Color(0xFFFFFFFF), Color(0xFFF8BBD0)),
    PANDA("Panda", "Speaking teacher", Color(0xFFFFFFFF), Color(0xFF263238)),
    KITTY("Kitty", "Sentence teacher", Color(0xFFFFB74D), Color(0xFFF48FB1)),
    COCO("Coco", "Story teacher", Color(0xFF81C784), Color(0xFFFF9800)),
}

/**
 * A teacher drawn in code: blinks, bobs, talks (mouth or beak moves),
 * jumps with sparkles when cheering. Wears the costume bought in the shop.
 */
@Composable
fun TeacherAvatar(
    teacher: Teacher,
    mood: Mood,
    size: Dp,
    modifier: Modifier = Modifier,
    accessory: String? = null,
) {
    val inf = rememberInfiniteTransition(label = "teacher")
    val bob by inf.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob",
    )
    val fast by inf.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(220, easing = LinearEasing), RepeatMode.Reverse), label = "fast",
    )
    val talk by inf.animateFloat(
        0.15f, 1f, infiniteRepeatable(tween(170), RepeatMode.Reverse), label = "talk",
    )
    val spin by inf.animateFloat(
        0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "spin",
    )
    val blink by inf.animateFloat(
        1f, 1f,
        infiniteRepeatable(
            keyframes {
                durationMillis = 3600
                1f at 3300
                0.08f at 3400
                1f at 3500
            }
        ),
        label = "blink",
    )
    val pop by animateFloatAsState(
        if (mood == Mood.CHEER) 1.1f else 1f, spring(dampingRatio = 0.3f, stiffness = 250f), label = "pop",
    )
    val px = with(LocalDensity.current) { size.toPx() }

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = when (mood) {
                        Mood.CHEER -> -abs(fast) * px * 0.08f
                        else -> bob * px * 0.025f
                    }
                    rotationZ = when (mood) {
                        Mood.CHEER -> fast * 7f
                        Mood.OOPS -> fast * 3f
                        Mood.LISTEN -> 8f
                        Mood.TALK -> bob * 4f
                        else -> bob * 2f
                    }
                    scaleX = pop
                    scaleY = pop
                }
        ) {
            val mouthOpen = when (mood) {
                Mood.TALK -> talk
                Mood.CHEER -> 1f
                Mood.LISTEN -> 0.3f
                else -> 0.15f
            }
            drawTeacher(teacher, mood, blink, mouthOpen)
        }
        if (accessory != null) {
            Text(
                accessory,
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = -(size * 0.02f))
                    .rotate(-10f),
                fontSize = (size.value * 0.28f).sp,
            )
        }
        if (mood == Mood.CHEER) {
            listOf(0f, 120f, 240f).forEach { start ->
                val rad = Math.toRadians((start + spin).toDouble())
                Text(
                    "✨",
                    Modifier.offset {
                        IntOffset((cos(rad) * px * 0.45).toInt(), (sin(rad) * px * 0.45).toInt())
                    },
                    fontSize = (size.value * 0.16f).sp,
                )
            }
        }
    }
}

private val Pink = Color(0xFFF48FB1)
private val Outline = Color(0x33000000)

private fun DrawScope.drawTeacher(t: Teacher, mood: Mood, blink: Float, open: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h * 0.6f
    val r = w * 0.32f

    // ---- ears / crest (behind the head) ----
    when (t) {
        Teacher.BUNNY -> listOf(-1f, 1f).forEach { side ->
            val ex = cx + side * r * 0.45f
            rotate(side * 12f, Offset(ex, cy - r * 0.6f)) {
                drawOval(Color.White, Offset(ex - r * 0.23f, cy - r * 2.05f), Size(r * 0.46f, r * 1.4f))
                drawOval(Outline, Offset(ex - r * 0.23f, cy - r * 2.05f), Size(r * 0.46f, r * 1.4f), style = Stroke(r * 0.03f))
                drawOval(t.accent, Offset(ex - r * 0.11f, cy - r * 1.9f), Size(r * 0.22f, r * 1.05f))
            }
        }

        Teacher.PANDA -> listOf(-1f, 1f).forEach { side ->
            drawCircle(t.accent, r * 0.33f, Offset(cx + side * r * 0.78f, cy - r * 0.78f))
        }

        Teacher.KITTY -> listOf(-1f, 1f).forEach { side ->
            val outer = Path().apply {
                moveTo(cx + side * r * 0.95f, cy - r * 0.25f)
                lineTo(cx + side * r * 0.75f, cy - r * 1.25f)
                lineTo(cx + side * r * 0.15f, cy - r * 0.85f)
                close()
            }
            drawPath(outer, t.body)
            val inner = Path().apply {
                moveTo(cx + side * r * 0.8f, cy - r * 0.45f)
                lineTo(cx + side * r * 0.72f, cy - r * 1.05f)
                lineTo(cx + side * r * 0.35f, cy - r * 0.8f)
                close()
            }
            drawPath(inner, t.accent)
        }

        Teacher.COCO -> listOf(Color(0xFFE53935), Color(0xFFFFCA28), Color(0xFF42A5F5)).forEachIndexed { i, c ->
            rotate(-25f + i * 25f, Offset(cx, cy - r * 0.8f)) {
                drawOval(c, Offset(cx - r * 0.13f, cy - r * 1.55f), Size(r * 0.26f, r * 0.8f))
            }
        }
    }

    // ---- head ----
    drawCircle(Color(0x1F000000), r * 1.02f, Offset(cx, cy + r * 0.05f))
    drawCircle(t.body, r, Offset(cx, cy))
    drawCircle(Outline, r, Offset(cx, cy), style = Stroke(r * 0.03f))
    if (t == Teacher.KITTY) {
        // stripes
        listOf(-0.25f, 0f, 0.25f).forEach { dx ->
            drawLine(Color(0xFFF57C00), Offset(cx + dx * r, cy - r * 0.95f), Offset(cx + dx * r * 0.8f, cy - r * 0.65f), r * 0.07f, StrokeCap.Round)
        }
    }

    // ---- eyes ----
    val eyeY = cy - r * 0.08f
    val eyeDx = r * 0.36f
    val eyeR = r * 0.12f
    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * eyeDx
        if (t == Teacher.PANDA) {
            rotate(side * -20f, Offset(ex, eyeY)) {
                drawOval(t.accent, Offset(ex - r * 0.22f, eyeY - r * 0.27f), Size(r * 0.44f, r * 0.55f))
            }
            drawCircle(Color.White, eyeR * 1.25f, Offset(ex, eyeY))
        }
        if (t == Teacher.COCO) drawCircle(Color.White, eyeR * 1.5f, Offset(ex, eyeY))
        drawOval(Ink, Offset(ex - eyeR, eyeY - eyeR * blink), Size(eyeR * 2, eyeR * 2 * blink))
        if (blink > 0.5f) drawCircle(Color.White, eyeR * 0.35f, Offset(ex + eyeR * 0.35f, eyeY - eyeR * 0.35f))
    }

    // ---- cheeks ----
    if (t != Teacher.PANDA) {
        drawCircle(Pink.copy(alpha = 0.55f), r * 0.13f, Offset(cx - r * 0.62f, cy + r * 0.25f))
        drawCircle(Pink.copy(alpha = 0.55f), r * 0.13f, Offset(cx + r * 0.62f, cy + r * 0.25f))
    }

    // ---- nose, whiskers, mouth / beak ----
    if (t == Teacher.COCO) {
        val upper = Path().apply {
            moveTo(cx - r * 0.22f, cy + r * 0.12f)
            lineTo(cx + r * 0.22f, cy + r * 0.12f)
            lineTo(cx, cy + r * 0.38f)
            close()
        }
        val lowerY = cy + r * (0.24f + 0.18f * open)
        val lower = Path().apply {
            moveTo(cx - r * 0.15f, cy + r * 0.22f)
            lineTo(cx + r * 0.15f, cy + r * 0.22f)
            lineTo(cx, lowerY + r * 0.12f)
            close()
        }
        drawPath(lower, Color(0xFFEF6C00))
        drawPath(upper, t.accent)
        return
    }

    val noseY = cy + r * 0.16f
    when (t) {
        Teacher.PANDA -> drawOval(t.accent, Offset(cx - r * 0.12f, noseY - r * 0.07f), Size(r * 0.24f, r * 0.15f))
        else -> drawOval(Pink, Offset(cx - r * 0.09f, noseY - r * 0.06f), Size(r * 0.18f, r * 0.12f))
    }
    if (t == Teacher.KITTY || t == Teacher.BUNNY) {
        listOf(-1f, 1f).forEach { side ->
            listOf(-0.08f, 0.06f).forEach { dy ->
                drawLine(
                    Color(0x66000000),
                    Offset(cx + side * r * 0.25f, noseY + dy * r),
                    Offset(cx + side * r * 0.8f, noseY + dy * r * 2.2f),
                    r * 0.02f,
                    StrokeCap.Round,
                )
            }
        }
    }
    val mouthTop = cy + r * 0.3f
    if (mood == Mood.OOPS) {
        drawCircle(Ink, r * 0.08f, Offset(cx, mouthTop + r * 0.08f), style = Stroke(r * 0.03f))
    } else {
        drawArc(
            Ink, 0f, 180f, useCenter = true,
            topLeft = Offset(cx - r * 0.2f, mouthTop - r * 0.06f),
            size = Size(r * 0.4f, r * (0.12f + 0.32f * open)),
        )
        if (open > 0.5f) {
            drawArc(
                Coral, 20f, 140f, useCenter = true,
                topLeft = Offset(cx - r * 0.1f, mouthTop + r * 0.05f * open),
                size = Size(r * 0.2f, r * 0.2f * open),
            )
        }
    }
}
