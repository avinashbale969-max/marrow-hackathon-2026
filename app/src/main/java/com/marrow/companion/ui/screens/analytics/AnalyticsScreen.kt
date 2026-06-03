package com.marrow.companion.ui.screens.analytics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val TealHeader = Color(0xFF4DC8D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tests") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = TealHeader,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { _ -> }
}
