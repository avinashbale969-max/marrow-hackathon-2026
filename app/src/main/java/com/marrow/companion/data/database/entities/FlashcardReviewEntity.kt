package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcard_reviews",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"]),
        ForeignKey(FlashcardEntity::class, ["id"], ["flashcardId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("flashcardId")]
)
data class FlashcardReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val flashcardId: Long,
    val easeFactor: Float = 2.5f,   // SM-2 E-Factor, min 1.3
    val interval: Int = 1,           // days until next review
    val repetitions: Int = 0,        // successful reviews in a row
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null
)