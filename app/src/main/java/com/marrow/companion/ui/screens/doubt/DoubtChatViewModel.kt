package com.marrow.companion.ui.screens.doubt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.BuildConfig
import com.marrow.companion.data.network.ClaudeApiService
import com.marrow.companion.data.network.ClaudeMessage
import com.marrow.companion.data.network.ClaudeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(val role: String, val content: String)

data class DoubtChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DoubtChatViewModel @Inject constructor(
    private val claudeApi: ClaudeApiService
) : ViewModel() {

    private val _state = MutableStateFlow(DoubtChatUiState())
    val state: StateFlow<DoubtChatUiState> = _state.asStateFlow()

    fun setInput(text: String) = _state.update { it.copy(inputText = text, error = null) }

    fun setContext(questionText: String) {
        if (questionText.isBlank() || _state.value.messages.isNotEmpty()) return
        _state.update {
            it.copy(messages = listOf(
                ChatMessage("user", "Help me understand this question: $questionText"),
            ))
        }
        sendToApi()
    }

    val hasApiKey: Boolean get() = BuildConfig.CLAUDE_API_KEY.isNotBlank()

    fun sendMessage() {
        if (!hasApiKey) {
            _state.update { it.copy(error = "Add your Anthropic API key in local.properties (CLAUDE_API_KEY=sk-ant-...) and rebuild.") }
            return
        }
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isLoading) return
        _state.update {
            it.copy(
                messages = it.messages + ChatMessage("user", text),
                inputText = "",
                isLoading = true,
                error = null
            )
        }
        sendToApi()
    }

    private fun sendToApi() {
        viewModelScope.launch {
            try {
                val apiMessages = _state.value.messages.map {
                    ClaudeMessage(role = it.role, content = it.content)
                }
                val response = claudeApi.sendMessage(ClaudeRequest(messages = apiMessages))
                val reply = response.content.firstOrNull()?.text ?: "No response received."
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage("assistant", reply),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = "Failed to get response. Check your API key.")
                }
            }
        }
    }

    fun clearChat() = _state.update { DoubtChatUiState() }
}
