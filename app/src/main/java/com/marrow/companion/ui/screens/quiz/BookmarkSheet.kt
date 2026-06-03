package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.marrow.companion.data.database.entities.BookmarkType
import kotlinx.coroutines.launch

// ── Bookmark type metadata ────────────────────────────────────────────────────

private data class BookmarkOption(
    val type: BookmarkType,
    val icon: ImageVector,
    val color: Color
)

private val bookmarkOptions = listOf(
    BookmarkOption(BookmarkType.NORMAL,  Icons.Filled.Bookmark, Color(0xFF2196F3)),
    BookmarkOption(BookmarkType.STARRED, Icons.Filled.Star,     Color(0xFFE53935)),
    BookmarkOption(BookmarkType.REVIEW,  Icons.Filled.Help,     Color(0xFFFFC107))
)

fun bookmarkColor(type: String?): Color = when (type) {
    BookmarkType.NORMAL.name  -> Color(0xFF2196F3)
    BookmarkType.STARRED.name -> Color(0xFFE53935)
    BookmarkType.REVIEW.name  -> Color(0xFFFFC107)
    else                      -> Color(0xFF9E9E9E)
}

fun bookmarkIcon(type: String?): ImageVector = when (type) {
    BookmarkType.NORMAL.name  -> Icons.Filled.Bookmark
    BookmarkType.STARRED.name -> Icons.Filled.Star
    BookmarkType.REVIEW.name  -> Icons.Filled.Help
    else                      -> Icons.Filled.BookmarkBorder
}

// ── Small popup above the action bar ─────────────────────────────────────────

@Composable
fun BookmarkTypePopup(
    currentType: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 112.dp)       // float above both bars
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3 type options
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bookmarkOptions.forEach { option ->
                    val selected = currentType == option.type.name
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) option.color.copy(alpha = 0.15f)
                                else Color(0xFFF5F5F5)
                            )
                            .then(
                                if (selected)
                                    Modifier.border(2.dp, option.color, RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable {
                                onSelect(if (selected) null else option.type.name)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.type.label,
                            tint = option.color,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            // Remove button — only when already bookmarked
            if (currentType != null) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(
                    "Remove bookmark",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .clickable { onSelect(null); onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ── Bottom action bar: Report | Share | Bookmark (with spring animation) ─────

@Composable
fun ExplanationActionBar(
    currentBookmarkType: String?,
    onBookmarkClick: () -> Unit
) {
    val isBookmarked = currentBookmarkType != null
    val color = bookmarkColor(currentBookmarkType)
    val icon  = bookmarkIcon(currentBookmarkType)

    // Spring bounce when bookmark type is set
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(currentBookmarkType) {
        if (currentBookmarkType != null) {
            scope.launch {
                scale.animateTo(
                    1.35f,
                    spring(dampingRatio = Spring.DampingRatioLowBouncy,
                           stiffness    = Spring.StiffnessMedium)
                )
                scale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                           stiffness    = Spring.StiffnessHigh)
                )
            }
        } else {
            scale.snapTo(1f)
        }
    }

    HorizontalDivider(color = Color(0xFFEEEEEE))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionBarButton(icon = Icons.Filled.Warning, label = "Report",
            tint = Color(0xFF9E9E9E), scale = 1f,         onClick = {})
        ActionBarButton(icon = Icons.Filled.Share,   label = "Share",
            tint = Color(0xFF9E9E9E), scale = 1f,         onClick = {})

        // Bookmark button with animation + dynamic label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onBookmarkClick)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Bookmark",
                tint = color,
                modifier = Modifier.size(20.dp).scale(scale.value)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text   = if (isBookmarked) "Bookmarked" else "Bookmark",
                fontSize = 11.sp,
                color    = color,
                fontWeight = if (isBookmarked) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ActionBarButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    scale: Float,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint,
            modifier = Modifier.size(20.dp).scale(scale))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Normal)
    }
}
