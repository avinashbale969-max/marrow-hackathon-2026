package com.marrow.companion.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marrow.companion.data.database.dao.*
import com.marrow.companion.data.database.entities.*
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteEntity
import com.marrow.companion.data.database.entities.PausedQuizEntity

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        QuestionOptionEntity::class,
        QuestionAttemptEntity::class,
        FlashcardEntity::class,
        FlashcardReviewEntity::class,
        StudySessionEntity::class,
        HighlightEntity::class,
        NoteEntity::class,
        PausedQuizEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun questionAttemptDao(): QuestionAttemptDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun pausedQuizDao(): PausedQuizDao
}