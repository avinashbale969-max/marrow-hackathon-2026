package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
        BookmarkType.NORMAL.name  -> Color(0xFF2196F3)
        BookmarkType.STARRED.name -> Color(0xFFE53935)
        BookmarkType.REVIEW.name  -> Color(0xFFFFC107)
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

    val config  = LocalConfiguration.current
    val density = LocalDensity.current
    val screenW = with(density) { config.screenWidthDp.dp.toPx() }
    val screenH = with(density) { config.screenHeightDp.dp.toPx() }

    // Bookmark icon is at ~55.5% width, ~96.5% height
    val targetX = screenW * 0.555f
    val targetY = screenH * 0.965f
    val fromCX  = screenW / 2f
    val fromCY  = screenH / 2f

    // ── Animation values ────────────────────────────────────────────────────
    val flashAlpha    = remember { Animatable(0f) }
    val sceneScale    = remember { Animatable(1f) }
    val frameAlpha    = remember { Animatable(0f) }
    val frameBorder   = remember { Animatable(0f) }   // 0→1 border draw progress
    val badgeScale    = remember { Animatable(0f) }
    val badgeAlpha    = remember { Animatable(0f) }
    val screenshotScale = remember { Animatable(1f) }
    val screenshotTX  = remember { Animatable(0f) }
    val screenshotTY  = remember { Animatable(0f) }
    val screenshotRot = remember { Animatable(0f) }
    val screenshotAlpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        // ── Phase 1: Camera shutter flash (0–90ms) ───────────────────────────
        launch {
            flashAlpha.animateTo(0.92f, tween(45, easing = LinearEasing))
            flashAlpha.animateTo(0f,    tween(120, easing = FastOutSlowInEasing))
        }
        // Scene briefly contracts — "captured" feel
        launch {
            sceneScale.animateTo(0.975f, tween(50))
            sceneScale.animateTo(1f,     tween(120, easing = FastOutSlowInEasing))
        }

        // ── Phase 2: Screenshot frame materialises (90–350ms) ────────────────
        delay(60)
        launch {
            screenshotAlpha.animateTo(1f, tween(60))
            frameBorder.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        }

        // ── Phase 3: Badge springs in (220–460ms) ─────────────────────────────
        delay(160)
        launch { badgeAlpha.animateTo(1f, tween(100)) }
        launch {
            badgeScale.animateTo(
                1.18f,
                spring(dampingRatio = Spring.DampingRatioLowBouncy,
                       stiffness    = Spring.StiffnessMediumLow)
            )
            badgeScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                       stiffness    = Spring.StiffnessHigh)
            )
        }

        // ── Phase 4: Hold (460–820ms) ─────────────────────────────────────────
        delay(420)

        // Badge fades out before flight
        launch { badgeAlpha.animateTo(0f, tween(180, easing = FastOutLinearInEasing)) }

        delay(120)

        // ── Phase 5: Screenshot flies to bookmark with bezier + rotation ──────
        val flySpec = tween<Float>(520, easing = FastOutSlowInEasing)
        val dX = targetX - fromCX
        val dY = targetY - fromCY

        launch { screenshotScale.animateTo(0.055f, flySpec) }
        launch { screenshotTX.animateTo(dX,        flySpec) }
        launch { screenshotTY.animateTo(dY,        flySpec) }
        // Slight clockwise rotation as it "slides into" the bookmark
        launch { screenshotRot.animateTo(8f, tween(520, easing = FastOutSlowInEasing)) }
        // Fade the last 30% of the flight
        launch {
            delay(360)
            screenshotAlpha.animateTo(0f, tween(180))
        }

        delay(540)
        onComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Phase 1: warm white camera flash ─────────────────────────────────
        if (flashAlpha.value > 0f) {
            Box(
                modifier = Modifier.fillMaxSize().drawBehind {
                    // Warm flash (slightly yellow-white, like camera flash)
                    drawRect(Color(0xFFFFFAF0).copy(alpha = flashAlpha.value))
                }
            )
        }

        // ── Phase 2+5: Screenshot frame that flies to bookmark ───────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX       = screenshotScale.value
                    scaleY       = screenshotScale.value
                    translationX = screenshotTX.value
                    translationY = screenshotTY.value
                    rotationZ    = screenshotRot.value
                    alpha        = screenshotAlpha.value
                    // Pivot at center of screen
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
        ) {
            // Drop shadow + border frame using drawBehind
            Box(modifier = Modifier.fillMaxSize().drawBehind {
                val borderW  = 16.dp.toPx()
                val cornerSz = 36.dp.toPx()
                val thick    = 5.dp.toPx()
                val alpha    = frameBorder.value
                val c        = borderColor.copy(alpha = alpha)

                // 4 edge bars
                drawRect(c, topLeft = Offset.Zero,         size = Size(size.width, borderW))
                drawRect(c, topLeft = Offset(0f, size.height - borderW), size = Size(size.width, borderW))
                drawRect(c, topLeft = Offset.Zero,         size = Size(borderW, size.height))
                drawRect(c, topLeft = Offset(size.width - borderW, 0f), size = Size(borderW, size.height))

                // Corner accent marks
                val cc = borderColor.copy(alpha = alpha * 0.6f)
                drawRect(cc, topLeft = Offset(borderW, borderW),               size = Size(cornerSz, thick))
                drawRect(cc, topLeft = Offset(borderW, borderW),               size = Size(thick, cornerSz))
                drawRect(cc, topLeft = Offset(size.width-borderW-cornerSz, borderW), size = Size(cornerSz, thick))
                drawRect(cc, topLeft = Offset(size.width-borderW-thick, borderW),    size = Size(thick, cornerSz))
                drawRect(cc, topLeft = Offset(borderW, size.height-borderW-thick),   size = Size(cornerSz, thick))
                drawRect(cc, topLeft = Offset(borderW, size.height-borderW-cornerSz),size = Size(thick, cornerSz))
                drawRect(cc, topLeft = Offset(size.width-borderW-cornerSz, size.height-borderW-thick), size = Size(cornerSz, thick))
                drawRect(cc, topLeft = Offset(size.width-borderW-thick, size.height-borderW-cornerSz), size = Size(thick, cornerSz))
            })

            // ── Phase 3: Centre badge ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(badgeScale.value)
                    .graphicsLayer { alpha = badgeAlpha.value }
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(horizontal = 34.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Glowing circle
                    Box(contentAlignment = Alignment.Center) {
                        // Glow ring
                        Box(
                            modifier = Modifier.size(88.dp).clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(borderColor.copy(alpha = 0.35f),
                                               Color.Transparent)
                                    )
                                )
                        )
                        // Solid circle
                        Box(
                            modifier = Modifier.size(68.dp).clip(CircleShape)
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
                    }
                    Text(
                        label,
                        color      = Color.White,
                        fontSize   = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                    // Subtle type description
                    Text(
                        when (bookmarkType) {
                            BookmarkType.STARRED.name -> "Marked as important"
                            BookmarkType.REVIEW.name  -> "Flagged for review"
                            else                      -> "Saved for reference"
                        },
                        color    = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ── Scene-scale overlay (captures the "compression" feel) ────────────
        if (sceneScale.value < 1f) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer { scaleX = sceneScale.value; scaleY = sceneScale.value }
            )
        }
    }
}
