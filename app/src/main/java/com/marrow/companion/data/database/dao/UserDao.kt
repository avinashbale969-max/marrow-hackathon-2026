package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 'local_user'")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE users SET streakCount = :streak, lastStudyDate = :date WHERE id = 'local_user'")
    suspend fun updateStreak(streak: Int, date: Long)

    @Query("UPDATE users SET totalAttempts = totalAttempts + 1, correctAttempts = correctAttempts + :correct WHERE id = 'local_user'")
    suspend fun incrementStats(correct: Int)
}