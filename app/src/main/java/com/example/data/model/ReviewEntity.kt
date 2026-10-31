package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val craftsmanId: Long,
    val clientName: String,
    val qualityRating: Float, // 1.0 to 5.0 - جودة العمل
    val punctualityRating: Float, // 1.0 to 5.0 - الالتزام بالمواعيد
    val communicationRating: Float, // 1.0 to 5.0 - سهولة التواصل
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val overallRating: Float
        get() = (qualityRating + punctualityRating + communicationRating) / 3f
}
