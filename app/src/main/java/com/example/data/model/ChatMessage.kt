package com.example.data.model

data class ChatMessage(
    val id: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = ROLE_CLIENT, // "CLIENT" or "CRAFTSMAN"
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    companion object {
        const val ROLE_CLIENT = "CLIENT"
        const val ROLE_CRAFTSMAN = "CRAFTSMAN"
    }
}
