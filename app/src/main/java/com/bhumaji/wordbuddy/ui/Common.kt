package com.bhumaji.wordbuddy.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhumaji.wordbuddy.data.Motion
import com.bhumaji.wordbuddy.data.Word

val Purple = Color(0xFF6C4AB6)
val Sun = Color(0xFFFFC93C)
val Sky = Color(0xFF4FC3F7)
val Grass = Color(0xFF66BB6A)
val Coral = Color(0xFFFF7A6B)
val Cream = Color(0xFFFFF8E7)
val Ink = Color(0xFF2D2A32)
val Soft = Color(0xFF7A7484)

@Composable
fun WordBuddyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Purple,
            onPrimary = Color.White,
            secondary = Coral,
            background = Cream,
            onBackground = Ink,
            surface = Color.White,
            onSurface = Ink,
        ),
        content = content,
    )
}

private val praises = listOf(
    "Wow", "Super", "Great job", "Well done", "Awesome", "You did it", "Brilliant", "Yay", "Superstar",
)

/** "Great job, Jhanvi!" — or just "Great job!" when no name is set. */
fun praise(name: String = ""): String {
    val p = praises.random()
    return if (name.isBlank()) "$p!" else "$p, ${name.trim()}!"
}

val PlayBackground = Brush.verticalGradient(listOf(Color(0xFFFFF1D0), Color(0xFFFFF8E7), Color(0xFFE1F5FE)))

@Composable
fun TopBar(title: String, onBack: () -> Unit, stars: Int? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton("⬅️", Color.White, onBack, size = 56.dp)
        Text(
            title,
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Purple,
        )
        if (stars != null) StarBadge(stars)
    }
}

@Composable
fun StarBadge(count: Int) {
    Text(
        "⭐ $count",
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Sun)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Ink,
    )
}

@Composable
fun RoundButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.4f)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(color)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = (size.value * 0.42f).sp)
    }
}

/** The picture for a word: an emoji, or a coloured circle for colour words. */
@Composable
fun WordPicture(word: Word, size: Dp, modifier: Modifier = Modifier, animate: Boolean = true) {
    MotionBox(if (animate) word.motion else Motion.NONE, size, modifier) {
        val c = word.color
        if (c != null) {
            Box(
                Modifier
                    .size(size * 0.78f)
                    .clip(CircleShape)
                    .background(c)
                    .border(4.dp, Color(0x33000000), CircleShape)
            )
        } else {
            Text(word.emoji, fontSize = (size.value * 0.6f).sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BigTile(
    emoji: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, label = "press")
    Column(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.85f), color)))
            .clickable(interactionSource = interaction, indication = LocalIndication.current, onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(
            label,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(subtitle, fontSize = 16.sp, color = Color.White.copy(alpha = 0.92f), textAlign = TextAlign.Center)
        }
    }
}

/** Big word text that shrinks a little for long words. */
@Composable
fun WordText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier,
        fontSize = if (text.length > 8) 40.sp else 54.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Purple,
        textAlign = TextAlign.Center,
    )
}
