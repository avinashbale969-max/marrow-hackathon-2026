package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.FlashcardEntity
import com.marrow.companion.data.database.entities.FlashcardReviewEntity
import kotlinx.coroutines.flow.Flow

data class FlashcardWithReview(
    @Embedded val flashcard: FlashcardEntity,
    @Relation(parentColumn = "id", entityColumn = "flashcardId")
    val review: FlashcardReviewEntity?
)

@Dao
interface FlashcardDao {
    @Transaction
    @Query("""
        SELECT f.* FROM flashcards f
        LEFT JOIN flashcard_reviews fr ON f.id = fr.flashcardId AND fr.userId = :userId
        WHERE fr.id IS NULL OR fr.nextReviewDate <= :now
        ORDER BY fr.nextReviewDate ASC
    """)
    fun getDueFlashcards(userId: String, now: Long = System.currentTimeMillis()): Flow<List<FlashcardWithReview>>

    @Transaction
    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun getFlashcardsForTopic(topicId: Long): Flow<List<FlashcardWithReview>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(review: FlashcardReviewEntity)

    @Query("SELECT COUNT(*) FROM flashcard_reviews WHERE userId = :userId AND nextReviewDate <= :now")
    fun getDueCount(userId: String, now: Long = System.currentTimeMillis()): Flow<Int>

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)
}