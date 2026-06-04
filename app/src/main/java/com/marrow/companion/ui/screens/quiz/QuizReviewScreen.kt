package com.marrow.companion.ui.screens.quiz

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.entities.BookmarkType
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag
import java.text.SimpleDateFormat
import java.util.*

private val TealHeader  = Color(0xFF4DC8D4)
private val CorrectGreen = Color(0xFF66BB6A)
private val GreenHL     = Color(0xFF66BB6A)
private val OrangeHL    = Color(0xFFFFA726)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizReviewScreen(
    correct: Int,
    total: Int,
    bookmarked: List<QuestionWithOptions>,
    notesMap: Map<Long, List<NoteEntity>>,
    highlightsMap: Map<Long, List<HighlightEntity>>,
    onDone: () -> Unit,
    onMcqClick: ((Long) -> Unit)? = null
) {
    val accuracy     = if (total > 0) (correct * 100) / total else 0
    var selectedTab  by remember { mutableIntStateOf(0) }

    // All notes and highlights across the quiz
    val allNotes      = notesMap.values.flatten()
    val allHighlights = highlightsMap.values.flatten()

    // Questions that have notes or highlights
    val annotatedQuestions = notesMap.keys.union(highlightsMap.keys)
        .mapNotNull { qId ->
            val notes = notesMap[qId].orEmpty()
            val hls   = highlightsMap[qId].orEmpty()
            if (notes.isEmpty() && hls.isEmpty()) null
            else Triple(qId, notes, hls)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealHeader,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF4F4F4)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Score card ────────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScoreStat("$correct/$total", "Score", TealHeader)
                        VerticalDivider(Modifier.height(40.dp), color = Color(0xFFEEEEEE))
                        ScoreStat("$accuracy%", "Accuracy",
                            if (accuracy >= 60) CorrectGreen else Color(0xFFEF5350))
                        VerticalDivider(Modifier.height(40.dp), color = Color(0xFFEEEEEE))
                        ScoreStat("${bookmarked.size}", "Bookmarked", Color(0xFF2196F3))
                        VerticalDivider(Modifier.height(40.dp), color = Color(0xFFEEEEEE))
                        ScoreStat("${allNotes.size + allHighlights.size}", "Saved", Color(0xFFFFA726))
                    }
                }
            }

            // ── Tabs ──────────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    ReviewTab("Bookmarks", Icons.Filled.Bookmark, bookmarked.size,
                        selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                    ReviewTab("Notes & Highlights",
                        Icons.Filled.Edit, allNotes.size + allHighlights.size,
                        selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Tab 0: Bookmarked questions ───────────────────────────────────
            if (selectedTab == 0) {
                if (bookmarked.isEmpty()) {
                    item {
                        EmptyReviewState(
                            icon     = Icons.Filled.BookmarkBorder,
                            message  = "No bookmarks in this quiz",
                            subtitle = "Tap the bookmark icon while answering to save questions"
                        )
                    }
                } else {
                    items(bookmarked) { qwo ->
                        BookmarkedQuestionCard(qwo = qwo)
                    }
                }
            }

            // ── Tab 1: Notes & Highlights ─────────────────────────────────────
            if (selectedTab == 1) {
                if (annotatedQuestions.isEmpty()) {
                    item {
                        EmptyReviewState(
                            icon     = Icons.Filled.Edit,
                            message  = "No notes or highlights yet",
                            subtitle = "Select text in any explanation to highlight or add a note"
                        )
                    }
                } else {
                    items(annotatedQuestions) { (qId, notes, highlights) ->
                        NotesHighlightsCard(
                            questionId = qId,
                            notes      = notes,
                            highlights = highlights,
                            onMcqClick = onMcqClick
                        )
                    }
                }
            }

            // ── Done button ───────────────────────────────────────────────────
            item {
                Button(
                    onClick  = onDone,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ScoreStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun ReviewTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = if (selected) TealHeader else Color.Gray,
                modifier = Modifier.size(14.dp))
            Text(label, color = if (selected) TealHeader else Color.Gray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp)
            if (count > 0) {
                Box(Modifier.clip(CircleShape)
                    .background(if (selected) TealHeader else Color(0xFFDDDDDD))
                    .padding(horizontal = 5.dp, vertical = 1.dp)) {
                    Text("$count", fontSize = 10.sp,
                        color = if (selected) Color.White else Color.Gray)
                }
            }
        }
        Box(Modifier.fillMaxWidth(0.7f).height(2.dp)
            .background(if (selected) TealHeader else Color.Transparent))
    }
}

@Composable
private fun BookmarkedQuestionCard(qwo: QuestionWithOptions) {
    val (bmColor, bmIcon, bmLabel) = when (qwo.question.bookmarkType) {
        BookmarkType.STARRED.name -> Triple(Color(0xFFE53935), Icons.Filled.Star, "Starred")
        BookmarkType.REVIEW.name  -> Triple(Color(0xFFFFC107), Icons.Filled.Help, "Review")
        else                      -> Triple(Color(0xFF2196F3), Icons.Filled.Bookmark, "Normal")
    }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Bookmark type badge
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(bmIcon, null, tint = bmColor, modifier = Modifier.size(14.dp))
                Text(bmLabel, fontSize = 11.sp, color = bmColor, fontWeight = FontWeight.SemiBold)
            }
            // Question text
            Text(qwo.question.questionText, fontSize = 14.sp, lineHeight = 21.sp,
                color = Color(0xFF1A1A1A), fontWeight = FontWeight.Medium)
            // Correct answer
            val correct = qwo.options.firstOrNull { it.isCorrect }
            if (correct != null) {
                val labels = listOf("A", "B", "C", "D")
                val lbl    = labels.getOrElse(correct.optionIndex) { "?" }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(18.dp).clip(CircleShape).background(CorrectGreen),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Check, null, tint = Color.White,
                            modifier = Modifier.size(11.dp))
                    }
                    Text("$lbl. ${correct.optionText}", fontSize = 13.sp,
                        color = CorrectGreen, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun NotesHighlightsCard(
    questionId: Long,
    notes: List<NoteEntity>,
    highlights: List<HighlightEntity>,
    onMcqClick: ((Long) -> Unit)? = null
) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // MCQ ID pill
            if (onMcqClick != null) {
                val label = "MRW-${questionId.toString().padStart(5, '0')}"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(TealHeader)
                            .clickable { onMcqClick(questionId) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                             fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                }
            }

            // Highlights
            highlights.forEach { hl ->
                val accent = if (hl.color == HighlightColor.ORANGE.name) OrangeHL else GreenHL
                val bg     = if (hl.color == HighlightColor.ORANGE.name)
                    Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(bg).padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.width(3.dp).heightIn(min = 16.dp)
                        .background(accent).clip(RoundedCornerShape(2.dp)))
                    Text("\"${hl.text}\"", fontSize = 13.sp, lineHeight = 19.sp,
                        color = Color(0xFF333333), modifier = Modifier.weight(1f))
                }
            }

            // Notes
            notes.forEach { note ->
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.attachedQuote?.let { quote ->
                        Text("\"${quote.take(60)}…\"", fontSize = 11.sp,
                            color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Edit, null, tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(13.dp).padding(top = 2.dp))
                        Text(note.text, fontSize = 13.sp, lineHeight = 19.sp,
                            color = Color(0xFF333333), modifier = Modifier.weight(1f))
                    }
                    if (tagLabel != null) {
                        Box(
                            Modifier.clip(RoundedCornerShape(20.dp))
                                .background(tagColor.copy(0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(tagLabel, fontSize = 10.sp, color = tagColor,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReviewState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subtitle: String
) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(44.dp))
            Text(message, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFFAAAAAA), fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

