package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoomEntity
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

val SUPPORTED_CURRENCIES = listOf(
    "Rs." to "NPR / INR (Rs.)",
    "₹" to "INR (₹)",
    "$" to "USD ($)",
    "€" to "EUR (€)",
    "£" to "GBP (£)",
    "AED" to "AED (د.إ)",
    "₱" to "PHP (₱)",
    "৳" to "BDT (৳)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentCurrency: String,
    themeMode: String,
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    onSelectCurrency: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onManageRooms: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> String,
    onRestoreJson: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Preferences, Rooms & Offline Data",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PREFERENCES
        SectionHeader(title = "Display & Currency")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                SettingsListItem(
                    icon = Icons.Default.CurrencyExchange,
                    title = "Currency Symbol",
                    subtitle = "Current: $currentCurrency",
                    onClick = { showCurrencyDialog = true },
                    testTag = "settings_currency_item"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SettingsListItem(
                    icon = Icons.Default.DarkMode,
                    title = "App Theme",
                    subtitle = when (themeMode) {
                        "DARK" -> "Dark Mode"
                        "LIGHT" -> "Light Mode"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true },
                    testTag = "settings_theme_item"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ROOM & PROPERTY MANAGEMENT
        SectionHeader(title = "Rooms & Properties")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                SettingsListItem(
                    icon = Icons.Default.HomeWork,
                    title = "Manage Rooms",
                    subtitle = "${allRooms.size} room(s) configured • Active: ${activeRoom?.name ?: "None"}",
                    onClick = onManageRooms,
                    testTag = "settings_manage_rooms_item"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DATA EXPORT & BACKUP (OFFLINE FIRST)
        SectionHeader(
            title = "Data Storage & Backup",
            subtitle = "Offline-first local database. Export or transfer anytime."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                SettingsListItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export to CSV Spreadsheet",
                    subtitle = "Share or backup all monthly records in CSV format",
                    onClick = onExportCsv,
                    testTag = "settings_export_csv_item"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SettingsListItem(
                    icon = Icons.Default.Backup,
                    title = "Backup Data (JSON)",
                    subtitle = "Save a complete offline backup of all rooms & history",
                    onClick = { showBackupDialog = true },
                    testTag = "settings_backup_json_item"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SettingsListItem(
                    icon = Icons.Default.Restore,
                    title = "Restore Data (JSON)",
                    subtitle = "Restore database from a previously copied JSON backup",
                    onClick = {
                        restoreInputText = ""
                        showRestoreDialog = true
                    },
                    testTag = "settings_restore_json_item"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // APP INFO
        SectionHeader(title = "About App")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Room Rent & Utility History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version 1.0 • Offline-First Native Android App",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Designed for room renters and landlords to effortlessly track rent, electricity & water meter units, waste charges, unit rates, and complete monthly history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    SUPPORTED_CURRENCIES.forEach { (symbol, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCurrency(symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentCurrency == symbol,
                                onClick = {
                                    onSelectCurrency(symbol)
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose App Theme") },
            text = {
                Column {
                    listOf("SYSTEM" to "System Default", "LIGHT" to "Light Mode", "DARK" to "Dark Mode").forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    onSelectTheme(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        val backupJson = remember { onExportJson() }
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup Database") },
            text = {
                Column {
                    Text(
                        text = "Your rooms and monthly records are compiled below. You can copy this JSON or save it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = backupJson,
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(backupJson))
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore From Backup") },
            text = {
                Column {
                    Text(
                        text = "Paste your previously exported JSON backup below. Valid rooms and records will be imported.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreInputText,
                        onValueChange = { restoreInputText = it },
                        placeholder = { Text("Paste JSON here...") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onRestoreJson(restoreInputText)
                        if (success) {
                            Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                            showRestoreDialog = false
                        } else {
                            Toast.makeText(context, "Invalid backup JSON format", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    enabled = restoreInputText.isNotBlank()
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
