package com.marrow.companion.di

import android.content.Context
import androidx.room.Room
import com.marrow.companion.data.database.AppDatabase
import com.marrow.companion.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "marrow_companion.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()
    @Provides fun provideTopicDao(db: AppDatabase): TopicDao = db.topicDao()
    @Provides fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()
    @Provides fun provideQuestionAttemptDao(db: AppDatabase): QuestionAttemptDao = db.questionAttemptDao()
    @Provides fun provideFlashcardDao(db: AppDatabase): FlashcardDao = db.flashcardDao()
    @Provides fun provideStudySessionDao(db: AppDatabase): StudySessionDao = db.studySessionDao()
    @Provides fun provideHighlightDao(db: AppDatabase): HighlightDao = db.highlightDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun providePausedQuizDao(db: AppDatabase): PausedQuizDao = db.pausedQuizDao()
}