package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SessionType { QUIZ, FLASHCARD, CUSTOM }

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(UserEntity::class, ["id"], ["userId"]),
        ForeignKey(SubjectEntity::class, ["id"], ["subjectId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("userId"), Index("subjectId")]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val subjectId: Long? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val sessionType: SessionType = SessionType.QUIZ
)