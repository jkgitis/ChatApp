package com.example.chatapp.presentation.chat

import com.example.chatapp.data.remote.OpenAiResponse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.Message
import com.example.chatapp.data.remote.OpenAiRequest
import com.example.chatapp.data.remote.RetrofitInstance
import com.example.chatapp.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenForMessages()
    }

    private fun listenForMessages() {
        listenerRegistration = repository.getMessagesQuery()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    _messages.value = msgs
                }
            }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch

            // Save User Message
            val message = Message(
                id = UUID.randomUUID().toString(),
                text = userMessage,
                senderId = currentUserId,
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(message)

            // Get AI reply from OpenAI API
            getAiReply(userMessage)
        }
    }

    private fun getAiReply(userMessage: String) {
        viewModelScope.launch {
            try {
                val request = OpenAiRequest(
                    messages = listOf(
                        OpenAiRequest.Message(
                            role = "user",
                            content = userMessage
                        )
                    )
                )

                val response = RetrofitInstance.api.getChatResponse(request)
                val reply = response.choices.firstOrNull()?.message?.content

                if (reply != null) {
                    saveAiReply(reply)
                } else {
                    // Handle case where response doesn't contain a valid reply
                    saveAiReply("I didn't understand that. Could you please rephrase?")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle network errors or API failures
                saveAiReply("Sorry, I'm having trouble connecting right now. Please try again later.")
            }
        }
    }

    private suspend fun saveAiReply(reply: String) {
        val aiMessage = Message(
            id = UUID.randomUUID().toString(),
            text = reply,
            senderId = "AI_BOT",
            timestamp = System.currentTimeMillis()
        )
        repository.sendMessage(aiMessage)
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
