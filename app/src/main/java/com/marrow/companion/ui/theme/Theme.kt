package com.marrow.companion.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = MarrowTealDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MarrowTealLight,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF002019),
    secondary = MarrowTeal,
    tertiary = MarrowSuccess
)

private val DarkColors = darkColorScheme(
    primary = MarrowTealLight,
    secondary = MarrowTeal,
    tertiary = MarrowSuccess
)

@Composable
fun MarrowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}