package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.QuestionAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionAttemptDao {
    @Insert
    suspend fun insert(attempt: QuestionAttemptEntity)

    @Query("""
        SELECT * FROM question_attempts
        WHERE userId = :userId
        ORDER BY attemptedAt DESC
        LIMIT :limit
    """)
    fun getRecentAttempts(userId: String, limit: Int = 50): Flow<List<QuestionAttemptEntity>>

    @Query("""
        SELECT strftime('%Y-%m-%d', attemptedAt / 1000, 'unixepoch') as day,
               COUNT(*) as total,
               SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct
        FROM question_attempts
        WHERE userId = :userId AND attemptedAt > :since
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getDailyStats(userId: String, since: Long): List<DailyStat>

    @Query("SELECT COUNT(*) FROM question_attempts WHERE userId = :userId AND questionId = :questionId")
    suspend fun attemptCount(userId: String, questionId: Long): Int

    @Query("""
        SELECT COUNT(DISTINCT qa.questionId) FROM question_attempts qa
        INNER JOIN questions q ON qa.questionId = q.id
        WHERE qa.userId = :userId AND q.topicId = :topicId
    """)
    fun getAttemptedCountForTopic(userId: String, topicId: Long): Flow<Int>

    @Query("""
        SELECT MAX(qa.attemptedAt) FROM question_attempts qa
        INNER JOIN questions q ON qa.questionId = q.id
        WHERE qa.userId = :userId AND q.topicId = :topicId
    """)
    suspend fun getLastAttemptTimeForTopic(userId: String, topicId: Long): Long?

    @Query("""
        SELECT selectedOptionId, COUNT(*) as count
        FROM question_attempts WHERE questionId = :questionId AND selectedOptionId IS NOT NULL
        GROUP BY selectedOptionId
    """)
    suspend fun getOptionStats(questionId: Long): List<OptionStat>

    @Query("""
        SELECT q.topicId as topicId, COUNT(DISTINCT qa.questionId) as attempted
        FROM questions q
        LEFT JOIN question_attempts qa ON q.id = qa.questionId AND qa.userId = :userId
        WHERE q.subjectId = :subjectId
        GROUP BY q.topicId
    """)
    fun getAttemptedPerTopic(userId: String, subjectId: Long): Flow<List<TopicAttempted>>
}

data class DailyStat(val day: String, val total: Int, val correct: Int)
data class OptionStat(val selectedOptionId: Long, val count: Int)
data class TopicAttempted(val topicId: Long, val attempted: Int)
