package com.marrow.companion.ui.screens.subjects

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

private val Teal     = Color(0xFF4DC8D4)
private val TealDot  = Color(0xFF26C6DA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicBookmarksScreen(
    subjectId: Long,
    topicId: Long,
    onBack: () -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val topics      by viewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())
    val subjectList by viewModel.subjects.collectAsState()
    val bookmarked  by viewModel.getBookmarkedForTopic(topicId).collectAsState(initial = emptyList())

    val topicName   = topics.find { it.id == topicId }?.name ?: "Bookmarks"
    val subjectName = subjectList.find { it.id == subjectId }?.name ?: ""

    // Pagination: groups of 10
    val pageSize    = 10
    val pages       = bookmarked.chunked(pageSize)
    var currentPage by remember { mutableIntStateOf(0) }
    val pageItems   = pages.getOrElse(currentPage) { emptyList() }
    val globalOffset = currentPage * pageSize

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topicName, fontWeight = FontWeight.Bold) },
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                ) {
                    pages.forEachIndexed { index, chunk ->
                        val start = index * pageSize + 1
                        val end   = start + chunk.size - 1
                        val sel   = index == currentPage
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentPage = index }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$start - $end",
                                fontSize   = 14.sp,
                                color      = if (sel) Teal else Color.Gray,
                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.fillMaxWidth(0.6f).height(2.dp)
                                    .background(if (sel) Teal else Color.Transparent)
                            )
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
                        Text("No bookmarks in this lesson", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(pageItems) { idx, qwo ->
                        BookmarkQuestionCard(
                            number      = globalOffset + idx + 1,
                            qwo         = qwo,
                            subjectName = subjectName,
                            topicName   = topics.find { it.id == qwo.question.topicId }?.name ?: ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkQuestionCard(
    number: Int,
    qwo: QuestionWithOptions,
    subjectName: String,
    topicName: String
) {
    val bmIcon  = when (qwo.question.bookmarkType) {
        BookmarkType.STARRED.name -> Icons.Filled.Star
        BookmarkType.REVIEW.name  -> Icons.Filled.Help
        else                      -> Icons.Filled.Bookmark
    }
    val bmColor = when (qwo.question.bookmarkType) {
        BookmarkType.STARRED.name -> Color(0xFFE53935)
        BookmarkType.REVIEW.name  -> Color(0xFFFFC107)
        else                      -> Color(0xFF4DC8D4)
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Question text
                Text(
                    "$number. ${qwo.question.questionText}",
                    fontSize   = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF1A1A1A)
                )
                // Subject • Topic
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(subjectName, fontSize = 12.sp, color = Color(0xFF888888))
                    // Green dot
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF66BB6A)))
                    }
                    Text(topicName, fontSize = 12.sp, color = Color(0xFF888888),
                        modifier = Modifier.weight(1f, fill = false))
                }
            }
            // Bookmark icon
            Icon(bmIcon, null, tint = bmColor, modifier = Modifier.size(22.dp).padding(top = 2.dp))
        }
    }
}
