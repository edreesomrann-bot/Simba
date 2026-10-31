package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.CraftsmanEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.ServiceRequestEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GreenAvailable
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.RedEmergency
import com.example.ui.viewmodel.HirafiViewModel
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun CraftsmanDashboardScreen(
    viewModel: HirafiViewModel,
    onEditProfileClick: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val myProfile by viewModel.myCraftsmanProfile.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val filteredCraftsmen by viewModel.filteredCraftsmen.collectAsState()

    var dashboardTab by remember { mutableIntStateOf(0) } // 0: Requests, 1: VIP Plan, 2: Ratings & Reviews

    val profile = myProfile ?: return
    // Find my computed rating item if present
    val myItem = filteredCraftsmen.firstOrNull { it.craftsman.id == profile.id || it.craftsman.isRegisteredByMe }
    val myStats = myItem?.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("craftsman_dashboard_screen")
    ) {
        // Craftsman Profile Summary Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(AmberContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handyman,
                                contentDescription = null,
                                tint = OnAmberContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (profile.isVipFeatured) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(AmberLight, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "👑 VIP",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${profile.craft} • ${profile.city} (${profile.district})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "📞 ${profile.phone}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.get("edit_profile", language), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Availability toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.get("toggle_availability", language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (profile.isAvailable)
                                Localization.get("status_available", language)
                            else
                                Localization.get("status_busy", language),
                            color = if (profile.isAvailable) GreenAvailable else Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = profile.isAvailable,
                        onCheckedChange = { viewModel.updateMyAvailability(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenAvailable
                        ),
                        modifier = Modifier.testTag("availability_toggle_switch")
                    )
                }
            }
        }

        // Dashboard Tabs
        TabRow(
            selectedTabIndex = dashboardTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AmberDark
        ) {
            Tab(
                selected = dashboardTab == 0,
                onClick = { dashboardTab = 0 },
                text = { Text(Localization.get("incoming_requests", language), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = dashboardTab == 1,
                onClick = { dashboardTab = 1 },
                text = { Text(if (language == AppLanguage.ARABIC) "باقة التميز (100 د.ل) 👑" else "VIP Priority (100 LYD) 👑", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = dashboardTab == 2,
                onClick = { dashboardTab = 2 },
                text = { Text(Localization.get("my_reviews_title", language), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        when (dashboardTab) {
            0 -> {
                // Incoming Real-Time Requests from Nearby Clients
                val incoming = allRequests.filter { it.craftsmanId == profile.id || profile.isRegisteredByMe }
                if (incoming.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.get("no_incoming_requests", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(incoming) { req ->
                            IncomingRequestCard(
                                req = req,
                                language = language,
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateRequestStatus(
                                        requestId = req.id,
                                        newStatus = newStatus,
                                        craftsmanName = profile.name,
                                        clientName = req.clientName
                                    )
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // VIP Priority Subscription Section (100 LYD / month)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberContainer)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = OnAmberContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = Localization.get("vip_title", language),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = OnAmberContainer
                                    )
                                    Text(
                                        text = Localization.get("vip_price", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = Localization.get("vip_desc", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnAmberContainer,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Benefits list
                            val benefits = listOf(
                                "ظهور دائم في المركز الأول بنتائج البحث والتصفح في مدينتك",
                                "علامة التميز الذهبية 👑 بجانب اسمك لزيادة ثقة العملاء",
                                "أولوية التنبيهات للطلبات القريبة وعروض العمل الطارئة",
                                "بدون أي إعلانات مزعجة وبدعم فني مباشر"
                            )
                            benefits.forEach { b ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = AmberDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = b,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnAmberContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { viewModel.toggleVipSubscription() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("vip_toggle_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (profile.isVipFeatured) GreenAvailable else AmberDark
                                )
                            ) {
                                Text(
                                    text = if (profile.isVipFeatured)
                                        Localization.get("vip_active", language)
                                    else
                                        Localization.get("vip_upgrade", language),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Craftsman's Ratings & Reviews Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Localization.get("ratings_reviews", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val avg = myStats?.averageOverall ?: 4.9f
                            val count = myStats?.reviewCount ?: 4
                            val q = myStats?.averageQuality ?: 5.0f
                            val p = myStats?.averagePunctuality ?: 4.8f
                            val c = myStats?.averageCommunication ?: 4.9f

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(end = 20.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", avg),
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberDark
                                    )
                                    Row {
                                        for (i in 1..5) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = AmberLight,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$count ${Localization.get("review_count", language)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    CriterionProgressBar(
                                        label = Localization.get("quality_rating", language),
                                        score = q
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CriterionProgressBar(
                                        label = Localization.get("punctuality_rating", language),
                                        score = p
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CriterionProgressBar(
                                        label = Localization.get("communication_rating", language),
                                        score = c
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomingRequestCard(
    req: ServiceRequestEntity,
    language: AppLanguage,
    onUpdateStatus: (String) -> Unit
) {
    val statusColor = when (req.status) {
        ServiceRequestEntity.STATUS_PENDING -> AmberDark
        ServiceRequestEntity.STATUS_ACCEPTED -> BlueAccent
        ServiceRequestEntity.STATUS_ON_THE_WAY -> AmberLight
        ServiceRequestEntity.STATUS_IN_PROGRESS -> BlueAccent
        ServiceRequestEntity.STATUS_COMPLETED -> GreenAvailable
        ServiceRequestEntity.STATUS_DECLINED -> RedEmergency
        else -> Color.Gray
    }

    val statusText = when (req.status) {
        ServiceRequestEntity.STATUS_PENDING -> Localization.get("status_pending", language)
        ServiceRequestEntity.STATUS_ACCEPTED -> Localization.get("status_accepted", language)
        ServiceRequestEntity.STATUS_ON_THE_WAY -> Localization.get("status_on_the_way", language)
        ServiceRequestEntity.STATUS_IN_PROGRESS -> Localization.get("status_in_progress", language)
        ServiceRequestEntity.STATUS_COMPLETED -> Localization.get("status_completed", language)
        ServiceRequestEntity.STATUS_DECLINED -> Localization.get("status_declined", language)
        else -> req.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("incoming_request_card_${req.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = req.clientName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (req.isEmergency) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(RedEmergency.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🚨 " + Localization.get("is_urgent_emergency", language),
                                color = RedEmergency,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

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
                if (req.clientPhone.isNotBlank()) {
                    Text(
                        text = "📞 ${req.clientPhone}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons to Progress the Job
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (req.status) {
                    ServiceRequestEntity.STATUS_PENDING -> {
                        Button(
                            onClick = { onUpdateStatus(ServiceRequestEntity.STATUS_ACCEPTED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.get("accept_request", language), fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus(ServiceRequestEntity.STATUS_DECLINED) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Localization.get("decline_btn", language), fontSize = 11.sp, color = RedEmergency)
                        }
                    }

                    ServiceRequestEntity.STATUS_ACCEPTED -> {
                        Button(
                            onClick = { onUpdateStatus(ServiceRequestEntity.STATUS_ON_THE_WAY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                        ) {
                            Icon(Icons.Filled.TwoWheeler, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.get("on_way_btn", language), fontSize = 11.sp)
                        }
                    }

                    ServiceRequestEntity.STATUS_ON_THE_WAY -> {
                        Button(
                            onClick = { onUpdateStatus(ServiceRequestEntity.STATUS_IN_PROGRESS) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                        ) {
                            Icon(Icons.Filled.Handyman, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.get("start_work_btn", language), fontSize = 11.sp)
                        }
                    }

                    ServiceRequestEntity.STATUS_IN_PROGRESS -> {
                        Button(
                            onClick = { onUpdateStatus(ServiceRequestEntity.STATUS_COMPLETED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenAvailable)
                        ) {
                            Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.get("complete_work_btn", language), fontSize = 11.sp)
                        }
                    }

                    ServiceRequestEntity.STATUS_COMPLETED -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GreenAvailable.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✅ تم إنجاز العمل وتسليمه بنجاح",
                                color = GreenAvailable,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    ServiceRequestEntity.STATUS_DECLINED -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RedEmergency.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "تم الاعتذار عن الطلب",
                                color = RedEmergency,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
