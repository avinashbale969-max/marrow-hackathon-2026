package com.marrow.companion.data.network

import retrofit2.http.Body
import retrofit2.http.POST

data class ClaudeMessage(val role: String, val content: String)

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-6",
    val max_tokens: Int = 1024,
    val system: String = """You are a medical education assistant helping MBBS students preparing for NEET PG.
        Answer questions concisely and accurately. Use clinical mnemonics where helpful.
        Keep responses under 300 words unless the question requires more detail.""",
    val messages: List<ClaudeMessage>
)

data class ClaudeContent(val type: String, val text: String)
data class ClaudeResponse(val content: List<ClaudeContent>)

interface ClaudeApiService {
    @POST("messages")
    suspend fun sendMessage(@Body request: ClaudeRequest): ClaudeResponse
}