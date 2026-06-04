package com.marrow.companion.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.dao.HighlightForReview

private val Teal   = Color(0xFF4DC8D4)
private val Green  = Color(0xFF66BB6A)
private val Orange = Color(0xFFFFA726)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightRecallScreen(
    onBack: () -> Unit,
    onRated: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val nextHighlight by viewModel.nextHighlightForReview.collectAsState()
    val dueCount      by viewModel.dueHighlightCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Smart Revision", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (dueCount > 0) Text("$dueCount highlight${if (dueCount > 1) "s" else ""} to review",
                            fontSize = 11.sp, color = Color.White.copy(0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = Teal,
                    titleContentColor         = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        if (nextHighlight == null) {
            // All done
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)) {
                    Text("🎉", fontSize = 52.sp)
                    Text("All caught up!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("No highlights due for review.\nCome back tomorrow!",
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                        Text("Back to Home")
                    }
                }
            }
            return@Scaffold
        }

        val hl = nextHighlight!!
        val now     = System.currentTimeMillis()
        val daysAgo = ((now - hl.createdAt) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        val timeLabel = when (daysAgo) { 0 -> "Today"; 1 -> "1 day ago"; else -> "$daysAgo days ago" }
        val hlColor = if (hl.color == "ORANGE") Orange else Green
        val hlBg    = if (hl.color == "ORANGE") Color(0xFFFFF3E0) else Color(0xFFE8F5E9)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject breadcrumb
            item {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(hlColor))
                    Text(
                        listOfNotNull(hl.subjectName.uppercase(), hl.topicName?.uppercase())
                            .joinToString(" · "),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555), letterSpacing = 0.5.sp)
                    Spacer(Modifier.weight(1f))
                    Text("⏰ $timeLabel", fontSize = 11.sp, color = Color(0xFF888888))
                }
            }

            // Full highlighted text card — same style as My Notes Highlights
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = hlBg),
                    elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.width(4.dp).heightIn(min = 20.dp)
                            .background(hlColor).clip(RoundedCornerShape(2.dp)))
                        Text("\"${hl.text}\"",
                            fontSize = 16.sp, lineHeight = 25.sp,
                            color = Color(0xFF1A1A1A), fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                    }
                }
            }

            // Explanation context (if available)
            if (hl.explanation.isNotBlank()) {
                item {
                    val idx  = hl.explanation.indexOf(hl.text, ignoreCase = true)
                    val ctx  = if (idx >= 0) {
                        val s = (idx - 100).coerceAtLeast(0)
                        val e = (idx + hl.text.length + 100).coerceAtMost(hl.explanation.length)
                        (if (s > 0) "…" else "") +
                        hl.explanation.substring(s, e) +
                        (if (e < hl.explanation.length) "…" else "")
                    } else hl.explanation.take(200) + "…"

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)) {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Context", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF888888))
                            Text(ctx, fontSize = 13.sp, lineHeight = 20.sp,
                                color = Color(0xFF333333))
                        }
                    }
                }
            }

            // Single "Got it" action
            item {
                Button(
                    onClick = {
                        viewModel.rateHighlight(hl.id, 3)  // revisit in 3 days
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Text("✓  Reviewed — next in 3 days",
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.rateHighlight(hl.id, 1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tomorrow", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { viewModel.rateHighlight(hl.id, 14) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("In 2 weeks", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
