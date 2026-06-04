package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights WHERE questionId = :questionId ORDER BY createdAt DESC")
    fun getForQuestion(questionId: Long): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights ORDER BY createdAt DESC")
    fun getAll(): Flow<List<HighlightEntity>>

    @Query("""
        SELECT h.* FROM highlights h
        INNER JOIN questions q ON h.questionId = q.id
        WHERE q.topicId = :topicId ORDER BY h.createdAt DESC
    """)
    fun getForTopic(topicId: Long): Flow<List<HighlightEntity>>

    @Query("""
        SELECT h.* FROM highlights h
        INNER JOIN questions q ON h.questionId = q.id
        WHERE q.subjectId = :subjectId ORDER BY h.createdAt DESC
    """)
    fun getForSubject(subjectId: Long): Flow<List<HighlightEntity>>

    @Query("""
        SELECT COUNT(h.id) FROM highlights h
        INNER JOIN questions q ON h.questionId = q.id
        WHERE q.subjectId = :subjectId
    """)
    fun getCountForSubject(subjectId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highlight: HighlightEntity): Long

    @Delete
    suspend fun delete(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM highlights WHERE questionId = :questionId AND text = :text")
    suspend fun deleteByQuestionAndText(questionId: Long, text: String)

    // Highlight Inbox — next due for review (nextReviewDate <= today OR null)
    @Query("""
        SELECT h.id, h.questionId, h.text, h.color, h.createdAt,
               h.lastReviewedAt, h.nextReviewDate,
               s.name as subjectName, t.name as topicName,
               COALESCE(q.explanation, '') as explanation
        FROM highlights h
        INNER JOIN questions q ON h.questionId = q.id
        INNER JOIN subjects s ON q.subjectId = s.id
        LEFT JOIN topics t ON q.topicId = t.id
        WHERE h.questionId > 0
          AND (h.nextReviewDate IS NULL OR h.nextReviewDate <= :today)
        ORDER BY h.createdAt ASC
        LIMIT 1
    """)
    fun getNextForReview(today: Long = System.currentTimeMillis()): Flow<HighlightForReview?>

    @Query("SELECT COUNT(*) FROM highlights WHERE questionId > 0 AND (nextReviewDate IS NULL OR nextReviewDate <= :today)")
    fun getDueCount(today: Long = System.currentTimeMillis()): Flow<Int>

    @Query("UPDATE highlights SET lastReviewedAt = :now, nextReviewDate = :nextDate WHERE id = :id")
    suspend fun setReviewResult(id: Long, nextDate: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE highlights SET lastReviewedAt = :time WHERE id = :id")
    suspend fun markReviewed(id: Long, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM highlights WHERE color = 'GREEN'")
    fun getGreenCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM highlights WHERE color = 'ORANGE'")
    fun getOrangeCount(): Flow<Int>

    @Query("""
        SELECT q.subjectId as subjectId, COUNT(h.id) as count
        FROM highlights h INNER JOIN questions q ON h.questionId = q.id
        GROUP BY q.subjectId
    """)
    fun getCountBySubject(): Flow<List<SubjectHighlightCount>>

    @Query("""
        SELECT h.id as id, h.questionId as questionId, h.text as text,
               h.color as color, h.createdAt as createdAt,
               s.name as subjectName, t.name as topicName
        FROM highlights h
        INNER JOIN questions q ON h.questionId = q.id
        INNER JOIN subjects s ON q.subjectId = s.id
        LEFT JOIN topics t ON q.topicId = t.id
        ORDER BY s.name ASC, h.createdAt DESC
    """)
    fun getAllWithSubjectName(): Flow<List<HighlightWithSubject>>

    @Query("""
        SELECT h.id as id, h.questionId as questionId, h.text as text,
               h.color as color, h.createdAt as createdAt,
               s.name as subjectName, t.name as topicName
        FROM highlights h
        INNER JOIN questions q ON h.questionId = q.id
        INNER JOIN subjects s ON q.subjectId = s.id
        LEFT JOIN topics t ON q.topicId = t.id
        WHERE q.subjectId = :subjectId
        ORDER BY h.createdAt DESC
    """)
    fun getForSubjectWithName(subjectId: Long): Flow<List<HighlightWithSubject>>
}

data class SubjectHighlightCount(val subjectId: Long, val count: Int)

data class HighlightForReview(
    val id: Long,
    val questionId: Long,
    val text: String,
    val color: String,
    val createdAt: Long,
    val lastReviewedAt: Long?,
    val nextReviewDate: Long?,
    val subjectName: String,
    val topicName: String?,
    val explanation: String = ""
)

data class HighlightWithSubject(
    val id: Long,
    val questionId: Long,
    val text: String,
    val color: String,
    val createdAt: Long,
    val subjectName: String,
    val topicName: String? = null
)
