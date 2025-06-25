package com.example.chatapp.data.remote

data class OpenAiRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Double = 0.7
) {
    data class Message(
        val role: String,
        val content: String
    )
}
