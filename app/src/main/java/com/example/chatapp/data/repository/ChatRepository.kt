package com.example.chatapp.data.repository

import com.example.chatapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Reference to user's private messages collection
    private fun messagesRef() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "unknown_user")
        .collection("messages")

    // Save message
    suspend fun sendMessage(message: Message) {
        messagesRef().document(message.id).set(message).await()
    }

    // Fetch messages ordered by timestamp
    fun getMessagesQuery(): Query {
        return messagesRef().orderBy("timestamp", Query.Direction.ASCENDING)
    }
}
