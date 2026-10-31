package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "craftsmen")
data class CraftsmanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val craft: String,
    val experienceYears: Int,
    val phone: String,
    val whatsapp: String,
    val city: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val bio: String,
    val priceEstimate: String,
    val isAvailable: Boolean = true,
    val hasEmergencyService: Boolean = false,
    val isRegisteredByMe: Boolean = false,
    val isVipFeatured: Boolean = false,
    val specializations: String = "",
    val avatarSeed: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
