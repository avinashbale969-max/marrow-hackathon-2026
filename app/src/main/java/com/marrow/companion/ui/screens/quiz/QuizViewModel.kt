package com.marrow.companion.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.HighlightDao
import com.marrow.companion.data.database.dao.NoteDao
import com.marrow.companion.data.database.dao.PausedQuizDao
import com.marrow.companion.data.database.dao.QuestionAttemptDao
import com.marrow.companion.data.database.dao.QuestionDao
import com.marrow.companion.data.database.dao.QuestionWithOptions
import com.marrow.companion.data.database.dao.StudySessionDao
import com.marrow.companion.data.database.dao.UserDao
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.NoteTag
import com.marrow.companion.data.database.entities.PausedQuizEntity
import com.marrow.companion.data.database.entities.QuestionAttemptEntity
import com.marrow.companion.data.database.entities.SessionType
import com.marrow.companion.data.database.entities.StudySessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val questions: List<QuestionWithOptions> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionId: Long? = null,
    val showExplanation: Boolean = false,
    val isFinished: Boolean = false,
    val correctCount: Int = 0,
    val timeRemainingMs: Long = 60_000L,
    val isLoading: Boolean = true,
    val sessionId: Long? = null,
    val optionStats: Map<Long, Int> = emptyMap(),
    val highlights: List<HighlightEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val answeredMap: Map<Int, Boolean> = emptyMap(),   // index → wasCorrect
    val reviewReady: Boolean = false,
    val reviewBookmarked: List<QuestionWithOptions> = emptyList(),
    val reviewNotes: Map<Long, List<NoteEntity>> = emptyMap(),
    val reviewHighlights: Map<Long, List<HighlightEntity>> = emptyMap()
) {
    val currentQuestion get() = questions.getOrNull(currentIndex)
    val progress get() = if (questions.isEmpty()) 0f else (currentIndex + 1) / questions.size.toFloat()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val questionDao: QuestionDao,
    private val attemptDao: QuestionAttemptDao,
    private val sessionDao: StudySessionDao,
    private val userDao: UserDao,
    private val highlightDao: HighlightDao,
    private val noteDao: NoteDao,
    private val pausedQuizDao: PausedQuizDao
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTime = System.currentTimeMillis()
    private var currentTopicId: Long? = null
    private var currentSubjectId: Long? = null

    fun loadQuestions(subjectId: Long?, topicId: Long?, random: Boolean, startFresh: Boolean = false) {
        currentTopicId   = topicId
        currentSubjectId = subjectId
        viewModelScope.launch {
            // If user chose "Start Fresh", wipe the saved state first
            if (startFresh) clearPausedState()

            // Check for a paused session
            val paused = if (startFresh) null else when {
                topicId   != null -> pausedQuizDao.getForTopic(topicId)
                subjectId != null -> pausedQuizDao.getForSubject(subjectId)
                else              -> null
            }

            val questions: List<QuestionWithOptions>
            val startIndex:  Int
            val correctCount: Int
            val answeredMap: Map<Int, Boolean>

            if (paused != null) {
                // Restore saved order
                val ids = paused.questionIds.split(",").mapNotNull { it.toLongOrNull() }
                val byId = questionDao.getQuestionsByIds(ids).associateBy { it.question.id }
                questions    = ids.mapNotNull { byId[it] }
                startIndex   = paused.currentIndex
                correctCount = paused.correctCount
                answeredMap  = parseAnsweredMap(paused.answeredMap)
            } else {
                questions = when {
                    random || (subjectId == null && topicId == null) -> questionDao.getRandomQuestions(10)
                    topicId   != null -> questionDao.getQuestionsForTopic(topicId)
                    subjectId != null -> questionDao.getQuestionsForSubject(subjectId)
                    else              -> questionDao.getRandomQuestions(10)
                }
                startIndex   = 0
                correctCount = 0
                answeredMap  = emptyMap()
            }

            val sessionId = sessionDao.insert(
                StudySessionEntity(
                    userId      = "local_user",
                    subjectId   = subjectId,
                    startTime   = System.currentTimeMillis(),
                    sessionType = SessionType.QUIZ
                )
            )
            _state.update {
                it.copy(
                    questions    = questions,
                    currentIndex = startIndex,
                    correctCount = correctCount,
                    answeredMap  = answeredMap,
                    isLoading    = false,
                    sessionId    = sessionId
                )
            }
            // Start observing notes/highlights for the starting question
            questions.getOrNull(startIndex)?.question?.id?.let { qId ->
                loadHighlights(qId)
                loadNotes(qId)
            }
            startTimer()
        }
    }

    fun selectOption(optionId: Long) {
        val state = _state.value
        if (state.selectedOptionId != null || state.currentQuestion == null) return

        timerJob?.cancel()
        val timeTaken = System.currentTimeMillis() - questionStartTime
        val correct   = state.currentQuestion!!.options.find { it.id == optionId }?.isCorrect == true
        val newAnsweredMap = state.answeredMap + (state.currentIndex to correct)

        _state.update {
            it.copy(
                selectedOptionId = optionId,
                showExplanation  = true,
                correctCount     = if (correct) it.correctCount + 1 else it.correctCount,
                answeredMap      = newAnsweredMap
            )
        }

        viewModelScope.launch {
            attemptDao.insert(QuestionAttemptEntity(
                userId           = "local_user",
                questionId       = state.currentQuestion!!.question.id,
                selectedOptionId = optionId,
                isCorrect        = correct,
                timeTakenMs      = timeTaken
            ))
            userDao.incrementStats(if (correct) 1 else 0)
            val stats    = attemptDao.getOptionStats(state.currentQuestion!!.question.id)
            val statsMap = stats.associate { it.selectedOptionId to it.count }
            _state.update { it.copy(optionStats = statsMap) }

            // Auto-save progress after every answer
            savePausedState()
        }
    }

    /** Call this when the user presses Close/Back mid-quiz. */
    fun pauseQuiz() {
        val state = _state.value
        // Don't save if nothing has been answered or quiz is finished
        if (state.isFinished || state.questions.isEmpty()) return
        viewModelScope.launch { savePausedState() }
    }

    private suspend fun savePausedState() {
        val state = _state.value
        if (state.questions.isEmpty()) return

        // If current question is already answered, resume at the NEXT question
        val resumeIndex = if (state.selectedOptionId != null) state.currentIndex + 1
                          else state.currentIndex

        // Nothing left to resume — quiz is effectively done
        if (resumeIndex >= state.questions.size) return

        val qIds    = state.questions.joinToString(",") { it.question.id.toString() }
        val aMapStr = state.answeredMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        pausedQuizDao.saveOrReplace(
            PausedQuizEntity(
                topicId      = currentTopicId,
                subjectId    = currentSubjectId,
                questionIds  = qIds,
                currentIndex = resumeIndex,
                correctCount = state.correctCount,
                answeredMap  = aMapStr
            )
        )
    }

    private suspend fun clearPausedState() {
        currentTopicId?.let   { pausedQuizDao.deleteForTopic(it) }
        currentSubjectId?.let { pausedQuizDao.deleteForSubject(it) }
    }

    fun addHighlight(text: String, color: HighlightColor) {
        val q = _state.value.currentQuestion ?: return
        viewModelScope.launch {
            highlightDao.insert(HighlightEntity(questionId = q.question.id, text = text, color = color.name))
            loadHighlights(q.question.id)
        }
    }

    fun deleteHighlight(hl: HighlightEntity) {
        viewModelScope.launch {
            highlightDao.deleteById(hl.id)
            _state.value.currentQuestion?.question?.id?.let { loadHighlights(it) }
        }
    }

    fun getSummary(questionId: Long) = noteDao.getSummaryForQuestion(questionId)
    fun getTagsForQuestion(questionId: Long) = noteDao.getTagsForQuestion(questionId)

    fun saveSummary(text: String) {
        val q = _state.value.currentQuestion ?: return
        viewModelScope.launch {
            // Delete existing summary for this question first
            val existing = noteDao.getSummaryForQuestion(q.question.id).first()
            existing?.let { noteDao.deleteById(it.id) }
            if (text.isNotBlank()) {
                noteDao.insert(NoteEntity(
                    questionId = q.question.id,
                    text       = text,
                    tag        = NoteTag.SUMMARY.name
                ))
            }
            loadNotes(q.question.id)
        }
    }

    fun addNote(text: String, tag: NoteTag, attachedQuote: String?) {
        val q = _state.value.currentQuestion ?: return
        viewModelScope.launch {
            noteDao.insert(NoteEntity(questionId = q.question.id, text = text,
                tag = tag.name, attachedQuote = attachedQuote))
            loadNotes(q.question.id)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteById(note.id)
            _state.value.currentQuestion?.question?.id?.let { loadNotes(it) }
        }
    }

    fun updateNote(note: NoteEntity, newText: String, newTag: NoteTag) {
        viewModelScope.launch {
            noteDao.update(note.copy(text = newText, tag = newTag.name))
            _state.value.currentQuestion?.question?.id?.let { loadNotes(it) }
        }
    }

    private var highlightsJob: kotlinx.coroutines.Job? = null
    private var notesJob: kotlinx.coroutines.Job? = null

    private fun loadHighlights(questionId: Long) {
        highlightsJob?.cancel()
        highlightsJob = viewModelScope.launch {
            highlightDao.getForQuestion(questionId).collect { list ->
                _state.update { it.copy(highlights = list) }
            }
        }
    }

    private fun loadNotes(questionId: Long) {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            noteDao.getForQuestion(questionId).collect { list ->
                _state.update { it.copy(notes = list) }
            }
        }
    }

    fun setBookmarkType(type: String?) {
        val q = _state.value.currentQuestion ?: return
        viewModelScope.launch {
            questionDao.setBookmarkType(q.question.id, type)
            _state.update { state ->
                state.copy(questions = state.questions.map {
                    if (it.question.id == q.question.id)
                        it.copy(question = it.question.copy(bookmarkType = type))
                    else it
                })
            }
        }
    }

    fun nextQuestion() {
        val state     = _state.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.questions.size) {
            finishQuiz()
            return
        }
        questionStartTime = System.currentTimeMillis()
        _state.update {
            it.copy(currentIndex = nextIndex, selectedOptionId = null,
                showExplanation = false, timeRemainingMs = 60_000L)
        }
        // Observe notes/highlights for the new question
        _state.value.questions.getOrNull(nextIndex)?.question?.id?.let { qId ->
            loadHighlights(qId)
            loadNotes(qId)
        }
        startTimer()
    }

    fun subjectNotes(subjectId: Long)      = noteDao.getForSubject(subjectId)
    fun subjectHighlights(subjectId: Long) = highlightDao.getForSubject(subjectId)
    fun subjectBookmarked(subjectId: Long) = questionDao.getBookmarkedForSubject(subjectId)

    private fun finishQuiz() {
        timerJob?.cancel()
        val state = _state.value
        viewModelScope.launch {
            clearPausedState()   // quiz done — remove paused entry
            state.sessionId?.let { id ->
                val session = sessionDao.getById(id) ?: return@launch
                sessionDao.update(session.copy(
                    endTime            = System.currentTimeMillis(),
                    questionsAttempted = state.questions.size,
                    correctAnswers     = state.correctCount
                ))
            }
            val bookmarked = state.questions.filter { it.question.bookmarkType != null }
            val allIds     = state.questions.map { it.question.id }
            val notesMap   = mutableMapOf<Long, List<NoteEntity>>()
            val hlMap      = mutableMapOf<Long, List<HighlightEntity>>()
            allIds.forEach { qId ->
                notesMap[qId] = noteDao.getForQuestion(qId).first()
                hlMap[qId]    = highlightDao.getForQuestion(qId).first()
            }
            _state.update {
                it.copy(
                    isFinished       = true,
                    reviewReady      = true,
                    reviewBookmarked = bookmarked,
                    reviewNotes      = notesMap,
                    reviewHighlights = hlMap
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = 60_000L
            while (remaining > 0) {
                delay(500L)
                remaining -= 500L
                _state.update { it.copy(timeRemainingMs = remaining) }
            }
            val correctId = _state.value.currentQuestion
                ?.options?.firstOrNull { it.isCorrect }?.id ?: -1L
            selectOption(correctId)
        }
    }

    override fun onCleared() { timerJob?.cancel() }

    companion object {
        fun parseAnsweredMap(raw: String): Map<Int, Boolean> {
            if (raw.isBlank()) return emptyMap()
            return raw.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) parts[0].toIntOrNull()?.let { it to (parts[1] == "true") }
                else null
            }.toMap()
        }
    }
}
