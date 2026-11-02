package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberDark
import com.example.ui.theme.GreenAvailable
import com.example.util.AppLanguage
import com.example.util.FrameRateHelper

@Composable
fun FpsDisplayBadge(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentFps by FrameRateHelper.currentFps.collectAsState()
    val activeTarget by FrameRateHelper.activeTargetFps.collectAsState()
    val is90Active by FrameRateHelper.is90FpsActive.collectAsState()
    var showFpsDialog by remember { mutableStateOf(false) }

    // Live 90 FPS Badge
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (is90Active) GreenAvailable.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { showFpsDialog = true }
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("fps_indicator_badge"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (currentFps >= 85) GreenAvailable else AmberDark)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$currentFps FPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (is90Active) GreenAvailable else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (showFpsDialog) {
        val activity = context as? Activity
        FpsControlDialog(
            currentFps = currentFps,
            activeTarget = activeTarget,
            language = language,
            onDismiss = { showFpsDialog = false },
            onSelectTarget = { target ->
                if (activity != null) {
                    FrameRateHelper.applyFrameRate(activity, target)
                }
            }
        )
    }
}

@Composable
fun FpsControlDialog(
    currentFps: Int,
    activeTarget: FrameRateHelper.TargetFps,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelectTarget: (FrameRateHelper.TargetFps) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = GreenAvailable,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.ARABIC) "معدل الإطارات (90 FPS)" else "Frame Rate Settings (90 FPS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live stats card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = GreenAvailable.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "معدل التحديث الفعلي الآن" else "Live Active FPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currentFps FPS",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenAvailable
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "تسريع العتاد GPU:" else "GPU Acceleration:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "مفعّل 100% ⚡",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.ARABIC) "اختر وضع التردد المفضل للشاشة:" else "Select target refresh rate:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                FrameRateHelper.TargetFps.values().forEach { target ->
                    val isSelected = target == activeTarget
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectTarget(target) },
                        color = if (isSelected) GreenAvailable.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) target.labelAr else target.labelEn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) GreenAvailable else MaterialTheme.colorScheme.onSurface
                                )
                                if (target == FrameRateHelper.TargetFps.FPS_90) {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "الموصى به لتجربة فائقة السلاسة والاستجابة" else "Recommended for ultra-smooth experience",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = GreenAvailable,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GreenAvailable)
            ) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "تم" else "Done",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
