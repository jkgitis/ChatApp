package com.example.chatapp.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.Message
import com.example.chatapp.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenForMessages()
    }

    private fun listenForMessages() {
        listenerRegistration = repository.getMessagesQuery()
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    _messages.value = msgs
                }
            }
    }

    // 🔥 Hardcoded AI replies map
    private val aiResponses = mapOf(
        "hi" to "Hello! How can I help you?",
        "hello" to "Hi there! Ask me anything.",
        "how are you" to "I'm just a bunch of code, but I'm doing great!",
        "what is your name" to "I'm ChatApp AI Assistant.",
        "bye" to "Goodbye! Have a nice day.",
        "thank you" to "You're welcome!",
        "help" to "Sure! You can ask me about our services or chat."
    )

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            // Save user message
            val message = Message(
                id = UUID.randomUUID().toString(),
                text = userMessage,
                senderId = currentUserId,
                timestamp = System.currentTimeMillis()
            )

            repository.sendMessage(message)


            val lowerMessage = userMessage.lowercase().trim()

            // Check for AI response
            aiResponses.forEach { (key, reply) ->
                if (lowerMessage.contains(key)) {
                    // Save AI reply with senderId = "AI_BOT"
                    repository.sendMessage(
                        Message(
                            id = UUID.randomUUID().toString(),
                            text = reply,
                            senderId = "AI_BOT",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    return@launch
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
