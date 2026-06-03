package com.marrow.companion.ui.screens.explanation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.HighlightDao
import com.marrow.companion.data.database.dao.NoteDao
import com.marrow.companion.data.database.dao.QuestionAttemptDao
import com.marrow.companion.data.database.dao.QuestionDao
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag
import com.marrow.companion.data.database.entities.QuestionAttemptEntity
import com.marrow.companion.data.database.entities.QuestionOptionEntity
import coil.compose.SubcomposeAsyncImage
import com.marrow.companion.ui.screens.quiz.BookmarkScreenshotEffect
import com.marrow.companion.ui.screens.quiz.BookmarkTypePopup
import com.marrow.companion.ui.screens.quiz.HighlightableText
import com.marrow.companion.ui.screens.quiz.MyNotesSheet
import com.marrow.companion.ui.screens.quiz.NoteInputDialog
import com.marrow.companion.ui.screens.quiz.SelectionTranslateSheet
import com.marrow.companion.ui.screens.quiz.StickyEditDialog
import com.marrow.companion.ui.screens.quiz.bookmarkColor
import com.marrow.companion.ui.screens.quiz.bookmarkIcon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private val TealHeader   = Color(0xFF4DC8D4)
private val CorrectGreen = Color(0xFF43A047)
private val WrongRed     = Color(0xFFE53935)
private val DimGray      = Color(0xFF9E9E9E)

// ── ViewModel ────────────────────────────────────────────────────────────────

data class ExplanationUiState(
    val qwo: QuestionWithOptions? = null,
    val selectedOptionId: Long? = null,
    val optionStats: Map<Long, Int> = emptyMap(),
    val highlights: List<HighlightEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class QuestionExplanationViewModel @Inject constructor(
    private val questionDao: QuestionDao,
    private val attemptDao: QuestionAttemptDao,
    private val highlightDao: HighlightDao,
    private val noteDao: NoteDao
) : ViewModel() {

    private val _state = MutableStateFlow(ExplanationUiState())
    val state: StateFlow<ExplanationUiState> = _state.asStateFlow()

    fun load(questionId: Long, preSelectedOptionId: Long? = null) {
        if (_state.value.qwo != null) return
        viewModelScope.launch {
            val qwo   = questionDao.getQuestionById(questionId)
            val stats = attemptDao.getOptionStats(questionId)
            _state.update {
                it.copy(
                    qwo              = qwo,
                    selectedOptionId = preSelectedOptionId,
                    optionStats      = stats.associate { s -> s.selectedOptionId to s.count },
                    isLoading        = false
                )
            }
            // Load highlights and notes for this question
            launch {
                highlightDao.getForQuestion(questionId).collect { list ->
                    _state.update { it.copy(highlights = list) }
                }
            }
            launch {
                noteDao.getForQuestion(questionId).collect { list ->
                    _state.update { it.copy(notes = list) }
                }
            }
        }
    }

    fun addHighlight(text: String, color: HighlightColor) {
        val qwo = _state.value.qwo ?: return
        viewModelScope.launch {
            highlightDao.insert(HighlightEntity(questionId = qwo.question.id,
                text = text, color = color.name))
        }
    }

    fun deleteHighlight(hl: HighlightEntity) {
        viewModelScope.launch { highlightDao.deleteById(hl.id) }
    }

    fun addNote(text: String, tag: NoteTag, attachedQuote: String?) {
        val qwo = _state.value.qwo ?: return
        viewModelScope.launch {
            noteDao.insert(NoteEntity(questionId = qwo.question.id,
                text = text, tag = tag.name, attachedQuote = attachedQuote))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteDao.deleteById(note.id) }
    }

    fun updateNote(note: NoteEntity, newText: String, newTag: NoteTag) {
        viewModelScope.launch { noteDao.update(note.copy(text = newText, tag = newTag.name)) }
    }

    fun getTagNotes(questionId: Long) = noteDao.getTagsForQuestion(questionId)

    fun selectOption(optionId: Long) {
        val qwo = _state.value.qwo ?: return
        if (_state.value.selectedOptionId != null) return
        val correct = qwo.options.find { it.id == optionId }?.isCorrect == true
        viewModelScope.launch {
            attemptDao.insert(QuestionAttemptEntity(
                userId = "local_user",
                questionId = qwo.question.id,
                selectedOptionId = optionId,
                isCorrect = correct,
                timeTakenMs = 0L
            ))
            val stats = attemptDao.getOptionStats(qwo.question.id)
            _state.update {
                it.copy(
                    selectedOptionId = optionId,
                    optionStats = stats.associate { s -> s.selectedOptionId to s.count }
                )
            }
        }
    }

    fun setBookmarkType(type: String?) {
        val qwo = _state.value.qwo ?: return
        viewModelScope.launch {
            questionDao.setBookmarkType(qwo.question.id, type)
            _state.update { it.copy(qwo = it.qwo?.copy(
                question = it.qwo.question.copy(bookmarkType = type))) }
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionExplanationScreen(
    questionId: Long,
    initialSelectedId: Long? = null,
    onBack: () -> Unit,
    viewModel: QuestionExplanationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showBookmarkPopup      by remember { mutableStateOf(false) }
    var screenshotType         by remember { mutableStateOf<String?>(null) }
    var showNotesSheet         by remember { mutableStateOf(false) }
    var showSelectionTranslate by remember { mutableStateOf(false) }
    var selectedTranslateText  by remember { mutableStateOf("") }
    var pendingTagQuote        by remember { mutableStateOf<String?>(null) }
    var showTagDialog          by remember { mutableStateOf(false) }
    var selectionActive        by remember { mutableStateOf(false) }
    val scrollState             = rememberScrollState()

    LaunchedEffect(questionId) { viewModel.load(questionId, initialSelectedId) }

    val tagNotes by viewModel.getTagNotes(questionId).collectAsState(initial = emptyList())

    if (showNotesSheet) {
        MyNotesSheet(
            highlights        = state.highlights,
            notes             = state.notes,
            bookmarkType      = state.qwo?.question?.bookmarkType,
            onDeleteHighlight = { viewModel.deleteHighlight(it) },
            onDeleteNote      = { viewModel.deleteNote(it) },
            onAddNote         = { text, tag, quote -> viewModel.addNote(text, tag, quote) },
            onEditNote        = { note, text, tag -> viewModel.updateNote(note, text, tag) },
            onDismiss         = { showNotesSheet = false }
        )
    }

    if (showSelectionTranslate && selectedTranslateText.isNotBlank()) {
        SelectionTranslateSheet(
            selectedText = selectedTranslateText,
            onDismiss    = { showSelectionTranslate = false; selectedTranslateText = "" }
        )
    }

    if (showBookmarkPopup) {
        BookmarkTypePopup(
            currentType = state.qwo?.question?.bookmarkType,
            onSelect    = { type ->
                viewModel.setBookmarkType(type)
                if (type != null) screenshotType = type
            },
            onDismiss = { showBookmarkPopup = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explanation", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealHeader,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    TextButton(onClick = { showNotesSheet = true }) {
                        Text("My Notes", color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            fontSize = 14.sp)
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (state.isLoading || state.qwo == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealHeader)
            }
            return@Scaffold
        }

        val qwo        = state.qwo!!
        val answered   = state.selectedOptionId != null
        val totalVotes = state.optionStats.values.sum().coerceAtLeast(1)
        val correctOpt = qwo.options.firstOrNull { it.isCorrect }
        val correctPct = if (answered)
            (state.optionStats.getOrDefault(correctOpt?.id ?: -1, 0) * 100 / totalVotes) else 0

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState, enabled = !selectionActive)
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(qwo.question.questionText, fontSize = 17.sp, lineHeight = 26.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))

                    // Image (if available)
                    qwo.question.imageUrl?.let { url ->
                        Spacer(Modifier.height(12.dp))
                        SubcomposeAsyncImage(
                            model   = url, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)
                                .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0F0F0)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            loading = {
                                Box(Modifier.fillMaxWidth().height(180.dp), Alignment.Center) {
                                    CircularProgressIndicator(color = TealHeader, modifier = Modifier.size(30.dp))
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    qwo.options.sortedBy { it.optionIndex }.forEach { option ->
                        ExplOptionRow(
                            option      = option,
                            selectedId  = state.selectedOptionId,
                            optionStats = state.optionStats,
                            totalVotes  = totalVotes,
                            answered    = answered,
                            onClick     = { if (!answered) viewModel.selectOption(option.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    if (answered) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(26.dp)) {
                                CircularProgressIndicator(
                                    progress = { correctPct / 100f },
                                    modifier = Modifier.fillMaxSize(), strokeWidth = 3.5.dp,
                                    color = CorrectGreen, trackColor = Color(0xFFE0E0E0))
                            }
                            Text("$correctPct% of the people got this right",
                                fontSize = 14.sp, color = Color(0xFF555555))
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TagChip("#Clinical"); TagChip("#NEET")
                        }
                        Spacer(Modifier.height(16.dp))

                        // Tag dialog
                        if (showTagDialog && pendingTagQuote != null) {
                            NoteInputDialog(
                                initialText = "",
                                onConfirm   = { noteText ->
                                    if (noteText.isNotBlank()) viewModel.addNote(
                                        noteText, NoteTag.TAG, attachedQuote = pendingTagQuote)
                                    pendingTagQuote = null
                                },
                                onDismiss = { showTagDialog = false; pendingTagQuote = null }
                            )
                        }

                        HighlightableText(
                            text                = qwo.question.explanation,
                            highlights          = state.highlights,
                            notes               = tagNotes,
                            onHighlight         = { text, color -> viewModel.addHighlight(text, color) },
                            onTagSelected       = { sel -> pendingTagQuote = sel; showTagDialog = true },
                            onDeleteTag         = { note -> viewModel.deleteNote(note) },
                            onEditTag           = { note, txt ->
                                viewModel.deleteNote(note)
                                viewModel.addNote(txt, NoteTag.TAG, note.attachedQuote)
                            },
                            onScrollEnabled     = { en -> selectionActive = !en },
                            scrollOffsetPx      = { scrollState.value },
                            onTranslateSelected = { text ->
                                selectedTranslateText  = text
                                showSelectionTranslate = true
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Bottom bar: Report | Share | Bookmark | (back to quiz not needed here)
                if (answered) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                            .background(Color.White).navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomAction(Icons.Filled.Warning, "Report", Color(0xFF9E9E9E), Modifier.weight(1f)) {}
                        VerticalDivider(Modifier.height(34.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))
                        BottomAction(Icons.Filled.Share,   "Share",  Color(0xFF9E9E9E), Modifier.weight(1f)) {}
                        VerticalDivider(Modifier.height(34.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))
                        val bmColor = bookmarkColor(qwo.question.bookmarkType)
                        val bmIcon  = bookmarkIcon(qwo.question.bookmarkType)
                        BottomAction(bmIcon,
                            if (qwo.question.bookmarkType != null) "Bookmarked" else "Bookmark",
                            bmColor, Modifier.weight(1f)) { showBookmarkPopup = true }
                    }
                }
            }

            // Bookmark screenshot effect
            screenshotType?.let { type ->
                BookmarkScreenshotEffect(bookmarkType = type, onComplete = { screenshotType = null })
            }
        }
    }
}

@Composable
private fun BottomAction(
    icon: ImageVector, label: String, tint: Color,
    modifier: Modifier, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, color = tint)
    }
}

@Composable
private fun ExplOptionRow(
    option: QuestionOptionEntity,
    selectedId: Long?,
    optionStats: Map<Long, Int>,
    totalVotes: Int,
    answered: Boolean,
    onClick: () -> Unit
) {
    val isSelected = selectedId == option.id
    val labels     = listOf("A", "B", "C", "D")
    val label      = labels.getOrElse(option.optionIndex) { "?" }
    val pct        = if (answered) optionStats.getOrDefault(option.id, 0) * 100 / totalVotes else null

    val textColor = when {
        !answered        -> Color(0xFF555555)
        option.isCorrect -> CorrectGreen
        isSelected       -> WrongRed
        else             -> DimGray
    }
    val isBold = answered && (option.isCorrect || isSelected)

    Row(modifier = Modifier.fillMaxWidth().clickable(enabled = !answered, onClick = onClick)
        .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        Box(modifier = Modifier.size(22.dp).padding(top = 1.dp), contentAlignment = Alignment.Center) {
            when {
                answered && option.isCorrect -> Box(
                    Modifier.size(20.dp).clip(CircleShape).background(CorrectGreen),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
                answered && isSelected && !option.isCorrect -> Box(
                    Modifier.size(20.dp).clip(CircleShape).background(WrongRed),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
            }
        }

        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    color = textColor)) {
                    append("$label.  ${option.optionText}")
                }
                if (pct != null) {
                    append("  ")
                    withStyle(SpanStyle(color = DimGray, fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                        append("[$pct%]")
                    }
                }
            },
            fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TagChip(label: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFF0F0F0))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 11.sp, color = DimGray)
    }
}
