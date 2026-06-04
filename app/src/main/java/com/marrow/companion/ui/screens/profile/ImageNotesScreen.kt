package com.marrow.companion.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.entities.NoteTag
import com.marrow.companion.ui.screens.quiz.HighlightableText
import com.marrow.companion.ui.screens.quiz.MyNotesSheet
import com.marrow.companion.ui.screens.quiz.NoteInputDialog
import com.marrow.companion.ui.screens.quiz.SelectionTranslateSheet

private val Teal = Color(0xFF4DC8D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageNotesScreen(
    imageId: String,
    imageUrl: String,
    title: String,
    onBack: () -> Unit,
    viewModel: VideoNotesViewModel = hiltViewModel()
) {
    // Use imageId as the lessonKey (stored as negative to avoid clashing with question IDs)
    val lessonKey = imageId.hashCode().toLong()

    val highlights by viewModel.getHighlights(lessonKey).collectAsState(initial = emptyList())
    val notes      by viewModel.getNotes(lessonKey).collectAsState(initial = emptyList())
    val tagNotes    = notes.filter { it.tag == NoteTag.TAG.name }

    var showNotesSheet   by remember { mutableStateOf(false) }
    var showTranslate    by remember { mutableStateOf(false) }
    var translateText    by remember { mutableStateOf("") }
    var pendingTagQuote  by remember { mutableStateOf<String?>(null) }
    var showTagDialog    by remember { mutableStateOf(false) }
    var selectionActive  by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val notesContent = lessonNotesContent(title, "")

    // ── Sheets ──────────────────────────────────────────────────────────────
    if (showNotesSheet) {
        MyNotesSheet(
            highlights        = highlights,
            notes             = notes.filter { it.tag != NoteTag.TAG.name },
            onDeleteHighlight = { viewModel.deleteHighlight(it) },
            onDeleteNote      = { viewModel.deleteNote(it) },
            onAddNote         = { text, tag, quote -> viewModel.addNote(lessonKey, text, tag, quote) },
            onEditNote        = { note, text, tag -> viewModel.updateNote(note, text, tag) },
            onDismiss         = { showNotesSheet = false }
        )
    }
    if (showTranslate && translateText.isNotBlank()) {
        SelectionTranslateSheet(selectedText = translateText,
            onDismiss = { showTranslate = false; translateText = "" })
    }
    if (showTagDialog && pendingTagQuote != null) {
        NoteInputDialog(
            initialText = "",
            onConfirm   = { text ->
                if (text.isNotBlank()) viewModel.addNote(lessonKey, text, NoteTag.TAG, pendingTagQuote)
                pendingTagQuote = null
            },
            onDismiss = { showTagDialog = false; pendingTagQuote = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    // My Notes button — same as QBank
                    TextButton(onClick = { showNotesSheet = true }) {
                        Text("My Notes", color = Color.White,
                            fontWeight = FontWeight.Medium, fontSize = 14.sp)
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
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState, enabled = !selectionActive)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Select-to-highlight hint (same as QBank)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFFFFA726),
                    modifier = Modifier.size(14.dp))
                Text("Select text → 🟢🟠 Highlight · 🏷 Tag · 🌐 Translate",
                    fontSize = 11.sp, color = Color(0xFF888888))
            }

            // Notes content — QBank-style HighlightableText
            HighlightableText(
                text                = notesContent,
                highlights          = highlights,
                notes               = tagNotes,
                onHighlight         = { text, color, _ ->
                    viewModel.addHighlight(lessonKey, text, color)
                },
                onTagSelected       = { sel -> pendingTagQuote = sel; showTagDialog = true },
                onDeleteTag         = { viewModel.deleteNote(it) },
                onEditTag           = { note, txt -> viewModel.updateNote(note, txt, NoteTag.TAG) },
                onScrollEnabled     = { en -> selectionActive = !en },
                scrollOffsetPx      = { scrollState.value },
                onTranslateSelected = { text -> translateText = text; showTranslate = true }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
