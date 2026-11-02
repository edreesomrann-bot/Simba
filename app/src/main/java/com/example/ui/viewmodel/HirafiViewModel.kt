package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HirafiDatabase
import com.example.data.model.AppNotificationEntity
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.CraftsmanEntity
import com.example.data.model.CraftsmanItem
import com.example.data.model.NotificationPreferences
import com.example.data.model.PortfolioItemEntity
import com.example.data.model.RatingBreakdown
import com.example.data.model.ReviewEntity
import com.example.data.model.ServiceRequestEntity
import com.example.data.remote.FirestoreChatManager
import com.example.data.remote.FirestoreSyncManager
import com.example.data.repository.HirafiRepository
import com.example.service.NotificationHelper
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
    CLIENT, // قسم العميل المستهلك
    CRAFTSMAN // قسم العامل الحرفي
}

enum class SortOrder {
    NEAREST, // الأقرب مسافة
    RATING, // الأعلى تقييماً
    EXPERIENCE // الأكثر خبرة
}

data class ClientLocation(
    val nameAr: String,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double
)

class HirafiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HirafiRepository
    val notificationHelper = NotificationHelper(application)
    private val syncManager: FirestoreSyncManager
    private val chatManager: FirestoreChatManager

    // Theme (Dark Mode / Light Mode)
    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null means follow system
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !(_isDarkMode.value ?: false)
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    init {
        val database = HirafiDatabase.getDatabase(application, viewModelScope)
        syncManager = FirestoreSyncManager(application, database.hirafiDao(), viewModelScope)
        chatManager = FirestoreChatManager(application, viewModelScope)
        repository = HirafiRepository(database.hirafiDao(), notificationHelper, syncManager)
        syncManager.startRealtimeSync()
        loadMyCraftsmanProfile()
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.stopRealtimeSync()
    }

    // Language
    private val _language = MutableStateFlow(AppLanguage.ARABIC)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
    }

    // Notification Preferences
    val notificationPreferences: StateFlow<NotificationPreferences> = repository.notificationPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationPreferences())

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        repository.updateNotificationPreferences(prefs)
    }

    fun triggerAppointmentReminder(requestId: Long, craftsmanName: String, clientName: String, timeText: String) {
        viewModelScope.launch {
            repository.sendAppointmentReminder(requestId, craftsmanName, clientName, timeText)
        }
    }

    // App Mode (Client vs Craftsman)
    private val _currentMode = MutableStateFlow(AppMode.CLIENT)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    fun setAppMode(mode: AppMode) {
        _currentMode.value = mode
    }

    // Client Location
    val presetLocations = listOf(
        ClientLocation("حي الأندلس (طرابلس)", "Hay Al-Andalus (Tripoli)", 32.8910, 13.1520),
        ClientLocation("بن عاشور (طرابلس)", "Ben Ashour (Tripoli)", 32.8760, 13.1860),
        ClientLocation("سوق الجمعة (طرابلس)", "Souq Al-Jumaa (Tripoli)", 32.8830, 13.2320),
        ClientLocation("جنزور (غرب طرابلس)", "Janzour (West Tripoli)", 32.8270, 13.0180),
        ClientLocation("الكيش (بنغازي)", "Al-Kish (Benghazi)", 32.1120, 20.0710),
        ClientLocation("الهواري (بنغازي)", "Al-Hawari (Benghazi)", 32.0820, 20.1020),
        ClientLocation("الرويسات (مصراتة)", "Al-Ruwaisat (Misrata)", 32.3810, 15.0920),
        ClientLocation("وسط الزاوية", "Downtown Zawiya", 32.7520, 12.7270)
    )

    private val _currentLocation = MutableStateFlow(presetLocations[0])
    val currentLocation: StateFlow<ClientLocation> = _currentLocation.asStateFlow()

    fun setLocation(location: ClientLocation) {
        _currentLocation.value = location
    }

    fun setCustomCoordinates(lat: Double, lon: Double, name: String) {
        _currentLocation.value = ClientLocation(name, name, lat, lon)
    }

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedCraft = MutableStateFlow("الكل")
    val selectedRadiusKm = MutableStateFlow(0.0) // 0.0 means all distances
    val selectedSortOrder = MutableStateFlow(SortOrder.NEAREST)
    val filterAvailableOnly = MutableStateFlow(false)
    val filterEmergencyOnly = MutableStateFlow(false)
    val filterMinRating4 = MutableStateFlow(false)

    // Trigger for updating filter state
    val filterUpdateTrigger = MutableStateFlow(0)

    fun updateFilters() {
        filterUpdateTrigger.value = filterUpdateTrigger.value + 1
    }

    fun setRadius(radius: Double) {
        selectedRadiusKm.value = radius
        updateFilters()
    }

    fun setSortOrder(sort: SortOrder) {
        selectedSortOrder.value = sort
        updateFilters()
    }

    fun toggleFilterAvailable() {
        filterAvailableOnly.value = !filterAvailableOnly.value
        updateFilters()
    }

    fun toggleFilterEmergency() {
        filterEmergencyOnly.value = !filterEmergencyOnly.value
        updateFilters()
    }

    fun toggleFilterMinRating4() {
        filterMinRating4.value = !filterMinRating4.value
        updateFilters()
    }

    // Favorites
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    fun toggleFavorite(craftsmanId: Long) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(craftsmanId)) {
            current.remove(craftsmanId)
        } else {
            current.add(craftsmanId)
        }
        _favoriteIds.value = current
        updateFilters()
    }

    // Base craftsmen stream from repository
    val allCraftsmen: StateFlow<List<CraftsmanItem>> = repository
        .getCraftsmenWithStats(32.8910, 13.1520)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered and sorted craftsmen for UI (5-flow combine, fully type-safe)
    val filteredCraftsmen: StateFlow<List<CraftsmanItem>> = combine(
        allCraftsmen,
        _currentLocation,
        searchQuery,
        selectedCraft,
        filterUpdateTrigger
    ) { rawCraftsmen, loc, query, craft, _ ->
        val radius = selectedRadiusKm.value
        val sort = selectedSortOrder.value
        val availOnly = filterAvailableOnly.value
        val emergOnly = filterEmergencyOnly.value
        val minRate4 = filterMinRating4.value
        val favs = _favoriteIds.value
        val q = query.trim().lowercase()

        rawCraftsmen.map { item ->
            val dist = HirafiRepository.calculateDistanceKm(
                loc.latitude, loc.longitude,
                item.craftsman.latitude, item.craftsman.longitude
            )
            item.copy(
                distanceKm = dist,
                isFavorite = favs.contains(item.craftsman.id)
            )
        }.filter { item ->
            val c = item.craftsman
            val matchesQuery = q.isEmpty() ||
                    c.name.lowercase().contains(q) ||
                    c.craft.lowercase().contains(q) ||
                    c.district.lowercase().contains(q) ||
                    c.city.lowercase().contains(q) ||
                    c.bio.lowercase().contains(q)

            val matchesCraft = craft == "الكل" || craft == "All" || c.craft.contains(craft)
            val matchesRadius = radius <= 0.0 || item.distanceKm <= radius
            val matchesAvail = !availOnly || c.isAvailable
            val matchesEmerg = !emergOnly || c.hasEmergencyService
            val matchesRate = !minRate4 || item.stats.averageOverall >= 4.5f

            matchesQuery && matchesCraft && matchesRadius && matchesAvail && matchesEmerg && matchesRate
        }.sortedWith { a, b ->
            // Priority 1: VIP Featured (100 LYD/month) craftsmen appear first in search & browsing!
            if (a.craftsman.isVipFeatured != b.craftsman.isVipFeatured) {
                if (a.craftsman.isVipFeatured) -1 else 1
            } else {
                when (sort) {
                    SortOrder.NEAREST -> a.distanceKm.compareTo(b.distanceKm)
                    SortOrder.RATING -> b.stats.averageOverall.compareTo(a.stats.averageOverall)
                    SortOrder.EXPERIENCE -> b.craftsman.experienceYears.compareTo(a.craftsman.experienceYears)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<AppNotificationEntity>> = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.getUnreadNotificationsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    // Requests Stream
    val allRequests: StateFlow<List<ServiceRequestEntity>> = repository.getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Craftsman Detail Sheet
    private val _selectedCraftsmanForDetail = MutableStateFlow<CraftsmanItem?>(null)
    val selectedCraftsmanForDetail: StateFlow<CraftsmanItem?> = _selectedCraftsmanForDetail.asStateFlow()

    private val _craftsmanReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val craftsmanReviews: StateFlow<List<ReviewEntity>> = _craftsmanReviews.asStateFlow()

    private val _craftsmanPortfolio = MutableStateFlow<List<PortfolioItemEntity>>(emptyList())
    val craftsmanPortfolio: StateFlow<List<PortfolioItemEntity>> = _craftsmanPortfolio.asStateFlow()

    fun openCraftsmanDetail(craftsmanItem: CraftsmanItem) {
        _selectedCraftsmanForDetail.value = craftsmanItem
        viewModelScope.launch {
            repository.getReviewsForCraftsman(craftsmanItem.craftsman.id).collect {
                _craftsmanReviews.value = it
            }
        }
        viewModelScope.launch {
            repository.getPortfolioForCraftsman(craftsmanItem.craftsman.id).collect {
                _craftsmanPortfolio.value = it
            }
        }
    }

    fun closeCraftsmanDetail() {
        _selectedCraftsmanForDetail.value = null
        _craftsmanPortfolio.value = emptyList()
    }

    // Dialogs
    var isRatingDialogOpen = MutableStateFlow(false)
    var ratingTargetCraftsman = MutableStateFlow<CraftsmanItem?>(null)

    fun openRatingDialog(item: CraftsmanItem) {
        ratingTargetCraftsman.value = item
        isRatingDialogOpen.value = true
    }

    fun closeRatingDialog() {
        isRatingDialogOpen.value = false
        ratingTargetCraftsman.value = null
    }

    fun submitRating(quality: Float, punctuality: Float, communication: Float, comment: String, clientName: String) {
        val target = ratingTargetCraftsman.value ?: return
        viewModelScope.launch {
            repository.submitReview(
                craftsmanId = target.craftsman.id,
                craftsmanName = target.craftsman.name,
                clientName = clientName,
                quality = quality,
                punctuality = punctuality,
                communication = communication,
                comment = comment
            )
            closeRatingDialog()
        }
    }

    var isRequestDialogOpen = MutableStateFlow(false)
    var requestTargetCraftsman = MutableStateFlow<CraftsmanItem?>(null)

    fun openRequestDialog(item: CraftsmanItem) {
        requestTargetCraftsman.value = item
        isRequestDialogOpen.value = true
    }

    fun closeRequestDialog() {
        isRequestDialogOpen.value = false
        requestTargetCraftsman.value = null
    }

    fun submitServiceRequest(
        description: String,
        clientName: String,
        clientPhone: String,
        clientLocation: String,
        isEmergency: Boolean
    ) {
        val target = requestTargetCraftsman.value ?: return
        viewModelScope.launch {
            repository.submitServiceRequest(
                craftsmanId = target.craftsman.id,
                craftsmanName = target.craftsman.name,
                craft = target.craftsman.craft,
                clientName = clientName,
                clientPhone = clientPhone,
                description = description,
                clientLocation = clientLocation,
                isEmergency = isEmergency
            )
            closeRequestDialog()
        }
    }

    // Craftsman Section
    private val _myCraftsmanProfile = MutableStateFlow<CraftsmanEntity?>(null)
    val myCraftsmanProfile: StateFlow<CraftsmanEntity?> = _myCraftsmanProfile.asStateFlow()

    private fun loadMyCraftsmanProfile() {
        viewModelScope.launch {
            repository.getMyCraftsmanProfile().collect { profile ->
                if (profile != null) {
                    _myCraftsmanProfile.value = profile
                } else {
                    // Initialize default profile
                    val defaultProf = repository.ensureDefaultCraftsmanProfileExists()
                    _myCraftsmanProfile.value = defaultProf
                }
            }
        }
    }

    fun saveMyProfile(updated: CraftsmanEntity) {
        viewModelScope.launch {
            repository.saveMyCraftsmanProfile(updated)
            _myCraftsmanProfile.value = updated
        }
    }

    fun updateMyAvailability(isAvailable: Boolean) {
        val current = _myCraftsmanProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(isAvailable = isAvailable)
            repository.updateCraftsmanAvailability(updated, isAvailable)
            _myCraftsmanProfile.value = updated
        }
    }

    fun toggleVipSubscription() {
        val current = _myCraftsmanProfile.value ?: return
        val newVipState = !current.isVipFeatured
        viewModelScope.launch {
            val updated = current.copy(isVipFeatured = newVipState)
            repository.saveMyCraftsmanProfile(updated)
            _myCraftsmanProfile.value = updated

            val title = if (newVipState) "👑 تم تفعيل باقة التميز (100 د.ل/شهرياً)" else "تم إلغاء باقة التميز"
            val msg = if (newVipState)
                "مبروك! يحظى ملفك الآن بأولوية الظهور الدائم في نتائج البحث والتصفح مع الشارة الذهبية."
            else
                "تمت العودة للباقة المجانية."
            notificationHelper.showNotification(title, msg)
        }
    }

    // Craftsman updates client request status
    fun updateRequestStatus(requestId: Long, newStatus: String, craftsmanName: String, clientName: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, newStatus, craftsmanName, clientName)
        }
    }

    // Craftsman Portfolio Management
    private val _myPortfolio = MutableStateFlow<List<PortfolioItemEntity>>(emptyList())
    val myPortfolio: StateFlow<List<PortfolioItemEntity>> = _myPortfolio.asStateFlow()

    fun loadMyPortfolio(craftsmanId: Long) {
        viewModelScope.launch {
            repository.getPortfolioForCraftsman(craftsmanId).collect {
                _myPortfolio.value = it
            }
        }
    }

    fun addPortfolioItem(
        title: String,
        description: String,
        mediaUri: String,
        mediaType: String,
        projectCostLyd: String,
        completedDate: String
    ) {
        val myId = _myCraftsmanProfile.value?.id ?: 1L
        viewModelScope.launch {
            val item = PortfolioItemEntity(
                craftsmanId = myId,
                title = title.trim(),
                description = description.trim(),
                mediaUri = mediaUri,
                mediaType = mediaType,
                projectCostLyd = projectCostLyd.trim(),
                completedDate = completedDate.trim()
            )
            repository.addPortfolioItem(item)
            val notif = AppNotificationEntity(
                title = "📸 تم إضافة عمل جديد لمعرضك",
                message = "تم نشر عمل: $title في معرض أعمالك ليظهر لجميع العملاء الباحثين عن خدماتك.",
                type = "PORTFOLIO_ADDED",
                relatedCraftsmanId = myId
            )
            notificationHelper.showNotification(notif.title, notif.message)
        }
    }

    fun deletePortfolioItem(id: Long) {
        viewModelScope.launch {
            repository.deletePortfolioItem(id)
        }
    }

    // UI Dialog States
    var isNotificationPreferencesDialogOpen = MutableStateFlow(false)
    var isAddPortfolioDialogOpen = MutableStateFlow(false)

    // Direct Firestore Chat State
    val chatThreads: StateFlow<List<ChatThread>> = chatManager.threadsState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatThread = MutableStateFlow<ChatThread?>(null)
    val activeChatThread: StateFlow<ChatThread?> = _activeChatThread.asStateFlow()

    val isConversationsListOpen = MutableStateFlow(false)

    fun getChatMessages(threadId: String): Flow<List<ChatMessage>> {
        return chatManager.getMessagesFlow(threadId)
    }

    fun openChatForCraftsman(item: CraftsmanItem) {
        val threadId = "thread_c_${item.craftsman.id}_u_client"
        val existing = chatThreads.value.firstOrNull { it.id == threadId }
        val thread = existing ?: ChatThread(
            id = threadId,
            craftsmanId = item.craftsman.id,
            craftsmanName = item.craftsman.name,
            craftsmanCraft = item.craftsman.craft,
            craftsmanPhone = item.craftsman.phone,
            clientId = "client_me",
            clientName = "الزبون",
            clientLocation = "${item.craftsman.city} (${item.craftsman.district})"
        )
        _activeChatThread.value = thread
    }

    fun openChatForRequest(request: ServiceRequestEntity) {
        val threadId = "thread_c_${request.craftsmanId}_u_client"
        val existing = chatThreads.value.firstOrNull { it.id == threadId }
        val thread = existing ?: ChatThread(
            id = threadId,
            craftsmanId = request.craftsmanId,
            craftsmanName = request.craftsmanName,
            craftsmanCraft = request.craft,
            craftsmanPhone = "",
            clientId = "client_me",
            clientName = request.clientName,
            clientLocation = request.clientLocation
        )
        _activeChatThread.value = thread
    }

    fun openChatThread(thread: ChatThread) {
        _activeChatThread.value = thread
    }

    fun closeActiveChat() {
        chatManager.stopListeningToMessages()
        _activeChatThread.value = null
    }

    fun sendChatMessage(thread: ChatThread, messageText: String) {
        val isCraftsmanMode = currentMode.value == AppMode.CRAFTSMAN
        val role = if (isCraftsmanMode) ChatMessage.ROLE_CRAFTSMAN else ChatMessage.ROLE_CLIENT
        val senderName = if (isCraftsmanMode) (_myCraftsmanProfile.value?.name ?: thread.craftsmanName) else thread.clientName
        chatManager.sendMessage(thread, messageText, role, senderName)
    }
}
