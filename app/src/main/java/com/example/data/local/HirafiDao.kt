package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CraftsmanEntity
import com.example.data.model.PortfolioItemEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.ServiceRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HirafiDao {

    // Craftsmen
    @Query("SELECT * FROM craftsmen ORDER BY id DESC")
    fun getAllCraftsmen(): Flow<List<CraftsmanEntity>>

    @Query("SELECT * FROM craftsmen WHERE id = :id LIMIT 1")
    fun getCraftsmanById(id: Long): Flow<CraftsmanEntity?>

    @Query("SELECT * FROM craftsmen WHERE isRegisteredByMe = 1 LIMIT 1")
    fun getMyCraftsmanProfile(): Flow<CraftsmanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCraftsman(craftsman: CraftsmanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCraftsmen(craftsmen: List<CraftsmanEntity>)

    @Update
    suspend fun updateCraftsman(craftsman: CraftsmanEntity)

    @Query("SELECT COUNT(*) FROM craftsmen")
    suspend fun getCraftsmenCount(): Int

    // Reviews
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE craftsmanId = :craftsmanId ORDER BY createdAt DESC")
    fun getReviewsForCraftsman(craftsmanId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReviews(reviews: List<ReviewEntity>)

    // Service Requests
    @Query("SELECT * FROM service_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE craftsmanId = :craftsmanId ORDER BY createdAt DESC")
    fun getRequestsForCraftsman(craftsmanId: Long): Flow<List<ServiceRequestEntity>>

    @Query("SELECT * FROM service_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Long): ServiceRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ServiceRequestEntity): Long

    @Query("UPDATE service_requests SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String, updatedAt: Long)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // Portfolio Items
    @Query("SELECT * FROM portfolio_items ORDER BY createdAt DESC")
    fun getAllPortfolioItems(): Flow<List<PortfolioItemEntity>>

    @Query("SELECT * FROM portfolio_items WHERE craftsmanId = :craftsmanId ORDER BY createdAt DESC")
    fun getPortfolioForCraftsman(craftsmanId: Long): Flow<List<PortfolioItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioItem(item: PortfolioItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPortfolioItems(items: List<PortfolioItemEntity>)

    @Query("DELETE FROM portfolio_items WHERE id = :id")
    suspend fun deletePortfolioItem(id: Long)
}
