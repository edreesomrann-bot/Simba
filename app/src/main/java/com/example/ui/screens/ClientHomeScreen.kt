package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CraftsmanItem
import com.example.data.model.ServiceRequestEntity
import com.example.ui.components.RadarMapVisualizer
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GreenAvailable
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.RedEmergency
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.ClientLocation
import com.example.ui.viewmodel.HirafiViewModel
import com.example.ui.viewmodel.SortOrder
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun ClientHomeScreen(
    viewModel: HirafiViewModel,
    onCallClick: (String) -> Unit,
    onWhatsAppClick: (String) -> Unit,
    onOpenLocationPicker: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val filteredCraftsmen by viewModel.filteredCraftsmen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCraft by viewModel.selectedCraft.collectAsState()
    val selectedRadius by viewModel.selectedRadiusKm.collectAsState()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsState()
    val filterAvailableOnly by viewModel.filterAvailableOnly.collectAsState()
    val filterEmergencyOnly by viewModel.filterEmergencyOnly.collectAsState()
    val filterMinRating4 by viewModel.filterMinRating4.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    var activeViewTab by remember { mutableIntStateOf(0) } // 0: List, 1: Radar/Map, 2: My Requests, 3: Favorites

    val craftFilters = listOf(
        "الكل", "سباكة", "كهرباء", "تكييف وتبريد", "نجارة",
        "دهان وديكور", "صيانة أجهزة منزلية", "حدادة وألمنيوم", "سيراميك وبلاط"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("client_home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Top Location Selector & Ad-Free Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLocationPicker() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.ARABIC) currentLocation.nameAr else currentLocation.nameEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "اضغط لتغيير الحي أو المدينة الليبية" else "Tap to change district / city",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 100% Ad-Free Badge
                Box(
                    modifier = Modifier
                        .background(GreenAvailable.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "خالٍ من الإعلانات 100%" else "100% Ad-Free",
                        color = GreenAvailable,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text(Localization.get("search_hint", language), fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = AmberDark)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("search_text_input")
        )

        // Craft Categories Filter Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(craftFilters) { craft ->
                val isSelected = selectedCraft == craft || (craft == "الكل" && selectedCraft == "All")
                val displayLabel = when (craft) {
                    "الكل" -> Localization.get("all", language)
                    "سباكة" -> Localization.get("plumbing", language)
                    "كهرباء" -> Localization.get("electrical", language)
                    "تكييف وتبريد" -> Localization.get("ac_cooling", language)
                    "نجارة" -> Localization.get("carpentry", language)
                    "دهان وديكور" -> Localization.get("painting", language)
                    "صيانة أجهزة منزلية" -> Localization.get("appliances", language)
                    "حدادة وألمنيوم" -> Localization.get("blacksmith", language)
                    "سيراميك وبلاط" -> Localization.get("tiles", language)
                    else -> craft
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCraft.value = craft },
                    label = { Text(displayLabel, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberDark,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("craft_chip_$craft")
                )
            }
        }

        // View Tabs: Directory List, Radar Map, My Requests, Favorites
        TabRow(
            selectedTabIndex = activeViewTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AmberDark
        ) {
            Tab(
                selected = activeViewTab == 0,
                onClick = { activeViewTab = 0 },
                text = { Text(Localization.get("tab_explore", language), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.ViewList, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeViewTab == 1,
                onClick = { activeViewTab = 1 },
                text = { Text(Localization.get("tab_radar", language), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeViewTab == 2,
                onClick = { activeViewTab = 2 },
                text = { Text(Localization.get("tab_my_requests", language), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.NearMe, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeViewTab == 3,
                onClick = { activeViewTab = 3 },
                text = { Text(Localization.get("tab_favorites", language), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Tab Contents
        when (activeViewTab) {
            0 -> {
                // Explore Directory Cards List
                // Quick Sort & Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort order chip
                    FilterChip(
                        selected = selectedSortOrder == SortOrder.NEAREST,
                        onClick = {
                            val nextSort = when (selectedSortOrder) {
                                SortOrder.NEAREST -> SortOrder.RATING
                                SortOrder.RATING -> SortOrder.EXPERIENCE
                                SortOrder.EXPERIENCE -> SortOrder.NEAREST
                            }
                            viewModel.setSortOrder(nextSort)
                        },
                        leadingIcon = { Icon(Icons.Filled.Sort, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = {
                            val sortText = when (selectedSortOrder) {
                                SortOrder.NEAREST -> Localization.get("sort_nearest", language)
                                SortOrder.RATING -> Localization.get("sort_rating", language)
                                SortOrder.EXPERIENCE -> Localization.get("sort_experience", language)
                            }
                            Text(sortText, fontSize = 11.sp)
                        }
                    )

                    FilterChip(
                        selected = filterAvailableOnly,
                        onClick = { viewModel.toggleFilterAvailable() },
                        label = { Text(Localization.get("filter_available", language), fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = filterEmergencyOnly,
                        onClick = { viewModel.toggleFilterEmergency() },
                        label = { Text(Localization.get("filter_emergency", language), fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = filterMinRating4,
                        onClick = { viewModel.toggleFilterMinRating4() },
                        label = { Text("4.5+ ★", fontSize = 11.sp) }
                    )
                }

                if (filteredCraftsmen.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "لا يوجد حرفيون مطابقون لخيارات البحث الحالية في منطقتك. جرب توسيع نطاق البحث أو تغيير الفلتر."
                            else
                                "No craftsmen matching search filters in your area. Try widening search radius.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCraftsmen) { item ->
                            CraftsmanCard(
                                item = item,
                                language = language,
                                onCardClick = { viewModel.openCraftsmanDetail(item) },
                                onCallClick = { onCallClick(item.craftsman.phone) },
                                onWhatsAppClick = { onWhatsAppClick(item.craftsman.whatsapp) },
                                onRequestClick = { viewModel.openRequestDialog(item) },
                                onToggleFavorite = { viewModel.toggleFavorite(item.craftsman.id) }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Radar & Map View
                RadarMapVisualizer(
                    craftsmen = filteredCraftsmen,
                    currentLocation = currentLocation,
                    selectedRadius = selectedRadius,
                    language = language,
                    onRadiusSelected = { viewModel.selectedRadiusKm.value = it },
                    onCraftsmanClick = { viewModel.openCraftsmanDetail(it) },
                    onCallClick = onCallClick,
                    onWhatsAppClick = onWhatsAppClick,
                    onRequestClick = { viewModel.openRequestDialog(it) }
                )
            }

            2 -> {
                // My Requests Tracker Tab
                ClientRequestsList(
                    requests = allRequests,
                    language = language
                )
            }

            3 -> {
                // Favorites List
                val favoriteCraftsmen = filteredCraftsmen.filter { favoriteIds.contains(it.craftsman.id) }
                if (favoriteCraftsmen.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC)
                                "لم تقم بإضافة أي حرفي للمفضلة بعد. انقر على أيقونة القلب في بطاقة الحرفي لحفظه للوصول السريع."
                            else
                                "No favorites saved yet. Tap the heart icon on any craftsman card to save for fast access.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(favoriteCraftsmen) { item ->
                            CraftsmanCard(
                                item = item,
                                language = language,
                                onCardClick = { viewModel.openCraftsmanDetail(item) },
                                onCallClick = { onCallClick(item.craftsman.phone) },
                                onWhatsAppClick = { onWhatsAppClick(item.craftsman.whatsapp) },
                                onRequestClick = { viewModel.openRequestDialog(item) },
                                onToggleFavorite = { viewModel.toggleFavorite(item.craftsman.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Emergency SOS Floating Action Button in BoxScope
    ExtendedFloatingActionButton(
        onClick = {
            viewModel.filterEmergencyOnly.value = true
            viewModel.filterAvailableOnly.value = true
            viewModel.setSortOrder(SortOrder.NEAREST)
        },
        icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White) },
        text = {
            Text(
                text = if (language == AppLanguage.ARABIC) "طوارئ سريعة 🚨" else "Emergency SOS 🚨",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = RedEmergency,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .testTag("emergency_sos_fab")
    )
}
}

@Composable
fun CraftsmanCard(
    item: CraftsmanItem,
    language: AppLanguage,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onRequestClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("craftsman_card_${item.craftsman.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name, Craft, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCardClick() }
                        .testTag("craftsman_profile_trigger_${item.craftsman.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (item.craftsman.isVipFeatured) AmberLight.copy(alpha = 0.35f) else AmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = null,
                            tint = if (item.craftsman.isVipFeatured) AmberDark else OnAmberContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.craftsman.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.craftsman.isVipFeatured) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(AmberLight, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "👑 VIP",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (item.craftsman.hasEmergencyService) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚨",
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = "${item.craftsman.craft} • ${item.craftsman.city} (${item.craftsman.district})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) RedEmergency else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row: Rating, Distance, Availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.stats.reviewCount > 0) String.format("%.1f", item.stats.averageOverall) else "جديد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (item.stats.reviewCount > 0) {
                        Text(
                            text = " (${item.stats.reviewCount} ${Localization.get("review_count", language)})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Distance
                Text(
                    text = "📍 ${item.distanceKm} ${Localization.get("km_away", language)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Availability chip
                Box(
                    modifier = Modifier
                        .background(
                            if (item.craftsman.isAvailable) GreenAvailable.copy(alpha = 0.15f)
                            else Color.Gray.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.craftsman.isAvailable)
                            Localization.get("status_available", language)
                        else
                            Localization.get("status_busy", language),
                        color = if (item.craftsman.isAvailable) GreenAvailable else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Call, WhatsApp, Request
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Localization.get("call_direct", language), fontSize = 11.sp)
                }

                Button(
                    onClick = onWhatsAppClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Localization.get("whatsapp_chat", language), fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = onRequestClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "طلب عمل" else "Request",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ClientRequestsList(
    requests: List<ServiceRequestEntity>,
    language: AppLanguage
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (language == AppLanguage.ARABIC)
                    "لا توجد طلبات خدمة مرسلة حالياً. يمكنك إرسال طلب جديد لأي حرفي في أي وقت لمتابعة حالته هنا في الوقت الفعلي."
                else
                    "No service requests sent yet. Send a request to any craftsman to track its live status here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(requests) { req ->
                val statusText = when (req.status) {
                    ServiceRequestEntity.STATUS_PENDING -> Localization.get("status_pending", language)
                    ServiceRequestEntity.STATUS_ACCEPTED -> Localization.get("status_accepted", language)
                    ServiceRequestEntity.STATUS_ON_THE_WAY -> Localization.get("status_on_the_way", language)
                    ServiceRequestEntity.STATUS_IN_PROGRESS -> Localization.get("status_in_progress", language)
                    ServiceRequestEntity.STATUS_COMPLETED -> Localization.get("status_completed", language)
                    ServiceRequestEntity.STATUS_DECLINED -> Localization.get("status_declined", language)
                    else -> req.status
                }

                val statusColor = when (req.status) {
                    ServiceRequestEntity.STATUS_PENDING -> AmberDark
                    ServiceRequestEntity.STATUS_ACCEPTED, ServiceRequestEntity.STATUS_ON_THE_WAY -> BlueAccent
                    ServiceRequestEntity.STATUS_IN_PROGRESS -> AmberLight
                    ServiceRequestEntity.STATUS_COMPLETED -> GreenAvailable
                    ServiceRequestEntity.STATUS_DECLINED -> RedEmergency
                    else -> Color.Gray
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${req.craftsmanName} (${req.craft})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = req.description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📍 ${req.clientLocation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (req.isEmergency) {
                                Text(
                                    text = "🚨 " + Localization.get("is_urgent_emergency", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RedEmergency,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
