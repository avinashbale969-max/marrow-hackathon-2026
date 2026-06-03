package com.marrow.companion.ui.screens.subjects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.entities.SubjectEntity
import com.marrow.companion.data.database.entities.TopicEntity

private val TealHeader   = Color(0xFF4DC8D4)
private val IconBg       = Color(0xFFDFF4F8)
private val IconTint     = Color(0xFF00ACC1)
private val ProgressGreen = Color(0xFF8BC34A)
private val TrackGray    = Color(0xFFE8E8E8)

private fun subjectIcon(name: String): ImageVector = when {
    name.contains("Anat",      ignoreCase = true) -> Icons.Filled.AccessibilityNew
    name.contains("Med",       ignoreCase = true) -> Icons.Filled.MedicalServices
    name.contains("Surg",      ignoreCase = true) -> Icons.Filled.ContentCut
    name.contains("OBG",       ignoreCase = true) ||
    name.contains("Obst",      ignoreCase = true) -> Icons.Filled.ChildFriendly
    name.contains("Pedi",      ignoreCase = true) -> Icons.Filled.ChildCare
    name.contains("Pharm",     ignoreCase = true) -> Icons.Filled.Medication
    name.contains("Path",      ignoreCase = true) -> Icons.Filled.Biotech
    name.contains("Physio",    ignoreCase = true) -> Icons.Filled.MonitorHeart
    name.contains("Micro",     ignoreCase = true) -> Icons.Filled.Science
    name.contains("Biochem",   ignoreCase = true) -> Icons.Filled.Science
    name.contains("PSM",       ignoreCase = true) ||
    name.contains("Community", ignoreCase = true) -> Icons.Filled.Groups
    name.contains("Psych",     ignoreCase = true) -> Icons.Filled.Psychology
    name.contains("Forensic",  ignoreCase = true) -> Icons.Filled.Gavel
    name.contains("Radio",     ignoreCase = true) -> Icons.Filled.RadioButtonChecked
    else                                           -> Icons.Filled.MenuBook
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onSubjectClick: (Long) -> Unit,
    onBookmarksClick: () -> Unit,
    onNotesClick: () -> Unit = {},
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val state          by viewModel.qbankState.collectAsState()
    val notesCount     by viewModel.notesCount.collectAsState()
    val highlightsCount by viewModel.highlightsCount.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("QBank Edition", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TealHeader,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealHeader)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // QBank Tracker
            item {
                Spacer(Modifier.height(8.dp))
                TrackerRow(
                    total = state.totalQuestions,
                    attempted = state.subjects.sumOf { it.attempted }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Bookmarks + Custom Module + Notes
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.Bookmark,
                        title    = "Bookmarks",
                        subtitle = "${state.bookmarkedCount} Bookmarks",
                        onClick  = onBookmarksClick
                    )
                    QuickCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.Add,
                        title    = "Custom Module",
                        subtitle = "Customised MCQs",
                        onClick  = {}
                    )
                }
                Spacer(Modifier.height(10.dp))

                // Notes widget — full width
                NotesWidget(
                    notesCount      = notesCount,
                    highlightsCount = highlightsCount,
                    onClick         = onNotesClick,
                    modifier        = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Subject cards
            items(state.subjects) { item ->
                SubjectCard(
                    subject = item.subject,
                    attempted = item.attempted,
                    onClick = { onSubjectClick(item.subject.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NotesWidget(
    notesCount: Int,
    highlightsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick    = onClick,
        modifier   = modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(12.dp),
        colors     = CardDefaults.cardColors(containerColor = Color.White),
        elevation  = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Edit, null,
                    tint = Color(0xFF43A047), modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("My Notes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = Color(0xFF1A1A1A))
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notes count chip
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Edit, null,
                            tint = TealHeader, modifier = Modifier.size(12.dp))
                        Text("$notesCount Notes",
                            fontSize = 12.sp, color = Color(0xFF666666))
                    }
                    Text("·", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    // Highlights count chip
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Highlight, null,
                            tint = Color(0xFFFFA726), modifier = Modifier.size(12.dp))
                        Text("$highlightsCount Highlights",
                            fontSize = 12.sp, color = Color(0xFF666666))
                    }
                }
            }

            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                tint = Color.LightGray, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
private fun TrackerRow(total: Int, attempted: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📈", fontSize = 18.sp)
                Text("QBank tracker", style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null,
                modifier = Modifier.size(13.dp), tint = Color.LightGray)
        }
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Icon inline with title on same row
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = TealHeader, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            }
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectEntity,
    attempted: Int,
    onClick: () -> Unit
) {
    val total = subject.totalQuestions.coerceAtLeast(1)
    val fraction = (attempted.toFloat() / total).coerceIn(0f, 1f)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Light blue icon circle — consistent across all subjects
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(IconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subjectIcon(subject.name),
                    contentDescription = null,
                    tint = IconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    subject.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = ProgressGreen,
                    trackColor = TrackGray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$attempted / ${subject.totalQuestions} modules",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = Color.LightGray
            )
        }
    }
}

// ── Topic list ──────────────────────────────────────────────────────────────

private enum class TopicFilter { ALL, PAUSED, COMPLETED, UNATTEMPTED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicListScreen(
    subjectId: Long,
    onTopicClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val topics         by viewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())
    val subjectName     = viewModel.subjects.collectAsState().value.find { it.id == subjectId }?.name ?: "Topics"
    val pausedIds      by viewModel.getAllPausedTopicIds().collectAsState(initial = emptyList())
    val attemptedList  by viewModel.getAttemptedPerTopic(subjectId).collectAsState(initial = emptyList())
    val attemptedMap    = attemptedList.associate { it.topicId to it.attempted }

    var selectedFilter by remember { mutableStateOf(TopicFilter.ALL) }

    val filteredTopics = topics.filter { topic ->
        val attempted  = attemptedMap[topic.id] ?: 0
        val isPaused   = pausedIds.contains(topic.id)
        val isComplete = attempted >= topic.questionCount && topic.questionCount > 0
        when (selectedFilter) {
            TopicFilter.ALL         -> true
            TopicFilter.PAUSED      -> isPaused
            TopicFilter.COMPLETED   -> isComplete
            TopicFilter.UNATTEMPTED -> attempted == 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subjectName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.FormatListBulleted, null,
                            tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Index", fontSize = 13.sp, color = Color.White,
                            fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = TealHeader,
                    titleContentColor         = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Filter tabs ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealHeader)
            ) {
                TopicFilter.entries.forEach { filter ->
                    val sel = filter == selectedFilter
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = filter }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            filter.label,
                            fontSize   = 13.sp,
                            color      = if (sel) Color.White else Color.White.copy(alpha = 0.65f),
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                        )
                        Box(
                            Modifier.fillMaxWidth(0.6f).height(2.dp)
                                .background(if (sel) Color.White else Color.Transparent)
                        )
                    }
                }
            }

            if (filteredTopics.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No topics found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    itemsIndexed(filteredTopics) { index, topic ->
                        val attempted  = attemptedMap[topic.id] ?: 0
                        val isPaused   = pausedIds.contains(topic.id)
                        val isComplete = attempted >= topic.questionCount && topic.questionCount > 0
                        val isLast     = index == filteredTopics.lastIndex

                        TopicItemMarrow(
                            number     = index + 1,
                            topic      = topic,
                            isPaused   = isPaused,
                            isComplete = isComplete,
                            attempted  = attempted,
                            isLast     = isLast,
                            onClick    = { onTopicClick(topic.id) }
                        )
                    }
                }
            }
        }
    }
}

private val TopicFilter.label get() = when (this) {
    TopicFilter.ALL         -> "All"
    TopicFilter.PAUSED      -> "Paused"
    TopicFilter.COMPLETED   -> "Completed"
    TopicFilter.UNATTEMPTED -> "Unattempted"
}

@Composable
private fun TopicItemMarrow(
    number: Int,
    topic: TopicEntity,
    isPaused: Boolean,
    isComplete: Boolean,
    attempted: Int,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val iconBg = topicGradientColor(number)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ── Left: number + dashed timeline ────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            Text(
                "$number",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = Color(0xFF888888)
            )
            if (!isLast) {
                Canvas(modifier = Modifier.width(1.dp).height(72.dp)) {
                    drawLine(
                        color       = Color(0xFFCCCCCC),
                        start       = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                        end         = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                        strokeWidth = 2f,
                        pathEffect  = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 6f), 0f
                        )
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // ── Right: card ────────────────────────────────────────────────────
        Card(
            onClick    = onClick,
            modifier   = Modifier.weight(1f).padding(bottom = 8.dp),
            shape      = RoundedCornerShape(10.dp),
            colors     = CardDefaults.cardColors(containerColor = Color.White),
            elevation  = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Topic icon (gradient colored box as image placeholder)
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        subjectIcon(topic.name),
                        contentDescription = null,
                        tint     = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Topic info
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        topic.name,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1A1A1A),
                        lineHeight = 20.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107),
                            modifier = Modifier.size(13.dp))
                        Text("4.5", fontSize = 12.sp, color = Color(0xFF888888))
                        Text("│", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                        Text("${topic.questionCount} MCQs", fontSize = 12.sp, color = Color(0xFF888888))
                    }
                    when {
                        isPaused   -> Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Pause, null, tint = Color(0xFF2196F3),
                                modifier = Modifier.size(12.dp))
                            Text("In Progress", fontSize = 11.sp, color = Color(0xFF2196F3))
                        }
                        isComplete -> Text("Completed", fontSize = 11.sp, color = Color(0xFF66BB6A),
                            fontWeight = FontWeight.Medium)
                        attempted > 0 -> {
                            val pct = (attempted * 100 / topic.questionCount.coerceAtLeast(1))
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                LinearProgressIndicator(
                                    progress     = { pct / 100f },
                                    modifier     = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(50)),
                                    color        = Color(0xFF8BC34A),
                                    trackColor   = Color(0xFFE8E8E8)
                                )
                                Text("$pct%", fontSize = 11.sp, color = Color(0xFF888888))
                            }
                        }
                    }
                }

                // Completion badge
                if (isComplete) {
                    Icon(Icons.Filled.CheckCircle, null,
                        tint     = Color(0xFF66BB6A),
                        modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

private fun topicGradientColor(index: Int): androidx.compose.ui.graphics.Color {
    val colors = listOf(
        Color(0xFF4DC8D4), Color(0xFF42A5F5), Color(0xFF66BB6A),
        Color(0xFFAB47BC), Color(0xFFFF7043), Color(0xFF26C6DA),
        Color(0xFF78909C), Color(0xFF5C6BC0), Color(0xFF26A69A)
    )
    return colors[index % colors.size]
}

@Composable
private fun TopicItem(topic: TopicEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(topic.name, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)
                Text("${topic.questionCount} questions",
                    style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null,
                modifier = Modifier.size(13.dp), tint = Color.LightGray)
        }
    }
}
