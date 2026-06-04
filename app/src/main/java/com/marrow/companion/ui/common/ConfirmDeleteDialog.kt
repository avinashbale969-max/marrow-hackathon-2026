package com.marrow.companion.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ConfirmDeleteDialog(
    title: String   = "Delete Highlight?",
    message: String = "This highlight will be permanently removed from your notes.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Delete icon in red circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete, null,
                        tint     = Color(0xFFE53935),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A),
                    textAlign  = TextAlign.Center
                )

                Text(
                    message,
                    fontSize  = 14.sp,
                    color     = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(4.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF555555)
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    // Delete
                    Button(
                        onClick  = { onConfirm(); onDismiss() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
                        )
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
