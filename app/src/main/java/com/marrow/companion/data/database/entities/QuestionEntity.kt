package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Difficulty { EASY, MEDIUM, HARD }

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(SubjectEntity::class, ["id"], ["subjectId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TopicEntity::class, ["id"], ["topicId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("subjectId"), Index("topicId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val topicId: Long,
    val questionText: String,
    val explanation: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val imageUrl: String? = null,
    val bookmarkType: String? = null,  // null=none, BookmarkType.name values
    val createdAt: Long = System.currentTimeMillis()
) {
    val isBookmarked: Boolean get() = bookmarkType != null
}