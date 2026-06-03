package com.marrow.companion.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.*
import com.marrow.companion.data.database.dao.DailyStat
import com.marrow.companion.data.database.entities.SubjectEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SubjectStat(val subject: SubjectEntity, val accuracy: Int, val total: Int)

data class AnalyticsUiState(
    val subjectStats: List<SubjectStat> = emptyList(),
    val dailyStats: List<DailyStat> = emptyList(),
    val totalStudyTimeHours: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val questionDao: QuestionDao,
    private val attemptDao: QuestionAttemptDao,
    private val sessionDao: StudySessionDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { loadAnalytics() }
    }

    private suspend fun loadAnalytics() {
        val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

        val accuracyBySubject = questionDao.getAccuracyBySubject("local_user")
        val allSubjects = subjectDao.getAllSubjects().first()
        val subjectMap = allSubjects.associateBy { it.id }

        val subjectStats = accuracyBySubject.mapNotNull { acc ->
            val subject = subjectMap[acc.subjectId] ?: return@mapNotNull null
            SubjectStat(
                subject = subject,
                accuracy = if (acc.total > 0) (acc.correct * 100) / acc.total else 0,
                total = acc.total
            )
        }.sortedBy { it.accuracy }

        val dailyStats = attemptDao.getDailyStats("local_user", sevenDaysAgo)
        val totalMs = sessionDao.getTotalStudyTimeMs("local_user", sevenDaysAgo) ?: 0L

        _state.update {
            it.copy(
                subjectStats = subjectStats,
                dailyStats = dailyStats,
                totalStudyTimeHours = totalMs / 3_600_000f,
                isLoading = false
            )
        }
    }
}