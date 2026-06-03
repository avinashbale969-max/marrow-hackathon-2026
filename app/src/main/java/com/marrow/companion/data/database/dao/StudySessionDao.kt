package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Insert
    suspend fun insert(session: StudySessionEntity): Long

    @Update
    suspend fun update(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(userId: String, limit: Int = 30): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getById(id: Long): StudySessionEntity?

    @Query("""
        SELECT SUM((endTime - startTime)) FROM study_sessions
        WHERE userId = :userId AND startTime > :since AND endTime IS NOT NULL
    """)
    suspend fun getTotalStudyTimeMs(userId: String, since: Long): Long?
}