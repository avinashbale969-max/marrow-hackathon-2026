package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // Excludes TAG notes (private flag annotations) from all display queries

    @Query("SELECT * FROM notes WHERE questionId = :questionId AND tag != 'TAG' ORDER BY createdAt DESC")
    fun getForQuestion(questionId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE tag != 'TAG' ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NoteEntity>>

    // Separate query that includes TAG notes — used only by HighlightableText for red flags
    @Query("SELECT * FROM notes WHERE questionId = :questionId AND tag = 'TAG' ORDER BY createdAt DESC")
    fun getTagsForQuestion(questionId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE questionId = :questionId AND tag = 'SUMMARY' LIMIT 1")
    fun getSummaryForQuestion(questionId: Long): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT n.* FROM notes n
        INNER JOIN questions q ON n.questionId = q.id
        WHERE q.topicId = :topicId AND n.tag != 'TAG' ORDER BY n.createdAt DESC
    """)
    fun getForTopic(topicId: Long): Flow<List<NoteEntity>>

    @Query("""
        SELECT COUNT(n.id) FROM notes n
        INNER JOIN questions q ON n.questionId = q.id
        WHERE q.topicId = :topicId AND n.tag != 'TAG'
    """)
    fun getCountForTopic(topicId: Long): Flow<Int>

    @Query("""
        SELECT n.* FROM notes n
        INNER JOIN questions q ON n.questionId = q.id
        WHERE q.subjectId = :subjectId AND n.tag != 'TAG' ORDER BY n.createdAt DESC
    """)
    fun getForSubject(subjectId: Long): Flow<List<NoteEntity>>

    @Query("""
        SELECT COUNT(n.id) FROM notes n
        INNER JOIN questions q ON n.questionId = q.id
        WHERE q.subjectId = :subjectId AND n.tag != 'TAG'
    """)
    fun getCountForSubject(subjectId: Long): Flow<Int>

    @Query("""
        SELECT q.subjectId as subjectId, COUNT(n.id) as count
        FROM notes n INNER JOIN questions q ON n.questionId = q.id
        WHERE n.tag != 'TAG'
        GROUP BY q.subjectId
    """)
    fun getCountBySubject(): Flow<List<SubjectNoteCount>>

    @Query("""
        SELECT n.id as id, n.questionId as questionId, n.text as text,
               n.tag as tag, n.attachedQuote as attachedQuote,
               n.createdAt as createdAt, s.name as subjectName
        FROM notes n
        INNER JOIN questions q ON n.questionId = q.id
        INNER JOIN subjects s ON q.subjectId = s.id
        WHERE n.tag != 'TAG'
        ORDER BY s.name ASC, n.createdAt DESC
    """)
    fun getAllWithSubjectName(): Flow<List<NoteWithSubject>>

    @Query("""
        SELECT n.id as id, n.questionId as questionId, n.text as text,
               n.tag as tag, n.attachedQuote as attachedQuote,
               n.createdAt as createdAt, s.name as subjectName
        FROM notes n
        INNER JOIN questions q ON n.questionId = q.id
        INNER JOIN subjects s ON q.subjectId = s.id
        WHERE q.subjectId = :subjectId AND n.tag != 'TAG'
        ORDER BY n.createdAt DESC
    """)
    fun getForSubjectWithName(subjectId: Long): Flow<List<NoteWithSubject>>
}

data class SubjectNoteCount(val subjectId: Long, val count: Int)
data class NoteWithSubject(
    val id: Long,
    val questionId: Long,
    val text: String,
    val tag: String,
    val attachedQuote: String?,
    val createdAt: Long,
    val subjectName: String
)
