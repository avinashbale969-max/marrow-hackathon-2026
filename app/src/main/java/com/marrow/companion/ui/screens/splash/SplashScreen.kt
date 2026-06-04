package com.marrow.companion.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TealLight = Color(0xFF4DC8D4)
private val TealDark  = Color(0xFF00838F)

// ~55 BPM (relaxed, visually calmer)
private const val BEAT_MS = 1100

// Detailed PQRST waveform: x ∈ [0,1], y ∈ [-0.2, 1.0] (positive = upward from baseline)
private val pqrstPoints = listOf(
    0.000f to  0.000f,  // isoelectric lead-in
    0.060f to  0.000f,
    0.080f to  0.030f,  // P-wave start
    0.110f to  0.130f,
    0.135f to  0.150f,  // P-wave peak
    0.160f to  0.130f,
    0.185f to  0.030f,
    0.200f to  0.000f,  // PR segment
    0.220f to  0.000f,
    0.232f to -0.060f,  // Q dip
    0.248f to  1.000f,  // R spike (tall narrow)
    0.264f to -0.200f,  // S dip
    0.275f to -0.090f,
    0.290f to  0.000f,  // J-point
    0.340f to  0.020f,  // ST segment (slight elevation)
    0.390f to  0.050f,  // T-wave start
    0.440f to  0.180f,
    0.490f to  0.300f,  // T-wave peak
    0.540f to  0.300f,
    0.590f to  0.180f,
    0.640f to  0.050f,
    0.680f to  0.000f,  // T-wave end
    0.800f to  0.000f,  // TP isoelectric
    1.000f to  0.000f,
)

@Composable
fun SplashScreen(onNavigate: () -> Unit) {

    // ── Auto-navigate after 2800 ms ──────────────────────────────────────────
    LaunchedEffect(Unit) { delay(2800); onNavigate() }

    // ── Animation state ──────────────────────────────────────────────────────
    val heartScale = remember { Animatable(1f) }
    val glowAlpha  = remember { Animatable(0f) }

    val r1Radius   = remember { Animatable(0f) };  val r1Alpha = remember { Animatable(0f) }
    val r2Radius   = remember { Animatable(0f) };  val r2Alpha = remember { Animatable(0f) }
    val r3Radius   = remember { Animatable(0f) };  val r3Alpha = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    // ── Heartbeat loop (lub–dub, ~72 BPM) ───────────────────────────────────
    LaunchedEffect(Unit) {
        while (true) {
            val t0 = System.currentTimeMillis()

            // --- LUB (systolic ejection — slowed) ---
            scope.launch {
                glowAlpha.animateTo(0.55f, tween(140, easing = FastOutLinearInEasing))
                glowAlpha.animateTo(0f,    tween(500, easing = LinearEasing))
            }
            scope.launch {
                r1Radius.snapTo(0f); r1Alpha.snapTo(0.70f)
                r1Radius.animateTo(220f, tween(1000, easing = LinearEasing))
                r1Alpha.animateTo(0f,    tween(1000, easing = LinearEasing))
            }
            scope.launch {
                delay(220)
                r2Radius.snapTo(0f); r2Alpha.snapTo(0.45f)
                r2Radius.animateTo(220f, tween(1000, easing = LinearEasing))
                r2Alpha.animateTo(0f,    tween(1000, easing = LinearEasing))
            }
            scope.launch {
                delay(440)
                r3Radius.snapTo(0f); r3Alpha.snapTo(0.25f)
                r3Radius.animateTo(220f, tween(1000, easing = LinearEasing))
                r3Alpha.animateTo(0f,    tween(1000, easing = LinearEasing))
            }
            heartScale.animateTo(1.38f, tween(160, easing = FastOutLinearInEasing))
            heartScale.animateTo(0.93f, tween(220, easing = LinearOutSlowInEasing))
            heartScale.animateTo(1.00f, tween(100, easing = FastOutSlowInEasing))

            delay(40) // gap between lub & dub

            // --- DUB (diastolic filling — softer, slower) ---
            scope.launch {
                glowAlpha.animateTo(0.32f, tween(120, easing = FastOutLinearInEasing))
                glowAlpha.animateTo(0f,    tween(400, easing = LinearEasing))
            }
            heartScale.animateTo(1.20f, tween(130, easing = FastOutLinearInEasing))
            heartScale.animateTo(0.96f, tween(190, easing = LinearOutSlowInEasing))
            heartScale.animateTo(1.00f, tween(80,  easing = FastOutSlowInEasing))

            // Wait for the rest of the beat period
            val elapsed = (System.currentTimeMillis() - t0).coerceAtLeast(0)
            val rest    = (BEAT_MS - elapsed).coerceAtLeast(0).toLong()
            if (rest > 0) delay(rest)
        }
    }

    // ── ECG: one full scroll per beat ────────────────────────────────────────
    val ecgTransition = rememberInfiniteTransition(label = "ecg")
    val ecgOffset by ecgTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(BEAT_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ecgScroll"
    )

    // ── Root layout ───────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealLight, TealDark))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            // ── Heart + Ripples — same Box so they share the same centre ────
            Box(
                modifier         = Modifier.size(340.dp), // larger to hold bigger rings
                contentAlignment = Alignment.Center
            ) {
                // Ripple rings & glow — canvas anchored to this Box's centre
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width  / 2f
                    val cy = size.height / 2f

                    // Soft glow disc behind the heart
                    if (glowAlpha.value > 0f) {
                        drawCircle(
                            brush  = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = glowAlpha.value * 0.30f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = 110.dp.toPx()
                            ),
                            radius = 110.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }

                    fun ring(radius: Float, alpha: Float) {
                        if (alpha <= 0f || radius <= 0f) return
                        drawCircle(
                            color  = Color.White.copy(alpha = alpha * 0.55f),
                            radius = radius,
                            center = Offset(cx, cy),
                            style  = Stroke(width = 2.2.dp.toPx())
                        )
                    }
                    ring(r1Radius.value, r1Alpha.value)
                    ring(r2Radius.value, r2Alpha.value)
                    ring(r3Radius.value, r3Alpha.value)
                }

                // Heart icon — centred in the same Box
                Icon(
                    imageVector        = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier
                        .size(110.dp)
                        .graphicsLayer {
                            scaleX = heartScale.value
                            scaleY = heartScale.value
                        }
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── ECG waveform ─────────────────────────────────────────────────
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                val w         = size.width
                val h         = size.height
                val midY      = h * 0.60f          // baseline (slightly below centre)
                val amplitude = h * 0.42f          // peak-to-baseline height
                val cycleW    = w * 0.48f          // one PQRST cycle width in px
                val shift     = ecgOffset * cycleW

                val path = Path()
                var first = true
                for (cycle in -1..3) {
                    for ((xRel, yRel) in pqrstPoints) {
                        val px = cycle * cycleW + xRel * cycleW - shift
                        val py = midY - yRel * amplitude
                        if (first) { path.moveTo(px, py); first = false }
                        else       { path.lineTo(px, py) }
                    }
                }
                drawPath(
                    path  = path,
                    color = Color.White,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Branding ──────────────────────────────────────────────────────
            Text(
                text          = "MARROW",
                fontSize      = 38.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color.White,
                letterSpacing = 6.sp,
                textAlign     = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "Put Your Heart Into It",
                fontSize      = 15.sp,
                fontWeight    = FontWeight.Light,
                color         = Color.White.copy(alpha = 0.85f),
                letterSpacing = 1.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(Modifier.weight(1.2f))
        }
    }
}
