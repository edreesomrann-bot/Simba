package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CraftsmanItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.GreenAvailable
import com.example.ui.theme.RedEmergency
import com.example.ui.theme.SlateNavyDark
import com.example.ui.theme.SlateNavyMedium
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.ClientLocation
import com.example.util.AppLanguage
import com.example.util.Localization
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarMapVisualizer(
    craftsmen: List<CraftsmanItem>,
    currentLocation: ClientLocation,
    selectedRadius: Double,
    language: AppLanguage,
    onRadiusSelected: (Double) -> Unit,
    onCraftsmanClick: (CraftsmanItem) -> Unit,
    onCallClick: (String) -> Unit,
    onWhatsAppClick: (String) -> Unit,
    onRequestClick: (CraftsmanItem) -> Unit
) {
    var selectedItemOnMap by remember { mutableStateOf<CraftsmanItem?>(craftsmen.firstOrNull()) }

    // Pulse animation
    val transition = rememberInfiniteTransition(label = "radarPulse")
    val pulseProgress by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Radius Selector Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("radius", language) + ":",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            val radiusOptions = listOf(5.0, 10.0, 25.0, 0.0)
            radiusOptions.forEach { r ->
                val isSelected = selectedRadius == r
                val label = if (r == 0.0) {
                    Localization.get("all_distances", language)
                } else {
                    "${r.toInt()} ${Localization.get("km_away", language).take(2)}"
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { onRadiusSelected(r) },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.testTag("radius_chip_${r.toInt()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Radar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(SlateNavyDark, shape = RoundedCornerShape(20.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(craftsmen) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRadiusPx = (size.width.coerceAtMost(size.height) / 2f) - 24.dp.toPx()
                            val viewMaxKm = if (selectedRadius > 0) selectedRadius else 30.0

                            // Find nearest craftsman to tap
                            var closest: CraftsmanItem? = null
                            var minDistancePx = 50.dp.toPx() // Touch slop

                            craftsmen.forEach { item ->
                                val distFraction = (item.distanceKm / viewMaxKm).coerceIn(0.05, 1.0)
                                val dLat = item.craftsman.latitude - currentLocation.latitude
                                val dLon = item.craftsman.longitude - currentLocation.longitude
                                val angle = atan2(dLat, dLon)

                                val x = center.x + (cos(angle) * distFraction * maxRadiusPx).toFloat()
                                val y = center.y - (sin(angle) * distFraction * maxRadiusPx).toFloat()

                                val tapDist = (Offset(x, y) - tapOffset).getDistance()
                                if (tapDist < minDistancePx) {
                                    minDistancePx = tapDist
                                    closest = item
                                }
                            }

                            if (closest != null) {
                                selectedItemOnMap = closest
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadiusPx = (size.width.coerceAtMost(size.height) / 2f) - 24.dp.toPx()
                val viewMaxKm = if (selectedRadius > 0) selectedRadius else 30.0

                // Draw radar background circles
                val ringFractions = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                ringFractions.forEach { frac ->
                    drawCircle(
                        color = Color(0xFF334155),
                        radius = maxRadiusPx * frac,
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Crosshairs
                drawLine(
                    color = Color(0x3364748B),
                    start = Offset(center.x - maxRadiusPx, center.y),
                    end = Offset(center.x + maxRadiusPx, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0x3364748B),
                    start = Offset(center.x, center.y - maxRadiusPx),
                    end = Offset(center.x, center.y + maxRadiusPx),
                    strokeWidth = 1.dp.toPx()
                )

                // Animated Pulse Wave
                drawCircle(
                    color = AmberLight.copy(alpha = (1f - pulseProgress) * 0.4f),
                    radius = maxRadiusPx * pulseProgress,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // User Center Pin (Glowing Blue)
                drawCircle(
                    color = BlueAccent.copy(alpha = 0.35f),
                    radius = 16.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = BlueAccent,
                    radius = 7.dp.toPx(),
                    center = center
                )

                // Draw Craftsmen Pins
                craftsmen.forEach { item ->
                    val isSelected = selectedItemOnMap?.craftsman?.id == item.craftsman.id
                    val distFraction = (item.distanceKm / viewMaxKm).coerceIn(0.08, 0.96)
                    val dLat = item.craftsman.latitude - currentLocation.latitude
                    val dLon = item.craftsman.longitude - currentLocation.longitude
                    val angle = atan2(dLat, dLon)

                    val x = center.x + (cos(angle) * distFraction * maxRadiusPx).toFloat()
                    val y = center.y - (sin(angle) * distFraction * maxRadiusPx).toFloat()
                    val pinCenter = Offset(x, y)

                    val pinColor = when {
                        item.craftsman.hasEmergencyService -> RedEmergency
                        item.craftsman.isAvailable -> GreenAvailable
                        else -> AmberLight
                    }

                    if (isSelected) {
                        // Halo around selected pin
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = 14.dp.toPx(),
                            center = pinCenter,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    drawCircle(
                        color = pinColor,
                        radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                        center = pinCenter
                    )
                }
            }

            // Radar Status & Location Tag overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(SlateNavyMedium.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.NearMe,
                        contentDescription = null,
                        tint = BlueAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) currentLocation.nameAr else currentLocation.nameEn,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Legend overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(SlateNavyMedium.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${craftsmen.size} ${Localization.get("craftsmen_found", language)}",
                    color = AmberLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Selected Pin Preview Card
        selectedItemOnMap?.let { selected ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radar_selected_craftsman_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selected.craftsman.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (selected.craftsman.isAvailable) GreenAvailable.copy(alpha = 0.15f)
                                            else Color.Gray.copy(alpha = 0.15f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (selected.craftsman.isAvailable)
                                            Localization.get("status_available", language)
                                        else
                                            Localization.get("status_busy", language),
                                        color = if (selected.craftsman.isAvailable) GreenAvailable else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${selected.craftsman.craft} • ${selected.craftsman.district}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Distance badge
                        Box(
                            modifier = Modifier
                                .background(AmberDark, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${selected.distanceKm} ${Localization.get("km_away", language).take(2)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", selected.stats.averageOverall),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = " (${selected.stats.reviewCount} ${Localization.get("review_count", language)})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${selected.craftsman.experienceYears} ${Localization.get("exp_years", language)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direct Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCallClick(selected.craftsman.phone) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.get("call_direct", language), fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onWhatsAppClick(selected.craftsman.whatsapp) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Text(Localization.get("whatsapp_chat", language), fontSize = 12.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { onCraftsmanClick(selected) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "الملف" else "Profile",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
