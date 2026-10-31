package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CraftsmanEntity
import com.example.ui.theme.AmberDark
import com.example.ui.theme.RedEmergency
import com.example.util.AppLanguage
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCraftsmanProfileDialog(
    profile: CraftsmanEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (CraftsmanEntity) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var craft by remember { mutableStateOf(profile.craft) }
    var experienceYears by remember { mutableIntStateOf(profile.experienceYears) }
    var phone by remember { mutableStateOf(profile.phone) }
    var whatsapp by remember { mutableStateOf(profile.whatsapp) }
    var city by remember { mutableStateOf(profile.city) }
    var district by remember { mutableStateOf(profile.district) }
    var bio by remember { mutableStateOf(profile.bio) }
    var priceEstimate by remember { mutableStateOf(profile.priceEstimate) }
    var isAvailable by remember { mutableStateOf(profile.isAvailable) }
    var hasEmergencyService by remember { mutableStateOf(profile.hasEmergencyService) }

    val craftOptions = listOf(
        "سباكة", "كهرباء", "تكييف وتبريد", "نجارة",
        "دهان وديكور", "صيانة أجهزة منزلية", "حدادة وألمنيوم", "سيراميك وبلاط"
    )
    var craftMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.get("edit_profile", language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Localization.get("craftsman_name", language)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("craftsman_edit_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = craftMenuExpanded,
                    onExpandedChange = { craftMenuExpanded = !craftMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = craft,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.get("craftsman_profession", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = craftMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = craftMenuExpanded,
                        onDismissRequest = { craftMenuExpanded = false }
                    ) {
                        craftOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    craft = opt
                                    craftMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = experienceYears.toString(),
                        onValueChange = { experienceYears = it.toIntOrNull() ?: 0 },
                        label = { Text(Localization.get("exp_years", language)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(Localization.get("craftsman_city", language)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(Localization.get("craftsman_district", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Localization.get("craftsman_phone", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text(Localization.get("craftsman_whatsapp", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceEstimate,
                    onValueChange = { priceEstimate = it },
                    label = { Text(Localization.get("craftsman_pricing", language)) },
                    placeholder = { Text("مثال: كشف يبدأ من 35 د.ل") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(Localization.get("craftsman_bio", language)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = hasEmergencyService,
                        onCheckedChange = { hasEmergencyService = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RedEmergency)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(Localization.get("toggle_emergency", language), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = profile.copy(
                        name = name.ifBlank { profile.name },
                        craft = craft,
                        experienceYears = experienceYears,
                        phone = phone,
                        whatsapp = whatsapp,
                        city = city,
                        district = district,
                        bio = bio,
                        priceEstimate = priceEstimate,
                        isAvailable = isAvailable,
                        hasEmergencyService = hasEmergencyService
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text(Localization.get("save_profile", language), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Localization.get("cancel", language))
            }
        }
    )
}
