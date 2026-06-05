package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marrow.companion.data.database.entities.BookmarkType
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag
import java.text.SimpleDateFormat
import java.util.*

private val TealHeader  = Color(0xFF4DC8D4)
private val GreenHL     = Color(0xFF66BB6A)
private val OrangeHL    = Color(0xFFFFA726)

// ── My Notes folder bottom sheet ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNotesSheet(
    highlights: List<HighlightEntity>,
    notes: List<NoteEntity>,
    taggedHighlightIds: Set<Long> = emptySet(),
    bookmarkType: String? = null,   // kept for API compat, unused
    onDeleteHighlight: (HighlightEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onAddNote: (String, NoteTag, String?) -> Unit,
    onEditNote: ((NoteEntity, String, NoteTag) -> Unit)? = null,
    onMcqClick: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedTab      by remember { mutableIntStateOf(0) }
    var hlColorFilter    by remember { mutableStateOf<String?>(null) }
    var noteTagFilter    by remember { mutableStateOf<String?>(null) }
    var showNoteInput    by remember { mutableStateOf(false) }
    var pendingQuote     by remember { mutableStateOf<String?>(null) }
    var editingNote      by remember { mutableStateOf<NoteEntity?>(null) }

    // Edit sheet
    editingNote?.let { note ->
        NoteInputSheet(
            attachedQuote = note.attachedQuote,
            initialText   = note.text,
            initialTag    = NoteTag.entries.find { it.name == note.tag } ?: NoteTag.NONE,
            isEditing     = true,
            onSave        = { text, tag ->
                onEditNote?.invoke(note, text, tag)
                editingNote = null
            },
            onDismiss     = { editingNote = null }
        )
        return
    }

    if (showNoteInput) {
        NoteInputSheet(
            attachedQuote = pendingQuote,
            onSave = { text, tag ->
                onAddNote(text, tag, pendingQuote)
                showNoteInput = false
                pendingQuote  = null
            },
            onDismiss = { showNoteInput = false; pendingQuote = null }
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier   = Modifier.fillMaxHeight(0.88f),
        containerColor = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().background(TealHeader)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("My Notes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, null, tint = Color.White)
                    }
                }
            }

            // ── Highlights only (Notes tab removed) ───────────────────────────
            run {
                HighlightsTab(
                    highlights         = highlights,
                    taggedHighlightIds = taggedHighlightIds,
                    colorFilter        = hlColorFilter,
                    onFilterColor      = { hlColorFilter = if (hlColorFilter == it) null else it },
                    onDelete           = onDeleteHighlight,
                    onAddNote          = { quote -> pendingQuote = quote; showNoteInput = true },
                    onMcqClick         = onMcqClick
                )
            }
        }
    }
}

// ── Notes tab ─────────────────────────────────────────────────────────────────

@Composable
private fun NotesTab(
    notes: List<NoteEntity>,
    tagFilter: String?,
    onFilterTag: (String) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onEdit: (NoteEntity) -> Unit,
    onAddNote: () -> Unit,
    onAttachNote: (String) -> Unit
) {
    val filtered = if (tagFilter == null) notes
    else notes.filter { it.tag == tagFilter }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tag filter chips
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TagFilterChip("⭐ Imp",    NoteTag.IMP.name,   tagFilter, onFilterTag)
            TagFilterChip("❓ Doubt",  NoteTag.DOUBT.name, tagFilter, onFilterTag)
        }

        if (filtered.isEmpty()) {
            EmptyState(
                icon    = Icons.Filled.Edit,
                title   = "No notes yet",
                subtitle = "Tap + to write a personal note",
                action  = "Add Note",
                onAction = onAddNote
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { note ->
                    NoteCard(
                        note     = note,
                        onDelete = { onDelete(note) },
                        onEdit   = { onEdit(note) }
                    )
                }
            }
        }
    }
}

// ── Highlights tab ────────────────────────────────────────────────────────────

@Composable
private fun HighlightsTab(
    highlights: List<HighlightEntity>,
    taggedHighlightIds: Set<Long> = emptySet(),
    colorFilter: String?,
    onFilterColor: (String) -> Unit,
    onDelete: (HighlightEntity) -> Unit,
    onAddNote: (String) -> Unit,
    onMcqClick: ((Long) -> Unit)? = null
) {
    val filtered = if (colorFilter == null) highlights
    else highlights.filter { it.color == colorFilter }

    Column(modifier = Modifier.fillMaxSize()) {
        // Color filter chips
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter:", fontSize = 12.sp, color = Color.Gray)
            ColorFilterChip(GreenHL,  HighlightColor.GREEN.name,  colorFilter, onFilterColor)
            ColorFilterChip(OrangeHL, HighlightColor.ORANGE.name, colorFilter, onFilterColor)
        }

        if (filtered.isEmpty()) {
            EmptyState(
                icon     = Icons.Filled.FormatPaint,
                title    = "No highlights yet",
                subtitle = "Select text in the explanation to highlight",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { hl ->
                    HighlightCard(
                        highlight  = hl,
                        hasTag     = hl.id in taggedHighlightIds,
                        onDelete   = { onDelete(hl) },
                        onAddNote  = { onAddNote(hl.text) },
                        onMcqClick = onMcqClick
                    )
                }
            }
        }
    }
}

// ── Bookmarks tab ─────────────────────────────────────────────────────────────

@Composable
private fun BookmarksTab(bookmarkType: String?) {
    if (bookmarkType == null) {
        EmptyState(
            icon     = Icons.Filled.BookmarkBorder,
            title    = "Not bookmarked",
            subtitle = "Tap the Bookmark button to save this question"
        )
        return
    }
    val (color, icon, label, desc) = when (bookmarkType) {
        BookmarkType.NORMAL.name  -> listOf(Color(0xFF2196F3), Icons.Filled.Bookmark,  "Normal",  "Saved for reference")
        BookmarkType.STARRED.name -> listOf(Color(0xFFE53935), Icons.Filled.Star,       "Starred", "Important question")
        BookmarkType.REVIEW.name  -> listOf(Color(0xFFFFC107), Icons.Filled.Help,       "Review",  "Needs revision")
        else -> listOf(Color.Gray, Icons.Filled.Bookmark, "Bookmarked", "")
    }
    @Suppress("UNCHECKED_CAST")
    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.TopCenter) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background((color as Color).copy(alpha = 0.12f)),
                    Alignment.Center
                ) {
                    Icon(icon as androidx.compose.ui.graphics.vector.ImageVector, null,
                        tint = color, modifier = Modifier.size(26.dp))
                }
                Column {
                    Text(label as String, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(desc as String, fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ── Cards ─────────────────────────────────────────────────────────────────────

@Composable
fun NoteCard(
    note: NoteEntity,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    var showNoteConfirm by remember { mutableStateOf(false) }
    if (showNoteConfirm) {
        com.marrow.companion.ui.common.ConfirmDeleteDialog(
            title   = "Delete Note?",
            message = "\"${note.text.take(60)}${if (note.text.length > 60) "…" else ""}\" will be permanently removed.",
            onConfirm = onDelete,
            onDismiss = { showNoteConfirm = false }
        )
    }
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
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(note.createdAt))

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Attached quote
            note.attachedQuote?.let { quote ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(Color(0xFFDDDDDD)))
                    Text("\"$quote\"", fontSize = 12.sp, color = Color.Gray,
                        lineHeight = 18.sp, modifier = Modifier.weight(1f))
                }
            }

            // Note text
            Text(note.text, fontSize = 14.sp, color = Color(0xFF2D2D2D), lineHeight = 21.sp)

            // Footer: tag + time + edit + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tagLabel != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(tagColor.copy(alpha = 0.15f))
                            .border(1.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(tagLabel, fontSize = 11.sp, color = tagColor,
                            fontWeight = FontWeight.Medium)
                    }
                } else Spacer(Modifier.size(0.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(timeStr, fontSize = 11.sp, color = Color.LightGray)
                    if (onEdit != null) {
                        Icon(Icons.Filled.Edit, null, tint = TealHeader,
                            modifier = Modifier.size(16.dp).clickable(onClick = onEdit))
                    }
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(16.dp).clickable { showNoteConfirm = true })
                }
            }
        }
    }
}

@Composable
fun HighlightCard(
    highlight: HighlightEntity,
    hasTag: Boolean = false,
    onDelete: () -> Unit,
    onAddNote: (() -> Unit)? = null,
    onMcqClick: ((Long) -> Unit)? = null
) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        com.marrow.companion.ui.common.ConfirmDeleteDialog(
            title   = "Delete Highlight?",
            message = "\"${highlight.text.take(60)}${if (highlight.text.length > 60) "…" else ""}\" will be permanently removed.",
            onConfirm = onDelete,
            onDismiss = { showConfirm = false }
        )
    }
    val bgColor     = if (highlight.color == HighlightColor.ORANGE.name) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
    val accentColor = if (highlight.color == HighlightColor.ORANGE.name) OrangeHL else GreenHL
    val timeStr     = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(highlight.createdAt))

    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("\"${highlight.text}\"", fontSize = 14.sp, lineHeight = 21.sp,
                color = Color(0xFF2D2D2D))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(accentColor))
                    Text(highlight.color.lowercase(), fontSize = 11.sp, color = Color.Gray)
                    if (hasTag) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Tagged",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(timeStr, fontSize = 11.sp, color = Color.LightGray)
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(16.dp).clickable { showConfirm = true })
                }
            }
            if (onMcqClick != null) {
                val label = "MRW-${highlight.questionId.toString().padStart(5, '0')}"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(accentColor)
                            .clickable { onMcqClick(highlight.questionId) }
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

// ── Note input bottom sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInputSheet(
    attachedQuote: String? = null,
    initialText: String = "",
    initialTag: NoteTag = NoteTag.NONE,
    isEditing: Boolean = false,
    onSave: (String, NoteTag) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText    by remember { mutableStateOf(initialText) }
    var selectedTag by remember { mutableStateOf(initialTag) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier   = Modifier.fillMaxHeight(0.7f),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isEditing) "Edit Note" else "Self Note",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                TextButton(
                    onClick = { if (noteText.isNotBlank()) onSave(noteText.trim(), selectedTag) },
                    enabled = noteText.isNotBlank()
                ) {
                    Text(if (isEditing) "Update" else "Save",
                        color = if (noteText.isNotBlank()) TealHeader else Color.Gray,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            // Attached quote display
            attachedQuote?.let { quote ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.width(3.dp).height(40.dp).background(TealHeader))
                    Text("\"$quote\"", fontSize = 13.sp, color = Color.Gray,
                        lineHeight = 19.sp, modifier = Modifier.weight(1f))
                }
            }

            // Tag selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tag:", fontSize = 13.sp, color = Color.Gray)
                NoteTagChip("⭐ Imp",   NoteTag.IMP,   selectedTag) { selectedTag = it }
                NoteTagChip("❓ Doubt", NoteTag.DOUBT, selectedTag) { selectedTag = it }
            }

            // Text input
            OutlinedTextField(
                value         = noteText,
                onValueChange = { noteText = it },
                modifier      = Modifier.fillMaxWidth().weight(1f),
                placeholder   = { Text("Write your note here...", color = Color(0xFFBBBBBB)) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = TealHeader,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp, lineHeight = 23.sp, color = Color(0xFF2D2D2D))
            )
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun FolderTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    count: Int,
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
            Icon(icon, null,
                tint = if (selected) TealHeader else Color.Gray,
                modifier = Modifier.size(14.dp))
            Text(label,
                color = if (selected) TealHeader else Color.Gray,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp)
            if (count > 0) {
                Box(
                    Modifier.clip(CircleShape).background(
                        if (selected) TealHeader else Color(0xFFDDDDDD))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
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
private fun TagFilterChip(label: String, tag: String, current: String?, onFilter: (String) -> Unit) {
    val active = current == tag
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) TealHeader else Color.White)
            .border(1.dp, if (active) TealHeader else Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
            .clickable { onFilter(tag) }
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 12.sp,
            color = if (active) Color.White else Color.Gray,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun ColorFilterChip(color: Color, colorName: String, current: String?, onFilter: (String) -> Unit) {
    val active = current == colorName
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (active) Modifier.border(3.dp, Color(0xFF333333), CircleShape) else Modifier)
            .clickable { onFilter(colorName) }
    )
}

@Composable
private fun NoteTagChip(label: String, tag: NoteTag, current: NoteTag, onSelect: (NoteTag) -> Unit) {
    val active = current == tag
    val color  = if (tag == NoteTag.IMP) Color(0xFFFFC107) else Color(0xFF2196F3)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) color.copy(alpha = 0.15f) else Color.White)
            .border(1.dp, if (active) color else Color(0xFFDDDDDD), RoundedCornerShape(20.dp))
            .clickable { onSelect(if (active) NoteTag.NONE else tag) }
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 12.sp,
            color = if (active) color else Color.Gray,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(44.dp))
            Text(title, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFFAAAAAA), fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (action != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onAction) {
                    Text(action, color = TealHeader, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
