package com.marrow.companion.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.entities.QuestionOptionEntity
import kotlinx.coroutines.launch

private val TealHeader   = Color(0xFF4DC8D4)
private val CorrectGreen = Color(0xFF66BB6A)
private val WrongRed     = Color(0xFFE57373)
private val RingGreen    = Color(0xFF8BC34A)

@Composable
fun DashboardScreen(
    onStartQuiz: () -> Unit,
    onStartSubjectQuiz: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenExplanation: (Long, Long) -> Unit = { _, _ -> },
    onOpenRecall: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state               by viewModel.uiState.collectAsState()
    val nextHighlight       by viewModel.nextHighlightForReview.collectAsState()
    val dueCount            by viewModel.dueHighlightCount.collectAsState()
    val drawerState          = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope                = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                userName  = state.user?.name ?: "Demo User",
                onLogout  = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
                onClose   = { scope.launch { drawerState.close() } }
            )
        }
    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Full-teal hero section ────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealHeader)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Menu, null, tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { scope.launch { drawerState.open() } })
                        Spacer(Modifier.width(12.dp))
                        Text("Marrow", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                            color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        // PRO badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFDD835))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.BookmarkBorder, null, tint = Color.White,
                            modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Filled.Search, null, tint = Color.White,
                            modifier = Modifier.size(24.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Circular progress ring with animated counter
                    val attempted = state.user?.totalAttempts ?: 0
                    val total     = state.totalQuestionsInDb.coerceAtLeast(1)

                    // Single animation drives both counter AND ring — always in sync
                    val animatedCount = remember { androidx.compose.animation.core.Animatable(0f) }
                    val animatedFraction = if (total > 0) animatedCount.value / total else 0f

                    LaunchedEffect(attempted) {
                        animatedCount.snapTo(0f)
                        if (attempted > 0) {
                            kotlinx.coroutines.delay(300) // wait for screen to settle
                            animatedCount.animateTo(
                                targetValue = attempted.toFloat(),
                                animationSpec = androidx.compose.animation.core.tween(
                                    durationMillis = 1800,
                                    easing = androidx.compose.animation.core.CubicBezierEasing(
                                        0.16f, 1f, 0.3f, 1f  // EaseOutExpo — fast burst, ultra-smooth stop
                                    )
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Track ring
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 7.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            strokeCap = StrokeCap.Round
                        )
                        // Progress ring — derived from same animatedCount, always in sync
                        CircularProgressIndicator(
                            progress = { animatedFraction },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 7.dp,
                            color = RingGreen,
                            strokeCap = StrokeCap.Round
                        )
                        // Animated count number
                        Text(
                            "${animatedCount.value.toInt()}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Modules completed",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // ── MCQ OF THE DAY ───────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2))
                    .padding(top = 20.dp, bottom = 8.dp)
            ) {
                // Section header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDDDDD))
                    Text(
                        "  MCQ OF THE DAY  ",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDDDDD))
                }

                Spacer(Modifier.height(12.dp))

                if (state.mcqOfDay != null) {
                    McqOfDayCard(
                        qwo             = state.mcqOfDay!!,
                        selectedOptionId = state.daySelectedOptionId,
                        onSelectOption  = { viewModel.selectDayOption(it) },
                        onSeeExplanation = {
                            onOpenExplanation(
                                state.mcqOfDay!!.question.id,
                                state.daySelectedOptionId ?: -1L
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TealHeader)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ── HIGHLIGHT INBOX (below MCQ) ──────────────────────────────────────
        if (nextHighlight != null) {
            item {
                HighlightInboxCard(
                    highlight    = nextHighlight!!,
                    dueCount     = dueCount,
                    onTestRecall = onOpenRecall,
                    onGotIt      = { viewModel.dismissHighlight(nextHighlight!!.id) },
                    modifier     = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
    } // end ModalNavigationDrawer
}

// ── Drawer content ────────────────────────────────────────────────────────────

@Composable
private fun DrawerContent(
    userName: String,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(280.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealHeader)
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.firstOrNull()?.uppercaseChar()?.toString() ?: "D",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(userName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("demo@marrow.com", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFDD835))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("PRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Menu items
        DrawerItem(icon = Icons.Filled.Home,        label = "Home",        onClick = onClose)
        DrawerItem(icon = Icons.Filled.Quiz,         label = "QBank",       onClick = onClose)
        DrawerItem(icon = Icons.Filled.BarChart,     label = "Analytics",   onClick = onClose)
        DrawerItem(icon = Icons.Filled.BookmarkBorder, label = "Bookmarks", onClick = onClose)

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFFEEEEEE))

        // Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLogout)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Filled.Logout, null, tint = Color(0xFFEF5350), modifier = Modifier.size(22.dp))
            Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF5350))
        }
    }
}

@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF555555), modifier = Modifier.size(22.dp))
        Text(label, fontSize = 15.sp, color = Color(0xFF333333))
    }
}

@Composable
private fun McqOfDayCard(
    qwo: QuestionWithOptions,
    selectedOptionId: Long?,
    onSelectOption: (Long) -> Unit,
    onSeeExplanation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val answered = selectedOptionId != null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "MCQ of the Day" tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(10.dp).clip(CircleShape)
                        .background(TealHeader)
                )
                Text(
                    "MCQ of the Day - NEET PG 2024",
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Question
            Text(
                text = qwo.question.questionText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                lineHeight = 23.sp
            )

            // Options
            qwo.options.sortedBy { it.optionIndex }.forEach { option ->
                DayOptionCard(
                    option = option,
                    selectedId = selectedOptionId,
                    answered = answered,
                    onClick = { if (!answered) onSelectOption(option.id) }
                )
            }

            // See Explanation
            if (answered) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "See Explanation",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TealHeader,
                        modifier = Modifier
                            .clickable(onClick = onSeeExplanation)
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayOptionCard(
    option: QuestionOptionEntity,
    selectedId: Long?,
    answered: Boolean,
    onClick: () -> Unit
) {
    val isSelected = selectedId == option.id
    val labels     = listOf("A", "B", "C", "D")
    val label      = labels.getOrElse(option.optionIndex) { "?" }

    val (bgColor, textColor) = when {
        !answered       -> Color(0xFFF5F5F5) to Color(0xFF444444)
        option.isCorrect -> CorrectGreen.copy(alpha = 0.85f) to Color.White
        isSelected      -> WrongRed.copy(alpha = 0.85f)  to Color.White
        else            -> Color(0xFFF5F5F5) to Color(0xFF9E9E9E)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(enabled = !answered, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "$label)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            option.optionText,
            fontSize = 14.sp,
            fontWeight = if (answered && (option.isCorrect || isSelected)) FontWeight.SemiBold
            else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
    }
}



// ── Highlight Inbox Card ───────────────────────────────────────────────────────

@Composable
private fun HighlightInboxCard(
    highlight: com.marrow.companion.data.database.dao.HighlightForReview,
    dueCount: Int,
    onTestRecall: () -> Unit,
    onGotIt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now        = System.currentTimeMillis()
    val daysAgo    = ((now - highlight.createdAt) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    val daysLabel  = when (daysAgo) { 0 -> "Today"; 1 -> "1 day ago"; else -> "$daysAgo days ago" }
    val isReviewed = highlight.lastReviewedAt != null
    // Full date-time
    val hlColor    = if (highlight.color == "ORANGE") Color(0xFFFFA726) else Color(0xFF66BB6A)

    Column(modifier = modifier) {
        // ── Section header — OUTSIDE the card ────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Highlight, null, tint = Color(0xFF888888),
                modifier = Modifier.size(14.dp))
            Text("HIGHLIGHT INBOX", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF888888), letterSpacing = 1.sp,
                modifier = Modifier.weight(1f))
            // Only show badge when more than 1 due
            if (dueCount > 1) {
                Box(Modifier.clip(RoundedCornerShape(20.dp))
                    .background(TealHeader)
                    .padding(horizontal = 10.dp, vertical = 3.dp)) {
                    Text("$dueCount due", fontSize = 11.sp, color = Color.White,
                        fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ── "Time to revisit" + highlighted date ──────────────────────
                Text("Time to revisit this",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = Color(0xFF1A1A1A))
                Text(
                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(highlight.createdAt)),
                    fontSize = 12.sp, color = Color(0xFF888888)
                )

                // ── Metadata snippet ──────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {

                        // "X days ago · not reviewed" pill
                        Box(Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF3D2B0A).copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Schedule, null, tint = Color(0xFFE65100),
                                    modifier = Modifier.size(12.dp))
                                Text("$daysLabel · ${if (isReviewed) "reviewed" else "not reviewed"}",
                                    fontSize = 12.sp, color = Color(0xFFE65100),
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Single color dot matching the highlight
                        Box(Modifier.size(14.dp).clip(CircleShape).background(hlColor))
                    }
                }

                // ── Smart Revision button ─────────────────────────────────────
                Button(
                    onClick  = onTestRecall,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = TealHeader)
                ) {
                    Text("Smart Revision →", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
