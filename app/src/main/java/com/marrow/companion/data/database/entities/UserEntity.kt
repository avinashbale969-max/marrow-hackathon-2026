package com.marrow.companion.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "local_user",
    val name: String = "",
    val email: String = "",
    val examDate: Long? = null,
    val streakCount: Int = 0,
    val lastStudyDate: Long? = null,
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)