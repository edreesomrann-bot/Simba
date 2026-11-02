package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_items")
data class PortfolioItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val craftsmanId: Long,
    val title: String,
    val description: String,
    val mediaUri: String = "", // Content Uri string or placeholder identifier
    val mediaType: String = "IMAGE", // "IMAGE" or "VIDEO"
    val projectCostLyd: String = "",
    val completedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
