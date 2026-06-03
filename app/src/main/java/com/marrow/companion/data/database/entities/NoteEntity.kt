package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NoteTag { NONE, IMP, DOUBT, SUMMARY, TAG }

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = QuestionEntity::class,
        parentColumns = ["id"],
        childColumns = ["questionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("questionId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val text: String,
    val tag: String = NoteTag.NONE.name,
    val attachedQuote: String? = null,   // the highlight text this note is attached to
    val createdAt: Long = System.currentTimeMillis()
)
