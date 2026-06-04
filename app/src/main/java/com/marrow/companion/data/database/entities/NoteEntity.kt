package com.marrow.companion.data.database.entities

import androidx.room.Entity
<<<<<<< HEAD
=======
import androidx.room.ForeignKey
>>>>>>> origin/Sri_hackthon
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NoteTag { NONE, IMP, DOUBT, SUMMARY, TAG }

@Entity(
    tableName = "notes",
<<<<<<< HEAD
    // Foreign key removed — questionId also holds video/image note IDs (negative values)
=======
    foreignKeys = [ForeignKey(
        entity = QuestionEntity::class,
        parentColumns = ["id"],
        childColumns = ["questionId"],
        onDelete = ForeignKey.CASCADE
    )],
>>>>>>> origin/Sri_hackthon
    indices = [Index("questionId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val text: String,
    val tag: String = NoteTag.NONE.name,
<<<<<<< HEAD
    val attachedQuote: String? = null,
=======
    val attachedQuote: String? = null,   // the highlight text this note is attached to
>>>>>>> origin/Sri_hackthon
    val createdAt: Long = System.currentTimeMillis()
)
