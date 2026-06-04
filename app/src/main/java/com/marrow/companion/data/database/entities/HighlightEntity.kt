package com.marrow.companion.data.database.entities

import androidx.room.Entity
<<<<<<< HEAD
=======
import androidx.room.ForeignKey
>>>>>>> origin/Sri_hackthon
import androidx.room.Index
import androidx.room.PrimaryKey

enum class HighlightColor { GREEN, ORANGE }

<<<<<<< HEAD
@Entity(tableName = "highlights", indices = [Index("questionId")])
=======
@Entity(
    tableName = "highlights",
    foreignKeys = [ForeignKey(
        entity = QuestionEntity::class,
        parentColumns = ["id"],
        childColumns = ["questionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("questionId")]
)
>>>>>>> origin/Sri_hackthon
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val text: String,
    val color: String = HighlightColor.GREEN.name,
<<<<<<< HEAD
    val createdAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val nextReviewDate: Long? = null,
    val startOffset: Int = -1
=======
    val startOffset: Int = -1,   // character offset in the full question text; -1 = legacy
    val createdAt: Long = System.currentTimeMillis()
>>>>>>> origin/Sri_hackthon
)
