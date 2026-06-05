package com.marrow.companion.ui.screens.subjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.dao.HighlightWithSubject
import com.marrow.companion.data.database.dao.NoteWithSubject
import com.marrow.companion.data.database.dao.TopicHighlightCount
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.NoteTag

private val Teal     = Color(0xFF4DC8D4)
private val GreenHL  = Color(0xFF66BB6A)
private val OrangeHL = Color(0xFFFFA726)

// ── Folder list screen (mirrors Marrow Bookmarks structure) ───────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNotesScreen(
    onBack: () -> Unit,
    onSubjectClick: (Long) -> Unit = {},
    onTopicClick: (subjectId: Long, topicId: Long) -> Unit = { _, _ -> },
    onNotesClick: () -> Unit = {},
    onGreenClick: () -> Unit = {},
    onOrangeClick: () -> Unit = {},
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val allHighlights  by viewModel.getAllHighlightsWithSubject().collectAsState(initial = emptyList())
    val hlBySubject    by viewModel.getHighlightsCountBySubject().collectAsState(initial = emptyList())
    val hlByTopic      by viewModel.getHighlightsCountByTopic().collectAsState(initial = emptyList())
    val subjects       by viewModel.subjects.collectAsState()
    val hlMap        = hlBySubject.associate { it.subjectId to it.count }
    val topicHlMap   = hlByTopic.groupBy { it.subjectId }  // subjectId -> List<TopicHighlightCount>
    val subjectsWithData = subjects
        .filter { it.id in hlMap.keys }
        .map { s -> Pair(s.id, s.name) }
        .sortedByDescending { hlMap[it.first] ?: 0 }

    val totalHighlights = allHighlights.size
    val expandedSubjects = remember { mutableStateMapOf<Long, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Highlights", fontWeight = FontWeight.Bold) },
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
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── All Subjects row ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable { onSubjectClick(-1L) }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All Subjects ($totalHighlights)", fontSize = 16.sp,
                        color = Color(0xFF333333), modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                        tint = Color(0xFFBBBBBB), modifier = Modifier.size(14.dp))
                }
            }

            // ── Type breakdown ────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypePill(Icons.Filled.Highlight, GreenHL,
                        allHighlights.count { it.color != HighlightColor.ORANGE.name }, "Green",
                        onClick = onGreenClick)
                    TypePill(Icons.Filled.Highlight, OrangeHL,
                        allHighlights.count { it.color == HighlightColor.ORANGE.name }, "Orange",
                        onClick = onOrangeClick)
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            // ── Per-subject expandable rows ───────────────────────────────
            if (subjectsWithData.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Highlight, null,
                                tint = Color(0xFFCCCCCC), modifier = Modifier.size(52.dp))
                            Text("No highlights yet",
                                color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                subjectsWithData.forEach { (subjectId, subjectName) ->
                    val count     = hlMap[subjectId] ?: 0
                    val expanded  = expandedSubjects[subjectId] ?: false
                    val topicRows = topicHlMap[subjectId]?.sortedByDescending { it.count } ?: emptyList()

                    // Subject header + animated lesson rows — single item for smooth animation
                    item(key = "subj_$subjectId") {
                        Column {
                            // Header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .clickable { expandedSubjects[subjectId] = !expanded }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(subjectName, fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF222222), modifier = Modifier.weight(1f))
                                Box(
                                    Modifier.clip(RoundedCornerShape(20.dp))
                                        .background(Teal.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("$count", fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, color = Teal)
                                }
                                Spacer(Modifier.width(10.dp))
                                Icon(
                                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(20.dp)
                                )
                            }

                            // Animated lesson rows
                            AnimatedVisibility(
                                visible = expanded,
                                enter = expandVertically(tween(160)) + fadeIn(tween(120)),
                                exit  = shrinkVertically(tween(140)) + fadeOut(tween(100))
                            ) {
                                Column {
                                    if (topicRows.isEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFAFAFA))
                                                .padding(start = 40.dp, end = 20.dp,
                                                    top = 12.dp, bottom = 12.dp)
                                        ) {
                                            Text("No lessons with highlights",
                                                fontSize = 13.sp, color = Color.Gray)
                                        }
                                    } else {
                                        topicRows.forEach { topicCount ->
                                            LessonRow(
                                                topicId   = topicCount.topicId,
                                                subjectId = subjectId,
                                                count     = topicCount.count,
                                                viewModel = viewModel,
                                                onClick   = { onTopicClick(subjectId, topicCount.topicId) }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    topicId: Long,
    subjectId: Long,
    count: Int,
    viewModel: SubjectsViewModel,
    onClick: () -> Unit
) {
    val topics by viewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())
    val topicName = topics.find { it.id == topicId }?.name ?: "Lesson $topicId"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .clickable(onClick = onClick)
            .padding(start = 40.dp, end = 20.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // lesson dot
        Box(
            Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Teal)
                .align(Alignment.CenterVertically)
        )
        Spacer(Modifier.width(10.dp))
        Text(topicName, fontSize = 13.sp, color = Color(0xFF444444),
            modifier = Modifier.weight(1f))
        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Teal)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
            tint = Color(0xFFCCCCCC), modifier = Modifier.size(11.dp))
    }
    HorizontalDivider(modifier = Modifier.padding(start = 40.dp, end = 16.dp),
        color = Color(0xFFF0F0F0))
}

@Composable
private fun TypePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color, count: Int, label: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column {
            Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// ── Subject-level detail screen ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectNotesScreen(
    subjectId: Long,
    onBack: () -> Unit,
    initialTab: Int = 0,
    initialColorFilter: String = "",
    onMcqClick: (Long) -> Unit = {},
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val isAll = subjectId == 0L
    val subjectName = if (isAll) "All Subjects"
        else viewModel.subjects.collectAsState().value.find { it.id == subjectId }?.name ?: "Notes"

    val notes      by (if (isAll) viewModel.getAllNotesWithSubject()
        else viewModel.getNotesWithSubject(subjectId)).collectAsState(initial = emptyList())
    val highlights by (if (isAll) viewModel.getAllHighlightsWithSubject()
        else viewModel.getHighlightsWithSubject(subjectId)).collectAsState(initial = emptyList())
    val tags       by (if (isAll) viewModel.getAllTagsWithSubject()
        else viewModel.getTagsWithSubject(subjectId)).collectAsState(initial = emptyList())

    var selectedTab  by remember { mutableIntStateOf(initialTab) }
    var colorFilter  by remember { mutableStateOf(initialColorFilter) }
    var searchQuery  by remember { mutableStateOf("") }
    var showSearch   by remember { mutableStateOf(false) }

    // Filtered lists based on search query
    val q = searchQuery.trim().lowercase()
    val filteredNotes = if (q.isEmpty()) notes else notes.filter {
        it.text.lowercase().contains(q) ||
        it.attachedQuote?.lowercase()?.contains(q) == true ||
        it.subjectName.lowercase().contains(q) ||
        it.topicName?.lowercase()?.contains(q) == true
    }
    val filteredHighlights = if (q.isEmpty()) highlights else highlights.filter {
        it.text.lowercase().contains(q) ||
        it.subjectName.lowercase().contains(q) ||
        it.topicName?.lowercase()?.contains(q) == true
    }
    val filteredTags = if (q.isEmpty()) tags else tags.filter {
        it.text.lowercase().contains(q) ||
        it.attachedQuote?.lowercase()?.contains(q) == true ||
        it.subjectName.lowercase().contains(q) ||
        it.topicName?.lowercase()?.contains(q) == true
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
                    IconButton(onClick = { showSearch = !showSearch; if (!showSearch) searchQuery = "" }) {
                        Icon(Icons.Filled.Search, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = Teal,
                    titleContentColor         = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search bar — shown below header when active
            if (showSearch) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search highlights, tags…",
                        fontSize = 14.sp, color = Color(0xFF999999)) },
                    leadingIcon   = { Icon(Icons.Filled.Search, null, tint = Teal,
                        modifier = Modifier.size(20.dp)) },
                    trailingIcon  = if (searchQuery.isNotEmpty()) {{
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, null, tint = Color(0xFF999999),
                                modifier = Modifier.size(18.dp))
                        }
                    }} else null,
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // When searching → hide tabs, show unified results across all types
            if (q.isNotEmpty()) {
                val totalResults = filteredNotes.size + filteredHighlights.size + filteredTags.size
                if (totalResults == 0) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Search, null,
                                tint = Color(0xFFCCCCCC), modifier = Modifier.size(48.dp))
                            Text("No results for \"$q\"", color = Color.Gray,
                                fontWeight = FontWeight.Medium)
                            Text("Try a different keyword", color = Color(0xFFAAAAAA), fontSize = 13.sp)
                        }
                    }
                } else {
                    // Result count summary
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$totalResults results", fontSize = 12.sp, color = Color.Gray)
                        if (filteredNotes.isNotEmpty())
                            ResultBadge("${filteredNotes.size} Notes", Teal)
                        if (filteredHighlights.isNotEmpty())
                            ResultBadge("${filteredHighlights.size} Highlights", GreenHL)
                        if (filteredTags.isNotEmpty())
                            ResultBadge("${filteredTags.size} Tags", Color(0xFFE53935))
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tags first (most specific search target)
                        if (filteredTags.isNotEmpty()) {
                            item { SearchSectionHeader("🏷 Tags (${filteredTags.size})") }
                            items(filteredTags, key = { "t${it.id}" }) { tag ->
                                TagCard(tag, showSubject = isAll)
                            }
                        }
                        if (filteredNotes.isNotEmpty()) {
                            item { SearchSectionHeader("✎ Notes (${filteredNotes.size})") }
                            items(filteredNotes, key = { "n${it.id}" }) { note ->
                                NoteCard(note, showSubject = isAll, onMcqClick = onMcqClick)
                            }
                        }
                        if (filteredHighlights.isNotEmpty()) {
                            item { SearchSectionHeader("Highlights (${filteredHighlights.size})") }
                            items(filteredHighlights, key = { "h${it.id}" }) { hl ->
                                HighlightCard(hl, showSubject = isAll,
                                    onDelete = { viewModel.deleteHighlight(hl.id) },
                                    onMcqClick = onMcqClick)
                            }
                        }
                    }
                }
            } else {
            // Highlights only (Notes tab removed)
            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Color filter chips
            if (true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = colorFilter.isEmpty(),
                        onClick  = { colorFilter = "" },
                        label    = { Text("All", fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal.copy(alpha = 0.15f),
                            selectedLabelColor     = Teal
                        )
                    )
                    FilterChip(
                        selected = colorFilter == "GREEN",
                        onClick  = { colorFilter = if (colorFilter == "GREEN") "" else "GREEN" },
                        label    = { Text("Green", fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenHL.copy(alpha = 0.15f),
                            selectedLabelColor     = GreenHL
                        )
                    )
                    FilterChip(
                        selected = colorFilter == "ORANGE",
                        onClick  = { colorFilter = if (colorFilter == "ORANGE") "" else "ORANGE" },
                        label    = { Text("Orange", fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeHL.copy(alpha = 0.15f),
                            selectedLabelColor     = OrangeHL
                        )
                    )
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            val displayedHighlights = if (colorFilter.isEmpty()) highlights
                                      else highlights.filter { it.color == colorFilter }
            val emptyMsg = if (colorFilter.isEmpty()) "No highlights yet"
                           else "No ${colorFilter.lowercase()} highlights yet"

            if (displayedHighlights.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(emptyMsg, color = Color.Gray, modifier = Modifier.padding(32.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedHighlights, key = { it.id }) {
                        HighlightCard(it, showSubject = isAll,
                            onDelete = { viewModel.deleteHighlight(it.id) },
                            onMcqClick = onMcqClick)
                    }
                }
            }
            } // end else (not searching)
        }
    }
}

@Composable
private fun ResultBadge(label: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.12f))
        .padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        color = Color(0xFF888888),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
}

@Composable
private fun NotesDetailTab(
    label: String, count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, fontSize = 14.sp,
                color = if (selected) Teal else Color.Gray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            if (count > 0) {
                Box(Modifier.clip(CircleShape)
                    .background(if (selected) Teal else Color(0xFFDDDDDD))
                    .padding(horizontal = 6.dp, vertical = 1.dp)) {
                    Text("$count", fontSize = 11.sp,
                        color = if (selected) Color.White else Color.Gray)
                }
            }
        }
        Box(Modifier.fillMaxWidth(0.6f).height(2.dp)
            .background(if (selected) Teal else Color.Transparent))
    }
}

// ── Cards with subject name label ─────────────────────────────────────────────

@Composable
private fun NoteCard(
    note: NoteWithSubject,
    showSubject: Boolean,
    onMcqClick: ((Long) -> Unit)? = null
) {
    val tagColor = when (note.tag) {
        NoteTag.IMP.name   -> Color(0xFFFFC107)
        NoteTag.DOUBT.name -> Color(0xFF2196F3)
        else               -> Color.Transparent
    }
    val tagLabel = when (note.tag) {
        NoteTag.IMP.name   -> "⭐ Imp"
        NoteTag.DOUBT.name -> "❓ Doubt"
        else               -> null
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {

            note.attachedQuote?.let { quote ->
                Row(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.width(3.dp).heightIn(min = 12.dp)
                        .background(Teal).clip(RoundedCornerShape(2.dp)))
                    Text("\"${quote.take(100)}\"", fontSize = 12.sp,
                        color = Color.Gray, fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Edit, null, tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(14.dp).padding(top = 2.dp))
                Text(note.text, fontSize = 14.sp, lineHeight = 20.sp,
                    color = Color(0xFF1A1A1A), modifier = Modifier.weight(1f))
            }
            if (tagLabel != null) {
                Box(Modifier.clip(RoundedCornerShape(20.dp))
                    .background(tagColor.copy(0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(tagLabel, fontSize = 11.sp, color = tagColor,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            // Subject • Topic
            SubjectTopicLabel(
                subjectName = note.subjectName,
                topicName   = note.topicName,
                color       = Color(0xFF888888)
            )

            // MCQ ID pill
            McqIdPill(
                questionId = note.questionId,
                accentColor = Teal,
                onClick     = onMcqClick
            )
        }
    }
}

@Composable
private fun TagCard(tag: NoteWithSubject, showSubject: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                // Red flag indicator
                Box(Modifier.size(0.dp), contentAlignment = Alignment.Center) {}
                androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, 0f); lineTo(size.width, size.height * 0.4f)
                        lineTo(0f, size.height * 0.8f); close()
                    }
                    drawPath(path, Color(0xFFE53935))
                }
                Text("Tag", fontSize = 11.sp, color = Color(0xFFE53935),
                    fontWeight = FontWeight.SemiBold)
            }
            tag.attachedQuote?.let { quote ->
                Text("\"${quote.take(120)}\"", fontSize = 12.sp,
                    color = Color(0xFF8D6E00), fontStyle = FontStyle.Italic)
                HorizontalDivider(color = Color(0xFFF9A825).copy(alpha = 0.3f))
            }
            Text(tag.text, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF5D4037),
                fontWeight = FontWeight.Medium)
            SubjectTopicLabel(subjectName = tag.subjectName, topicName = tag.topicName,
                color = Color(0xFF888888))
        }
    }
}

/** Tappable MCQ-ID badge. Formats questionId as "MRW-XXXXX". */
@Composable
private fun McqIdPill(
    questionId: Long,
    accentColor: Color,
    onClick: ((Long) -> Unit)?
) {
    val label = "MRW-${questionId.toString().padStart(5, '0')}"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor)
                    .clickable { onClick(questionId) }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    label,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color      = Color.White
                )
            }
        }
    }
}

@Composable
private fun SubjectTopicLabel(subjectName: String, topicName: String?, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(subjectName, fontSize = 12.sp, color = color)
        if (!topicName.isNullOrBlank()) {
            Box(Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape)
                .background(GreenHL))
            Text(topicName, fontSize = 12.sp, color = color)
        }
    }
}

@Composable
private fun HighlightCard(
    hl: HighlightWithSubject,
    showSubject: Boolean,
    onDelete: (() -> Unit)? = null,
    onMcqClick: ((Long) -> Unit)? = null
) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        com.marrow.companion.ui.common.ConfirmDeleteDialog(
            title   = "Delete Highlight?",
            message = "\"${hl.text.take(60)}${if (hl.text.length > 60) "…" else ""}\" will be permanently removed.",
            onConfirm = { onDelete?.invoke() },
            onDismiss = { showConfirm = false }
        )
    }

    val isOrange = hl.color == HighlightColor.ORANGE.name
    val accent   = if (isOrange) OrangeHL else GreenHL
    val bg       = if (isOrange) Color(0xFFFFFBF0) else Color(0xFFF0FBF1)
    val borderColor = if (isOrange) Color(0xFFFFCC80) else Color(0xFFA5D6A7)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top) {
                Box(Modifier.width(4.dp).heightIn(min = 18.dp)
                    .clip(RoundedCornerShape(2.dp)).background(accent))
                Text("\"${hl.text}\"", fontSize = 14.sp, lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A), modifier = Modifier.weight(1f))
                if (hl.hasTag) {
                    Icon(Icons.Filled.Flag, contentDescription = "Tagged",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp).padding(top = 2.dp))
                }
                if (onDelete != null) {
                    Icon(Icons.Filled.Delete, null, tint = accent.copy(alpha = 0.6f),
                        modifier = Modifier.size(17.dp).padding(top = 2.dp)
                            .clickable { showConfirm = true })
                }
            }

            SubjectTopicLabel(subjectName = hl.subjectName, topicName = hl.topicName,
                color = Color(0xFF888888))

            McqIdPill(
                questionId  = hl.questionId,
                accentColor = accent,
                onClick     = onMcqClick
            )
        }
    }
}
