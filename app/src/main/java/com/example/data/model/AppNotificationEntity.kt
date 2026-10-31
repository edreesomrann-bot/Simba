package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // CLIENT_REQUEST, STATUS_UPDATE, REVIEW_RECEIVED
    val relatedRequestId: Long? = null,
    val relatedCraftsmanId: Long? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
