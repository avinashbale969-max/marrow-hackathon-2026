package com.marrow.companion.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.entities.QuestionOptionEntity

private val TealHeader    = Color(0xFF4DC8D4)
private val CorrectGreen  = Color(0xFF66BB6A)
private val WrongRed      = Color(0xFFEF5350)
private val DimGray       = Color(0xFF9E9E9E)
private val OptionBorder  = Color(0xFFE2E2E2)
private val OptionLabelUn = Color(0xFFAAAAAA)   // unanswered label (A, B…)
private val OptionTextUn  = Color(0xFF333333)   // unanswered option text
private val BgGray        = Color(0xFFF4F4F4)

// ── Main entry ───────────────────────────────────────────────────────────────

@Composable
fun QuizScreen(
    subjectId: Long?,
    topicId: Long?,
    random: Boolean,
    startFresh: Boolean = false,
    onFinish: () -> Unit,
    onMcqClick: ((Long) -> Unit)? = null,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Controls whether we show explanation or question view
    var showExplanation   by remember { mutableStateOf(false) }
    var showBookmarkPopup by remember { mutableStateOf(false) }

    // Reset explanation view when question changes
    LaunchedEffect(state.currentIndex) { showExplanation = false }

    // After answering, wait 1.5s then auto-navigate to explanation
    LaunchedEffect(state.selectedOptionId) {
        if (state.selectedOptionId != null) {
            delay(1_500L)
            showExplanation = true
        }
    }

    LaunchedEffect(Unit) { viewModel.loadQuestions(subjectId, topicId, random, startFresh) }

    // No questions available for this topic
    if (!state.isLoading && state.questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Filled.Close, null, tint = Color(0xFFCCCCCC),
                    modifier = Modifier.size(52.dp))
                Text("No questions available",
                    fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                Text("Questions for this topic haven't been added yet.",
                    fontSize = 13.sp, color = Color(0xFFAAAAAA),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4DC8D4))) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    if (state.isFinished && state.reviewReady) {
        QuizReviewScreen(
            correct       = state.correctCount,
            total         = state.questions.size,
            bookmarked    = state.reviewBookmarked,
            notesMap      = state.reviewNotes,
            highlightsMap = state.reviewHighlights,
            onDone        = onFinish,
            onMcqClick    = onMcqClick
        )
        return
    }

    // Close = pause (save progress) then exit
    val handleClose: () -> Unit = {
        viewModel.pauseQuiz()
        onFinish()
    }

    // Show loading while review data is being prepared
    if (state.isFinished) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4DC8D4))
        }
        return
    }

    var screenshotType by remember { mutableStateOf<String?>(null) }

    if (showBookmarkPopup) {
        BookmarkTypePopup(
            currentType = state.currentQuestion?.question?.bookmarkType,
            onSelect    = { type ->
                viewModel.setBookmarkType(type)
                if (type != null) screenshotType = type
            },
            onDismiss   = { showBookmarkPopup = false }
        )
    }

    val answered = state.selectedOptionId != null

    // Feature tip — show once per login session
    var showFeatureTip by remember {
        mutableStateOf(!FeatureTipState.explanationTipShown)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showExplanation && answered) {
            // ── Explanation view ──────────────────────────────────────────────
            ExplanationScreen(
                state           = state,
                answeredMap     = state.answeredMap,
                viewModel       = viewModel,
                subjectId       = subjectId,
                onNext          = { showExplanation = false; viewModel.nextQuestion() },
                onClose         = handleClose,
                onBookmarkClick = { showBookmarkPopup = true },
                onMcqClick      = onMcqClick
            )
        } else {
            // ── Question view ─────────────────────────────────────────────────
            QuestionScreen(
                state       = state,
                answeredMap = state.answeredMap,
                onClose     = handleClose,
                onSelect    = { optionId -> viewModel.selectOption(optionId) }
            )
        }

        // Screenshot effect overlay
        screenshotType?.let { type ->
            BookmarkScreenshotEffect(
                bookmarkType = type,
                onComplete   = { screenshotType = null }
            )
        }

        // Feature tip overlay — shown once per login when explanation first appears
        if (showFeatureTip && showExplanation && answered) {
            ExplanationFeatureTip(
                onDismiss = {
                    FeatureTipState.explanationTipShown = true
                    showFeatureTip = false
                }
            )
        }
    }
}

// ── Question screen ───────────────────────────────────────────────────────────

@Composable
private fun QuestionScreen(
    state: QuizUiState,
    answeredMap: Map<Int, Boolean>,
    onClose: () -> Unit,
    onSelect: (Long) -> Unit
) {
    val q        = state.currentQuestion ?: return
    val answered = state.selectedOptionId != null
    val labels   = listOf("A", "B", "C", "D")

    Column(modifier = Modifier.fillMaxSize().background(BgGray)) {

        // ── Header: MARROW + × ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("MARROW", color = TealHeader, fontWeight = FontWeight.Bold,
                fontSize = 16.sp, letterSpacing = 1.sp)
            Icon(Icons.Filled.Close, null, tint = Color(0xFF777777),
                modifier = Modifier.size(20.dp).clickable(onClick = onClose))
        }

        // ── Progress dots ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val total = state.questions.size.coerceAtMost(20)
            repeat(total) { i ->
                val dotColor = when {
                    answeredMap[i] == true  -> CorrectGreen   // answered correct
                    answeredMap[i] == false -> WrongRed       // answered wrong
                    i == state.currentIndex -> TealHeader     // current
                    else                   -> Color(0xFFCCCCCC) // future
                }
                val size = if (i == state.currentIndex) 8.dp else 6.dp
                Box(modifier = Modifier.size(size).clip(CircleShape).background(dotColor))
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Question text ─────────────────────────────────────────────────────
        Text(
            text = q.question.questionText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
            lineHeight = 30.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Timer (only shown when running low)
        val secs = (state.timeRemainingMs / 1000).toInt()
        if (!answered && secs <= 15) {
            Text("${secs}s", fontSize = 12.sp,
                color = if (secs <= 10) WrongRed else DimGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        }

        Spacer(Modifier.weight(1f))

        // ── Options ───────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            q.options.sortedBy { it.optionIndex }.forEach { option ->
                val isSelected = state.selectedOptionId == option.id
                OptionCard(
                    label      = labels.getOrElse(option.optionIndex) { "?" },
                    text       = option.optionText,
                    isSelected = isSelected,
                    isCorrect  = option.isCorrect,
                    answered   = answered,
                    onClick    = { onSelect(option.id) }
                )
            }

            // Small loading indicator while waiting to auto-navigate
            if (answered) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(0.4f).height(2.dp),
                        color = TealHeader,
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

// ── Option card ───────────────────────────────────────────────────────────────

@Composable
private fun OptionCard(
    label: String,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    answered: Boolean,
    onClick: () -> Unit
) {
    val bgColor     : Color
    val borderColor : Color
    val labelColor  : Color
    val textColor   : Color

    when {
        !answered -> {
            bgColor     = Color.White
            borderColor = OptionBorder
            labelColor  = OptionLabelUn
            textColor   = OptionTextUn
        }
        isCorrect -> {                          // correct answer → full green
            bgColor     = CorrectGreen
            borderColor = CorrectGreen
            labelColor  = Color.White
            textColor   = Color.White
        }
        isSelected -> {                        // wrong selected → full red
            bgColor     = WrongRed
            borderColor = WrongRed
            labelColor  = Color.White
            textColor   = Color.White
        }
        else -> {                              // other options → dim
            bgColor     = Color.White
            borderColor = OptionBorder
            labelColor  = Color(0xFFCCCCCC)
            textColor   = Color(0xFFAAAAAA)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !answered, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("$label)", fontSize = 15.sp, color = labelColor,
            fontWeight = FontWeight.Medium, modifier = Modifier.width(28.dp))
        Text(text, fontSize = 15.sp, color = textColor,
            fontWeight = if (answered && (isCorrect || isSelected)) FontWeight.SemiBold
            else FontWeight.Normal,
            modifier = Modifier.weight(1f), lineHeight = 21.sp)
    }
}

// ── Explanation screen (same MARROW header style, integrated NEXT in bottom bar) ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplanationScreen(
    state: QuizUiState,
    answeredMap: Map<Int, Boolean>,
    viewModel: QuizViewModel,
    subjectId: Long? = null,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMcqClick: ((Long) -> Unit)? = null
) {
    val q          = state.currentQuestion ?: return
    val totalVotes = state.optionStats.values.sum().coerceAtLeast(1)
    val correctOpt = q.options.firstOrNull { it.isCorrect }
    val correctPct = (state.optionStats.getOrDefault(correctOpt?.id ?: -1, 0) * 100 / totalVotes)
    val labels     = listOf("A", "B", "C", "D")
    val isLast     = state.currentIndex + 1 >= state.questions.size
    val scrollState     = rememberScrollState()
    var selectionActive by remember { mutableStateOf(false) }

    var showNotesSheet         by remember { mutableStateOf(false) }
    var pendingTagQuote        by remember { mutableStateOf<String?>(null) }
    var showTagDialog          by remember { mutableStateOf(false) }

    // My Notes bottom sheet
    if (showNotesSheet) {
        MyNotesSheet(
            highlights        = state.highlights,
            notes             = state.notes,
            bookmarkType      = q.question.bookmarkType,
            onDeleteHighlight = { viewModel.deleteHighlight(it) },
            onDeleteNote      = { viewModel.deleteNote(it) },
            onAddNote         = { text, tag, quote -> viewModel.addNote(text, tag, quote) },
            onEditNote        = { note, text, tag -> viewModel.updateNote(note, text, tag) },
            onMcqClick        = onMcqClick,
            onDismiss         = { showNotesSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explanation", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, null)
                    }
                },
                actions = {
                    TextButton(onClick = { showNotesSheet = true }) {
                        Text("My Notes", color = Color.White,
                            fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealHeader,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BgGray
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Explanation view ──────────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f)
                    .verticalScroll(scrollState, enabled = !selectionActive)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Question text
                Text(q.question.questionText, fontSize = 18.sp, lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D))

            // Question image (if available)
            q.question.imageUrl?.let { url ->
                Spacer(Modifier.height(12.dp))
                SubcomposeAsyncImage(
                    model   = url,
                    contentDescription = "Question image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 280.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TealHeader,
                                modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.BrokenImage, null,
                                    tint = Color(0xFFCCCCCC), modifier = Modifier.size(36.dp))
                                Text("Image unavailable", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Options with results
            q.options.sortedBy { it.optionIndex }.forEach { option ->
                val isSelected = state.selectedOptionId == option.id
                val pct        = state.optionStats.getOrDefault(option.id, 0) * 100 / totalVotes
                val textColor  = when {
                    option.isCorrect -> CorrectGreen
                    isSelected       -> WrongRed
                    else             -> DimGray
                }
                val lbl = labels.getOrElse(option.optionIndex) { "?" }

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // ✅ / ❌ / blank
                    Box(Modifier.size(24.dp).padding(top = 2.dp), Alignment.Center) {
                        when {
                            option.isCorrect -> Box(
                                Modifier.size(22.dp).clip(CircleShape).background(CorrectGreen),
                                Alignment.Center) {
                                Icon(Icons.Filled.Check, null, tint = Color.White,
                                    modifier = Modifier.size(14.dp))
                            }
                            isSelected -> Box(
                                Modifier.size(22.dp).clip(CircleShape).background(WrongRed),
                                Alignment.Center) {
                                Icon(Icons.Filled.Close, null, tint = Color.White,
                                    modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(
                            fontWeight = if (option.isCorrect || isSelected) FontWeight.Bold
                            else FontWeight.Normal, color = textColor, fontSize = 16.sp)) {
                            append("$lbl.  ${option.optionText}")
                        }
                        append("  ")
                        withStyle(SpanStyle(color = DimGray, fontSize = 13.sp,
                            fontWeight = FontWeight.Normal)) { append("[$pct%]") }
                    }, lineHeight = 23.sp, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(Modifier.height(14.dp))

            // "X% of the people got this right"
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(28.dp)) {
                    CircularProgressIndicator(progress = { correctPct / 100f },
                        modifier = Modifier.fillMaxSize(), strokeWidth = 4.dp,
                        color = CorrectGreen, trackColor = Color(0xFFE0E0E0))
                }
                Text("$correctPct% of the people got this right",
                    fontSize = 14.sp, color = Color(0xFF555555))
            }

            Spacer(Modifier.height(14.dp))

            // Question hash + tags
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Text(q.question.id.toString().padStart(16, '0').take(16),
                    fontSize = 10.sp, color = DimGray)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TagChip("#Clinical"); TagChip("#NEET")
            }

            Spacer(Modifier.height(16.dp))

            // Tag input dialog (from "🏷 Tag" in toolbar)
            if (showTagDialog && pendingTagQuote != null) {
                NoteInputDialog(
                    initialText = "",
                    onConfirm   = { noteText ->
                        if (noteText.isNotBlank()) {
                            viewModel.addNote(noteText,
                                com.marrow.companion.data.database.entities.NoteTag.TAG,
                                attachedQuote = pendingTagQuote)
                        }
                        pendingTagQuote = null
                    },
                    onDismiss = { showTagDialog = false; pendingTagQuote = null }
                )
            }

            val tagNotes by viewModel.getTagsForQuestion(q.question.id)
                .collectAsState(initial = emptyList())

            // Selectable explanation text with highlights + inline tags
            HighlightableText(
                text                = q.question.explanation,
                highlights          = state.highlights,
                notes               = tagNotes,
                onHighlight         = { text, color, offset -> viewModel.addHighlight(text, color, offset) },
                onDeleteHighlight   = { hl -> viewModel.deleteHighlight(hl) },
                onTagSelected       = { selectedText ->
                    pendingTagQuote = selectedText
                    showTagDialog   = true
                },
                onDeleteTag         = { note -> viewModel.deleteNote(note) },
                onEditTag           = { note, newText ->
                    viewModel.deleteNote(note)
                    viewModel.addNote(newText,
                        com.marrow.companion.data.database.entities.NoteTag.TAG,
                        attachedQuote = note.attachedQuote)
                },
                onScrollEnabled     = { enabled -> selectionActive = !enabled },
                scrollOffsetPx      = { scrollState.value }
            )

            Spacer(Modifier.height(16.dp))
            }   // end explanation Column

            // ── Bottom bar: Report | Share | Bookmark | [NEXT] ───────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()   // padding below the Row for nav bar
        ) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Row(
                modifier = Modifier.fillMaxWidth().height(58.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Report — weight 1f each (3 icons share 60% of bar)
            BottomBarIcon(Icons.Filled.Warning, "Report", Color(0xFF9E9E9E), weight = 1f) {}
            VerticalDivider(modifier = Modifier.height(36.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))
            BottomBarIcon(Icons.Filled.Share, "Share", Color(0xFF9E9E9E), weight = 1f) {}
            VerticalDivider(modifier = Modifier.height(36.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))
            val bmColor = bookmarkColor(q.question.bookmarkType)
            val bmIcon  = bookmarkIcon(q.question.bookmarkType)
            BottomBarIcon(
                icon    = bmIcon,
                label   = if (q.question.bookmarkType != null) "Bookmarked" else "Bookmark",
                tint    = bmColor,
                weight  = 1f,
                onClick = onBookmarkClick
            )
            // NEXT — weight 1.5f so it's wider than each icon column
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .background(TealHeader)
                    .clickable(onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (isLast) "FINISH" else "NEXT",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    letterSpacing = 1.5.sp
                )
            }
            }           // end Row
        }           // end nav Column
        }           // end Scaffold content Column
    }               // end Scaffold
}

@Composable
private fun RowScope.BottomBarIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    weight: Float = 1f,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Normal)
    }
}

@Composable
private fun TagChip(label: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFF0F0F0))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 11.sp, color = DimGray)
    }
}

@Composable
private fun ResultScreen(correct: Int, total: Int, onDone: () -> Unit) {
    val accuracy = if (total > 0) (correct * 100) / total else 0
    Column(modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Quiz Complete!", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        Text("$correct / $total", style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold, color = TealHeader)
        Text("Accuracy: $accuracy%", style = MaterialTheme.typography.titleMedium,
            color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TealHeader)) {
            Text("Done", fontWeight = FontWeight.SemiBold)
        }
    }
}
