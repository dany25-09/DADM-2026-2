package com.example.reto_9.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reto_9.data.model.PoiCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentRadiusKm: Float,
    currentCategory: PoiCategory,
    onRadiusChanged: (Float) -> Unit,
    onCategoryChanged: (PoiCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var tempRadius by remember { mutableFloatStateOf(currentRadiusKm) }
    var tempCategory by remember { mutableStateOf(currentCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
        title = { Text(text = "Configuración de Búsqueda (Preferencias)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radio in Kilometers
                Text(
                    text = "Radio de búsqueda: %.1f km".format(tempRadius),
                    style = MaterialTheme.typography.titleMedium
                )

                Slider(
                    value = tempRadius,
                    onValueChange = { tempRadius = it },
                    valueRange = 0.5f..15.0f,
                    steps = 28 // 0.5 km increments
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0.5 km", style = MaterialTheme.typography.bodySmall)
                    Text(text = "7.5 km", style = MaterialTheme.typography.bodySmall)
                    Text(text = "15 km", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                // Category Filter
                Text(
                    text = "Categoría preferida:",
                    style = MaterialTheme.typography.titleMedium
                )

                PoiCategory.entries.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tempCategory == category,
                            onClick = { tempCategory = category }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${category.iconEmoji} ${category.displayName}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRadiusChanged(tempRadius)
                    onCategoryChanged(tempCategory)
                    onDismiss()
                }
            ) {
                Text("Guardar Preferencias")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
