package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
    val allCraftsmen by viewModel.allCraftsmen.collectAsState()
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
    var showFilterSheet by remember { mutableStateOf(false) }

    val craftFilters = listOf(
        "الكل", "سباكة", "كهرباء", "تكييف وتبريد", "نجارة",
        "دهان وديكور", "صيانة أجهزة منزلية", "حدادة وألمنيوم", "سيراميك وبلاط"
    )

    val hasActiveFilters = selectedSortOrder != SortOrder.NEAREST || filterAvailableOnly || filterEmergencyOnly || filterMinRating4

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
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLocationPicker() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
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
                        Spacer(modifier = Modifier.width(8.dp))
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
                            .background(GreenAvailable.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
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

            // Top Search & Filter Bar (Fixed / Sticky Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Dedicated Separate Filter & Sort Button
                Surface(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    color = if (hasActiveFilters) AmberDark else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasActiveFilters) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("filter_sort_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = if (language == AppLanguage.ARABIC) "الفلاتر والترتيب" else "Filters & Sorting",
                            modifier = Modifier.size(24.dp)
                        )
                        if (hasActiveFilters) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(RedEmergency)
                            )
                        }
                    }
                }
            }

            // Craft Categories Horizontal Scrolling Bar (Smooth LazyRow)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(craftFilters) { craft ->
                    val isSelected = selectedCraft == craft || (craft == "الكل" && selectedCraft == "All")
                    val icon = when (craft) {
                        "الكل" -> "🌟"
                        "سباكة" -> "🔧"
                        "كهرباء" -> "⚡"
                        "تكييف وتبريد" -> "❄️"
                        "نجارة" -> "🪚"
                        "دهان وديكور" -> "🎨"
                        "صيانة أجهزة منزلية" -> "📺"
                        "حدادة وألمنيوم" -> "🔩"
                        "سيراميك وبلاط" -> "🧱"
                        else -> "🛠️"
                    }
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
                        leadingIcon = {
                            Text(icon, fontSize = 12.sp)
                        },
                        label = {
                            Text(
                                text = displayLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberDark,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("craft_chip_$craft")
                    )
                }
            }

            // Active Filters Summary Pill Row (if any filter is applied)
            if (hasActiveFilters && activeViewTab == 0) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "الفلاتر النشطة:" else "Active:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (selectedSortOrder != SortOrder.NEAREST) {
                        item {
                            val sortText = when (selectedSortOrder) {
                                SortOrder.RATING -> if (language == AppLanguage.ARABIC) "الأعلى تقييماً ⭐" else "Top Rated"
                                SortOrder.EXPERIENCE -> if (language == AppLanguage.ARABIC) "الأكثر خبرة 🛠️" else "Experience"
                                else -> ""
                            }
                            ActiveFilterPill(text = sortText, onRemove = { viewModel.setSortOrder(SortOrder.NEAREST) })
                        }
                    }

                    if (filterAvailableOnly) {
                        item {
                            ActiveFilterPill(
                                text = if (language == AppLanguage.ARABIC) "متاح الآن 🟢" else "Available",
                                onRemove = { viewModel.toggleFilterAvailable() }
                            )
                        }
                    }

                    if (filterEmergencyOnly) {
                        item {
                            ActiveFilterPill(
                                text = if (language == AppLanguage.ARABIC) "طوارئ 🚨" else "Emergency",
                                onRemove = { viewModel.toggleFilterEmergency() }
                            )
                        }
                    }

                    if (filterMinRating4) {
                        item {
                            ActiveFilterPill(
                                text = "4.5+ ★",
                                onRemove = { viewModel.toggleFilterMinRating4() }
                            )
                        }
                    }

                    item {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "إلغاء الكل" else "Clear All",
                            fontSize = 11.sp,
                            color = RedEmergency,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.setSortOrder(SortOrder.NEAREST)
                                    viewModel.filterAvailableOnly.value = false
                                    viewModel.filterEmergencyOnly.value = false
                                    viewModel.filterMinRating4.value = false
                                    viewModel.updateFilters()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
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
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        language = language,
                        onRateCraftsman = { craftsmanId, _ ->
                            val targetItem = filteredCraftsmen.firstOrNull { it.craftsman.id == craftsmanId }
                                ?: allCraftsmen.firstOrNull { it.craftsman.id == craftsmanId }
                            if (targetItem != null) {
                                viewModel.openRatingDialog(targetItem)
                            }
                        },
                        onChatRequest = { viewModel.openChatForRequest(it) }
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
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
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

        // Quick Emergency SOS Floating Action Button in BoxScope (anchored in bottom-end corner)
        val isEmergencyActive = filterEmergencyOnly
        ExtendedFloatingActionButton(
            onClick = {
                if (isEmergencyActive) {
                    viewModel.filterEmergencyOnly.value = false
                    viewModel.updateFilters()
                } else {
                    viewModel.filterEmergencyOnly.value = true
                    viewModel.filterAvailableOnly.value = true
                    viewModel.setSortOrder(SortOrder.NEAREST)
                }
            },
            icon = {
                Icon(
                    imageVector = if (isEmergencyActive) Icons.Filled.Close else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            text = {
                Text(
                    text = if (isEmergencyActive) {
                        if (language == AppLanguage.ARABIC) "إلغاء الطوارئ ✕" else "Exit Emergency ✕"
                    } else {
                        if (language == AppLanguage.ARABIC) "طوارئ سريعة 🚨" else "Emergency SOS 🚨"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = if (isEmergencyActive) AmberDark else RedEmergency,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("emergency_sos_fab")
        )
    }

    // Filter & Sort Bottom Sheet Modal
    if (showFilterSheet) {
        FilterSortBottomSheet(
            selectedSortOrder = selectedSortOrder,
            filterAvailableOnly = filterAvailableOnly,
            filterEmergencyOnly = filterEmergencyOnly,
            filterMinRating4 = filterMinRating4,
            language = language,
            onSortSelected = { order ->
                viewModel.setSortOrder(order)
            },
            onToggleAvailable = {
                viewModel.toggleFilterAvailable()
            },
            onToggleEmergency = {
                viewModel.toggleFilterEmergency()
            },
            onToggleRating = {
                viewModel.toggleFilterMinRating4()
            },
            onResetFilters = {
                viewModel.setSortOrder(SortOrder.NEAREST)
                viewModel.filterAvailableOnly.value = false
                viewModel.filterEmergencyOnly.value = false
                viewModel.filterMinRating4.value = false
                viewModel.updateFilters()
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun ActiveFilterPill(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AmberDark.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, AmberDark.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onRemove() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                color = AmberDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove",
                tint = AmberDark,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortBottomSheet(
    selectedSortOrder: SortOrder,
    filterAvailableOnly: Boolean,
    filterEmergencyOnly: Boolean,
    filterMinRating4: Boolean,
    language: AppLanguage,
    onSortSelected: (SortOrder) -> Unit,
    onToggleAvailable: () -> Unit,
    onToggleEmergency: () -> Unit,
    onToggleRating: () -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "تصفية وترتيب الحرفيين" else "Filter & Sort Craftsmen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort Section
            Text(
                text = if (language == AppLanguage.ARABIC) "الترتيب حسب" else "Sort By",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AmberDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            val sortOptions = listOf(
                Triple(SortOrder.NEAREST, if (language == AppLanguage.ARABIC) "الأقرب مسافةً إليك" else "Nearest Distance", "📍"),
                Triple(SortOrder.RATING, if (language == AppLanguage.ARABIC) "الأعلى تقييماً من الزبائن" else "Highest Customer Rating", "⭐"),
                Triple(SortOrder.EXPERIENCE, if (language == AppLanguage.ARABIC) "الأكثر سنوات خبرة" else "Years of Experience", "🛠️")
            )

            sortOptions.forEach { (order, label, icon) ->
                val isSelected = selectedSortOrder == order
                Surface(
                    onClick = { onSortSelected(order) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) AmberDark.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (isSelected) BorderStroke(1.5.dp, AmberDark) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSortSelected(order) },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberDark)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filters Section
            Text(
                text = if (language == AppLanguage.ARABIC) "فلاتر إضافية" else "Additional Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AmberDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Available now toggle
            Surface(
                onClick = onToggleAvailable,
                shape = RoundedCornerShape(14.dp),
                color = if (filterAvailableOnly) GreenAvailable.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (filterAvailableOnly) BorderStroke(1.5.dp, GreenAvailable) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟢", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "متاح للعمل الآن فقط" else "Available Now Only",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (filterAvailableOnly) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = if (language == AppLanguage.ARABIC) "إظهار الفنيين المستعدين للخدمة الفورية" else "Show craftsmen ready for immediate work",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Checkbox(
                        checked = filterAvailableOnly,
                        onCheckedChange = { onToggleAvailable() },
                        colors = CheckboxDefaults.colors(checkedColor = GreenAvailable)
                    )
                }
            }

            // Emergency toggle
            Surface(
                onClick = onToggleEmergency,
                shape = RoundedCornerShape(14.dp),
                color = if (filterEmergencyOnly) RedEmergency.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (filterEmergencyOnly) BorderStroke(1.5.dp, RedEmergency) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "خدمة طوارئ سريعة 24/7" else "24/7 Emergency Service",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (filterEmergencyOnly) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = if (language == AppLanguage.ARABIC) "حالات الأعطال المفاجئة والطارئة" else "Sudden emergency maintenance cases",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Checkbox(
                        checked = filterEmergencyOnly,
                        onCheckedChange = { onToggleEmergency() },
                        colors = CheckboxDefaults.colors(checkedColor = RedEmergency)
                    )
                }
            }

            // Rating 4.5+ toggle
            Surface(
                onClick = onToggleRating,
                shape = RoundedCornerShape(14.dp),
                color = if (filterMinRating4) AmberDark.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (filterMinRating4) BorderStroke(1.5.dp, AmberDark) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "تقييم 4.5+ فما فوق" else "Rating 4.5+ and above",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (filterMinRating4) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = if (language == AppLanguage.ARABIC) "الحرفيون المتميزون بأعلى مراجعات" else "Top reviewed & trusted craftsmen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Checkbox(
                        checked = filterMinRating4,
                        onCheckedChange = { onToggleRating() },
                        colors = CheckboxDefaults.colors(checkedColor = AmberDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onResetFilters,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "إعادة تعيين" else "Reset All",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "تطبيق النتائج" else "Apply Filters",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Simplified Craftsman Card:
 * Displays photo/avatar, name, specialty, rating, and distance.
 * Action buttons are cleanly removed from the card body; tapping anywhere
 * opens the full profile & contact details modal.
 */
@Composable
fun CraftsmanCard(
    item: CraftsmanItem,
    language: AppLanguage,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit = {},
    onWhatsAppClick: () -> Unit = {},
    onRequestClick: () -> Unit = {},
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("craftsman_card_${item.craftsman.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main Top Row: Avatar with Availability Indicator, Info Column, Favorite Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with online status badge dot
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (item.craftsman.isVipFeatured) AmberLight.copy(alpha = 0.35f) else AmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = null,
                            tint = if (item.craftsman.isVipFeatured) AmberDark else OnAmberContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Online indicator badge dot
                    if (item.craftsman.isAvailable) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(GreenAvailable)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Specialty Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("craftsman_profile_trigger_${item.craftsman.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.craftsman.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "🚨", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${item.craftsman.craft} • ${item.craftsman.city} (${item.craftsman.district})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Favorite Bookmark Icon
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) RedEmergency else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Metadata Bar: Rating, Distance, Availability Chip, Details Hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating with star
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.stats.reviewCount > 0) String.format("%.1f", item.stats.averageOverall) else if (language == AppLanguage.ARABIC) "جديد" else "New",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (item.stats.reviewCount > 0) {
                        Text(
                            text = " (${item.stats.reviewCount})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.distanceKm} ${Localization.get("km_away", language)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Availability Status Chip
                Box(
                    modifier = Modifier
                        .background(
                            if (item.craftsman.isAvailable) GreenAvailable.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (item.craftsman.isAvailable)
                            Localization.get("status_available", language)
                        else
                            Localization.get("status_busy", language),
                        color = if (item.craftsman.isAvailable) GreenAvailable else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Subtle forward chevron indicating tap reveals full profile & contact actions
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = if (language == AppLanguage.ARABIC) "عرض التفاصيل والاتصال" else "View details",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ClientRequestsList(
    requests: List<ServiceRequestEntity>,
    language: AppLanguage,
    onRateCraftsman: (craftsmanId: Long, craftsmanName: String) -> Unit = { _, _ -> },
    onChatRequest: (ServiceRequestEntity) -> Unit = {}
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Direct chat coordination button
                        OutlinedButton(
                            onClick = { onChatRequest(req) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("chat_request_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.ARABIC) "محادثة وتنسيق العمل 💬" else "Chat & Coordinate 💬",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberDark
                            )
                        }

                        // Completed service rating action
                        if (req.status == ServiceRequestEntity.STATUS_COMPLETED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onRateCraftsman(req.craftsmanId, req.craftsmanName) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("rate_completed_service_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "قيّم الحرفي بعد اكتمال الخدمة ⭐" else "Rate Craftsman for Completed Work ⭐",
                                    fontSize = 12.sp,
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
