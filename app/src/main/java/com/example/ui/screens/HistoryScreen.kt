//package com.example.ui.screens
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.data.MonthlyRecordEntity
//import com.example.data.RoomEntity
//import com.example.ui.components.PaymentStatusBadge
//import com.example.ui.components.RoomSelectorBar
//import com.example.ui.theme.*
//import com.example.util.FormatUtils
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HistoryScreen(
//    activeRoom: RoomEntity?,
//    allRooms: List<RoomEntity>,
//    records: List<MonthlyRecordEntity>,
//    currencySymbol: String,
//    onSelectRoom: (RoomEntity) -> Unit,
//    onManageRooms: () -> Unit,
//    onAddRecord: () -> Unit,
//    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
//    onEditRecord: (MonthlyRecordEntity) -> Unit,
//    onDuplicateRecord: (MonthlyRecordEntity) -> Unit,
//    onDeleteRecord: (MonthlyRecordEntity) -> Unit,
//    onExportCsv: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var searchQuery by remember { mutableStateOf("") }
//    var recordToDelete by remember { mutableStateOf<MonthlyRecordEntity?>(null) }
//
//    val filteredRecords = remember(records, searchQuery) {
//        if (searchQuery.isBlank()) records
//        else records.filter { it.billingMonth.contains(searchQuery, ignoreCase = true) }
//    }
//
//    Scaffold(
//        topBar = {
//            Column {
//                RoomSelectorBar(
//                    activeRoom = activeRoom,
//                    allRooms = allRooms,
//                    onSelectRoom = onSelectRoom,
//                    onManageRooms = onManageRooms
//                )
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 6.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    OutlinedTextField(
//                        value = searchQuery,
//                        onValueChange = { searchQuery = it },
//                        placeholder = { Text("Search by month...") },
//                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                        trailingIcon = {
//                            if (searchQuery.isNotBlank()) {
//                                IconButton(onClick = { searchQuery = "" }) {
//                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .testTag("history_search_input"),
//                        singleLine = true,
//                        shape = RoundedCornerShape(8.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    IconButton(
//                        onClick = onExportCsv,
//                        modifier = Modifier.testTag("history_export_csv_button")
//                    ) {
//                        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
//                    }
//                }
//            }
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = onAddRecord,
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = MaterialTheme.colorScheme.onPrimary,
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier.testTag("history_fab_add")
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add Record")
//            }
//        },
//        modifier = modifier
//    ) { innerPadding ->
//        if (filteredRecords.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Icon(
//                        imageVector = Icons.Default.History,
//                        contentDescription = null,
//                        modifier = Modifier.size(64.dp),
//                        tint = MaterialTheme.colorScheme.outline
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Text(
//                        text = if (searchQuery.isNotBlank()) "No records found matching \"$searchQuery\"" else "No monthly records yet",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(filteredRecords, key = { it.id }) { record ->
//                    HistoryRecordCard(
//                        record = record,
//                        currencySymbol = currencySymbol,
//                        onClick = { onOpenRecordDetail(record) },
//                        onEdit = { onEditRecord(record) },
//                        onDuplicate = { onDuplicateRecord(record) },
//                        onDelete = { recordToDelete = record }
//                    )
//                }
//            }
//        }
//
//        // Delete Confirmation Dialog
//        if (recordToDelete != null) {
//            AlertDialog(
//                onDismissRequest = { recordToDelete = null },
//                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusUnpaid) },
//                title = { Text("Delete Record?") },
//                text = {
//                    Text("Are you sure you want to permanently delete the bill record for \"${recordToDelete?.billingMonth}\"? This action cannot be undone.")
//                },
//                confirmButton = {
//                    Button(
//                        onClick = {
//                            recordToDelete?.let { onDeleteRecord(it) }
//                            recordToDelete = null
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid),
//                        shape = RoundedCornerShape(8.dp)
//                    ) {
//                        Text("Delete")
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = { recordToDelete = null }) {
//                        Text("Cancel")
//                    }
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun HistoryRecordCard(
//    record: MonthlyRecordEntity,
//    currencySymbol: String,
//    onClick: () -> Unit,
//    onEdit: () -> Unit,
//    onDuplicate: () -> Unit,
//    onDelete: () -> Unit
//) {
//    var menuExpanded by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .testTag("record_card_${record.id}"),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = record.billingMonth,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                    Text(
//                        text = if (record.paymentDate.isNotBlank()) "Paid on: ${record.paymentDate}" else "Not paid",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    PaymentStatusBadge(status = record.paymentStatus)
//                    Box {
//                        IconButton(onClick = { menuExpanded = true }) {
//                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
//                        }
//                        DropdownMenu(
//                            expanded = menuExpanded,
//                            onDismissRequest = { menuExpanded = false }
//                        ) {
//                            DropdownMenuItem(
//                                text = { Text("View Details") },
//                                leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
//                                onClick = {
//                                    menuExpanded = false
//                                    onClick()
//                                }
//                            )
//                            DropdownMenuItem(
//                                text = { Text("Edit Record") },
//                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
//                                onClick = {
//                                    menuExpanded = false
//                                    onEdit()
//                                }
//                            )
//                            DropdownMenuItem(
//                                text = { Text("Duplicate for Next Month") },
//                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
//                                onClick = {
//                                    menuExpanded = false
//                                    onDuplicate()
//                                }
//                            )
//                            HorizontalDivider()
//                            DropdownMenuItem(
//                                text = { Text("Delete", color = StatusUnpaid) },
//                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusUnpaid) },
//                                onClick = {
//                                    menuExpanded = false
//                                    onDelete()
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // Summary row of metrics
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Column {
//                    Text(text = "Rent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//                    Text(
//                        text = FormatUtils.formatMoney(record.roomRent, currencySymbol),
//                        style = MaterialTheme.typography.bodySmall,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//                Column {
//                    Text(text = "Electricity", style = MaterialTheme.typography.labelSmall, color = ElectricityAccent)
//                    Text(
//                        text = "${FormatUtils.formatUnits(record.electricityUnits)} u (${FormatUtils.formatMoney(record.electricityCost, currencySymbol)})",
//                        style = MaterialTheme.typography.bodySmall,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//                if (record.waterUnits > 0 || record.waterCost > 0) {
//                    Column {
//                        Text(text = "Water", style = MaterialTheme.typography.labelSmall, color = WaterAccent)
//                        Text(
//                            text = "${FormatUtils.formatUnits(record.waterUnits)} u (${FormatUtils.formatMoney(record.waterCost, currencySymbol)})",
//                            style = MaterialTheme.typography.bodySmall,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//                if (record.wasteCharge > 0) {
//                    Column {
//                        Text(text = "Waste", style = MaterialTheme.typography.labelSmall, color = WasteAccent)
//                        Text(
//                            text = FormatUtils.formatMoney(record.wasteCharge, currencySymbol),
//                            style = MaterialTheme.typography.bodySmall,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // Total Amount & Remaining
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = "Total: ",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = FormatUtils.formatMoney(record.totalAmount, currencySymbol),
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//
//                if (record.remainingAmount > 0) {
//                    Text(
//                        text = "Due: " + FormatUtils.formatMoney(record.remainingAmount, currencySymbol),
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = StatusUnpaid
//                    )
//                } else {
//                    Text(
//                        text = "Paid: " + FormatUtils.formatMoney(record.amountPaid, currencySymbol),
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.SemiBold,
//                        color = StatusPaid
//                    )
//                }
//            }
//        }
//    }
//}


package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.RoomSelectorBar
import com.example.ui.theme.*
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    records: List<MonthlyRecordEntity>,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onManageRooms: () -> Unit,
    onAddRecord: () -> Unit,
    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
    onEditRecord: (MonthlyRecordEntity) -> Unit,
    onDuplicateRecord: (MonthlyRecordEntity) -> Unit,
    onDeleteRecord: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (List<MonthlyRecordEntity>) -> Unit,
    onExportExcel: (List<MonthlyRecordEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var recordToDelete by remember { mutableStateOf<MonthlyRecordEntity?>(null) }
    var exportMenuExpanded by remember { mutableStateOf(false) }

    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records
        else records.filter { it.billingMonth.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            Column {
                RoomSelectorBar(
                    activeRoom = activeRoom,
                    allRooms = allRooms,
                    onSelectRoom = onSelectRoom,
                    onManageRooms = onManageRooms
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by month...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        IconButton(
                            onClick = { exportMenuExpanded = true },
                            modifier = Modifier.testTag("history_export_csv_button")
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Export CSV",
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
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecord,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("history_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No records found matching \"$searchQuery\"" else "No monthly records yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        currencySymbol = currencySymbol,
                        onClick = { onOpenRecordDetail(record) },
                        onEdit = { onEditRecord(record) },
                        onDuplicate = { onDuplicateRecord(record) },
                        onDelete = { recordToDelete = record }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (recordToDelete != null) {
            AlertDialog(
                onDismissRequest = { recordToDelete = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusUnpaid) },
                title = { Text("Delete Record?") },
                text = {
                    Text("Are you sure you want to permanently delete the bill record for \"${recordToDelete?.billingMonth}\"? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            recordToDelete?.let { onDeleteRecord(it) }
                            recordToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recordToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: MonthlyRecordEntity,
    currencySymbol: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
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
                        text = if (record.paymentDate.isNotBlank()) "Paid on: ${record.paymentDate}" else "Not paid",
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
                                leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Record") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate for Next Month") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = StatusUnpaid) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusUnpaid) },
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
                    Text(text = "Rent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatMoney(record.roomRent, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text(text = "Electricity", style = MaterialTheme.typography.labelSmall, color = ElectricityAccent)
                    Text(
                        text = "${FormatUtils.formatUnits(record.electricityUnits)} u (${FormatUtils.formatMoney(record.electricityCost, currencySymbol)})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (record.waterUnits > 0 || record.waterCost > 0) {
                    Column {
                        Text(text = "Water", style = MaterialTheme.typography.labelSmall, color = WaterAccent)
                        Text(
                            text = "${FormatUtils.formatUnits(record.waterUnits)} u (${FormatUtils.formatMoney(record.waterCost, currencySymbol)})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (record.wasteCharge > 0) {
                    Column {
                        Text(text = "Waste", style = MaterialTheme.typography.labelSmall, color = WasteAccent)
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