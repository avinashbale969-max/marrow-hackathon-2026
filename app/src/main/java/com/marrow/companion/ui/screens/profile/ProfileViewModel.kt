package com.marrow.companion.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.UserDao
import com.marrow.companion.data.database.entities.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val userDao: UserDao) : ViewModel() {

    val user: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateName(name: String) {
        viewModelScope.launch {
            val current = userDao.getUser().first() ?: UserEntity()
            userDao.upsert(current.copy(name = name.trim().ifBlank { "Student" }))
        }
    }

    fun updateExamDate(dateMs: Long) {
        viewModelScope.launch {
            val current = userDao.getUser().first() ?: UserEntity()
            userDao.upsert(current.copy(examDate = dateMs))
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            val current = userDao.getUser().first() ?: return@launch
            userDao.upsert(current.copy(streakCount = 0, totalAttempts = 0, correctAttempts = 0))
        }
    }
}