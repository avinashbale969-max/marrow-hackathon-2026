package com.marrow.companion.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val StickyYellow = Color(0xFFFFEE58)
private val StickyDark   = Color(0xFF795548)

// ── Small inline tag shown beside/below highlighted text ──────────────────────

@Composable
fun StickyNoteTag(
    text: String,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(StickyYellow)
            .border(1.dp, Color(0xFFF9A825).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Filled.StickyNote2, null,
            tint = StickyDark, modifier = Modifier.size(13.dp))
        Text(
            text = if (text.length > 40) text.take(40) + "…" else text,
            fontSize   = 12.sp,
            color      = StickyDark,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(1f, fill = false)
        )
        Icon(Icons.Filled.Close, null,
            tint = StickyDark.copy(alpha = 0.6f),
            modifier = Modifier.size(12.dp).clickable(onClick = onDelete))
    }
}

// ── "Add Note" trigger link ────────────────────────────────────────────────────

@Composable
fun AddStickyNoteTrigger(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(Icons.Filled.StickyNote2, null,
            tint = Color(0xFFF9A825), modifier = Modifier.size(14.dp))
        Text("Add Summary Note",
            fontSize = 12.sp, color = Color(0xFFF9A825), fontWeight = FontWeight.Medium)
    }
}

// ── Note input dialog (matches screenshot style) ───────────────────────────────

@Composable
fun NoteInputDialog(
    initialText: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Note",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1A1A1A))
                    Icon(Icons.Filled.StickyNote2, null,
                        tint = Color(0xFFF9A825), modifier = Modifier.size(20.dp))
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Text area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 200.dp)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    BasicTextField(
                        value         = text,
                        onValueChange = { text = it },
                        textStyle     = TextStyle(
                            fontSize   = 15.sp,
                            color      = Color(0xFF333333),
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF4DC8D4)),
                        modifier    = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (text.isEmpty()) {
                                    Text("Write your note here…",
                                        fontSize = 15.sp, color = Color(0xFFBBBBBB))
                                }
                                inner()
                            }
                        }
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Cancel | OK buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).padding(8.dp)
                    ) {
                        Text("Cancel",
                            fontSize = 15.sp, color = Color(0xFF888888),
                            fontWeight = FontWeight.Medium)
                    }
                    Box(Modifier.width(1.dp).height(48.dp).background(Color(0xFFEEEEEE))
                        .align(Alignment.CenterVertically))
                    TextButton(
                        onClick  = { onConfirm(text.trim()); onDismiss() },
                        modifier = Modifier.weight(1f).padding(8.dp)
                    ) {
                        Text("OK",
                            fontSize = 15.sp, color = Color(0xFF4DC8D4),
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
