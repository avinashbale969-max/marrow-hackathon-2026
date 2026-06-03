package com.marrow.companion.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.QuestionDao
import com.marrow.companion.data.database.dao.SubjectBookmarkCount
import com.marrow.companion.data.database.entities.BookmarkType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class BookmarksUiState(
    val totalBookmarks: Int = 0,
    val normalCount: Int = 0,
    val starredCount: Int = 0,
    val reviewCount: Int = 0,
    val bySubject: List<SubjectBookmarkCount> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val questionDao: QuestionDao
) : ViewModel() {

    val state: StateFlow<BookmarksUiState> = combine(
        questionDao.getBookmarkedCount(),
        questionDao.getBookmarkedCountByType(BookmarkType.NORMAL.name),
        questionDao.getBookmarkedCountByType(BookmarkType.STARRED.name),
        questionDao.getBookmarkedCountByType(BookmarkType.REVIEW.name),
        questionDao.getBookmarkedCountBySubject()
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        BookmarksUiState(
            totalBookmarks = values[0] as Int,
            normalCount    = values[1] as Int,
            starredCount   = values[2] as Int,
            reviewCount    = values[3] as Int,
            bySubject      = values[4] as List<SubjectBookmarkCount>,
            isLoading      = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookmarksUiState())
}
