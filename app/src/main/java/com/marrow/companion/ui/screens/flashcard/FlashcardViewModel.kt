package com.marrow.companion.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.FlashcardDao
import com.marrow.companion.data.database.dao.FlashcardWithReview
import com.marrow.companion.data.database.entities.FlashcardReviewEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

enum class ReviewRating(val quality: Int) { AGAIN(0), HARD(2), GOOD(4), EASY(5) }

data class FlashcardUiState(
    val dueCards: List<FlashcardWithReview> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true
) {
    val current get() = dueCards.getOrNull(currentIndex)
}

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao
) : ViewModel() {

    private val _state = MutableStateFlow(FlashcardUiState())
    val state: StateFlow<FlashcardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            flashcardDao.getDueFlashcards("local_user").collect { cards ->
                _state.update { it.copy(dueCards = cards, isLoading = false,
                    isFinished = cards.isEmpty()) }
            }
        }
    }

    fun flip() = _state.update { it.copy(isFlipped = !it.isFlipped) }

    fun rate(rating: ReviewRating) {
        val current = _state.value.current ?: return
        viewModelScope.launch {
            val updated = sm2Update(current.review, current.flashcard.id, rating.quality)
            flashcardDao.upsertReview(updated)
        }
        val nextIndex = _state.value.currentIndex + 1
        if (nextIndex >= _state.value.dueCards.size) {
            _state.update { it.copy(isFinished = true) }
        } else {
            _state.update { it.copy(currentIndex = nextIndex, isFlipped = false) }
        }
    }

    private fun sm2Update(existing: FlashcardReviewEntity?, flashcardId: Long, q: Int): FlashcardReviewEntity {
        val ef = max(1.3f, (existing?.easeFactor ?: 2.5f) + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))
        val reps = if (q < 3) 0 else (existing?.repetitions ?: 0) + 1
        val interval = when {
            q < 3 -> 1
            reps == 1 -> 1
            reps == 2 -> 6
            else -> ((existing?.interval ?: 1) * ef).roundToInt()
        }
        val nextReview = System.currentTimeMillis() + interval * 24 * 60 * 60 * 1000L
        return FlashcardReviewEntity(
            id = existing?.id ?: 0,
            userId = "local_user",
            flashcardId = flashcardId,
            easeFactor = ef,
            interval = interval,
            repetitions = reps,
            nextReviewDate = nextReview,
            lastReviewedAt = System.currentTimeMillis()
        )
    }
}