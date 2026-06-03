package com.marrow.companion.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.network.ClaudeApiService
import com.marrow.companion.data.network.ClaudeMessage
import com.marrow.companion.data.network.ClaudeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TealHeader = Color(0xFF4DC8D4)

val LANGUAGES = listOf(
    "Hindi" to "हिंदी",
    "Tamil" to "தமிழ்",
    "Telugu" to "తెలుగు",
    "Kannada" to "ಕನ್ನಡ",
    "Malayalam" to "മലയാളം",
    "Bengali" to "বাংলা",
    "Marathi" to "मराठी",
    "Gujarati" to "ગુજરાતી"
)

data class TranslationState(
    val selectedLanguage: String = "Hindi",
    val translatedText: String  = "",
    val isLoading: Boolean      = false,
    val error: String?          = null
)

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val claudeApi: ClaudeApiService
) : ViewModel() {

    private val _state = MutableStateFlow(TranslationState())
    val state: StateFlow<TranslationState> = _state.asStateFlow()

    fun translate(text: String, language: String) {
        _state.update { it.copy(isLoading = true, error = null, selectedLanguage = language) }
        viewModelScope.launch {
            try {
                val response = claudeApi.sendMessage(
                    ClaudeRequest(
                        max_tokens = 2048,
                        system = "You are a medical translator. Translate the given medical explanation accurately to the requested language. Keep medical terms in English where appropriate but explain them in the target language. Be concise and clear.",
                        messages = listOf(
                            ClaudeMessage(
                                role    = "user",
                                content = "Translate this medical explanation to $language. Keep it accurate and student-friendly:\n\n$text"
                            )
                        )
                    )
                )
                _state.update {
                    it.copy(
                        translatedText = response.content.firstOrNull()?.text ?: "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false,
                        error = "Translation unavailable. Add API key in local.properties.")
                }
            }
        }
    }

    fun selectLanguage(lang: String) {
        _state.update { it.copy(selectedLanguage = lang, translatedText = "", error = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSheet(
    originalText: String,
    onDismiss: () -> Unit,
    viewModel: TranslationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier   = Modifier.fillMaxHeight(0.85f),
        containerColor = Color(0xFFF5F5F5)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(TealHeader)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Translate, null, tint = Color.White,
                        modifier = Modifier.size(20.dp))
                    Text("Translate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, null, tint = Color.White)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Language grid
                Text("Select Language", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    color = Color(0xFF444444))

                // 4-column language grid
                val rows = LANGUAGES.chunked(2)
                rows.forEach { rowLangs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowLangs.forEach { (english, native) ->
                            val selected = state.selectedLanguage == english
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) TealHeader else Color.White)
                                    .clickable { viewModel.selectLanguage(english) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(english, fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Color(0xFF333333))
                                    Text(native, fontSize = 11.sp,
                                        color = if (selected) Color.White.copy(0.85f) else Color.Gray)
                                }
                            }
                        }
                        // Pad if odd number in row
                        if (rowLangs.size < 2) Spacer(Modifier.weight(1f))
                    }
                }

                // Translate button
                Button(
                    onClick = { viewModel.translate(originalText, state.selectedLanguage) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = TealHeader),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (state.isLoading) "Translating..." else "Translate to ${state.selectedLanguage}",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Result
                state.error?.let { err ->
                    Text(err, color = Color(0xFFE53935), fontSize = 13.sp)
                }

                if (state.translatedText.isNotBlank()) {
                    Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(state.selectedLanguage, fontWeight = FontWeight.SemiBold,
                                    color = TealHeader, fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.translatedText,
                                fontSize = 15.sp, lineHeight = 24.sp,
                                color = Color(0xFF2D2D2D),
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }
    }
}
