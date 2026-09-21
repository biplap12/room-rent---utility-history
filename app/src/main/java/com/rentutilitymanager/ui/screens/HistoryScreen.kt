
package com.rentutilitymanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rentutilitymanager.data.MonthlyRecordEntity
import com.rentutilitymanager.data.RoomEntity
import com.rentutilitymanager.ui.components.PaymentStatusBadge
import com.rentutilitymanager.ui.components.RoomPickerDialog
import com.rentutilitymanager.ui.components.RoomSwitcherCard
import com.rentutilitymanager.ui.theme.*
import com.rentutilitymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    records: List<MonthlyRecordEntity>,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onManageRooms: () -> Unit,
    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
    onEditRecord: (MonthlyRecordEntity) -> Unit,
    onDeleteRecord: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (List<MonthlyRecordEntity>) -> Unit,
    onExportExcel: (List<MonthlyRecordEntity>) -> Unit,
    deleteProtectionEnabled: Boolean = false,
    hasDeletePin: Boolean = false,
    onVerifyDeletePin: (String) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    var recordToDelete by remember { mutableStateOf<MonthlyRecordEntity?>(null) }
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) }

    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records
        else records.filter { it.billingMonth.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 40.dp)
    ) {
        // ── Room switcher ─────────────────────────────────────
        RoomSwitcherCard(
            activeRoom = activeRoom,
            allRooms = allRooms,
            currencySymbol = currencySymbol,
            onSwitchClick = { showRoomPicker = true }
            // ★ removed inner horizontal padding — parent Column already handles it
        )

        Spacer(Modifier.height(20.dp))

        // ── Section header ────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradientIconBadge(
                icon = Icons.Default.History,
                accent = MaterialTheme.colorScheme.primary,
                size = 36.dp,
                iconSize = 18.dp,
                corner = 10.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (filteredRecords.isEmpty())
                        "No records yet"
                    else
                        "${filteredRecords.size} record(s) for ${activeRoom?.name ?: "this room"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Search + export ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by month...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(
                    onClick = { exportMenuExpanded = true },
                    modifier = Modifier.testTag("history_export_csv_button")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Export",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = exportMenuExpanded,
                    onDismissRequest = { exportMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export PDF") },
                        leadingIcon = {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        },
                        onClick = {
                            exportMenuExpanded = false
                            onExportPdf(filteredRecords)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export Excel") },
                        leadingIcon = {
                            Icon(Icons.Default.GridOn, contentDescription = null)
                        },
                        onClick = {
                            exportMenuExpanded = false
                            onExportExcel(filteredRecords)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Records ───────────────────────────────────────────
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank())
                            "No records found matching \"$searchQuery\""
                        else "No monthly records yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tap + Add below to create the first bill",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            filteredRecords.forEach { record ->
                HistoryRecordCard(
                    record = record,
                    currencySymbol = currencySymbol,
                    onClick = { onOpenRecordDetail(record) },
                    onEdit = { onEditRecord(record) },
                    onDelete = { recordToDelete = record }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  🏠 ROOM PICKER DIALOG       ★ NEW — was missing
    // ══════════════════════════════════════════════════════════
    if (showRoomPicker) {
        RoomPickerDialog(
            activeRoom = activeRoom,
            allRooms = allRooms,
            onSelect = { onSelectRoom(it) },
            onManage = { onManageRooms() },
            onDismiss = { showRoomPicker = false }
        )
    }

    // ══════════════════════════════════════════════════════════
    //  DELETE CONFIRMATION (PIN or room name)
    // ══════════════════════════════════════════════════════════
    if (recordToDelete != null) {

        val roomName = activeRoom?.name.orEmpty()
        val pinMode = deleteProtectionEnabled && hasDeletePin
        val nameMode = !pinMode

        var pinInput by remember(recordToDelete) { mutableStateOf("") }
        var nameInput by remember(recordToDelete) { mutableStateOf("") }

        val pinValid = !pinMode ||
                (pinInput.isNotEmpty() && onVerifyDeletePin(pinInput))

        val nameValid = !nameMode || (
                nameInput.isNotBlank() &&
                        roomName.isNotBlank() &&
                        nameInput.trim().equals(roomName.trim(), ignoreCase = true)
                )

        val canDelete = if (pinMode) pinValid else nameValid

        AlertDialog(
            onDismissRequest = {
                pinInput = ""
                nameInput = ""
                recordToDelete = null
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusUnpaid
                )
            },
            title = { Text("Delete Record?") },
            text = {
                Column {
                    Text(
                        "Are you sure you want to permanently delete the bill " +
                                "record for \"${recordToDelete?.billingMonth}\"? " +
                                "This action cannot be undone."
                    )

                    if (pinMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Delete Protection is ON",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { new ->
                                if (new.length <= 8 && new.all { it.isDigit() }) {
                                    pinInput = new
                                }
                            },
                            label = { Text("Enter PIN to delete") },
                            singleLine = true,
                            isError = pinInput.isNotEmpty() && !pinValid,
                            supportingText = if (pinInput.isNotEmpty() && !pinValid) {
                                { Text("Incorrect PIN") }
                            } else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_pin_input")
                        )
                    }

                    if (nameMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Confirm deletion",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Type the room name to confirm deletion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Room name") },
                            placeholder = {
                                Text(
                                    if (roomName.isNotBlank()) "e.g. $roomName"
                                    else "Enter room name"
                                )
                            },
                            singleLine = true,
                            isError = nameInput.isNotEmpty() && !nameValid,
                            supportingText = if (nameInput.isNotEmpty() && !nameValid) {
                                { Text("Room name doesn't match") }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_room_name_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = recordToDelete
                        if (target != null) onDeleteRecord(target)
                        pinInput = ""
                        nameInput = ""
                        recordToDelete = null
                    },
                    enabled = canDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("delete_confirm_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pinInput = ""
                    nameInput = ""
                    recordToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun HistoryRecordCard(
    record: MonthlyRecordEntity,
    currencySymbol: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("record_card_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.billingMonth,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (record.paymentDate.isNotBlank())
                            "Paid on: ${record.paymentDate}"
                        else "Not paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PaymentStatusBadge(status = record.paymentStatus)
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Details") },
                                leadingIcon = {
                                    Icon(Icons.Default.Visibility, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Record") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = StatusUnpaid) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = StatusUnpaid
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary row of metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatMoney(record.roomRent, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text(
                        text = "Electricity",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricityAccent
                    )
                    Text(
                        text = "${FormatUtils.formatUnits(record.electricityUnits)} u (${FormatUtils.formatMoney(record.electricityCost, currencySymbol)})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (record.waterUnits > 0 || record.waterCost > 0) {
                    Column {
                        Text(
                            text = "Water",
                            style = MaterialTheme.typography.labelSmall,
                            color = WaterAccent
                        )
                        Text(
                            text = "${FormatUtils.formatUnits(record.waterUnits)} u (${FormatUtils.formatMoney(record.waterCost, currencySymbol)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (record.wasteCharge > 0) {
                    Column {
                        Text(
                            text = "Waste",
                            style = MaterialTheme.typography.labelSmall,
                            color = WasteAccent
                        )
                        Text(
                            text = FormatUtils.formatMoney(record.wasteCharge, currencySymbol),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Total Amount & Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatMoney(record.totalAmount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (record.remainingAmount > 0) {
                    Text(
                        text = "Due: " + FormatUtils.formatMoney(record.remainingAmount, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatusUnpaid
                    )
                } else {
                    Text(
                        text = "Paid: " + FormatUtils.formatMoney(record.amountPaid, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusPaid
                    )
                }
            }
        }
    }
}