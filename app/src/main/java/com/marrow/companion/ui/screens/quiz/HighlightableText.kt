package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val GreenHL   = Color(0xFF66BB6A)
private val OrangeHL  = Color(0xFFFFA726)
private val GreenBg   = Color(0xFF66BB6A).copy(alpha = 0.28f)
private val OrangeBg  = Color(0xFFFFA726).copy(alpha = 0.28f)
private val ToolbarBg = Color(0xFF1A1A1A)

@Composable
fun HighlightableText(
    text: String,
    highlights: List<HighlightEntity>,
    notes: List<NoteEntity> = emptyList(),
    onHighlight: (String, HighlightColor, Int) -> Unit,
    onDeleteHighlight: ((HighlightEntity) -> Unit)? = null,
    onTagSelected: ((String) -> Unit)? = null,
    onDeleteTag: ((NoteEntity) -> Unit)? = null,
    onEditTag: ((NoteEntity, String) -> Unit)? = null,
    onScrollEnabled: ((Boolean) -> Unit)? = null,
    scrollOffsetPx: (() -> Int)? = null,
    onScrollTo: ((Int) -> Unit)? = null,   // restore scroll to a position
    modifier: Modifier = Modifier
) {
    val coroutineScope   = rememberCoroutineScope()
    val density          = LocalDensity.current
    val view             = LocalView.current
    val focusManager     = androidx.compose.ui.platform.LocalFocusManager.current

    // Intercept clipboard so selected text is captured locally — never touches the
    // system clipboard, which means Android 13+ never shows the "Copied" toast.
    val capturedText = remember { mutableStateOf<String?>(null) }
    val interceptClipboard = remember {
        object : androidx.compose.ui.platform.ClipboardManager {
            override fun setText(annotatedString: androidx.compose.ui.text.AnnotatedString) {
                capturedText.value = annotatedString.text   // store locally only
            }
            override fun getText(): androidx.compose.ui.text.AnnotatedString? =
                capturedText.value?.let { androidx.compose.ui.text.AnnotatedString(it) }
            override fun hasText(): Boolean = capturedText.value?.isNotEmpty() == true
        }
    }

    var showPicker            by remember { mutableStateOf(false) }
    var userDismissed         by remember { mutableStateOf(false) }
    var savedScrollOffset     by remember { mutableIntStateOf(0) }
    // Re-enable scroll any time picker hides (covers all showPicker=false paths)
    LaunchedEffect(showPicker) { if (!showPicker) onScrollEnabled?.invoke(true) }
    var pendingCopy           by remember { mutableStateOf<(() -> Unit)?>(null) }
    var lastHighlightText     by remember { mutableStateOf("") }
    var pendingSelectionOffset by remember { mutableIntStateOf(-1) }
    var popupOffset  by remember { mutableStateOf(IntOffset.Zero) }
    // Store LayoutCoordinates so positionInWindow() is called FRESH inside showMenu
    // (fresh call walks the transform chain including current scroll transform)
    val boxCoords = remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

    var textLayout    by remember { mutableStateOf<TextLayoutResult?>(null) }
    var editingNote   by remember { mutableStateOf<NoteEntity?>(null) }
    var previewNote   by remember { mutableStateOf<NoteEntity?>(null) }
    var previewOffset by remember { mutableStateOf(IntOffset.Zero) }
    var removeTarget  by remember { mutableStateOf<HighlightEntity?>(null) }
    var removeOffset  by remember { mutableStateOf(IntOffset.Zero) }

    val flashAlpha = remember { Animatable(0f) }

    val tagNotes = remember(notes, text) {
        notes.filter { n -> n.attachedQuote != null && text.contains(n.attachedQuote!!) }
    }

    // Custom TextToolbar
    val toolbar = remember {
        object : TextToolbar {
            override var status = TextToolbarStatus.Hidden
            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                status      = TextToolbarStatus.Shown
                pendingCopy = onCopyRequested

                val toolbarW = with(density) { 280.dp.toPx() }.toInt()
                val toolbarH = with(density) { 106.dp.toPx() }.toInt()
                val caret    = with(density) { 10.dp.toPx() }.toInt()
                // Get FRESH box position — positionInWindow() walks the transform chain,
                // which includes the current scroll transform (unlike the stale cached value)
                val freshPos = boxCoords.value?.positionInWindow()
                val boxX     = freshPos?.x?.toInt() ?: 0
                val boxY     = freshPos?.y?.toInt() ?: 0
                val cx       = ((rect.left + rect.right) / 2f).toInt()
                val x        = (cx - boxX - toolbarW / 2).coerceIn(0, view.width - toolbarW - 8)
                val y        = (rect.top.toInt() - boxY - toolbarH - caret - 8).coerceAtLeast(0)

                // Compute which character the selection midpoint falls on
                val relX = (rect.left + rect.right) / 2f - boxX
                val relY = (rect.top  + rect.bottom) / 2f - boxY
                pendingSelectionOffset = textLayout?.getOffsetForPosition(
                    androidx.compose.ui.geometry.Offset(relX, relY)
                ) ?: -1

                popupOffset = IntOffset(x, y)
                if (!userDismissed) {
                    onScrollEnabled?.invoke(false)
                    // Restore scroll to pre-selection position (SelectionContainer jumps via bringIntoView)
                    onScrollTo?.invoke(savedScrollOffset)
                    showPicker = true
                }
            }
            override fun hide() {
                status = TextToolbarStatus.Hidden
                showPicker = false
                userDismissed = false
                onScrollEnabled?.invoke(true)  // re-enable scroll when picker dismissed
            }
        }
    }

    // AnnotatedString: colour highlights only (tags show only as red flag, no bg)
    val annotated = remember(text, highlights) {
        buildAnnotatedString {
            append(text)
            highlights.sortedByDescending { it.text.length }.forEach { hl ->
                // Use the stored offset when available — highlights only that exact occurrence.
                // Fall back to first occurrence only for legacy entries (startOffset == -1).
                val idx = if (hl.startOffset >= 0 &&
                              hl.startOffset + hl.text.length <= text.length &&
                              text.substring(hl.startOffset, hl.startOffset + hl.text.length) == hl.text) {
                    hl.startOffset
                } else {
                    text.indexOf(hl.text)
                }
                if (idx >= 0) {
                    addStyle(
                        SpanStyle(
                            background = if (hl.color == HighlightColor.ORANGE.name) OrangeBg else GreenBg,
                            fontWeight = FontWeight.Medium
                        ), idx, idx + hl.text.length
                    )
                }
            }
        }
    }

    val flashColor = if (highlights.any { it.text == lastHighlightText && it.color == HighlightColor.ORANGE.name })
        OrangeHL else GreenHL

    // Edit dialog shown when user taps a sticky
    editingNote?.let { note ->
        StickyEditDialog(
            initialText = note.text,
            onSave   = { newText ->
                if (newText.isNotBlank()) onEditTag?.invoke(note, newText)
                else onDeleteTag?.invoke(note)
                editingNote = null
            },
            onDelete = { onDeleteTag?.invoke(note); editingNote = null },
            onDismiss = { editingNote = null }
        )
    }

    // Helper: find the highlight (if any) whose span covers charOffset
    fun highlightAt(charOffset: Int): HighlightEntity? =
        highlights.firstOrNull { hl ->
            val idx = if (hl.startOffset >= 0 &&
                          hl.startOffset + hl.text.length <= text.length &&
                          text.substring(hl.startOffset, hl.startOffset + hl.text.length) == hl.text)
                hl.startOffset else text.indexOf(hl.text)
            idx >= 0 && charOffset >= idx && charOffset < idx + hl.text.length
        }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords -> boxCoords.value = coords }
            // Save scroll position on every touch-down so we can restore after SelectionContainer jump
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    savedScrollOffset = scrollOffsetPx?.invoke() ?: 0
                }
            }
            .pointerInput(highlights, text) {
                detectTapGestures { tapOffset ->
                    if (onDeleteHighlight == null) return@detectTapGestures
                    val layout = textLayout ?: return@detectTapGestures
                    val charOffset = layout.getOffsetForPosition(tapOffset)
                    val hit = highlightAt(charOffset)
                    if (hit != null) {
                        val bbox = layout.getBoundingBox(charOffset)
                        removeOffset = IntOffset(
                            (tapOffset.x.toInt() - with(density) { 80.dp.roundToPx() }).coerceAtLeast(0),
                            (bbox.top.toInt() - with(density) { 52.dp.roundToPx() }).coerceAtLeast(0)
                        )
                        removeTarget = hit
                    }
                }
            }
    ) {
        // ── Explanation text — pushed right when stickies exist ───────────
        CompositionLocalProvider(
            LocalTextToolbar      provides toolbar,
            LocalClipboardManager provides interceptClipboard
        ) {
            SelectionContainer {
                Text(
                    text       = annotated,
                    fontSize   = 15.sp,
                    lineHeight = 24.sp,
                    color      = Color(0xFF2D2D2D),
                    onTextLayout = { textLayout = it },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .then(
                            if (flashAlpha.value > 0f)
                                Modifier.border(2.dp, flashColor.copy(alpha = flashAlpha.value),
                                    RoundedCornerShape(4.dp))
                            else Modifier
                        )
                )
            }
        }

        // ── Red flag at start of each tagged span ────────────────────────
        if (textLayout != null) {
            tagNotes.forEach { note ->
                val quote = note.attachedQuote ?: return@forEach
                val idx   = text.indexOf(quote)
                if (idx >= 0 && idx < text.length) {
                    val bbox = textLayout!!.getBoundingBox(idx)
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(bbox.left.toInt(), bbox.top.toInt()) }
                            .size(14.dp)
                            .clickable {
                                if (previewNote?.id == note.id) {
                                    previewNote = null  // toggle off
                                } else {
                                    previewOffset = IntOffset(
                                        bbox.left.toInt(),
                                        (bbox.top - with(density) { 90.dp.toPx() }).toInt()
                                            .coerceAtLeast(0)
                                    )
                                    previewNote = note
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, size.height * 0.4f)
                                lineTo(0f, size.height * 0.8f)
                                close()
                            }
                            drawPath(path, Color(0xFFE53935))
                        }
                    }
                }
            }
        }

        // ── Sticky preview popup shown when flag is tapped ────────────────
        previewNote?.let { note ->
            Popup(
                alignment        = Alignment.TopStart,
                offset           = previewOffset,
                onDismissRequest = { previewNote = null },
                properties       = PopupProperties(focusable = false)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(min = 90.dp, max = 160.dp)
                        .shadow(4.dp, RoundedCornerShape(4.dp, 12.dp, 12.dp, 4.dp))
                        .clip(RoundedCornerShape(4.dp, 12.dp, 12.dp, 4.dp))
                        .background(Color(0xFFFFF176))
                        .clickable { editingNote = note; previewNote = null }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            note.text,
                            fontSize   = 12.sp,
                            lineHeight = 17.sp,
                            color      = Color(0xFF5D4037),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Tap to edit",
                            fontSize = 10.sp,
                            color    = Color(0xFF8D6E00).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // ── Remove-highlight popup (appears when user taps a highlight) ──────
        removeTarget?.let { hl ->
            Popup(
                alignment        = Alignment.TopStart,
                offset           = removeOffset,
                onDismissRequest = { removeTarget = null },
                properties       = PopupProperties(focusable = false)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1A1A1A))
                        .clickable {
                            onDeleteHighlight?.invoke(hl)
                            removeTarget = null
                        }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text("✕  Remove highlight",
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── Selection toolbar popup ───────────────────────────────────────
        if (showPicker) {
            Popup(
                alignment        = Alignment.TopStart,
                offset           = popupOffset,
                onDismissRequest = { userDismissed = true; showPicker = false; focusManager.clearFocus(force = true) },
                properties       = PopupProperties(focusable = false)
            ) {
                Box(modifier = Modifier.width(280.dp)) {
                    // Close button floating at top-right corner, OUTSIDE the card
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF555555))
                            .clickable { userDismissed = true; showPicker = false; focusManager.clearFocus(force = true) }
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, null, tint = Color.White,
                            modifier = Modifier.size(13.dp))
                    }

                    Column(
                        modifier            = Modifier.padding(top = 13.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Card(
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(containerColor = ToolbarBg),
                        elevation = CardDefaults.cardElevation(10.dp),
                        modifier  = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ColorSwatch(GreenHL) {
                                    doHighlight(HighlightColor.GREEN, pendingCopy, capturedText, coroutineScope, text, pendingSelectionOffset) { t, offset ->
                                        onHighlight(t, HighlightColor.GREEN, offset); lastHighlightText = t; showPicker = false
                                        coroutineScope.launch { flashAlpha.snapTo(1f); flashAlpha.animateTo(0f, tween(700)) }
                                    }
                                }
                                ColorSwatch(OrangeHL) {
                                    doHighlight(HighlightColor.ORANGE, pendingCopy, capturedText, coroutineScope, text, pendingSelectionOffset) { t, offset ->
                                        onHighlight(t, HighlightColor.ORANGE, offset); lastHighlightText = t; showPicker = false
                                        coroutineScope.launch { flashAlpha.snapTo(1f); flashAlpha.animateTo(0f, tween(700)) }
                                    }
                                }
                                // Delete icon — shown only when selection overlaps an existing highlight
                                val selectionHit = highlightAt(pendingSelectionOffset)
                                if (selectionHit != null && onDeleteHighlight != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clickable {
                                                coroutineScope.launch {
                                                    pendingCopy?.invoke()
                                                    delay(80)
                                                    val selected = capturedText.value?.trim() ?: ""
                                                    val hlStart = if (selectionHit.startOffset >= 0 &&
                                                                      selectionHit.startOffset + selectionHit.text.length <= text.length &&
                                                                      text.substring(selectionHit.startOffset, selectionHit.startOffset + selectionHit.text.length) == selectionHit.text)
                                                        selectionHit.startOffset else text.indexOf(selectionHit.text)
                                                    val hlEnd   = if (hlStart >= 0) hlStart + selectionHit.text.length else -1
                                                    val hlColor = HighlightColor.entries.firstOrNull { it.name == selectionHit.color } ?: HighlightColor.GREEN
                                                    if (selected.isBlank() || hlStart < 0 || hlEnd < 0) {
                                                        onDeleteHighlight.invoke(selectionHit); showPicker = false; return@launch
                                                    }
                                                    val selStart = findOccurrenceStart(text, selected, pendingSelectionOffset)
                                                    val selEnd   = if (selStart >= 0) selStart + selected.length else -1
                                                    if (selStart < 0 || selEnd < 0) {
                                                        onDeleteHighlight.invoke(selectionHit); showPicker = false; return@launch
                                                    }
                                                    val removeStart = maxOf(hlStart, selStart)
                                                    val removeEnd   = minOf(hlEnd, selEnd)
                                                    onDeleteHighlight.invoke(selectionHit)
                                                    if (removeStart < removeEnd) {
                                                        if (removeStart > hlStart) {
                                                            val rawBefore = text.substring(hlStart, removeStart)
                                                            val trimBefore = rawBefore.trim()
                                                            if (trimBefore.isNotBlank()) {
                                                                val lead = rawBefore.length - rawBefore.trimStart().length
                                                                onHighlight(trimBefore, hlColor, hlStart + lead)
                                                            }
                                                        }
                                                        if (removeEnd < hlEnd) {
                                                            val rawAfter = text.substring(removeEnd, hlEnd)
                                                            val trimAfter = rawAfter.trim()
                                                            if (trimAfter.isNotBlank()) {
                                                                val lead = rawAfter.length - rawAfter.trimStart().length
                                                                onHighlight(trimAfter, hlColor, removeEnd + lead)
                                                            }
                                                        }
                                                    }
                                                    showPicker = false
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove highlight",
                                            tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                Text("Highlight", color = Color.White, fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)

                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                ActionText("✎ Highlight") {
                                    doHighlight(HighlightColor.GREEN, pendingCopy, capturedText, coroutineScope, text, pendingSelectionOffset) { t, offset ->
                                        onHighlight(t, HighlightColor.GREEN, offset); lastHighlightText = t; showPicker = false
                                        coroutineScope.launch { flashAlpha.snapTo(1f); flashAlpha.animateTo(0f, tween(700)) }
                                    }
                                }
                                ActionText("🏷 Tag") {
                                    coroutineScope.launch {
                                        pendingCopy?.invoke()
                                        delay(80)
                                        val selected = capturedText.value?.trim() ?: ""
                                        if (selected.isNotBlank()) onTagSelected?.invoke(selected)
                                        showPicker = false
                                    }
                                }
                            }
                        }
                    }
                    Canvas(modifier = Modifier.size(width = 20.dp, height = 10.dp)) {
                        drawPath(Path().apply {
                            moveTo(0f, 0f); lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height); close()
                        }, ToolbarBg)
                    }
                    } // end Column
                } // end Box
            }
        }
    }
}

// ── Sticky edit dialog — Delete | Cancel | OK (matches screenshot) ────────────

@Composable
fun StickyEditDialog(
    initialText: String,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
        ) {
            // Header bar (dark grey, like screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF555555))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Note", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Icon(Icons.Filled.Close, null, tint = Color.White,
                    modifier = Modifier.size(18.dp).clickable(onClick = onDismiss))
            }

            // Text area (light grey background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp)
                    .background(Color(0xFFF0F0F0))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value         = text,
                    onValueChange = { text = it },
                    textStyle     = androidx.compose.ui.text.TextStyle(
                        fontSize   = 15.sp,
                        color      = Color(0xFF333333),
                        lineHeight = 22.sp
                    ),
                    cursorBrush   = androidx.compose.ui.graphics.SolidColor(Color(0xFF4DC8D4)),
                    modifier      = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Column {
                            if (text.isEmpty()) {
                                Text("Write your note…", fontSize = 15.sp, color = Color(0xFFBBBBBB))
                            }
                            inner()
                            Spacer(Modifier.height(4.dp))
                            // Blue underline cursor indicator
                            HorizontalDivider(color = Color(0xFF4DC8D4), thickness = 1.5.dp,
                                modifier = Modifier.fillMaxWidth(0.9f))
                        }
                    }
                )
            }

            // Buttons row: Delete | Cancel | OK (dark grey, matching screenshot)
            Row(modifier = Modifier.fillMaxWidth()) {
                // Delete
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF555555))
                        .clickable(onClick = onDelete)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Box(Modifier.width(1.dp).height(50.dp).background(Color(0xFF777777)))
                // Cancel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF555555))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Box(Modifier.width(1.dp).height(50.dp).background(Color(0xFF777777)))
                // OK
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF555555))
                        .clickable { onSave(text.trim()) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun doHighlight(
    color: HighlightColor,
    pendingCopy: (() -> Unit)?,
    capturedText: androidx.compose.runtime.MutableState<String?>,
    scope: kotlinx.coroutines.CoroutineScope,
    fullText: String,
    selectionOffset: Int,
    onDone: (String, Int) -> Unit
) {
    scope.launch {
        pendingCopy?.invoke()   // writes to interceptClipboard only — no system clipboard touch
        delay(80)
        val selected = capturedText.value?.trim() ?: ""
        if (selected.isNotBlank()) {
            val startOffset = findOccurrenceStart(fullText, selected, selectionOffset)
            onDone(selected, startOffset)
        }
    }
}

/**
 * Returns the start index of the occurrence of [selected] in [fullText] that
 * contains (or is closest to) [nearOffset].  Returns -1 if [selected] is not
 * found at all.
 */
private fun findOccurrenceStart(fullText: String, selected: String, nearOffset: Int): Int {
    if (nearOffset < 0) return fullText.indexOf(selected)

    // First pass: find the occurrence whose range contains nearOffset
    var pos = 0
    while (true) {
        val idx = fullText.indexOf(selected, pos)
        if (idx < 0) break
        if (idx <= nearOffset && nearOffset <= idx + selected.length) return idx
        pos = idx + 1
    }

    // Second pass: find the occurrence whose midpoint is closest to nearOffset
    var bestIdx  = -1
    var bestDist = Int.MAX_VALUE
    pos = 0
    while (true) {
        val idx = fullText.indexOf(selected, pos)
        if (idx < 0) break
        val mid  = idx + selected.length / 2
        val dist = kotlin.math.abs(mid - nearOffset)
        if (dist < bestDist) { bestDist = dist; bestIdx = idx }
        pos = idx + 1
    }
    return bestIdx
}

@Composable
private fun ColorSwatch(color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(color).clickable(onClick = onClick))
}

@Composable
private fun ActionText(label: String, onClick: () -> Unit) {
    Text(label, color = Color(0xFFDDDDDD), fontSize = 12.sp,
        modifier = Modifier.clickable(onClick = onClick))
}
