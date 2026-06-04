package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class HighlightColor { GREEN, ORANGE }

@Entity(tableName = "highlights", indices = [Index("questionId")])
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val text: String,
    val color: String = HighlightColor.GREEN.name,
    val createdAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val nextReviewDate: Long? = null,
    val startOffset: Int = -1
)
