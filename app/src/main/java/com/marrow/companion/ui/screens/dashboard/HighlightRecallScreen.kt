package com.marrow.companion.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.marrow.companion.ui.common.ConfirmDeleteDialog

private val Teal  = Color(0xFF4DC8D4)
private val Green = Color(0xFF66BB6A)
private val GreenBg = Color(0xFFE8F5E9)
private val OrangeBg = Color(0xFFFFF3E0)
private val Orange = Color(0xFFFFA726)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightRecallScreen(
    onBack: () -> Unit,
    onRated: () -> Unit = {},
    onOpenQuestion: (Long) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val highlight by viewModel.nextHighlightForReview.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val highlightId = highlight?.id

    // Mark as reviewed (next in 1 day) when user navigates BACK
    DisposableEffect(Unit) {
        onDispose {
            highlightId?.let { id -> viewModel.rateHighlight(id, 1) }
        }
    }

    if (showDeleteConfirm && highlight != null) {
        ConfirmDeleteDialog(
            title   = "Delete Highlight?",
            message = "This highlight will be removed permanently.",
            onConfirm = {
                highlight?.let { viewModel.dismissHighlight(it.id) }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Revision", fontWeight = FontWeight.Bold) },
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
        containerColor = Color.White
    ) { padding ->

        if (highlight == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)) {
                    Text("🎉", fontSize = 52.sp)
                    Text("All caught up!", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A))
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

        val hl       = highlight!!
        val hlColor  = if (hl.color == "ORANGE") Orange else Green
        val hlBg     = if (hl.color == "ORANGE") OrangeBg else GreenBg
        val qIdLabel = "MRW-${hl.questionId.toString().padStart(5, '0')}"

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Highlighted text card (matches Image 23) ──────────────────────
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(hlBg)
                    .padding(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top) {
                    Box(Modifier.width(4.dp).height(IntrinsicSize.Max).heightIn(min = 20.dp)
                        .fillMaxHeight().background(hlColor).clip(RoundedCornerShape(2.dp)))
                    Text(
                        "\"${hl.text}\"",
                        fontSize   = 16.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF1A1A1A),
                        modifier   = Modifier.weight(1f)
                    )
                }
            }

            // ── Subject · Topic ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(hl.subjectName, fontSize = 13.sp, color = Color(0xFF888888))
                Box(Modifier.size(6.dp).clip(CircleShape).background(hlColor))
                Text(hl.topicName ?: "", fontSize = 13.sp, color = Color(0xFF888888))
            }

            // ── MRW badge — tappable → opens question ─────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(hlColor)
                        .clickable { if (hl.questionId > 0) onOpenQuestion(hl.questionId) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(qIdLabel, fontSize = 13.sp, color = Color.White,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
