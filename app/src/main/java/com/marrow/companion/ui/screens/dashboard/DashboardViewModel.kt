package com.marrow.companion.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.FlashcardDao
<<<<<<< HEAD
import com.marrow.companion.data.database.dao.HighlightDao
import com.marrow.companion.data.database.dao.HighlightForReview
=======
>>>>>>> origin/Sri_hackthon
import com.marrow.companion.data.database.dao.QuestionDao
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.dao.StudySessionDao
import com.marrow.companion.data.database.dao.UserDao
import com.marrow.companion.data.database.entities.UserEntity
import com.marrow.companion.data.database.DatabaseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val user: UserEntity? = null,
    val dueFlashcardCount: Int = 0,
    val totalQuestionsInDb: Int = 0,
    val mcqOfDay: QuestionWithOptions? = null,
    val daySelectedOptionId: Long? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userDao: UserDao,
    private val flashcardDao: FlashcardDao,
    private val sessionDao: StudySessionDao,
    private val questionDao: QuestionDao,
<<<<<<< HEAD
    private val seeder: DatabaseSeeder,
    private val highlightDao: HighlightDao
) : ViewModel() {

    val nextHighlightForReview: StateFlow<HighlightForReview?> =
        highlightDao.getNextForReview()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val dueHighlightCount: StateFlow<Int> =
        highlightDao.getDueCount()
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun rateHighlight(id: Long, daysUntilNext: Int) {
        viewModelScope.launch {
            val nextDate = System.currentTimeMillis() + daysUntilNext * 24L * 60 * 60 * 1000
            highlightDao.setReviewResult(id, nextDate)
        }
    }

    fun dismissHighlight(id: Long) {
        viewModelScope.launch { highlightDao.markReviewed(id) }
    }

=======
    private val seeder: DatabaseSeeder
) : ViewModel() {

>>>>>>> origin/Sri_hackthon
    private val _extra = MutableStateFlow(
        Triple<Int, QuestionWithOptions?, Long?>(0, null, null)
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        userDao.getUser(),
        flashcardDao.getDueCount("local_user"),
        _extra
    ) { user, dueCount, extra ->
        DashboardUiState(
            user = user,
            dueFlashcardCount = dueCount,
            totalQuestionsInDb = extra.first,
            mcqOfDay = extra.second,
            daySelectedOptionId = extra.third,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        viewModelScope.launch {
            if (userDao.getUser().first() == null) {
                userDao.upsert(UserEntity(name = "Student"))
            }
            seeder.seedIfEmpty()
            updateStreak()
            loadMcqOfDay()
        }
    }

    fun selectDayOption(optionId: Long) {
        val current = _extra.value
        if (current.third != null) return  // already answered
        _extra.value = Triple(current.first, current.second, optionId)
    }

    private suspend fun loadMcqOfDay() {
        val total = questionDao.getCount()
        if (total == 0) return
        val dayNumber = System.currentTimeMillis() / (1000L * 60 * 60 * 24)
        val offset = (dayNumber % total).toInt()
        val mcq = questionDao.getQuestionAtOffset(offset)
        _extra.value = Triple(total, mcq, null)
    }

    private suspend fun updateStreak() {
        val user = userDao.getUser().first() ?: return
        val now = System.currentTimeMillis()
        val todayStart = now - (now % 86_400_000L)
        val lastStudy = user.lastStudyDate ?: 0L
        val daysSince = (todayStart - (lastStudy - lastStudy % 86_400_000L)) / 86_400_000L
        val newStreak = when {
            lastStudy == 0L  -> 0
            daysSince == 0L  -> user.streakCount
            daysSince == 1L  -> user.streakCount + 1
            else             -> 0
        }
        if (newStreak != user.streakCount || lastStudy == 0L) {
            userDao.updateStreak(newStreak, now)
        }
    }
}
