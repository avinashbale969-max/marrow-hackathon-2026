package com.marrow.companion.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.entities.BookmarkType
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag

private val TealRef  = Color(0xFF4DC8D4)
private val GreenHL  = Color(0xFF66BB6A)
private val OrangeHL = Color(0xFFFFA726)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectReferenceSheet(
    notes: List<NoteEntity>,
    highlights: List<HighlightEntity>,
    bookmarked: List<QuestionWithOptions>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF4F4F4),
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.9f)) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Notes & Bookmarks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, null, tint = Color(0xFF555555))
                }
            }

            // Tab bar
            Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                RefTab("Notes",      notes.size,      selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                RefTab("Highlights", highlights.size, selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                RefTab("Bookmarks",  bookmarked.size, selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (notes.isEmpty()) {
                            item { RefEmptyState("No notes for this subject yet") }
                        } else {
                            items(notes) { note -> RefNoteCard(note) }
                        }
                    }
                    1 -> {
                        if (highlights.isEmpty()) {
                            item { RefEmptyState("No highlights for this subject yet") }
                        } else {
                            items(highlights) { hl -> RefHighlightCard(hl) }
                        }
                    }
                    2 -> {
                        if (bookmarked.isEmpty()) {
                            item { RefEmptyState("No bookmarked questions for this subject") }
                        } else {
                            items(bookmarked) { qwo -> RefBookmarkCard(qwo) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefTab(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                color = if (selected) TealRef else Color.Gray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp
            )
            if (count > 0) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(if (selected) TealRef else Color(0xFFDDDDDD))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        "$count",
                        fontSize = 10.sp,
                        color = if (selected) Color.White else Color.Gray
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth(0.7f)
                .height(2.dp)
                .background(if (selected) TealRef else Color.Transparent)
        )
    }
}

@Composable
private fun RefNoteCard(note: NoteEntity) {
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

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            note.attachedQuote?.let { quote ->
                Text(
                    "\"${quote.take(80)}\"",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Edit, null, tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(13.dp).padding(top = 2.dp))
                Text(note.text, fontSize = 13.sp, lineHeight = 19.sp,
                    color = Color(0xFF333333), modifier = Modifier.weight(1f))
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
    }
}

@Composable
private fun RefHighlightCard(hl: HighlightEntity) {
    val accent = if (hl.color == HighlightColor.ORANGE.name) OrangeHL else GreenHL
    val bg     = if (hl.color == HighlightColor.ORANGE.name) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .width(3.dp)
                    .heightIn(min = 16.dp)
                    .background(accent)
                    .clip(RoundedCornerShape(2.dp))
            )
            Text(
                "\"${hl.text}\"",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RefBookmarkCard(qwo: QuestionWithOptions) {
    val (bmColor, bmIcon, bmLabel) = when (qwo.question.bookmarkType) {
        BookmarkType.STARRED.name -> Triple(Color(0xFFE53935), Icons.Filled.Star, "Starred")
        BookmarkType.REVIEW.name  -> Triple(Color(0xFFFFC107), Icons.Filled.Help, "Review")
        else                      -> Triple(Color(0xFF2196F3), Icons.Filled.Bookmark, "Normal")
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(bmIcon, null, tint = bmColor, modifier = Modifier.size(13.dp))
                Text(bmLabel, fontSize = 11.sp, color = bmColor, fontWeight = FontWeight.SemiBold)
            }
            Text(qwo.question.questionText, fontSize = 13.sp, lineHeight = 20.sp,
                color = Color(0xFF1A1A1A), fontWeight = FontWeight.Medium)
            val correctOpt = qwo.options.firstOrNull { it.isCorrect }
            if (correctOpt != null) {
                val lbl = listOf("A", "B", "C", "D").getOrElse(correctOpt.optionIndex) { "?" }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(16.dp).clip(CircleShape).background(GreenHL),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Check, null, tint = Color.White,
                            modifier = Modifier.size(10.dp))
                    }
                    Text("$lbl. ${correctOpt.optionText}", fontSize = 12.sp,
                        color = GreenHL, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun RefEmptyState(message: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
