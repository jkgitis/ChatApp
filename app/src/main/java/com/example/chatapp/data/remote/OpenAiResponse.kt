package com.example.chatapp.data.remote

data class OpenAiResponse(
    val choices: List<Choice>
) {
    data class Choice(
        val message: Message
    )

    data class Message(
        val role: String,
        val content: String
    )
}
