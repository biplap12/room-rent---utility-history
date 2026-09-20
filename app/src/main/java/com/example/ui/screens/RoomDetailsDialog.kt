package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RoomEntity
import com.example.ui.components.BeautifulCard
import com.example.ui.components.GradientIconBadge
import com.example.ui.components.propertyTypeIcon
import com.example.ui.components.propertyTypeLabel
import com.example.ui.theme.StatusPaid
import com.example.util.FormatUtils

/**
 * Details dialog for the CURRENTLY ACTIVE room.
 * "Switch" opens a picker of other rooms to make one of them active.
 */
@Composable
internal fun RoomDetailsDialog(
    room: RoomEntity,
    currencySymbol: String,
    allRooms: List<RoomEntity>,               // ★ all rooms (incl. this one)
    onSwitchTo: (RoomEntity) -> Unit,         // ★ called with the chosen room
    onClose: () -> Unit,
    onEdit: () -> Unit
) {
    var showRoomPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                elevation = 12.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Header ────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GradientIconBadge(
                                icon = propertyTypeIcon(room.propertyType),
                                accent = MaterialTheme.colorScheme.primary,
                                size = 46.dp,
                                iconSize = 22.dp,
                                corner = 14.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        room.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    ActiveChip()
                                }
                                Spacer(Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Home, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        propertyTypeLabel(room.propertyType),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "  ·  ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Icon(
                                        Icons.Outlined.Tag, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        "#${room.id}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                onClick = onClose,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Close, "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Scroll body ───────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))

                        SectionTitle(Icons.Outlined.Badge, "Identity")
                        DetailCard {
                            DetailRow(Icons.Outlined.Tag, "ID", room.id.toString())
                            DetailDivider()
                            DetailRow(Icons.Outlined.Home, "Name", room.name)
                            DetailDivider()
                            DetailRow(
                                Icons.Outlined.Category, "Type",
                                propertyTypeLabel(room.propertyType)
                            )
                            DetailDivider()
                            DetailRow(
                                Icons.Outlined.LocationOn, "Address",
                                room.address.ifBlank { "—" }
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        SectionTitle(Icons.Outlined.Receipt, "Billing",
                            color = MaterialTheme.colorScheme.tertiary)
                        DetailCard {
                            DetailRow(
                                Icons.Outlined.Payments, "Monthly Rent",
                                FormatUtils.formatMoney(room.defaultRent, currencySymbol),
                                highlight = true
                            )
                            DetailDivider()
                            DetailRow(Icons.Outlined.Bolt, "Electricity",
                                "${room.electricityRate} / unit")
                            DetailDivider()
                            DetailRow(Icons.Outlined.WaterDrop, "Water Mode",
                                if (room.waterBillingMode == "DIRECT") "Flat" else "Per Unit")
                            DetailDivider()
                            if (room.waterBillingMode == "DIRECT") {
                                DetailRow(Icons.Outlined.WaterDrop, "Water Amount",
                                    FormatUtils.formatMoney(room.directWaterAmount, currencySymbol))
                            } else {
                                DetailRow(Icons.Outlined.WaterDrop, "Water Rate",
                                    "${room.waterRate} / unit")
                            }
                            DetailDivider()
                            DetailRow(Icons.Outlined.DeleteSweep, "Waste",
                                if (room.wasteCharge > 0)
                                    FormatUtils.formatMoney(room.wasteCharge, currencySymbol)
                                else "—")
                            DetailDivider()
                            DetailRow(Icons.Outlined.Shield, "Deposit",
                                if (room.securityDeposit > 0)
                                    FormatUtils.formatMoney(room.securityDeposit, currencySymbol)
                                else "—")
                        }

                        Spacer(Modifier.height(18.dp))

                        SectionTitle(Icons.Outlined.Person, "Tenant",
                            badge = if (room.tenantName.isBlank()) "Vacant" else null,
                            color = if (room.tenantName.isBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else StatusPaid)
                        DetailCard {
                            DetailRow(Icons.Outlined.Person, "Name",
                                room.tenantName.ifBlank { "—" })
                            DetailDivider()
                            DetailRow(Icons.Outlined.Phone, "Phone",
                                room.tenantPhone.ifBlank { "—" })
                            DetailDivider()
                            DetailRow(Icons.Outlined.Email, "Email",
                                room.tenantEmail.ifBlank { "—" })
                            DetailDivider()
                            DetailRow(Icons.Outlined.Badge, "ID No.",
                                room.tenantIdNumber.ifBlank { "—" })
                            DetailDivider()
                            DetailRow(Icons.Outlined.Emergency, "Emergency",
                                room.emergencyContact.ifBlank { "—" })
                            DetailDivider()
                            DetailRow(Icons.Outlined.Event, "Move-in",
                                room.moveInDate.ifBlank { "—" })
                        }

                        if (room.tenantNotes.isNotBlank()) {
                            Spacer(Modifier.height(18.dp))
                            SectionTitle(Icons.Outlined.StickyNote2, "Notes")
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp, 42.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        room.tenantNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                    // ── Footer actions ────────────────────────
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Edit, null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // ★ "Switch Room" opens a picker of OTHER rooms
                        SwitchRoomButton(
                            otherRoomsCount = allRooms.count { it.id != room.id },
                            onClick = { showRoomPicker = true },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }
        }
    }

    // ── Room picker (nested dialog) ───────────────────────────
    if (showRoomPicker) {
        RoomPickerDialog(
            currentRoomId = room.id,
            allRooms = allRooms,
            currencySymbol = currencySymbol,
            onPick = { picked ->
                showRoomPicker = false
                onSwitchTo(picked)
            },
            onDismiss = { showRoomPicker = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// ROOM PICKER DIALOG
// ═══════════════════════════════════════════════════════════════

@Composable
private fun RoomPickerDialog(
    currentRoomId: Long,
    allRooms: List<RoomEntity>,
    currencySymbol: String,
    onPick: (RoomEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val others = remember(allRooms, currentRoomId) {
        allRooms.filter { it.id != currentRoomId }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    "Switch Room",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (others.isEmpty())
                        "No other rooms available"
                    else
                        "Choose a room to make active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (others.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.MeetingRoom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Add another room first",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(others, key = { it.id }) { r ->
                        RoomPickerRow(
                            room = r,
                            currencySymbol = currencySymbol,
                            onClick = { onPick(r) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RoomPickerRow(
    room: RoomEntity,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GradientIconBadge(
                icon = propertyTypeIcon(room.propertyType),
                accent = MaterialTheme.colorScheme.primary,
                size = 38.dp,
                iconSize = 18.dp,
                corner = 11.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    room.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        propertyTypeLabel(room.propertyType),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "  ·  ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        FormatUtils.formatMoney(room.defaultRent, currencySymbol),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SWITCH ROOM BUTTON
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SwitchRoomButton(
    otherRoomsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = otherRoomsCount > 0
    val shape = RoundedCornerShape(14.dp)
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Surface(
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        shape = shape,
        color = if (enabled) Color.Transparent else primary.copy(alpha = 0.25f),
        shadowElevation = if (enabled) 4.dp else 0.dp,
        modifier = modifier.height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (enabled) Modifier.background(
                        Brush.horizontalGradient(
                            listOf(
                                primary,
                                primary.copy(alpha = 0.85f),
                                primary.copy(alpha = 0.75f)
                            )
                        )
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(Color.White.copy(alpha = 0.20f))
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = if (enabled) onPrimary else onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (enabled) "Switch Room" else "No Other Rooms",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) onPrimary else onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SUB-COMPONENTS (unchanged)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ActiveChip() {
    Surface(
        shape = RoundedCornerShape(50),
        color = StatusPaid.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, StatusPaid.copy(alpha = 0.40f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StatusPaid)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                "ACTIVE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = StatusPaid,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            title.uppercase(),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = color
        )
        if (badge != null) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            ) {
                Text(
                    badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        modifier = Modifier.padding(start = 34.dp)
    )
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (highlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint = if (highlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.80f),
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = if (highlight) 13.5.sp else 12.5.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}