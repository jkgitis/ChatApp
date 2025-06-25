package com.example.chatapp.data.repository

import com.example.chatapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val messagesRef = db.collection("messages")
    private val auth = FirebaseAuth.getInstance()

    suspend fun sendMessage(text: String) {
        try {
            val currentUser = auth.currentUser ?: throw Exception("Not authenticated")
            val messageId = messagesRef.document().id

            val message = Message(
                id = messageId,
                text = text,
                senderId = currentUser.uid,
                timestamp = System.currentTimeMillis()
            )

            messagesRef.document(messageId).set(message).await()
        } catch (e: Exception) {
            throw e // Re-throw to be handled by ViewModel
        }
    }

    fun getMessagesQuery(): Query {
        return messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }
}