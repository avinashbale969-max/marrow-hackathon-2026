package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_attempts",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"]),
        ForeignKey(QuestionEntity::class, ["id"], ["questionId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("questionId")]
)
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val questionId: Long,
    val selectedOptionId: Long?,
    val isCorrect: Boolean,
    val timeTakenMs: Long,
    val attemptedAt: Long = System.currentTimeMillis()
)