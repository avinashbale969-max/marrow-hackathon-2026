package com.marrow.companion.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TealHeader = Color(0xFF4DC8D4)

@Composable
fun AnalyticsScreen() {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealHeader)
                    .statusBarsPadding()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tests", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        },
        containerColor = Color(0xFFF2F2F2)
    ) { _ -> }
}
