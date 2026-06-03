package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paused_quizzes")
data class PausedQuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long?,
    val subjectId: Long?,
    val questionIds: String,      // "1,2,3,4,5" — order preserved
    val currentIndex: Int,
    val correctCount: Int,
    val answeredMap: String,      // "0:true,1:false" — index:wasCorrect
    val updatedAt: Long = System.currentTimeMillis()
)
