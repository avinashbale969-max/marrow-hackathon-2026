package com.marrow.companion.ui.screens.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.entities.BookmarkType
import com.marrow.companion.ui.screens.subjects.SubjectsViewModel

private val Teal = Color(0xFF4DC8D4)
private val Green = Color(0xFF66BB6A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectBookmarksScreen(
    subjectId: Long,
    onBack: () -> Unit,
    subjectsViewModel: SubjectsViewModel = hiltViewModel()
) {
    val subjects   by subjectsViewModel.subjects.collectAsState()
    val topics     by subjectsViewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())
    val bookmarked by subjectsViewModel.getBookmarkedForTopic_Subject(subjectId).collectAsState(initial = emptyList())

    val subjectName = subjects.find { it.id == subjectId }?.name ?: "Bookmarks"
    val topicMap    = topics.associateBy { it.id }

    val pageSize     = 10
    val pages        = bookmarked.chunked(pageSize)
    var currentPage  by remember { mutableIntStateOf(0) }
    val pageItems    = pages.getOrElse(currentPage) { emptyList() }
    val globalOffset = currentPage * pageSize

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subjectName, fontWeight = FontWeight.Bold) },
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

            // Pagination tabs
            if (pages.size > 1) {
                Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    pages.forEachIndexed { index, chunk ->
                        val start = index * pageSize + 1
                        val end   = start + chunk.size - 1
                        val sel   = index == currentPage
                        Column(
                            modifier = Modifier.weight(1f).clickable { currentPage = index }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("$start - $end", fontSize = 14.sp,
                                color = if (sel) Teal else Color(0xFF888888),
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                            Box(Modifier.fillMaxWidth(0.5f).height(2.dp)
                                .background(if (sel) Teal else Color.Transparent))
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            if (bookmarked.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.BookmarkBorder, null,
                            tint = Color(0xFFCCCCCC), modifier = Modifier.size(48.dp))
                        Text("No bookmarks for $subjectName", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(pageItems) { idx, qwo ->
                        val topicName = topicMap[qwo.question.topicId]?.name ?: ""
                        SubjectBookmarkCard(
                            number      = globalOffset + idx + 1,
                            qwo         = qwo,
                            subjectName = subjectName,
                            topicName   = topicName
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectBookmarkCard(
    number: Int,
    qwo: QuestionWithOptions,
    subjectName: String,
    topicName: String
) {
    val bmIcon  = when (qwo.question.bookmarkType) {
        BookmarkType.STARRED.name -> Icons.Filled.Star
        BookmarkType.REVIEW.name  -> Icons.Filled.Help
        BookmarkType.NORMAL.name  -> Icons.Filled.Bookmark
        else                      -> null
    }
    val bmColor = when (qwo.question.bookmarkType) {
        BookmarkType.STARRED.name -> Color(0xFFE53935)
        BookmarkType.REVIEW.name  -> Color(0xFFFFC107)
        else                      -> Teal
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$number. ${qwo.question.questionText}",
                    fontSize = 14.sp, lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))

                // Subject • Topic
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(subjectName, fontSize = 12.sp, color = Color(0xFF888888))
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Green))
                    Text(topicName, fontSize = 12.sp, color = Color(0xFF888888),
                        modifier = Modifier.weight(1f, fill = false))
                }
            }
            if (bmIcon != null) {
                Icon(bmIcon, null, tint = bmColor,
                    modifier = Modifier.size(22.dp).padding(top = 2.dp))
            }
        }
    }
}
