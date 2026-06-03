package com.marrow.companion.data.database.dao

import androidx.room.*
import com.marrow.companion.data.database.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(subjects: List<SubjectEntity>): List<Long>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    suspend fun getAll(): List<SubjectEntity>

    @Update
    suspend fun update(subject: SubjectEntity)
}