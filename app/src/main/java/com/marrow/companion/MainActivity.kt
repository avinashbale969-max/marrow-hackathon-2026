package com.marrow.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
<<<<<<< HEAD
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
=======
>>>>>>> origin/Sri_hackthon
import com.marrow.companion.ui.navigation.AppNavGraph
import com.marrow.companion.ui.theme.MarrowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
<<<<<<< HEAD
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate (required by API)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Edge-to-edge: works on Android 11–16
        // On API 30+: transparent status + nav bars, content draws behind them
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

=======
class

MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
>>>>>>> origin/Sri_hackthon
        setContent {
            MarrowTheme {
                AppNavGraph()
            }
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/Sri_hackthon
