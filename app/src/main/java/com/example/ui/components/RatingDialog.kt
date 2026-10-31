package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CraftsmanItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.util.AppLanguage
import com.example.util.Localization

@Composable
fun RatingDialog(
    targetItem: CraftsmanItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (quality: Float, punctuality: Float, communication: Float, comment: String, clientName: String) -> Unit
) {
    var qualityRating by remember { mutableFloatStateOf(5.0f) }
    var punctualityRating by remember { mutableFloatStateOf(5.0f) }
    var communicationRating by remember { mutableFloatStateOf(5.0f) }
    var clientName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    val overallAvg = (qualityRating + punctualityRating + communicationRating) / 3f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = Localization.get("rate_craftsman_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${targetItem.craftsman.name} (${targetItem.craftsman.craft})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Average summary preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.get("average_rating", language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f / 5.0", overallAvg),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Criterion 1: Quality
                CriteriaRatingRow(
                    label = Localization.get("quality_rating", language),
                    rating = qualityRating,
                    onRatingChanged = { qualityRating = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Criterion 2: Punctuality
                CriteriaRatingRow(
                    label = Localization.get("punctuality_rating", language),
                    rating = punctualityRating,
                    onRatingChanged = { punctualityRating = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Criterion 3: Communication
                CriteriaRatingRow(
                    label = Localization.get("communication_rating", language),
                    rating = communicationRating,
                    onRatingChanged = { communicationRating = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text(Localization.get("your_name", language)) },
                    placeholder = { Text(if (language == AppLanguage.ARABIC) "مثال: عبد الله" else "e.g. John") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(Localization.get("review_comment_hint", language)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_comment_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(qualityRating, punctualityRating, communicationRating, comment, clientName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Text(Localization.get("submit_review", language), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_review_button")
            ) {
                Text(Localization.get("cancel", language))
            }
        }
    )
}

@Composable
fun CriteriaRatingRow(
    label: String,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%.0f/5", rating),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val isSelected = i <= rating
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "$label: $i",
                    tint = if (isSelected) AmberLight else Color.LightGray,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(2.dp)
                        .clickable { onRatingChanged(i.toFloat()) }
                )
            }
        }
    }
}
