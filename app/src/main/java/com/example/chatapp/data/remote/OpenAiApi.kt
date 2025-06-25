package com.example.chatapp.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApi {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatResponse(
        @Body request: OpenAiRequest
    ): OpenAiResponse
}
