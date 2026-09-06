package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.RoomEntity
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoomsDialog(
    allRooms: List<RoomEntity>,
    activeRoom: RoomEntity?,
    editingRoom: RoomEntity?,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onSaveRoom: (name: String, address: String, rent: Double, elecRate: Double, waterRate: Double, wasteCharge: Double) -> Unit,
    onDeleteRoom: (RoomEntity) -> Unit,
    onClose: () -> Unit
) {
    var isEditing by remember { mutableStateOf(editingRoom != null) }
    var currentEditRoom by remember { mutableStateOf(editingRoom) }

    var name by remember(currentEditRoom) { mutableStateOf(currentEditRoom?.name ?: "") }
    var address by remember(currentEditRoom) { mutableStateOf(currentEditRoom?.address ?: "") }
    var rent by remember(currentEditRoom) { mutableStateOf(currentEditRoom?.defaultRent?.toString() ?: "10000") }
    var elecRate by remember(currentEditRoom) { mutableStateOf(currentEditRoom?.electricityRate?.toString() ?: "15") }
    var waterRate by remember(currentEditRoom) { mutableStateOf(currentEditRoom?.waterRate?.toString() ?: "30") }
    var wasteCharge by remember(currentEditRoom) { mutableStateOf(currentEditRoom?.wasteCharge?.toString() ?: "100") }

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) (if (currentEditRoom != null) "Edit Room" else "Add New Room") else "Manage Rooms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isEditing) {
                    // Room Form
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Room / Property Name *") },
                        placeholder = { Text("e.g. Room 102") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / Description") },
                        placeholder = { Text("e.g. Floor 2, East Wing") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_address_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = rent,
                            onValueChange = { rent = it },
                            label = { Text("Rent ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = elecRate,
                            onValueChange = { elecRate = it },
                            label = { Text("Elec ($currencySymbol/u)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = waterRate,
                            onValueChange = { waterRate = it },
                            label = { Text("Water ($currencySymbol/u)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = wasteCharge,
                            onValueChange = { wasteCharge = it },
                            label = { Text("Waste ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                currentEditRoom = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                onSaveRoom(
                                    name.trim(),
                                    address.trim(),
                                    FormatUtils.parseDoubleOrZero(rent),
                                    FormatUtils.parseDoubleOrZero(elecRate),
                                    FormatUtils.parseDoubleOrZero(waterRate),
                                    FormatUtils.parseDoubleOrZero(wasteCharge)
                                )
                                isEditing = false
                                currentEditRoom = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_room_button"),
                            enabled = name.isNotBlank()
                        ) {
                            Text("Save Room")
                        }
                    }
                } else {
                    // Room List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allRooms, key = { it.id }) { room ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectRoom(room) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (room.id == activeRoom?.id) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = room.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (room.id == activeRoom?.id) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (room.address.isNotBlank()) {
                                            Text(
                                                text = room.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Rent: ${FormatUtils.formatMoney(room.defaultRent, currencySymbol)} • Elec: ${FormatUtils.formatMoney(room.electricityRate, currencySymbol)}/u",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            currentEditRoom = room
                                            isEditing = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Room", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        if (allRooms.size > 1) {
                                            IconButton(onClick = { onDeleteRoom(room) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = StatusUnpaid)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            currentEditRoom = null
                            name = ""
                            address = ""
                            rent = "10000"
                            elecRate = "15"
                            waterRate = "30"
                            wasteCharge = "100"
                            isEditing = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_new_room_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Room")
                    }
                }
            }
        }
    }
}
