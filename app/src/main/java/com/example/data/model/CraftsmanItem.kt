package com.example.data.model

data class RatingBreakdown(
    val averageOverall: Float = 0f,
    val averageQuality: Float = 0f,
    val averagePunctuality: Float = 0f,
    val averageCommunication: Float = 0f,
    val reviewCount: Int = 0
)

data class CraftsmanItem(
    val craftsman: CraftsmanEntity,
    val stats: RatingBreakdown,
    val distanceKm: Double = 0.0,
    val isFavorite: Boolean = false
)
