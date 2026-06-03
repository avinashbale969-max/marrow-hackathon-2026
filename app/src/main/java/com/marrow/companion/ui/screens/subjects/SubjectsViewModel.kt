package com.marrow.companion.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.HighlightDao
import com.marrow.companion.data.database.dao.NoteDao
import com.marrow.companion.data.database.dao.PausedQuizDao
import com.marrow.companion.data.database.dao.QuestionAttemptDao
import com.marrow.companion.data.database.dao.QuestionDao
import com.marrow.companion.data.database.dao.SubjectDao
import com.marrow.companion.data.database.dao.TopicDao
import com.marrow.companion.data.database.dao.UserDao
import com.marrow.companion.data.database.entities.SubjectEntity
import com.marrow.companion.data.database.entities.TopicEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SubjectWithProgress(
    val subject: SubjectEntity,
    val attempted: Int = 0
)

data class QBankUiState(
    val subjects: List<SubjectWithProgress> = emptyList(),
    val totalQuestions: Int = 0,
    val bookmarkedCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val questionDao: QuestionDao,
    private val userDao: UserDao,
    private val noteDao: NoteDao,
    private val highlightDao: HighlightDao,
    private val pausedQuizDao: PausedQuizDao,
    private val attemptDao: QuestionAttemptDao
) : ViewModel() {

    val qbankState: StateFlow<QBankUiState> = combine(
        subjectDao.getAllSubjects(),
        questionDao.getAttemptedPerSubject("local_user"),
        questionDao.getBookmarkedCount()
    ) { subjects, attemptedList, bookmarked ->
        val attemptMap = attemptedList.associate { it.subjectId to it.attempted }
        QBankUiState(
            subjects = subjects.map { s ->
                SubjectWithProgress(subject = s, attempted = attemptMap[s.id] ?: 0)
            },
            totalQuestions = subjects.sumOf { it.totalQuestions },
            bookmarkedCount = bookmarked,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QBankUiState())

    val subjects: StateFlow<List<SubjectEntity>> = subjectDao.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Eagerly — always live, update instantly when DB changes from any screen
    val notesCount: StateFlow<Int> = noteDao.getAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val highlightsCount: StateFlow<Int> = highlightDao.getAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val greenHighlightsCount: StateFlow<Int> = highlightDao.getGreenCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val orangeHighlightsCount: StateFlow<Int> = highlightDao.getOrangeCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Live lists — always subscribed to DB, reflect changes immediately
    private val _allNotesWithSubject = noteDao.getAllWithSubjectName()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _allHighlightsWithSubject = highlightDao.getAllWithSubjectName()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun getAllNotes()             = noteDao.getAll()
    fun getAllHighlights()        = highlightDao.getAll()
    fun getAllNotesWithSubject()  = _allNotesWithSubject
    fun getAllHighlightsWithSubject() = _allHighlightsWithSubject

    fun getNotesCountBySubject()      = noteDao.getCountBySubject()
    fun getHighlightsCountBySubject() = highlightDao.getCountBySubject()

    fun getNotesForSubject(subjectId: Long)       = noteDao.getForSubject(subjectId)
    fun getHighlightsForSubject(subjectId: Long)  = highlightDao.getForSubject(subjectId)
    fun getNotesWithSubject(subjectId: Long)      = noteDao.getForSubjectWithName(subjectId)
    fun getHighlightsWithSubject(subjectId: Long) = highlightDao.getForSubjectWithName(subjectId)

    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>> =
        topicDao.getTopicsForSubject(subjectId)

    fun getNotesForTopic(topicId: Long)      = noteDao.getForTopic(topicId)
    fun getHighlightsForTopic(topicId: Long) = highlightDao.getForTopic(topicId)
    fun getBookmarkedForTopic(topicId: Long) = questionDao.getBookmarkedForTopic(topicId)
    fun getBookmarkedForTopic_Subject(subjectId: Long) = questionDao.getBookmarkedForSubject(subjectId)
    fun getBookmarkCountForTopic(topicId: Long) = questionDao.getBookmarkedCountForTopic(topicId)

    fun hasPausedQuiz(topicId: Long) = pausedQuizDao.hasPausedForTopic(topicId)
    fun getPausedIndex(topicId: Long) = pausedQuizDao.getPausedIndexForTopic(topicId)
    fun getAttemptedCountForTopic(topicId: Long) =
        attemptDao.getAttemptedCountForTopic("local_user", topicId)
    suspend fun getLastAttemptTime(topicId: Long) =
        attemptDao.getLastAttemptTimeForTopic("local_user", topicId)
    fun getAllQuestionsOrdered(topicId: Long) =
        questionDao.getAllQuestionsForTopicOrdered(topicId)
    fun getAllPausedTopicIds() = pausedQuizDao.getAllPausedTopicIds()
    fun getAttemptedPerTopic(subjectId: Long) =
        attemptDao.getAttemptedPerTopic("local_user", subjectId)
}
