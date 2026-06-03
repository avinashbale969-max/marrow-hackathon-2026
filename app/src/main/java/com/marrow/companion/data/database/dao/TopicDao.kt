package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY name ASC")
    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getById(id: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(topics: List<TopicEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(topic: TopicEntity): Long
}