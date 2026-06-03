package com.marrow.companion.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import com.google.android.gms.tasks.Tasks

private val TealHeader = Color(0xFF4DC8D4)

data class LangOption(val label: String, val native: String, val mlkitCode: String)

val QUICK_LANGUAGES = listOf(
    LangOption("Hindi", "हिंदी",   TranslateLanguage.HINDI),
    LangOption("Tamil", "தமிழ்",  TranslateLanguage.TAMIL)
)

suspend fun translateWithMlKit(text: String, targetLangCode: String): String {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(targetLangCode)
        .build()
    val translator = Translation.getClient(options)
    return try {
        translator.downloadModelIfNeeded().await()
        translator.translate(text).await()
    } finally {
        translator.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTranslateSheet(
    selectedText: String,
    onDismiss: () -> Unit
) {
    var selectedLang     by remember { mutableStateOf(QUICK_LANGUAGES[0]) }
    var translatedText   by remember { mutableStateOf("") }
    var isLoading        by remember { mutableStateOf(false) }
    var error            by remember { mutableStateOf<String?>(null) }

    // Auto-translate to Hindi on open
    LaunchedEffect(selectedText) {
        if (selectedText.isNotBlank()) {
            isLoading = true; error = null
            translatedText = try {
                translateWithMlKit(selectedText, selectedLang.mlkitCode)
            } catch (e: Exception) {
                error = "Translation failed. Check internet for first-time model download."
                ""
            }
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier         = Modifier.fillMaxHeight(0.65f),
        containerColor   = Color.White
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(TealHeader)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Translate Selection", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, null, tint = Color.White)
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Selected text preview
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.width(3.dp).heightIn(min = 20.dp)
                        .background(TealHeader).clip(RoundedCornerShape(2.dp)))
                    Text(
                        "\"${selectedText.take(120)}${if (selectedText.length > 120) "…" else ""}\"",
                        fontSize = 13.sp, color = Color(0xFF444444), lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Language toggle: Hindi | Tamil
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QUICK_LANGUAGES.forEach { lang ->
                        val active = selectedLang.label == lang.label
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) TealHeader else Color(0xFFF0F0F0))
                                .clickable {
                                    if (!active) {
                                        selectedLang = lang
                                        isLoading = true; error = null; translatedText = ""
                                        // trigger re-translate via key change handled below
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(lang.label, fontSize = 13.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) Color.White else Color(0xFF333333))
                                Text(lang.native, fontSize = 11.sp,
                                    color = if (active) Color.White.copy(0.85f) else Color.Gray)
                            }
                        }
                    }
                }

                // Re-translate when language changes
                LaunchedEffect(selectedLang) {
                    if (selectedText.isNotBlank()) {
                        isLoading = true; error = null
                        translatedText = try {
                            translateWithMlKit(selectedText, selectedLang.mlkitCode)
                        } catch (e: Exception) {
                            error = "Translation failed. Check internet on first use."
                            ""
                        }
                        isLoading = false
                    }
                }

                // Result area
                when {
                    isLoading -> Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.size(18.dp), TealHeader, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Translating to ${selectedLang.label}…", fontSize = 13.sp, color = Color.Gray)
                    }
                    error != null -> Text(error!!, color = Color(0xFFE53935), fontSize = 13.sp)
                    translatedText.isNotBlank() -> Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(selectedLang.label, fontSize = 11.sp,
                                color = TealHeader, fontWeight = FontWeight.SemiBold)
                            Text(translatedText, fontSize = 16.sp,
                                lineHeight = 26.sp, color = Color(0xFF2D2D2D))
                        }
                    }
                }
            }
        }
    }
}
