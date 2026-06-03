package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.QuestionEntity
import com.marrow.companion.data.database.entities.QuestionOptionEntity
import kotlinx.coroutines.flow.Flow

data class QuestionWithOptions(
    @Embedded val question: QuestionEntity,
    @Relation(parentColumn = "id", entityColumn = "questionId")
    val options: List<QuestionOptionEntity>
)

@Dao
interface QuestionDao {
    @Transaction
    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsForTopic(topicId: Long, limit: Int = 20): List<QuestionWithOptions>

    @Transaction
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsForSubject(subjectId: Long, limit: Int = 20): List<QuestionWithOptions>

    @Transaction
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int = 10): List<QuestionWithOptions>

    @Transaction
    @Query("SELECT * FROM questions WHERE bookmarkType IS NOT NULL")
    fun getBookmarked(): Flow<List<QuestionWithOptions>>

    @Transaction
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND bookmarkType IS NOT NULL ORDER BY id DESC")
    fun getBookmarkedForSubject(subjectId: Long): Flow<List<QuestionWithOptions>>

    @Query("SELECT COUNT(*) FROM questions WHERE subjectId = :subjectId AND bookmarkType IS NOT NULL")
    fun getBookmarkedCountForSubject(subjectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE topicId = :topicId AND bookmarkType IS NOT NULL")
    fun getBookmarkedCountForTopic(topicId: Long): Flow<Int>

    @Transaction
    @Query("SELECT * FROM questions WHERE topicId = :topicId AND bookmarkType IS NOT NULL ORDER BY id ASC")
    fun getBookmarkedForTopic(topicId: Long): Flow<List<QuestionWithOptions>>

    @Query("UPDATE questions SET bookmarkType = :type WHERE id = :id")
    suspend fun setBookmarkType(id: Long, type: String?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOptions(options: List<QuestionOptionEntity>)

    @Transaction
    suspend fun insertQuestionWithOptions(question: QuestionEntity, options: List<QuestionOptionEntity>) {
        val questionId = insertQuestion(question)
        insertOptions(options.map { it.copy(questionId = questionId) })
    }

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getCount(): Int

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): QuestionWithOptions?

    @Transaction
    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionWithOptions>

    @Transaction
    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY id ASC")
    fun getAllQuestionsForTopicOrdered(topicId: Long): Flow<List<QuestionWithOptions>>

    @Transaction
    @Query("SELECT * FROM questions ORDER BY id LIMIT 1 OFFSET :offset")
    suspend fun getQuestionAtOffset(offset: Int): QuestionWithOptions?

    @Query("SELECT COUNT(*) FROM questions WHERE bookmarkType IS NOT NULL")
    fun getBookmarkedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE bookmarkType = :type")
    fun getBookmarkedCountByType(type: String): Flow<Int>

    @Query("""
        SELECT s.name as subjectName, COUNT(q.id) as count
        FROM questions q INNER JOIN subjects s ON q.subjectId = s.id
        WHERE q.bookmarkType IS NOT NULL
        GROUP BY q.subjectId ORDER BY s.name ASC
    """)
    fun getBookmarkedCountBySubject(): Flow<List<SubjectBookmarkCount>>

    @Query("""
        SELECT q.subjectId, COUNT(DISTINCT qa.questionId) as attempted
        FROM question_attempts qa INNER JOIN questions q ON qa.questionId = q.id
        WHERE qa.userId = :userId GROUP BY q.subjectId
    """)
    fun getAttemptedPerSubject(userId: String): Flow<List<SubjectAttempted>>

    @Query("""
        SELECT q.subjectId,
               COUNT(*) as total,
               SUM(CASE WHEN qa.isCorrect = 1 THEN 1 ELSE 0 END) as correct
        FROM questions q
        LEFT JOIN question_attempts qa ON q.id = qa.questionId AND qa.userId = :userId
        WHERE qa.id IS NOT NULL
        GROUP BY q.subjectId
    """)
    suspend fun getAccuracyBySubject(userId: String): List<SubjectAccuracy>
}

data class SubjectAccuracy(val subjectId: Long, val total: Int, val correct: Int)
data class SubjectBookmarkCount(val subjectName: String, val count: Int)
data class SubjectAttempted(val subjectId: Long, val attempted: Int)