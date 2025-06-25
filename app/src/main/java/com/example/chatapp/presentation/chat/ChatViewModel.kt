package com.example.chatapp.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.model.Message
import com.example.chatapp.data.repository.ChatRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenToMessages()
    }

    private fun listenToMessages() {
        _uiState.value = ChatUiState.Loading
        listenerRegistration = repository.getMessagesQuery()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = ChatUiState.Error(error.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val msgs = it.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)
                    }.sortedBy { it.timestamp }
                    _messages.value = msgs
                    _uiState.value = ChatUiState.Success
                }
            }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = ChatUiState.Sending
            try {
                repository.sendMessage(text)
                _uiState.value = ChatUiState.Success
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Failed to send message")
            }
        }
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}

sealed class ChatUiState {
    object Idle : ChatUiState()
    object Loading : ChatUiState()
    object Sending : ChatUiState()
    object Success : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}