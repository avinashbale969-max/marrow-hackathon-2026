package com.marrow.companion

import android.app.Application
import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MarrowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Pre-download Hindi and Tamil models silently in background
        // so translation is instant when user first uses it
        preDownloadTranslationModels()
    }

    private fun preDownloadTranslationModels() {
        listOf(TranslateLanguage.HINDI, TranslateLanguage.TAMIL).forEach { lang ->
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(lang)
                .build()
            val translator = Translation.getClient(options)
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    Log.d("MarrowApp", "Translation model ready: $lang")
                    translator.close()
                }
                .addOnFailureListener {
                    // Silent fail — model will download on first translate tap instead
                    translator.close()
                }
        }
    }
}
