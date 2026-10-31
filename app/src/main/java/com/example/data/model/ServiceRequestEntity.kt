package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val craftsmanId: Long,
    val craftsmanName: String,
    val craft: String,
    val clientName: String,
    val clientPhone: String,
    val description: String,
    val clientLocation: String,
    val isEmergency: Boolean = false,
    val status: String = STATUS_PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_ACCEPTED = "ACCEPTED"
        const val STATUS_ON_THE_WAY = "ON_THE_WAY"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_DECLINED = "DECLINED"
    }
}
