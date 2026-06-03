package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.PausedQuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PausedQuizDao {

    @Query("SELECT * FROM paused_quizzes WHERE topicId = :topicId LIMIT 1")
    suspend fun getForTopic(topicId: Long): PausedQuizEntity?

    @Query("SELECT * FROM paused_quizzes WHERE subjectId = :subjectId AND topicId IS NULL LIMIT 1")
    suspend fun getForSubject(subjectId: Long): PausedQuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PausedQuizEntity)

    @Query("DELETE FROM paused_quizzes WHERE topicId = :topicId")
    suspend fun deleteForTopic(topicId: Long)

    @Query("DELETE FROM paused_quizzes WHERE subjectId = :subjectId AND topicId IS NULL")
    suspend fun deleteForSubject(subjectId: Long)

    // Delete existing entry then insert fresh — prevents duplicate rows
    @Transaction
    suspend fun saveOrReplace(session: PausedQuizEntity) {
        session.topicId?.let { deleteForTopic(it) }
            ?: session.subjectId?.let { deleteForSubject(it) }
        insert(session)
    }

    @Query("SELECT COUNT(*) > 0 FROM paused_quizzes WHERE topicId = :topicId")
    fun hasPausedForTopic(topicId: Long): Flow<Boolean>

    @Query("SELECT topicId FROM paused_quizzes WHERE topicId IS NOT NULL")
    fun getAllPausedTopicIds(): Flow<List<Long>>

    @Query("SELECT currentIndex FROM paused_quizzes WHERE topicId = :topicId ORDER BY id DESC LIMIT 1")
    fun getPausedIndexForTopic(topicId: Long): Flow<Int?>
}
