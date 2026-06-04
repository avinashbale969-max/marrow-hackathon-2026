package com.marrow.companion.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TealLight = Color(0xFF4DC8D4)
private val TealDark  = Color(0xFF00838F)

@Composable
fun SplashScreen(onNavigate: () -> Unit) {

    val titleAlpha  = remember { Animatable(0f) }
    val titleSlide  = remember { Animatable(60f) }
    val subAlpha    = remember { Animatable(0f) }
    val subSlide    = remember { Animatable(40f) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // MARROW slides up + fades in
        scope.launch {
            titleSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        titleAlpha.animateTo(1f, tween(500))

        // Subtitle follows with a short delay
        delay(150)
        scope.launch {
            subSlide.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        subAlpha.animateTo(1f, tween(450))

        delay(900)
        onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealLight, TealDark))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text          = "MARROW",
                fontSize      = 38.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color.White,
                letterSpacing = 6.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.graphicsLayer {
                    alpha             = titleAlpha.value
                    translationY      = titleSlide.value
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "Put Your Heart Into It",
                fontSize      = 15.sp,
                fontWeight    = FontWeight.Light,
                color         = Color.White.copy(alpha = 0.85f),
                letterSpacing = 1.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.graphicsLayer {
                    alpha        = subAlpha.value
                    translationY = subSlide.value
                }
            )
        }
    }
}
