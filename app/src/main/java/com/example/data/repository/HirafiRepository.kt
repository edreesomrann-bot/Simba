package com.example.data.repository

import com.example.data.local.HirafiDao
import com.example.data.local.HirafiDatabase
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CraftsmanEntity
import com.example.data.model.CraftsmanItem
import com.example.data.model.NotificationPreferences
import com.example.data.model.PortfolioItemEntity
import com.example.data.model.RatingBreakdown
import com.example.data.model.ReviewEntity
import com.example.data.model.ServiceRequestEntity
import com.example.data.remote.FirestoreSyncManager
import com.example.service.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

class HirafiRepository(
    private val dao: HirafiDao,
    private val notificationHelper: NotificationHelper,
    private val syncManager: FirestoreSyncManager? = null
) {
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())
    val notificationPreferences: Flow<NotificationPreferences> = _notificationPreferences.asStateFlow()

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        _notificationPreferences.value = prefs
    }

    // Craftsmen list with computed rating breakdowns and distances
    fun getCraftsmenWithStats(userLat: Double, userLon: Double): Flow<List<CraftsmanItem>> {
        return combine(
            dao.getAllCraftsmen(),
            dao.getAllReviews()
        ) { craftsmen, reviews ->
            val reviewsByCraftsman = reviews.groupBy { it.craftsmanId }

            craftsmen.map { craftsman ->
                val craftsmanReviews = reviewsByCraftsman[craftsman.id] ?: emptyList()
                val stats = if (craftsmanReviews.isEmpty()) {
                    RatingBreakdown(0f, 0f, 0f, 0f, 0)
                } else {
                    val count = craftsmanReviews.size
                    val avgQuality = craftsmanReviews.map { it.qualityRating }.average().toFloat()
                    val avgPunctuality = craftsmanReviews.map { it.punctualityRating }.average().toFloat()
                    val avgCommunication = craftsmanReviews.map { it.communicationRating }.average().toFloat()
                    val avgOverall = craftsmanReviews.map { it.overallRating }.average().toFloat()

                    RatingBreakdown(
                        averageOverall = round(avgOverall * 10f) / 10f,
                        averageQuality = round(avgQuality * 10f) / 10f,
                        averagePunctuality = round(avgPunctuality * 10f) / 10f,
                        averageCommunication = round(avgCommunication * 10f) / 10f,
                        reviewCount = count
                    )
                }

                val distance = calculateDistanceKm(
                    userLat, userLon,
                    craftsman.latitude, craftsman.longitude
                )

                CraftsmanItem(
                    craftsman = craftsman,
                    stats = stats,
                    distanceKm = distance
                )
            }
        }
    }

    fun getReviewsForCraftsman(craftsmanId: Long): Flow<List<ReviewEntity>> {
        return dao.getReviewsForCraftsman(craftsmanId)
    }

    fun getMyCraftsmanProfile(): Flow<CraftsmanEntity?> {
        return dao.getMyCraftsmanProfile()
    }

    suspend fun saveMyCraftsmanProfile(profile: CraftsmanEntity): Long {
        val saved = profile.copy(isRegisteredByMe = true)
        val id = dao.insertCraftsman(saved)
        // Sync to cloud Firestore in real-time
        syncManager?.pushCraftsmanToFirestore(saved.copy(id = id))
        return id
    }

    suspend fun updateCraftsmanAvailability(craftsman: CraftsmanEntity, isAvailable: Boolean) {
        val updated = craftsman.copy(isAvailable = isAvailable)
        dao.updateCraftsman(updated)
        syncManager?.pushCraftsmanToFirestore(updated)
    }

    // Portfolio items
    fun getPortfolioForCraftsman(craftsmanId: Long): Flow<List<PortfolioItemEntity>> {
        return dao.getPortfolioForCraftsman(craftsmanId)
    }

    suspend fun addPortfolioItem(item: PortfolioItemEntity): Long {
        return dao.insertPortfolioItem(item)
    }

    suspend fun deletePortfolioItem(id: Long) {
        dao.deletePortfolioItem(id)
    }

    // Reviews submission
    suspend fun submitReview(
        craftsmanId: Long,
        craftsmanName: String,
        clientName: String,
        quality: Float,
        punctuality: Float,
        communication: Float,
        comment: String
    ): Long {
        val review = ReviewEntity(
            craftsmanId = craftsmanId,
            clientName = clientName.ifBlank { "عميل" },
            qualityRating = quality,
            punctualityRating = punctuality,
            communicationRating = communication,
            comment = comment.trim(),
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insertReview(review)

        // Notification for the craftsman
        val notif = AppNotificationEntity(
            title = "⭐ تقييم جديد للحرفي $craftsmanName",
            message = "أضاف العميل ${review.clientName} تقييماً جديداً بمتوسط ${String.format("%.1f", review.overallRating)} نجوم.",
            type = "REVIEW_RECEIVED",
            relatedCraftsmanId = craftsmanId
        )
        dao.insertNotification(notif)
        notificationHelper.showNotification(notif.title, notif.message)

        return id
    }

    // Service Requests
    fun getAllRequests(): Flow<List<ServiceRequestEntity>> {
        return dao.getAllRequests()
    }

    fun getRequestsForCraftsman(craftsmanId: Long): Flow<List<ServiceRequestEntity>> {
        return dao.getRequestsForCraftsman(craftsmanId)
    }

    suspend fun submitServiceRequest(
        craftsmanId: Long,
        craftsmanName: String,
        craft: String,
        clientName: String,
        clientPhone: String,
        description: String,
        clientLocation: String,
        isEmergency: Boolean
    ): Long {
        val request = ServiceRequestEntity(
            craftsmanId = craftsmanId,
            craftsmanName = craftsmanName,
            craft = craft,
            clientName = clientName.ifBlank { "عميل جديد" },
            clientPhone = clientPhone,
            description = description.trim(),
            clientLocation = clientLocation.ifBlank { "موقعي الحالي" },
            isEmergency = isEmergency,
            status = ServiceRequestEntity.STATUS_PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val requestId = dao.insertRequest(request)

        val prefs = _notificationPreferences.value
        // Real-time Notification dispatched to the craftsman if preference allows
        if (prefs.newMatchingRequestsEnabled) {
            val urgentTag = if (isEmergency) " [عاجل/طوارئ 🚨]" else ""
            val notif = AppNotificationEntity(
                title = "🔔 طلب عمل جديد$urgentTag",
                message = "طلب جديد من ${request.clientName} للحرفي $craftsmanName ($craft): ${request.description}",
                type = "CLIENT_REQUEST",
                relatedRequestId = requestId,
                relatedCraftsmanId = craftsmanId
            )
            dao.insertNotification(notif)
            notificationHelper.showNotification(notif.title, notif.message)
        }

        return requestId
    }

    suspend fun updateRequestStatus(
        requestId: Long,
        newStatus: String,
        craftsmanName: String,
        clientName: String
    ) {
        val now = System.currentTimeMillis()
        dao.updateRequestStatus(requestId, newStatus, now)

        val prefs = _notificationPreferences.value
        val shouldNotify = when (newStatus) {
            ServiceRequestEntity.STATUS_ACCEPTED -> prefs.requestAcceptedEnabled
            else -> prefs.serviceStatusUpdatesEnabled
        }

        val statusText = when (newStatus) {
            ServiceRequestEntity.STATUS_ACCEPTED -> "تم قبول طلبك بواسطة $craftsmanName"
            ServiceRequestEntity.STATUS_ON_THE_WAY -> "الحرفي $craftsmanName في الطريق إليك الآن 🚗"
            ServiceRequestEntity.STATUS_IN_PROGRESS -> "بدأ الحرفي $craftsmanName العمل على إصلاح العطل 🛠️"
            ServiceRequestEntity.STATUS_COMPLETED -> "تم إنجاز عملك بنجاح من قبل $craftsmanName! يمكنك تقييمه الآن ⭐"
            ServiceRequestEntity.STATUS_DECLINED -> "اعتذر الحرفي $craftsmanName عن استلام الطلب حالياً."
            else -> "تحديث في حالة طلبك"
        }

        val notif = AppNotificationEntity(
            title = "📢 تحديث في حالة طلب الخدمة",
            message = "عزيزي $clientName: $statusText",
            type = "STATUS_UPDATE",
            relatedRequestId = requestId
        )
        dao.insertNotification(notif)
        if (shouldNotify) {
            notificationHelper.showNotification(notif.title, notif.message)
        }
    }

    suspend fun sendAppointmentReminder(requestId: Long, craftsmanName: String, clientName: String, appointmentTime: String) {
        val prefs = _notificationPreferences.value
        if (!prefs.appointmentRemindersEnabled) return

        val notif = AppNotificationEntity(
            title = "⏰ تذكير بموعد الخدمة",
            message = "تذكير: لديك موعد مجدول مع الحرفي $craftsmanName في $appointmentTime. يرجى التواجد في الموقع.",
            type = "REMINDER",
            relatedRequestId = requestId
        )
        dao.insertNotification(notif)
        notificationHelper.showNotification(notif.title, notif.message)
    }

    // Notifications
    fun getAllNotifications(): Flow<List<AppNotificationEntity>> {
        return dao.getAllNotifications()
    }

    fun getUnreadNotificationsCount(): Flow<Int> {
        return dao.getUnreadNotificationsCount()
    }

    suspend fun markNotificationAsRead(id: Long) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun ensureDefaultCraftsmanProfileExists(): CraftsmanEntity {
        // Find existing or seed
        val existing = dao.getCraftsmenCount()
        if (existing == 0) {
            HirafiDatabase.populateInitialData(dao)
        }
        // Return first or registered
        return CraftsmanEntity(
            name = "عبد الرحمن الشريف",
            craft = "سباكة",
            experienceYears = 8,
            phone = "+218919988776",
            whatsapp = "+218919988776",
            city = "طرابلس",
            district = "حي الأندلس",
            latitude = 32.8920,
            longitude = 13.1530,
            bio = "أسطى سباكة معتمد لتأسيس وصيانة شبكات المياه المنزلية، كشف التسريبات وتركيب المضخات والسخانات.",
            priceEstimate = "كشف 35 د.ل",
            isAvailable = true,
            hasEmergencyService = true,
            isRegisteredByMe = true
        )
    }

    companion object {
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val dist = r * c
            return round(dist * 10.0) / 10.0
        }
    }
}
