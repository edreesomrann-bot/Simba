package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.AddPortfolioItemDialog
import com.example.ui.components.ConversationsListDialog
import com.example.ui.components.EditCraftsmanProfileDialog
import com.example.ui.components.FpsDisplayBadge
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.NewRequestDialog
import com.example.ui.components.NotificationDrawer
import com.example.ui.components.NotificationPreferencesDialog
import com.example.ui.components.RatingDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ClientHomeScreen
import com.example.ui.screens.CraftsmanDashboardScreen
import com.example.ui.screens.CraftsmanDetailSheet
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.RedEmergency
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.HirafiViewModel
import com.example.util.AppLanguage
import com.example.util.FrameRateHelper
import com.example.util.Localization

class MainActivity : ComponentActivity() {

    private val viewModel: HirafiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Enforce 90 FPS ultra-smooth refresh rate with hardware GPU acceleration
        FrameRateHelper.applyFrameRate(this, FrameRateHelper.TargetFps.FPS_90)

        setContent {
            val language by viewModel.language.collectAsState()
            val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            // Android 13+ Notification Permission Launcher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result received */ }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val isDark by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val currentDark = isDark ?: systemDark

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = currentDark) {
                    MainScreen(
                        viewModel = viewModel,
                        onCall = { phone -> makePhoneCall(phone) },
                        onWhatsApp = { phone -> openWhatsApp(phone) },
                        onOpenMap = { lat, lon, name -> openMapDirections(lat, lon, name) },
                        onShare = { item -> shareCraftsmanProfile(item) }
                    )
                }
            }
        }
    }

    private fun makePhoneCall(phone: String) {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phone.trim()}")
            }
            startActivity(dialIntent)
        } catch (_: Exception) {
            Toast.makeText(this, "تعذر فتح تطبيق الهاتف", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(phone: String) {
        try {
            val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "تعذر فتح تطبيق واتساب", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMapDirections(lat: Double, lon: Double, name: String) {
        try {
            val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($name)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (_: Exception) {
            try {
                val webUri = Uri.parse("https://maps.google.com/?q=$lat,$lon")
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (_: Exception) {
                Toast.makeText(this, "تعذر فتح الخرائط", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareCraftsmanProfile(item: com.example.data.model.CraftsmanItem) {
        try {
            val text = "تطبيق دوّرني 🇱🇾\nبيانات الحرفي: ${item.craftsman.name}\nالمهنة: ${item.craftsman.craft}\nالمدينة والحي: ${item.craftsman.city} (${item.craftsman.district})\nالهاتف: ${item.craftsman.phone}\nالواتساب: ${item.craftsman.whatsapp}"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "مشاركة بيانات الحرفي"))
        } catch (_: Exception) {
            Toast.makeText(this, "تعذر مشاركة البيانات", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HirafiViewModel,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onOpenMap: (Double, Double, String) -> Unit,
    onShare: (com.example.data.model.CraftsmanItem) -> Unit
) {
    val language by viewModel.language.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val unreadNotifCount by viewModel.unreadNotificationsCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val selectedCraftsmanItem by viewModel.selectedCraftsmanForDetail.collectAsState()
    val craftsmanReviews by viewModel.craftsmanReviews.collectAsState()
    val craftsmanPortfolio by viewModel.craftsmanPortfolio.collectAsState()

    val isRatingDialogOpen by viewModel.isRatingDialogOpen.collectAsState()
    val ratingTargetCraftsman by viewModel.ratingTargetCraftsman.collectAsState()

    val isRequestDialogOpen by viewModel.isRequestDialogOpen.collectAsState()
    val requestTargetCraftsman by viewModel.requestTargetCraftsman.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    val isNotificationPreferencesOpen by viewModel.isNotificationPreferencesDialogOpen.collectAsState()
    val notificationPreferences by viewModel.notificationPreferences.collectAsState()
    val isAddPortfolioOpen by viewModel.isAddPortfolioDialogOpen.collectAsState()

    var isNotificationDrawerOpen by remember { mutableStateOf(false) }
    var isLocationPickerOpen by remember { mutableStateOf(false) }
    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    val myProfile by viewModel.myCraftsmanProfile.collectAsState()

    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val notifSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val chatSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeChatThread by viewModel.activeChatThread.collectAsState()
    val isConversationsListOpen by viewModel.isConversationsListOpen.collectAsState()
    val chatThreads by viewModel.chatThreads.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            // Distinctive Top Navigation Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Brand Title with Libyan Flag
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmberDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handyman,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Localization.get("app_name", language),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "🇱🇾", fontSize = 16.sp)
                                }
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "دليل الحرفيين والزبائن المباشر" else "Craftsmen & Clients Directory",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Actions: Theme Toggle, Preferences, Language, Notifications
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isDark by viewModel.isDarkMode.collectAsState()
                            val isSystemDark = isSystemInDarkTheme()
                            val isDarkActive = isDark ?: isSystemDark

                            // Live 90 FPS Refresh Rate Indicator Badge
                            FpsDisplayBadge(language = language)

                            Spacer(modifier = Modifier.width(4.dp))

                            // Direct Firestore Chat Threads Inbox
                            IconButton(
                                onClick = { viewModel.isConversationsListOpen.value = true },
                                modifier = Modifier.testTag("chat_inbox_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (chatThreads.isNotEmpty()) {
                                            Badge(containerColor = AmberDark) {
                                                Text(text = chatThreads.size.toString(), color = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Chat,
                                        contentDescription = "المحادثات المباشرة",
                                        tint = AmberDark
                                    )
                                }
                            }

                            // Dark / Light Mode Switcher
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDarkActive) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = if (isDarkActive) "تفعيل الوضع الفاتح" else "تفعيل الوضع الداكن",
                                    tint = AmberDark
                                )
                            }

                            // Notification Preferences
                            IconButton(
                                onClick = { viewModel.isNotificationPreferencesDialogOpen.value = true },
                                modifier = Modifier.testTag("notification_preferences_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = "تخصيص الإشعارات",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Language switcher chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.toggleLanguage() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("language_toggle_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Language,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = AmberDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "English" else "عربي",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Notifications Bell with unread counter
                            IconButton(
                                onClick = { isNotificationDrawerOpen = true },
                                modifier = Modifier.testTag("notifications_bell_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifCount > 0) {
                                            Badge(containerColor = RedEmergency) {
                                                Text(text = unreadNotifCount.toString(), color = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = Localization.get("notifications_title", language),
                                        tint = if (unreadNotifCount > 0) AmberDark else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Mode Switcher Pill Row: [قسم العميل المستهلك | قسم العامل الحرفي]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Client Mode Pill
                        val isClient = currentMode == AppMode.CLIENT
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isClient) AmberDark else Color.Transparent)
                                .clickable { viewModel.setAppMode(AppMode.CLIENT) }
                                .padding(vertical = 8.dp)
                                .testTag("switch_to_client_mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = if (isClient) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.get("client_section", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isClient) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Craftsman Mode Pill
                        val isCraftsman = currentMode == AppMode.CRAFTSMAN
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCraftsman) AmberDark else Color.Transparent)
                                .clickable { viewModel.setAppMode(AppMode.CRAFTSMAN) }
                                .padding(vertical = 8.dp)
                                .testTag("switch_to_craftsman_mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Handyman,
                                    contentDescription = null,
                                    tint = if (isCraftsman) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.get("craftsman_section", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isCraftsman) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentMode) {
                AppMode.CLIENT -> {
                    ClientHomeScreen(
                        viewModel = viewModel,
                        onCallClick = onCall,
                        onWhatsAppClick = onWhatsApp,
                        onOpenLocationPicker = { isLocationPickerOpen = true }
                    )
                }

                AppMode.CRAFTSMAN -> {
                    CraftsmanDashboardScreen(
                        viewModel = viewModel,
                        onEditProfileClick = { isEditProfileDialogOpen = true }
                    )
                }
            }

            // Craftsman Profile Detail BottomSheet
            selectedCraftsmanItem?.let { item ->
                CraftsmanDetailSheet(
                    item = item,
                    reviews = craftsmanReviews,
                    portfolio = craftsmanPortfolio,
                    language = language,
                    sheetState = detailSheetState,
                    onDismiss = { viewModel.closeCraftsmanDetail() },
                    onCallClick = onCall,
                    onWhatsAppClick = onWhatsApp,
                    onOpenMapClick = onOpenMap,
                    onShareClick = onShare,
                    onAddReviewClick = {
                        viewModel.openRatingDialog(item)
                    },
                    onRequestServiceClick = {
                        viewModel.openRequestDialog(item)
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onChatClick = {
                        viewModel.closeCraftsmanDetail()
                        viewModel.openChatForCraftsman(item)
                    }
                )
            }

            // Rating Dialog (User requested in-app multi-criteria review system)
            if (isRatingDialogOpen && ratingTargetCraftsman != null) {
                RatingDialog(
                    targetItem = ratingTargetCraftsman!!,
                    language = language,
                    onDismiss = { viewModel.closeRatingDialog() },
                    onSubmit = { quality, punctuality, communication, comment, clientName ->
                        viewModel.submitRating(quality, punctuality, communication, comment, clientName)
                    }
                )
            }

            // New Service Request Dialog
            if (isRequestDialogOpen && requestTargetCraftsman != null) {
                val defaultLocName = if (language == AppLanguage.ARABIC) currentLocation.nameAr else currentLocation.nameEn
                NewRequestDialog(
                    targetItem = requestTargetCraftsman!!,
                    defaultLocation = defaultLocName,
                    language = language,
                    onDismiss = { viewModel.closeRequestDialog() },
                    onSubmit = { desc, clientName, clientPhone, loc, isEmerg ->
                        viewModel.submitServiceRequest(desc, clientName, clientPhone, loc, isEmerg)
                    }
                )
            }

            // Real-Time Notification History Drawer
            if (isNotificationDrawerOpen) {
                NotificationDrawer(
                    notifications = notifications,
                    language = language,
                    sheetState = notifSheetState,
                    onDismiss = { isNotificationDrawerOpen = false },
                    onMarkRead = { viewModel.markNotificationRead(it) },
                    onMarkAllRead = { viewModel.markAllNotificationsRead() }
                )
            }

            // Edit Craftsman Profile Dialog
            if (isEditProfileDialogOpen && myProfile != null) {
                EditCraftsmanProfileDialog(
                    profile = myProfile!!,
                    language = language,
                    onDismiss = { isEditProfileDialogOpen = false },
                    onSave = { updated ->
                        viewModel.saveMyProfile(updated)
                        isEditProfileDialogOpen = false
                    }
                )
            }

            // Location Picker Dialog (Libyan cities and districts)
            if (isLocationPickerOpen) {
                LocationPickerDialog(
                    presetLocations = viewModel.presetLocations,
                    selectedLocation = currentLocation,
                    language = language,
                    onDismiss = { isLocationPickerOpen = false },
                    onSelectLocation = { viewModel.setLocation(it) }
                )
            }

            // Notification Preferences Dialog
            if (isNotificationPreferencesOpen) {
                NotificationPreferencesDialog(
                    currentPreferences = notificationPreferences,
                    onSave = { viewModel.updateNotificationPreferences(it) },
                    onDismiss = { viewModel.isNotificationPreferencesDialogOpen.value = false }
                )
            }

            // Add Portfolio Item Dialog (Craftsman)
            if (isAddPortfolioOpen && myProfile != null) {
                AddPortfolioItemDialog(
                    craftName = myProfile!!.craft,
                    onSave = { title, desc, uri, type, cost, date ->
                        viewModel.addPortfolioItem(title, desc, uri, type, cost, date)
                    },
                    onDismiss = { viewModel.isAddPortfolioDialogOpen.value = false }
                )
            }

            // Direct Firestore Coordination Conversations BottomSheet
            if (isConversationsListOpen) {
                ConversationsListDialog(
                    threads = chatThreads,
                    language = language,
                    sheetState = chatSheetState,
                    onDismiss = { viewModel.isConversationsListOpen.value = false },
                    onSelectThread = { thread ->
                        viewModel.openChatThread(thread)
                    }
                )
            }

            // Fullscreen Real-Time Direct Chat Screen (with BackHandler for seamless navigation)
            activeChatThread?.let { thread ->
                BackHandler {
                    viewModel.closeActiveChat()
                }
                ChatScreen(
                    thread = thread,
                    viewModel = viewModel,
                    language = language,
                    onBackClick = { viewModel.closeActiveChat() },
                    onCallClick = onCall
                )
            }
        }
    }
}
