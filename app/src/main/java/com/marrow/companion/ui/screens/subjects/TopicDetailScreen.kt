package com.marrow.companion.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Teal      = Color(0xFF4DC8D4)
private val TealLight = Color(0xFFE0F7FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    subjectId: Long,
    topicId: Long,
    onBack: () -> Unit,
    onSolve: (Long) -> Unit,
    onStartFresh: (Long) -> Unit,
    onReview: (Long, Long) -> Unit,
    onBookmarks: (Long, Long) -> Unit,
    onNotes: (Long, Long) -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val topics      by viewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())
    val subjectList by viewModel.subjects.collectAsState()
    val topic        = topics.find { it.id == topicId }
    val subjectName  = subjectList.find { it.id == subjectId }?.name ?: ""

    val notes          by viewModel.getNotesForTopic(topicId).collectAsState(initial = emptyList())
    val highlights     by viewModel.getHighlightsForTopic(topicId).collectAsState(initial = emptyList())
    val bookmarkCount  by viewModel.getBookmarkCountForTopic(topicId).collectAsState(initial = 0)
    val notesCount      = notes.size + highlights.size
    val hasPaused      by viewModel.hasPausedQuiz(topicId).collectAsState(initial = false)
    val pausedIndex    by viewModel.getPausedIndex(topicId).collectAsState(initial = null)
    val attemptedCount by viewModel.getAttemptedCountForTopic(topicId).collectAsState(initial = 0)
    val totalQuestions  = topic?.questionCount ?: 0
    val isCompleted     = totalQuestions > 0 && attemptedCount >= totalQuestions && !hasPaused

    var completionDate by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            val ts = viewModel.getLastAttemptTime(topicId)
            completionDate = ts?.let {
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.name ?: "Topic", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Subject + topic header ─────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(subjectName, fontSize = 14.sp, color = Teal, fontWeight = FontWeight.SemiBold)
                    Text(topic?.name ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D))
                    if (isCompleted) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.CheckCircle, null,
                                tint = Color(0xFF66BB6A), modifier = Modifier.size(18.dp))
                            Text(
                                "You've completed this module${completionDate?.let { " on $it" } ?: ""}",
                                fontSize = 13.sp, color = Color(0xFF555555)
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            // ── No questions empty state ──────────────────────────────────
            if (totalQuestions == 0) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.HourglassEmpty, null,
                                tint = Color(0xFFCCCCCC), modifier = Modifier.size(52.dp))
                            Text("No questions added yet", color = Color.Gray,
                                fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("Questions for this topic will appear here soon",
                                color = Color(0xFFAAAAAA), fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }

            // ── MCQ count + SOLVE / CONTINUE card ────────────────────────────
            if (totalQuestions > 0) item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: MCQ count + status
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "$totalQuestions MCQs",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D2D2D)
                                )
                                Text(
                                    when {
                                        isCompleted -> "All Completed"
                                        hasPaused   -> "In Progress"
                                        else        -> "Solve now"
                                    },
                                    fontSize = 13.sp,
                                    color = when {
                                        isCompleted -> Color(0xFF66BB6A)
                                        hasPaused   -> Color(0xFF888888)
                                        else        -> Color(0xFF888888)
                                    }
                                )
                            }
                            // Pause badge (only when in progress)
                            if (hasPaused && !isCompleted) {
                                Box(
                                    Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF0F0F0))
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Pause, null,
                                        tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Right: action button(s)
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isCompleted) onReview(subjectId, topicId)
                                    else onSolve(topicId)
                                },
                                colors  = ButtonDefaults.buttonColors(containerColor = Teal),
                                shape   = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                if (hasPaused && !isCompleted) {
                                    Icon(Icons.Filled.PlayArrow, null,
                                        modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    when {
                                        isCompleted -> "REVIEW"
                                        hasPaused   -> "CONTINUE"
                                        else        -> "SOLVE"
                                    },
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            if (hasPaused && !isCompleted) {
                                Text("Start Fresh", fontSize = 12.sp, color = Color(0xFF888888),
                                    modifier = Modifier.clickable { onStartFresh(topicId) })
                            }
                            if (isCompleted) {
                                Text("Solve Again", fontSize = 12.sp, color = Color(0xFF888888),
                                    modifier = Modifier.clickable { onStartFresh(topicId) })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Bookmarks + My Notes rows (only when questions exist) ─────────
            if (totalQuestions > 0) item {
                DetailRow(
                    icon    = Icons.Filled.Bookmark,
                    label   = "$bookmarkCount Bookmarks",
                    count   = null,
                    onClick = { onBookmarks(subjectId, topicId) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFEEEEEE))
            }

            // ── My Notes row ───────────────────────────────────────────────────
            if (totalQuestions > 0) item {
                DetailRow(
                    icon    = Icons.Filled.Edit,
                    label   = "My Notes",
                    count   = if (notesCount > 0) notesCount else null,
                    onClick = { onNotes(subjectId, topicId) }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Teal, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
        if (count != null && count > 0) {
            Box(
                Modifier.clip(RoundedCornerShape(20.dp)).background(Teal)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("$count", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null,
            tint = Color(0xFFBBBBBB), modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun TopicNoteCard(note: NoteEntity) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        note.attachedQuote?.let { quote ->
            Text(
                "\"${quote.take(80)}\"",
                fontSize  = 11.sp,
                color     = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .heightIn(min = 14.dp)
                    .background(Teal)
                    .clip(RoundedCornerShape(2.dp))
            )
            Text(
                note.text,
                fontSize   = 14.sp,
                lineHeight = 20.sp,
                color      = Color(0xFF333333),
                modifier   = Modifier.weight(1f)
            )
        }
        if (tagLabel != null) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(tagColor.copy(0.12f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(tagLabel, fontSize = 10.sp, color = tagColor,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color    = Color(0xFFEEEEEE)
    )
}
