package com.example.data.model

data class NotificationPreferences(
    val newMatchingRequestsEnabled: Boolean = true,
    val requestAcceptedEnabled: Boolean = true,
    val serviceStatusUpdatesEnabled: Boolean = true,
    val appointmentRemindersEnabled: Boolean = true,
    val soundAndVibrateEnabled: Boolean = true
)
