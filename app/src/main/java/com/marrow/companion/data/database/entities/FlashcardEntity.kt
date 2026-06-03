package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(
        entity = TopicEntity::class,
        parentColumns = ["id"],
        childColumns = ["topicId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("topicId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long? = null,
    val front: String,
    val back: String,
    val imageUrl: String? = null,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)