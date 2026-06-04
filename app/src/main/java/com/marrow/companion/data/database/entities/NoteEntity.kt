package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NoteTag { NONE, IMP, DOUBT, SUMMARY, TAG }

@Entity(
    tableName = "notes",
    // Foreign key removed — questionId also holds video/image note IDs (negative values)
    indices = [Index("questionId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val text: String,
    val tag: String = NoteTag.NONE.name,
    val attachedQuote: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
