package com.marrow.companion.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marrow.companion.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TealDark = Color(0xFF00838F)
private val BarBg    = Color(0xFF000000).copy(alpha = 0.15f)
private val BarFill  = Color(0xFF4DC8D4)

@Composable
fun SplashScreen(onNavigate: () -> Unit) {

    val imageAlpha  = remember { Animatable(0f) }
    val titleAlpha  = remember { Animatable(0f) }
    val titleSlide  = remember { Animatable(40f) }
    val subAlpha    = remember { Animatable(0f) }
    val barProgress = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Image fades in
        imageAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))

        // Title slides up + fades in
        scope.launch {
            titleSlide.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        titleAlpha.animateTo(1f, tween(450))

        delay(100)
        subAlpha.animateTo(1f, tween(350))

        // Loading bar fills
        delay(100)
        barProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )

        delay(150)
        onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E8)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main image
            Image(
                painter = painterResource(id = R.drawable.splash_image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { alpha = imageAlpha.value }
            )

            // Bottom: title + subtitle + loading bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MARROW",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealDark,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha        = titleAlpha.value
                        translationY = titleSlide.value
                    }
                )

                Text(
                    text = "Put Your Heart Into It",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TealDark.copy(alpha = 0.75f),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = subAlpha.value }
                )

                Spacer(Modifier.height(8.dp))

                // Loading bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BarBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barProgress.value)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(BarFill)
                    )
                }
            }
        }
    }
}
