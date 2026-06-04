package com.marrow.companion.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.Delete
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag

private val Teal     = Color(0xFF4DC8D4)
private val GreenHL  = Color(0xFF66BB6A)
private val OrangeHL = Color(0xFFFFA726)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicNotesScreen(
    subjectId: Long,
    topicId: Long,
    onBack: () -> Unit,
    onMcqClick: (Long) -> Unit = {},
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val topics     by viewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())
    val notes      by viewModel.getNotesForTopic(topicId).collectAsState(initial = emptyList())
    val highlights by viewModel.getHighlightsForTopic(topicId).collectAsState(initial = emptyList())

    val topicName  = topics.find { it.id == topicId }?.name ?: "My Notes"

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topicName, fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Tab bar
            Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                NotesTab(
                    label    = "Notes",
                    count    = notes.size,
                    selected = selectedTab == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 0 }
                NotesTab(
                    label    = "Highlights",
                    count    = highlights.size,
                    selected = selectedTab == 1,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 1 }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            val isEmpty = if (selectedTab == 0) notes.isEmpty() else highlights.isEmpty()

            if (isEmpty) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Edit else Icons.Filled.Highlight,
                            null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(48.dp)
                        )
                        Text(
                            if (selectedTab == 0) "No notes for this lesson"
                            else "No highlights for this lesson",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedTab == 0) {
                        items(notes) { note -> NoteCard(note) }
                    } else {
                        items(highlights) { hl ->
                            HighlightCard(hl,
                                onDelete   = { viewModel.deleteHighlight(hl.id) },
                                onMcqClick = onMcqClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesTab(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                fontSize   = 14.sp,
                color      = if (selected) Teal else Color.Gray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (count > 0) {
                Box(
                    Modifier.clip(CircleShape)
                        .background(if (selected) Teal else Color(0xFFDDDDDD))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("$count", fontSize = 11.sp,
                        color = if (selected) Color.White else Color.Gray)
                }
            }
        }
        Box(
            Modifier.fillMaxWidth(0.6f).height(2.dp)
                .background(if (selected) Teal else Color.Transparent)
        )
    }
}

@Composable
private fun NoteCard(note: NoteEntity) {
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
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            note.attachedQuote?.let { quote ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.width(3.dp).heightIn(min = 12.dp)
                        .background(Teal).clip(RoundedCornerShape(2.dp)))
                    Text(
                        "\"${quote.take(100)}\"",
                        fontSize  = 12.sp,
                        color     = Color.Gray,
                        fontStyle = FontStyle.Italic,
                        modifier  = Modifier.weight(1f)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.Top
            ) {
                Icon(Icons.Filled.Edit, null, tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(14.dp).padding(top = 2.dp))
                Text(note.text, fontSize = 14.sp, lineHeight = 20.sp,
                    color = Color(0xFF1A1A1A), modifier = Modifier.weight(1f))
            }
            if (tagLabel != null) {
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(tagColor.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(tagLabel, fontSize = 11.sp, color = tagColor,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun HighlightCard(hl: HighlightEntity, onDelete: (() -> Unit)? = null, onMcqClick: ((Long) -> Unit)? = null) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        com.marrow.companion.ui.common.ConfirmDeleteDialog(
            title   = "Delete Highlight?",
            message = "\"${hl.text.take(60)}\" will be permanently removed.",
            onConfirm = { onDelete?.invoke() },
            onDismiss = { showConfirm = false }
        )
    }
    val isOrange = hl.color == HighlightColor.ORANGE.name
    val accent   = if (isOrange) OrangeHL else GreenHL
    val bg       = if (isOrange) Color(0xFFFFF8E1) else Color(0xFFE8F5E9)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier.width(4.dp).heightIn(min = 16.dp)
                        .background(accent).clip(RoundedCornerShape(2.dp))
                )
                Text(
                    "\"${hl.text}\"",
                    fontSize   = 14.sp,
                    lineHeight = 21.sp,
                    color      = Color(0xFF333333),
                    modifier   = Modifier.weight(1f)
                )
                if (onDelete != null) {
                    Icon(Icons.Filled.Delete, null, tint = accent.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp).clickable { showConfirm = true })
                }
            }
            if (onMcqClick != null) {
                val label = "MRW-${hl.questionId.toString().padStart(5, '0')}"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(accent)
                            .clickable { onMcqClick(hl.questionId) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                             fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                }
            }
        }
    }
}
