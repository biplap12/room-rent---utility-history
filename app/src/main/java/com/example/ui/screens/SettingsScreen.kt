package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.RoomEntity
import com.example.security.AppLockConfig
import com.example.security.AppLockSettingsSection
import com.example.ui.components.SectionHeader
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val SUPPORTED_CURRENCIES = listOf(
    "रु" to "NPR (रु)",
    "₹" to "INR (₹)",
    "$" to "USD ($)",
    "€" to "EUR (€)",
    "£" to "GBP (£)",
    "AED" to "AED (د.إ)",
    "₱" to "PHP (₱)",
    "৳" to "BDT (৳)"
)

private const val FORGOT_PIN_REQUIRED_TAPS = 10
private const val FORGOT_PIN_WINDOW_MS = 2_000L

/**
 * Creates a temporary JSON backup file inside cacheDir/backups.
 */
private fun createBackupFile(
    context: Context,
    json: String
): File {

    val timestamp = SimpleDateFormat(
        "yyyy-MM-dd_HH-mm-ss",
        Locale.US
    ).format(Date())

    val backupDirectory = File(
        context.cacheDir,
        "backups"
    )

    if (!backupDirectory.exists()) {
        backupDirectory.mkdirs()
    }

    val backupFile = File(
        backupDirectory,
        "RoomRent_Backup_$timestamp.json"
    )

    backupFile.writeText(
        json,
        Charsets.UTF_8
    )

    return backupFile
}

/**
 * Locates the active Room database file (.db) stored in the app's
 * private databases directory.
 */
private fun findRoomDatabaseFile(
    context: Context
): File? {

    return try {

        val databasesDir = File(
            context.applicationInfo.dataDir,
            "databases"
        )

        if (!databasesDir.exists()) {
            return null
        }

        databasesDir.listFiles()
            ?.firstOrNull { file ->

                file.isFile &&
                        file.name.endsWith(".db") &&
                        !file.name.contains("-wal") &&
                        !file.name.contains("-shm")
            }

    } catch (e: Exception) {

        null
    }
}

/**
 * Shares the JSON backup through Android's native share sheet.
 */
private fun shareBackupJson(
    context: Context,
    json: String
) {
    try {

        val backupFile = createBackupFile(
            context = context,
            json = json
        )

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )

        val shareIntent = Intent(
            Intent.ACTION_SEND
        ).apply {

            type = "application/json"

            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val chooser = Intent.createChooser(
            shareIntent,
            "Share JSON Backup"
        )

        context.startActivity(chooser)

    } catch (e: ActivityNotFoundException) {

        Toast.makeText(
            context,
            "No app is available to share the backup",
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {

        Toast.makeText(
            context,
            "Unable to share backup: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * Copies the Room .db file into cacheDir/backups and shares it.
 */
private fun shareDatabaseFile(context: Context) {
    try {
        val dbFile = findRoomDatabaseFile(context)

        if (dbFile == null || !dbFile.exists()) {
            Toast.makeText(context, "Database file not found", Toast.LENGTH_LONG).show()
            return
        }

        val backupDirectory = File(context.cacheDir, "backups").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val exportedDb = File(backupDirectory, "RoomRent_Database_$timestamp.db")

        try {
            android.database.sqlite.SQLiteDatabase
                .openDatabase(
                    dbFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                .use { db ->
                    db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val busy = cursor.getInt(0)
                            val log = cursor.getInt(1)
                            val checkpointed = cursor.getInt(2)
                            android.util.Log.d(
                                "DbShare",
                                "checkpoint: busy=$busy log=$log checkpointed=$checkpointed"
                            )
                        }
                    }
                    db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use { it.moveToFirst() }
                }
        } catch (e: Exception) {
            android.util.Log.e("DbShare", "checkpoint failed: ${e.message}", e)
        }

        Thread.sleep(80)

        dbFile.copyTo(exportedDb, overwrite = true)

        android.util.Log.d(
            "DbShare",
            "exported size=${exportedDb.length()} bytes to ${exportedDb.absolutePath}"
        )

        if (!exportedDb.exists() || exportedDb.length() == 0L) {
            Toast.makeText(
                context,
                "Exported database is empty. Try again after opening the app once.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportedDb
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share Database File")
        )

    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No app is available to share the database",
            Toast.LENGTH_LONG
        ).show()
        android.util.Log.e("DbShare", "ActivityNotFound", e)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Unable to share database: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        android.util.Log.e("DbShare", "Unexpected error", e)
    }
}

/**
 * Saves JSON into a URI selected by the Android file picker.
 */
private fun saveJsonToUri(
    context: Context,
    uri: Uri,
    json: String
): Boolean {

    return try {

        context.contentResolver
            .openOutputStream(uri)
            ?.use { outputStream ->

                outputStream.write(
                    json.toByteArray(
                        Charsets.UTF_8
                    )
                )

                outputStream.flush()
            }

        true

    } catch (e: Exception) {

        false
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentCurrency: String,
    themeMode: String,
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    onSelectCurrency: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onManageRooms: () -> Unit,
    onExportExcel: () -> Unit,
    onExportJson: () -> String,
    onExportPdfAll: () -> Unit,
    onRestoreJson: (String) -> Boolean,
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit,

    deleteProtectionEnabled: Boolean,
    onToggleDeleteProtection: (Boolean) -> Unit,
    hasDeletePin: Boolean,
    onSetDeletePin: (String) -> Unit,
    onVerifyDeletePin: (String) -> Boolean,
    onClearDeletePin: () -> Unit,

    modifier: Modifier = Modifier,
    appLockConfig: AppLockConfig,
    onAppLockConfigChanged: (AppLockConfig) -> Unit,
) {

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var showCurrencyDialog by remember {
        mutableStateOf(false)
    }

    var showThemeDialog by remember {
        mutableStateOf(false)
    }

    var showBackupDialog by remember {
        mutableStateOf(false)
    }

    var showExportDialog by remember {
        mutableStateOf(false)
    }

    var showSetPinDialog by remember {
        mutableStateOf(false)
    }

    var showRemovePinDialog by remember {
        mutableStateOf(false)
    }

    var showDisableProtectionDialog by remember {
        mutableStateOf(false)
    }

    var showForgotPinRecoveryDialog by remember {
        mutableStateOf(false)
    }

    var changeCurrentPinFirst by remember {
        mutableStateOf(false)
    }

    var removePinTapCount by remember {
        mutableIntStateOf(0)
    }

    var removePinFirstTapTime by remember {
        mutableLongStateOf(0L)
    }

    var pendingBackupJson by remember {
        mutableStateOf<String?>(null)
    }

    val createBackupFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                "application/json"
            )
        ) { uri ->

            val json = pendingBackupJson

            if (uri != null && json != null) {

                val success = saveJsonToUri(
                    context = context,
                    uri = uri,
                    json = json
                )

                Toast.makeText(
                    context,
                    if (success) {
                        "JSON backup saved successfully"
                    } else {
                        "Failed to save JSON backup"
                    },
                    Toast.LENGTH_LONG
                ).show()
            }

            pendingBackupJson = null
        }

    val restoreFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            try {

                val json = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { reader ->
                        reader.readText()
                    }

                if (json.isNullOrBlank()) {

                    Toast.makeText(
                        context,
                        "Backup file is empty",
                        Toast.LENGTH_LONG
                    ).show()

                    return@rememberLauncherForActivityResult
                }

                val success = onRestoreJson(json)

                Toast.makeText(
                    context,
                    if (success) {
                        "Backup restored successfully!"
                    } else {
                        "Invalid or incompatible backup file"
                    },
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Failed to read backup file",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    LaunchedEffect(removePinFirstTapTime) {

        if (removePinFirstTapTime > 0L) {

            delay(FORGOT_PIN_WINDOW_MS)

            removePinTapCount = 0
            removePinFirstTapTime = 0L
        }
    }

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

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // ============================================================
        // NOTIFICATIONS
        // ============================================================

        SectionHeader(
            title = "Notifications",
            subtitle = "Payment alerts and reminders"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(
                            RoundedCornerShape(10.dp)
                        )
                        .background(
                            MaterialTheme.colorScheme.primary
                                .copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        "Payment reminders",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "Alerts when rent is due or paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = onToggleReminders
                )
            }
        }

        // ============================================================
        // DISPLAY & CURRENCY
        // ============================================================

        SectionHeader(
            title = "Display & Currency"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Column {

                SettingsListItem(
                    icon = Icons.Default.CurrencyExchange,
                    title = "Currency Symbol",
                    subtitle = "Current: $currentCurrency",
                    onClick = {
                        showCurrencyDialog = true
                    },
                    testTag = "settings_currency_item"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                SettingsListItem(
                    icon = Icons.Default.DarkMode,
                    title = "App Theme",
                    subtitle = when (themeMode) {
                        "DARK" -> "Dark Mode"
                        "LIGHT" -> "Light Mode"
                        else -> "System Default"
                    },
                    onClick = {
                        showThemeDialog = true
                    },
                    testTag = "settings_theme_item"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ============================================================
        // SECURITY
        // ============================================================

        SectionHeader(
            title = "Security",
            subtitle = "Protect rooms and records from accidental deletion"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(
                                RoundedCornerShape(10.dp)
                            )
                            .background(
                                (
                                        if (deleteProtectionEnabled)
                                            StatusPaid
                                        else
                                            MaterialTheme.colorScheme.outline
                                        ).copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            if (deleteProtectionEnabled)
                                Icons.Default.Lock
                            else
                                Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (deleteProtectionEnabled)
                                StatusPaid
                            else
                                MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            "Delete Protection",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            if (deleteProtectionEnabled)
                                "PIN required to delete rooms & records"
                            else
                                "Anyone can delete without a PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = deleteProtectionEnabled,
                        onCheckedChange = { newValue ->

                            when {

                                newValue && !hasDeletePin -> {

                                    changeCurrentPinFirst = false
                                    showSetPinDialog = true
                                }

                                !newValue && hasDeletePin -> {

                                    showDisableProtectionDialog = true
                                }

                                else -> {

                                    onToggleDeleteProtection(
                                        newValue
                                    )
                                }
                            }
                        }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                SettingsListItem(
                    icon = Icons.Default.Password,
                    title = if (hasDeletePin)
                        "Change Delete PIN"
                    else
                        "Set Delete PIN",
                    subtitle = if (hasDeletePin)
                        "PIN is set"
                    else
                        "No PIN set yet",
                    onClick = {

                        changeCurrentPinFirst = hasDeletePin
                        showSetPinDialog = true
                    },
                    testTag = "settings_set_pin_item"
                )

                if (hasDeletePin) {

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(
                            alpha = 0.4f
                        )
                    )

                    SettingsListItem(
                        icon = Icons.Default.LockReset,
                        title = "Remove PIN",
                        subtitle = "Long press to remove PIN",
                        onClick = {

                            val now =
                                System.currentTimeMillis()

                            if (
                                removePinTapCount == 0 ||
                                now - removePinFirstTapTime >
                                FORGOT_PIN_WINDOW_MS
                            ) {

                                removePinFirstTapTime = now
                                removePinTapCount = 1

                            } else {

                                removePinTapCount += 1
                            }

                            if (
                                removePinTapCount >=
                                FORGOT_PIN_REQUIRED_TAPS
                            ) {

                                removePinTapCount = 0
                                removePinFirstTapTime = 0L

                                showForgotPinRecoveryDialog =
                                    true
                            }
                        },
                        onLongClick = {

                            removePinTapCount = 0
                            removePinFirstTapTime = 0L

                            showRemovePinDialog = true
                        },
                        testTag = "settings_remove_pin_item"
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ============================================================
        // APP LOCK
        // ============================================================

        SectionHeader(
            title = "App Lock",
            subtitle = "Lock the whole app with a PIN, password or biometrics"
        )

        AppLockSettingsSection(
            config = appLockConfig,
            onConfigChanged = onAppLockConfigChanged
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ============================================================
        // ROOMS
        // ============================================================

        SectionHeader(
            title = "Rooms & Properties"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            SettingsListItem(
                icon = Icons.Default.HomeWork,
                title = "Manage Rooms",
                subtitle = "${allRooms.size} room(s) configured • Active: ${activeRoom?.name ?: "None"}",
                onClick = onManageRooms,
                testTag = "settings_manage_rooms_item"
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ============================================================
        // DATA & BACKUP (single box)
        // ============================================================

        SectionHeader(
            title = "Data Storage & Backup",
            subtitle = "Offline-first local database. Export, share or restore anytime."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Column {

                SettingsListItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export All Data",
                    subtitle = "Export all monthly records in Excel and PDF format",
                    onClick = {
                        showExportDialog = true
                    },
                    testTag = "settings_export_csv_item"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                SettingsListItem(
                    icon = Icons.Default.Backup,
                    title = "Backup Data",
                    subtitle = "Share, copy or download JSON & database",
                    onClick = {
                        showBackupDialog = true
                    },
                    testTag = "settings_backup_item"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                SettingsListItem(
                    icon = Icons.Default.Restore,
                    title = "Restore Data (JSON)",
                    subtitle = "Select a previously created JSON backup",
                    onClick = {

                        restoreFileLauncher.launch(
                            arrayOf(
                                "application/json",
                                "text/json",
                                "text/plain",
                                "*/*"
                            )
                        )
                    },
                    testTag = "settings_restore_json_item"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ============================================================
        // ABOUT
        // ============================================================

        SectionHeader(
            title = "About App"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Rent & Utility Manager",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Version 1.0 • Made By Biplap Neupane.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Designed for room renters and landlords to effortlessly track rent, electricity & water meter units, waste charges, unit rates, and complete monthly history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ============================================================
        // DEVELOPER
        // ============================================================

        SectionHeader(
            title = "Developer Info"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.7f
                )
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Biplap Neupane",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "About My Portfolio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Explore my portfolio to learn more about my projects, development work, skills, and experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.biplapneupane.com.np"
                                )
                            )

                            try {

                                context.startActivity(intent)

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Unable to open website",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Portfolio website",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Visit My Portfolio",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "www.biplapneupane.com.np",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open portfolio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Thank you for using Rent & Utility Manager!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        val currentYear = remember {
            Calendar.getInstance().get(Calendar.YEAR)
        }

        val copyrightMessage = androidx.compose.ui.res.stringResource(
            R.string.copyright_template,
            currentYear,
            androidx.compose.ui.res.stringResource(
                R.string.author_name
            )
        )

        Text(
            text = copyrightMessage,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }

    // ================================================================
    // SET / CHANGE PIN
    // ================================================================

    if (showSetPinDialog) {

        SetPinDialog(
            isChanging = changeCurrentPinFirst,
            verifyCurrentPin = onVerifyDeletePin,
            onDismiss = {
                showSetPinDialog = false
            },
            onSave = { newPin ->

                onSetDeletePin(newPin)

                if (!deleteProtectionEnabled) {
                    onToggleDeleteProtection(true)
                }

                Toast.makeText(
                    context,
                    "Delete PIN saved",
                    Toast.LENGTH_SHORT
                ).show()

                showSetPinDialog = false
            }
        )
    }

    // ================================================================
    // DISABLE DELETE PROTECTION
    // ================================================================

    if (showDisableProtectionDialog) {

        var pinInput by remember {
            mutableStateOf("")
        }

        var pinValid by remember {
            mutableStateOf(false)
        }

        AlertDialog(
            onDismissRequest = {

                pinInput = ""
                pinValid = false
                showDisableProtectionDialog = false
            },

            shape = RoundedCornerShape(22.dp),

            containerColor =
                MaterialTheme.colorScheme.surface,

            icon = {

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary
                                        .copy(alpha = 0.24f),
                                    MaterialTheme.colorScheme.primary
                                        .copy(alpha = 0.06f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.LockOpen,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },

            title = {

                Text(
                    "Turn Off Delete Protection?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {

                Column {

                    Text(
                        "Enter your current PIN to turn off delete protection.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = pinInput,

                        onValueChange = { new ->

                            if (
                                new.length <= 8 &&
                                new.all {
                                    it.isDigit()
                                }
                            ) {

                                pinInput = new

                                pinValid =
                                    new.isNotEmpty() &&
                                            onVerifyDeletePin(new)
                            }
                        },

                        label = {
                            Text("Current PIN")
                        },

                        singleLine = true,

                        isError =
                            pinInput.isNotEmpty() &&
                                    !pinValid,

                        supportingText =
                            if (
                                pinInput.isNotEmpty() &&
                                !pinValid
                            ) {
                                {
                                    Text("Incorrect PIN")
                                }
                            } else {
                                null
                            },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.NumberPassword
                            ),

                        visualTransformation =
                            PasswordVisualTransformation(),

                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        onToggleDeleteProtection(false)

                        Toast.makeText(
                            context,
                            "Delete protection turned off",
                            Toast.LENGTH_SHORT
                        ).show()

                        pinInput = ""
                        pinValid = false

                        showDisableProtectionDialog =
                            false
                    },

                    enabled = pinValid,

                    shape = RoundedCornerShape(10.dp)
                ) {

                    Text(
                        "Turn Off",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        pinInput = ""
                        pinValid = false

                        showDisableProtectionDialog =
                            false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    // ================================================================
    // REMOVE PIN
    // ================================================================

    if (showRemovePinDialog) {

        var pinInput by remember {
            mutableStateOf("")
        }

        var pinValid by remember {
            mutableStateOf(false)
        }

        AlertDialog(
            onDismissRequest = {

                pinInput = ""
                pinValid = false
                showRemovePinDialog = false
            },

            shape = RoundedCornerShape(22.dp),

            containerColor =
                MaterialTheme.colorScheme.surface,

            icon = {

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    StatusUnpaid.copy(
                                        alpha = 0.24f
                                    ),
                                    StatusUnpaid.copy(
                                        alpha = 0.06f
                                    )
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.LockReset,
                        null,
                        tint = StatusUnpaid,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },

            title = {

                Text(
                    "Remove Delete PIN?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {

                Column {

                    Text(
                        "Delete protection will be turned off and the PIN will be forgotten. Enter your current PIN to confirm.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = pinInput,

                        onValueChange = { new ->

                            if (
                                new.length <= 8 &&
                                new.all {
                                    it.isDigit()
                                }
                            ) {

                                pinInput = new

                                pinValid =
                                    new.isNotEmpty() &&
                                            onVerifyDeletePin(new)
                            }
                        },

                        label = {
                            Text("Current PIN")
                        },

                        singleLine = true,

                        isError =
                            pinInput.isNotEmpty() &&
                                    !pinValid,

                        supportingText =
                            if (
                                pinInput.isNotEmpty() &&
                                !pinValid
                            ) {
                                {
                                    Text("Incorrect PIN")
                                }
                            } else {
                                null
                            },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.NumberPassword
                            ),

                        visualTransformation =
                            PasswordVisualTransformation(),

                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        onClearDeletePin()
                        onToggleDeleteProtection(false)

                        Toast.makeText(
                            context,
                            "Delete PIN removed",
                            Toast.LENGTH_SHORT
                        ).show()

                        pinInput = ""
                        pinValid = false

                        showRemovePinDialog = false
                    },

                    enabled = pinValid,

                    shape = RoundedCornerShape(10.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = StatusUnpaid
                        )
                ) {

                    Text(
                        "Remove",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        pinInput = ""
                        pinValid = false

                        showRemovePinDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    // ================================================================
    // FORGOT PIN RECOVERY
    // ================================================================

    if (showForgotPinRecoveryDialog) {

        AlertDialog(
            onDismissRequest = {
                showForgotPinRecoveryDialog = false
            },

            shape = RoundedCornerShape(22.dp),

            containerColor =
                MaterialTheme.colorScheme.surface,

            icon = {

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    StatusUnpaid.copy(
                                        alpha = 0.24f
                                    ),
                                    StatusUnpaid.copy(
                                        alpha = 0.06f
                                    )
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.LockReset,
                        null,
                        tint = StatusUnpaid,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },

            title = {

                Text(
                    "Forgot PIN?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {

                Text(
                    "This will remove the delete PIN and turn off delete protection. Anyone with access to this device will be able to delete rooms and records afterwards.\n\nContinue?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        onClearDeletePin()
                        onToggleDeleteProtection(false)

                        Toast.makeText(
                            context,
                            "PIN removed",
                            Toast.LENGTH_SHORT
                        ).show()

                        showForgotPinRecoveryDialog =
                            false
                    },

                    shape = RoundedCornerShape(10.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = StatusUnpaid
                        )
                ) {

                    Text(
                        "Remove PIN",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showForgotPinRecoveryDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    // ================================================================
    // CURRENCY
    // ================================================================

    if (showCurrencyDialog) {

        AlertDialog(
            onDismissRequest = {
                showCurrencyDialog = false
            },

            title = {
                Text("Select Currency")
            },

            text = {

                Column {

                    SUPPORTED_CURRENCIES.forEach {
                            (symbol, name) ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    onSelectCurrency(symbol)

                                    showCurrencyDialog =
                                        false
                                }
                                .padding(
                                    vertical = 12.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected =
                                    currentCurrency ==
                                            symbol,

                                onClick = {

                                    onSelectCurrency(
                                        symbol
                                    )

                                    showCurrencyDialog =
                                        false
                                }
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(
                                text = name,
                                fontWeight =
                                    FontWeight.Medium
                            )
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showCurrencyDialog = false
                    }
                ) {

                    Text("Close")
                }
            }
        )
    }

    // ================================================================
    // THEME
    // ================================================================

    if (showThemeDialog) {

        AlertDialog(
            onDismissRequest = {
                showThemeDialog = false
            },

            title = {
                Text("Choose App Theme")
            },

            text = {

                Column {

                    listOf(
                        "SYSTEM" to "System Default",
                        "LIGHT" to "Light Mode",
                        "DARK" to "Dark Mode"
                    ).forEach {
                            (mode, label) ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    onSelectTheme(mode)

                                    showThemeDialog =
                                        false
                                }
                                .padding(
                                    vertical = 12.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected =
                                    themeMode == mode,

                                onClick = {

                                    onSelectTheme(mode)

                                    showThemeDialog =
                                        false
                                }
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(
                                text = label,
                                fontWeight =
                                    FontWeight.Medium
                            )
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showThemeDialog = false
                    }
                ) {

                    Text("Close")
                }
            }
        )
    }

    // ================================================================
    // EXPORT PDF / EXCEL
    // ================================================================

    if (showExportDialog) {

        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
            },

            shape = RoundedCornerShape(24.dp),

            containerColor =
                MaterialTheme.colorScheme.surface,

            title = {

                Column {

                    Text(
                        text = "Export Data",
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = "Choose a format to download your records",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            text = {

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Surface(
                        onClick = {

                            showExportDialog = false

                            onExportPdfAll()

                            Toast.makeText(
                                context,
                                "PDF exported!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },

                        shape =
                            RoundedCornerShape(16.dp),

                        color =
                            Color(0xFFFFF1F2),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(14.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            14.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFF43F5E)
                                    ),
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    null,
                                    tint = Color.White,
                                    modifier =
                                        Modifier.size(24.dp)
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.width(14.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    "PDF Report",
                                    style =
                                        MaterialTheme.typography.titleSmall,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    color =
                                        Color(0xFF881337)
                                )

                                Text(
                                    "Full formatted statement for printing",
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color =
                                        Color(0xFF9F1239)
                                )
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint =
                                    Color(0xFFF43F5E)
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Surface(
                        onClick = {

                            showExportDialog = false

                            onExportExcel()

                            Toast.makeText(
                                context,
                                "Excel exported!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },

                        shape =
                            RoundedCornerShape(16.dp),

                        color =
                            Color(0xFFECFDF5),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(14.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            14.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFF10B981)
                                    ),
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    Icons.Default.TableChart,
                                    null,
                                    tint = Color.White,
                                    modifier =
                                        Modifier.size(24.dp)
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.width(14.dp)
                            )

                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    "Excel Workbook",
                                    style =
                                        MaterialTheme.typography.titleSmall,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    color =
                                        Color(0xFF064E3B)
                                )

                                Text(
                                    "Editable .xlsx file, one sheet per room",
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color =
                                        Color(0xFF065F46)
                                )
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint =
                                    Color(0xFF10B981)
                            )
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showExportDialog = false
                    },
                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    Text(
                        "Close",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        )
    }

    // ================================================================
    // SINGLE BACKUP DIALOG (Share / Copy / Download JSON + Share DB)
    // ================================================================

    if (showBackupDialog) {

        AlertDialog(
            onDismissRequest = {
                showBackupDialog = false
            },

            shape = RoundedCornerShape(24.dp),

            containerColor =
                MaterialTheme.colorScheme.surface,

            title = {

                Column {

                    Text(
                        text = "Backup Data",
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = "Choose how you want to save or share your data",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            text = {

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    BackupOptionItem(
                        icon = Icons.Default.Share,
                        title = "Share JSON",
                        subtitle = "Send full backup as .json",
                        containerColor = Color(0xFFEFF6FF),
                        iconBackground = Color(0xFF3B82F6),
                        titleColor = Color(0xFF1E3A8A),
                        subtitleColor = Color(0xFF1D4ED8),
                        onClick = {

                            showBackupDialog = false

                            try {

                                val json = onExportJson()

                                shareBackupJson(
                                    context = context,
                                    json = json
                                )

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Failed to create backup",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    BackupOptionItem(
                        icon = Icons.Default.Storage,
                        title = "Share Database (.db)",
                        subtitle = "Raw Room database file",
                        containerColor = Color(0xFFF5F3FF),
                        iconBackground = Color(0xFF8B5CF6),
                        titleColor = Color(0xFF3B0764),
                        subtitleColor = Color(0xFF5B21B6),
                        onClick = {

                            showBackupDialog = false

                            shareDatabaseFile(context)
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    BackupOptionItem(
                        icon = Icons.Default.ContentCopy,
                        title = "Copy JSON",
                        subtitle = "Copy backup to clipboard",
                        containerColor = Color(0xFFFFFBEB),
                        iconBackground = Color(0xFFF59E0B),
                        titleColor = Color(0xFF78350F),
                        subtitleColor = Color(0xFF92400E),
                        onClick = {

                            showBackupDialog = false

                            try {

                                val json = onExportJson()

                                clipboardManager.setText(
                                    AnnotatedString(json)
                                )

                                Toast.makeText(
                                    context,
                                    "JSON copied to clipboard!",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Failed to create JSON backup",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    BackupOptionItem(
                        icon = Icons.Default.FileDownload,
                        title = "Download JSON",
                        subtitle = "Save .json file to device",
                        containerColor = Color(0xFFECFDF5),
                        iconBackground = Color(0xFF10B981),
                        titleColor = Color(0xFF064E3B),
                        subtitleColor = Color(0xFF065F46),
                        onClick = {

                            showBackupDialog = false

                            try {

                                val json = onExportJson()

                                pendingBackupJson = json

                                val timestamp =
                                    SimpleDateFormat(
                                        "yyyy-MM-dd_HH-mm-ss",
                                        Locale.US
                                    ).format(Date())

                                createBackupFileLauncher.launch(
                                    "RoomRent_Backup_$timestamp.json"
                                )

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Failed to create backup: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showBackupDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {

                    Text(
                        "Close",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

/**
 * A rounded, tappable backup option row used inside the backup dialog.
 */
@Composable
private fun BackupOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconBackground: Color,
    titleColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    icon,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )

                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = iconBackground
            )
        }
    }
}

/**
 * Reusable Settings List Item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    onLongClick: (() -> Unit)? = null
) {

    val clickModifier = if (onLongClick != null) {

        Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )

    } else {

        Modifier.clickable {
            onClick()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(16.dp)
            .testTag(testTag),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            modifier =
                Modifier.weight(1f)
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.size(24.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )

            Column {

                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleSmall,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = subtitle,
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector =
                Icons.Default.ChevronRight,

            contentDescription = null,

            tint =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}