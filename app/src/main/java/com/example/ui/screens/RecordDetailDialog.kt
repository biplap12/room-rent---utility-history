package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun RecordDetailDialog(
    record: MonthlyRecordEntity,
    room: RoomEntity?,
    currencySymbol: String,
    onEdit: (MonthlyRecordEntity) -> Unit,
    onDelete: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (MonthlyRecordEntity) -> Unit,
    onClose: () -> Unit,
    deleteProtectionEnabled: Boolean = false,
    hasDeletePin: Boolean = false,
    onVerifyDeletePin: (String) -> Boolean = { true }
) {
    val scrollState = rememberScrollState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ══════════════════════════════════════════════
                //  HEADER BANNER
                // ══════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BILL STATEMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.4.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = record.billingMonth,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = room?.name ?: "Room ${record.roomId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // ══════════════════════════════════════════════
                //  SCROLLABLE BODY
                // ══════════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {

                    // ── Status strip ─────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (record.paymentDate.isNotBlank())
                                    "Paid on ${record.paymentDate}"
                                else "No payment date recorded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PaymentStatusBadge(status = record.paymentStatus)
                    }

                    // ── Itemized charges ─────────────────────
                    Text(
                        text = "ITEMIZED CHARGES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ChargeRow(
                                icon = Icons.Default.Home,
                                tint = MaterialTheme.colorScheme.primary,
                                title = "Room Rent",
                                subtitle = "Fixed monthly base rent",
                                value = FormatUtils.formatMoney(record.roomRent, currencySymbol)
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            ChargeRow(
                                icon = Icons.Default.FlashOn,
                                tint = ElectricityAccent,
                                title = "Electricity",
                                subtitle = buildString {
                                    append(
                                        "${FormatUtils.formatUnits(record.previousElectricityReading)} → " +
                                                "${FormatUtils.formatUnits(record.currentElectricityReading)}"
                                    )
                                    if (record.electricityUnits > 0 || record.electricityRate > 0) {
                                        append("  •  ")
                                        append("${FormatUtils.formatUnits(record.electricityUnits)} u × ")
                                        append(FormatUtils.formatMoney(record.electricityRate, currencySymbol))
                                    }
                                },
                                value = FormatUtils.formatMoney(record.electricityCost, currencySymbol),
                                valueColor = ElectricityAccent
                            )

                            if (record.waterUnits > 0 || record.waterCost > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ChargeRow(
                                    icon = Icons.Default.WaterDrop,
                                    tint = WaterAccent,
                                    title = "Water",
                                    subtitle = buildString {
                                        append(
                                            "${FormatUtils.formatUnits(record.previousWaterReading)} → " +
                                                    "${FormatUtils.formatUnits(record.currentWaterReading)}"
                                        )
                                        if (record.waterUnits > 0) {
                                            append("  •  ")
                                            append("${FormatUtils.formatUnits(record.waterUnits)} u × ")
                                            append(FormatUtils.formatMoney(record.waterRate, currencySymbol))
                                        }
                                    },
                                    value = FormatUtils.formatMoney(record.waterCost, currencySymbol),
                                    valueColor = WaterAccent
                                )
                            }

                            if (record.wasteCharge > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ChargeRow(
                                    icon = Icons.Default.DeleteSweep,
                                    tint = WasteAccent,
                                    title = "Waste / Sanitation",
                                    subtitle = "Municipal / minimum charge",
                                    value = FormatUtils.formatMoney(record.wasteCharge, currencySymbol)
                                )
                            }

                            if (record.otherCharges > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ChargeRow(
                                    icon = Icons.Default.Add,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    title = "Other Charges",
                                    subtitle = "Additional expenses",
                                    value = FormatUtils.formatMoney(record.otherCharges, currencySymbol)
                                )
                            }

                            if (record.discount > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ChargeRow(
                                    icon = Icons.Default.LocalOffer,
                                    tint = StatusPaid,
                                    title = "Discount",
                                    subtitle = "Deduction applied",
                                    value = "- " + FormatUtils.formatMoney(record.discount, currencySymbol),
                                    valueColor = StatusPaid
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Payment summary ──────────────────────
                    Text(
                        text = "PAYMENT SUMMARY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    val isPaid = record.remainingAmount <= 0
                    val pillColor = if (isPaid) StatusPaid else StatusUnpaid

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {

                            // Big total
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = "TOTAL AMOUNT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = FormatUtils.formatMoney(record.totalAmount, currencySymbol),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                            // Amount paid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusPaid,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Amount Paid",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = FormatUtils.formatMoney(record.amountPaid, currencySymbol),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusPaid
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                            // Paid / Due banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(pillColor.copy(alpha = 0.10f))
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isPaid) "PAID IN FULL" else "REMAINING DUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = pillColor,
                                        letterSpacing = 1.2.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = FormatUtils.formatMoney(
                                            if (isPaid) record.amountPaid else record.remainingAmount,
                                            currencySymbol
                                        ),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = pillColor
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(pillColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPaid)
                                            Icons.Default.CheckCircle
                                        else
                                            Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = pillColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Notes ────────────────────────────────
                    if (record.notes.isNotBlank()) {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = record.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // ══════════════════════════════════════════════
                //  BOTTOM ACTIONS
                // ══════════════════════════════════════════════
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onExportPdf(record) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_pdf_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onEdit(record)
                            onClose()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_edit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StatusUnpaid.copy(alpha = 0.12f))
                            .testTag("detail_delete_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = StatusUnpaid,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  DELETE CONFIRMATION (PIN or room name)
    // ══════════════════════════════════════════════════════════
    if (showDeleteConfirm) {

        val roomName = room?.name.orEmpty()
        val pinMode = deleteProtectionEnabled && hasDeletePin
        val nameMode = !pinMode

        var pinInput by remember { mutableStateOf("") }
        var nameInput by remember { mutableStateOf("") }

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
                showDeleteConfirm = false
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusUnpaid
                )
            },
            title = { Text("Delete This Record?") },
            text = {
                Column {
                    Text("Are you sure you want to permanently delete the bill for ${record.billingMonth}?")

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
                                .testTag("detail_delete_pin_input")
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
                                .testTag("detail_delete_room_name_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(record)
                        pinInput = ""
                        nameInput = ""
                        showDeleteConfirm = false
                        onClose()
                    },
                    enabled = canDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("detail_delete_confirm_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pinInput = ""
                    nameInput = ""
                    showDeleteConfirm = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ──────────────────────────────────────────────────────────
 *  Charge row — icon chip + label + optional subtitle + value
 * ────────────────────────────────────────────────────────── */
@Composable
private fun ChargeRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String?,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}



