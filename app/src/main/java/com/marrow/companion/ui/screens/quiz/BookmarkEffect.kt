package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marrow.companion.data.database.entities.BookmarkType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BookmarkScreenshotEffect(
    bookmarkType: String,
    onComplete: () -> Unit
) {
    val borderColor = when (bookmarkType) {
        BookmarkType.NORMAL.name  -> Color(0xFF2196F3)   // Blue
        BookmarkType.STARRED.name -> Color(0xFFE53935)   // Red
        BookmarkType.REVIEW.name  -> Color(0xFFFFC107)   // Yellow
        else                      -> Color(0xFF2196F3)
    }
    val icon = when (bookmarkType) {
        BookmarkType.STARRED.name -> Icons.Filled.Star
        BookmarkType.REVIEW.name  -> Icons.Filled.Help
        else                      -> Icons.Filled.Bookmark
    }
    val label = when (bookmarkType) {
        BookmarkType.NORMAL.name  -> "Bookmarked"
        BookmarkType.STARRED.name -> "Starred"
        BookmarkType.REVIEW.name  -> "Question"
        else                      -> "Bookmarked"
    }

    // ── Screen dimensions → bookmark icon target position ──────────────────
    val screenWidthDp  = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val density        = LocalDensity.current

    val screenW = with(density) { screenWidthDp.dp.toPx() }
    val screenH = with(density) { screenHeightDp.dp.toPx() }

    // Bottom bar: Report(1) | Share(1) | Bookmark(1) | NEXT(1.5) = 4.5 total weight
    // Bookmark center x = (1 + 1 + 0.5) / 4.5 = 55.5%
    val bookmarkTargetX = screenW * 0.555f
    // Bottom bar ~58dp tall; bookmark icon center ≈ 29dp from bottom
    val bookmarkTargetY = screenH - with(density) { 29.dp.toPx() }

    // Translation needed (graphicsLayer translates from composable's original centre)
    val targetTX = bookmarkTargetX - screenW / 2f
    val targetTY = bookmarkTargetY - screenH / 2f

    // ── Animation state ─────────────────────────────────────────────────────
    val flashAlpha   = remember { Animatable(0f) }
    val frameAlpha   = remember { Animatable(0f) }
    val frameScale   = remember { Animatable(1f) }
    val frameTX      = remember { Animatable(0f) }
    val frameTY      = remember { Animatable(0f) }
    val badgeAlpha   = remember { Animatable(0f) }
    val badgeScale   = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {

        // ── Phase 1: camera shutter flash ───────────────────────────────────
        launch {
            flashAlpha.animateTo(0.90f, tween(55, easing = LinearEasing))
            flashAlpha.animateTo(0f,    tween(140, easing = LinearEasing))
        }

        // ── Phase 2: colored border frame appears ────────────────────────────
        launch {
            delay(40)
            frameAlpha.animateTo(1f, tween(90, easing = FastOutSlowInEasing))
        }

        // ── Phase 3: badge springs in ────────────────────────────────────────
        delay(90)
        launch {
            badgeAlpha.animateTo(1f, tween(100))
        }
        badgeScale.animateTo(
            1.12f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy,
                   stiffness    = Spring.StiffnessMediumLow)
        )
        badgeScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                   stiffness    = Spring.StiffnessHigh)
        )

        // ── Hold ─────────────────────────────────────────────────────────────
        delay(420)

        // ── Phase 4: badge fades, frame flies to bookmark icon ───────────────
        // Fade out badge quickly
        launch { badgeAlpha.animateTo(0f, tween(160, easing = LinearEasing)) }

        // After short delay, frame rockets to bookmark corner
        delay(80)

        val flyDuration = 480
        val flySpec = tween<Float>(flyDuration, easing = FastOutSlowInEasing)

        launch { frameScale.animateTo(0.07f, flySpec) }
        launch { frameTX.animateTo(targetTX, flySpec) }
        launch { frameTY.animateTo(targetTY, flySpec) }
        // Fade out as it arrives
        launch {
            delay((flyDuration * 0.7f).toLong())
            frameAlpha.animateTo(0f, tween((flyDuration * 0.3f).toInt()))
        }

        delay((flyDuration + 60).toLong())
        onComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── White camera flash ───────────────────────────────────────────────
        if (flashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha.value))
            )
        }

        // ── Screenshot frame (border + badge) — flies to bookmark ────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX       = frameScale.value
                    scaleY       = frameScale.value
                    translationX = frameTX.value
                    translationY = frameTY.value
                    alpha        = frameAlpha.value
                }
        ) {
            // Colored border using Canvas (4 edge bars)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 18.dp.toPx()
                val c      = borderColor
                // Top
                drawRect(c, topLeft = Offset.Zero,
                    size = Size(size.width, stroke))
                // Bottom
                drawRect(c, topLeft = Offset(0f, size.height - stroke),
                    size = Size(size.width, stroke))
                // Left
                drawRect(c, topLeft = Offset.Zero,
                    size = Size(stroke, size.height))
                // Right
                drawRect(c, topLeft = Offset(size.width - stroke, 0f),
                    size = Size(stroke, size.height))
            }

            // Corner accent squares for "screenshot frame" look
            val cornerSize = 28.dp
            val cornerThick = 4.dp
            listOf(
                Alignment.TopStart, Alignment.TopEnd,
                Alignment.BottomStart, Alignment.BottomEnd
            ).forEach { alignment ->
                Box(
                    modifier = Modifier
                        .size(cornerSize)
                        .align(alignment)
                        .background(borderColor.copy(alpha = 0.4f))
                )
            }

            // Badge: icon + label in centre
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(badgeScale.value)
                    .graphicsLayer { alpha = badgeAlpha.value }
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(horizontal = 30.dp, vertical = 22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(borderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        label,
                        color      = Color.White,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
