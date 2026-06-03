package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias OnTranslateSelected = (String) -> Unit

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
    onHighlight: (String, HighlightColor) -> Unit,
    onTagSelected: ((String) -> Unit)? = null,
    onDeleteTag: ((NoteEntity) -> Unit)? = null,
    onEditTag: ((NoteEntity, String) -> Unit)? = null,
    onScrollEnabled: ((Boolean) -> Unit)? = null,
    scrollOffsetPx: (() -> Int)? = null,   // current vertical scroll offset in px
    onTranslateSelected: OnTranslateSelected? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope   = rememberCoroutineScope()
    val density          = LocalDensity.current
    val view             = LocalView.current

    var showPicker        by remember { mutableStateOf(false) }
    var pendingCopy       by remember { mutableStateOf<(() -> Unit)?>(null) }
    var lastHighlightText by remember { mutableStateOf("") }
    var popupOffset       by remember { mutableStateOf(IntOffset.Zero) }
    val boxWindowPos      = remember { mutableStateOf(IntOffset.Zero) }

    var textLayout   by remember { mutableStateOf<TextLayoutResult?>(null) }
    var editingNote  by remember { mutableStateOf<NoteEntity?>(null) }
    var previewNote  by remember { mutableStateOf<NoteEntity?>(null) }
    var previewOffset by remember { mutableStateOf(IntOffset.Zero) }

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
                onScrollEnabled?.invoke(false)

                val toolbarW = with(density) { 280.dp.toPx() }.toInt()
                val toolbarH = with(density) { 106.dp.toPx() }.toInt()
                val caret    = with(density) { 10.dp.toPx() }.toInt()
                // Compensate for scroll: boxWindowPos is stale after scrolling
                val scrollY  = scrollOffsetPx?.invoke() ?: 0
                val boxX     = boxWindowPos.value.x
                val boxY     = boxWindowPos.value.y - scrollY  // adjusted for current scroll
                val cx       = ((rect.left + rect.right) / 2f).toInt()
                val x        = (cx - boxX - toolbarW / 2).coerceIn(0, view.width - toolbarW - 8)
                val y        = (rect.top.toInt() - boxY - toolbarH - caret - 8).coerceAtLeast(0)

                popupOffset = IntOffset(x, y)
                showPicker  = true
            }
            override fun hide() {
                status = TextToolbarStatus.Hidden; showPicker = false
                onScrollEnabled?.invoke(true)
            }
        }
    }

    // AnnotatedString: colour highlights only (tags show only as red flag, no bg)
    val annotated = remember(text, highlights) {
        buildAnnotatedString {
            append(text)
            highlights.forEach { hl ->
                var start = 0
                while (true) {
                    val idx = text.indexOf(hl.text, start)
                    if (idx < 0) break
                    addStyle(
                        SpanStyle(
                            background = if (hl.color == HighlightColor.ORANGE.name) OrangeBg else GreenBg,
                            fontWeight = FontWeight.Medium
                        ), idx, idx + hl.text.length
                    )
                    start = idx + hl.text.length
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

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            val pos = coords.positionInWindow()
            boxWindowPos.value = IntOffset(pos.x.toInt(), pos.y.toInt())
        }
    ) {
        // ── Explanation text — pushed right when stickies exist ───────────
        CompositionLocalProvider(LocalTextToolbar provides toolbar) {
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

        // ── Selection toolbar popup ───────────────────────────────────────
        if (showPicker) {
            Popup(
                alignment        = Alignment.TopStart,
                offset           = popupOffset,
                onDismissRequest = { showPicker = false },
                properties       = PopupProperties(focusable = false)
            ) {
                Column(
                    modifier            = Modifier.width(280.dp),
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
                                    doHighlight(HighlightColor.GREEN, pendingCopy, clipboardManager, coroutineScope) { t ->
                                        onHighlight(t, HighlightColor.GREEN); lastHighlightText = t; showPicker = false
                                        coroutineScope.launch { flashAlpha.snapTo(1f); flashAlpha.animateTo(0f, tween(700)) }
                                    }
                                }
                                ColorSwatch(OrangeHL) {
                                    doHighlight(HighlightColor.ORANGE, pendingCopy, clipboardManager, coroutineScope) { t ->
                                        onHighlight(t, HighlightColor.ORANGE); lastHighlightText = t; showPicker = false
                                        coroutineScope.launch { flashAlpha.snapTo(1f); flashAlpha.animateTo(0f, tween(700)) }
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
                                    doHighlight(HighlightColor.GREEN, pendingCopy, clipboardManager, coroutineScope) { t ->
                                        onHighlight(t, HighlightColor.GREEN); lastHighlightText = t; showPicker = false
                                        coroutineScope.launch { flashAlpha.snapTo(1f); flashAlpha.animateTo(0f, tween(700)) }
                                    }
                                }
                                ActionText("🏷 Tag") {
                                    coroutineScope.launch {
                                        pendingCopy?.invoke()
                                        delay(80)
                                        val selected = clipboardManager.getText()?.text?.trim() ?: ""
                                        if (selected.isNotBlank()) onTagSelected?.invoke(selected)
                                        showPicker = false
                                    }
                                }
                                ActionText("🌐 Translate") {
                                    coroutineScope.launch {
                                        pendingCopy?.invoke()
                                        delay(80)
                                        val selected = clipboardManager.getText()?.text?.trim() ?: ""
                                        if (selected.isNotBlank()) onTranslateSelected?.invoke(selected)
                                        showPicker = false
                                    }
                                }
                                Icon(Icons.Filled.Close, null, tint = Color(0xFF888888),
                                    modifier = Modifier.size(14.dp).clickable { showPicker = false })
                            }
                        }
                    }
                    Canvas(modifier = Modifier.size(width = 20.dp, height = 10.dp)) {
                        drawPath(Path().apply {
                            moveTo(0f, 0f); lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height); close()
                        }, ToolbarBg)
                    }
                }
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
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    scope: kotlinx.coroutines.CoroutineScope,
    onDone: (String) -> Unit
) {
    scope.launch {
        pendingCopy?.invoke()
        delay(80)
        val selected = clipboardManager.getText()?.text?.trim() ?: ""
        if (selected.isNotBlank()) onDone(selected)
    }
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
