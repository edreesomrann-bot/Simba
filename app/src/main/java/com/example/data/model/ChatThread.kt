package com.example.data.model

data class ChatThread(
    val id: String = "",
    val craftsmanId: Long = 0L,
    val craftsmanName: String = "",
    val craftsmanCraft: String = "",
    val craftsmanPhone: String = "",
    val clientId: String = "client_user",
    val clientName: String = "الزبون",
    val clientLocation: String = "طرابلس",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)
