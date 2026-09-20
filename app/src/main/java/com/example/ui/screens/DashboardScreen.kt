package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.SimpleBarChart
import com.example.ui.theme.*
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.propertyTypeIcon
import com.example.ui.components.propertyTypeLabel

enum class OccupancyFilter(val label: String) {
    ALL("All"),
    OCCUPIED("Occupied"),
    VACANT("Vacant")
}

private enum class PendingAction { NEW_BILL, HISTORY, EXPORT }

@Composable
fun DashboardScreen(
    activeRoom: RoomEntity?,
    allRooms: List<RoomEntity>,
    records: List<MonthlyRecordEntity>,
    allRecords: List<MonthlyRecordEntity>,
    latestRecord: MonthlyRecordEntity?,
    currencySymbol: String,
    onSelectRoom: (RoomEntity) -> Unit,
    onManageRooms: () -> Unit,
    onAddRecord: () -> Unit,
    onViewHistory: () -> Unit,
    onOpenRecordDetail: (MonthlyRecordEntity) -> Unit,
    onExportPdf: (MonthlyRecordEntity) -> Unit,
    onSaveRoom: (RoomEntity) -> Unit = {},
    onDeleteRoom: (RoomEntity) -> Unit = {},
    onExportPdfAll: () -> Unit = {},
    onExportExcel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedRoomId by remember { mutableStateOf(activeRoom?.id) }
    var dropdownOpen by remember { mutableStateOf(false) }
    var occupancyFilter by remember { mutableStateOf(OccupancyFilter.ALL) }
    var detailRoom by remember { mutableStateOf<RoomEntity?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showRoomListExpanded by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // ★ Internal Manage dialog — drives Edit → form directly
    var showEditDialog by remember { mutableStateOf(false) }
    var editTargetRoom by remember { mutableStateOf<RoomEntity?>(null) }

    val isAllRoomsView = selectedRoomId == null
    val selectedRoom = allRooms.firstOrNull { it.id == selectedRoomId }

    val pagerState = rememberPagerState(
        initialPage = if (activeRoom == null) 0
        else (allRooms.indexOfFirst { it.id == activeRoom.id }
            .coerceAtLeast(0) + 1),
        pageCount = { allRooms.size + 1 }
    )

    val scopedRecords = remember(allRecords, selectedRoomId) {
        if (selectedRoomId == null) allRecords
        else allRecords.filter { it.roomId == selectedRoomId }
    }

    val roomIdsWithRecords = remember(allRecords) { allRecords.map { it.roomId }.toSet() }
    val occupiedCount = remember(allRooms, roomIdsWithRecords) {
        allRooms.count { it.id in roomIdsWithRecords }
    }
    val vacantCount = remember(allRooms, roomIdsWithRecords) {
        allRooms.count { it.id !in roomIdsWithRecords }
    }

    val filteredRooms = remember(allRooms, occupancyFilter, roomIdsWithRecords) {
        when (occupancyFilter) {
            OccupancyFilter.ALL -> allRooms
            OccupancyFilter.OCCUPIED -> allRooms.filter { it.id in roomIdsWithRecords }
            OccupancyFilter.VACANT -> allRooms.filter { it.id !in roomIdsWithRecords }
        }
    }

    fun monthly(selector: (MonthlyRecordEntity) -> Double): List<Pair<String, Double>> =
        scopedRecords.groupBy { it.billingMonth }
            .map { (m, r) -> m to r.sumOf(selector) }
            .sortedBy { it.first }
            .takeLast(6)

    val electricityHistory = remember(scopedRecords) { monthly { it.electricityUnits } }
    val waterHistory       = remember(scopedRecords) { monthly { it.waterUnits } }
    val rentHistory        = remember(scopedRecords) { monthly { it.roomRent } }
    val billHistory        = remember(scopedRecords) { monthly { it.totalAmount } }
    val paidHistory        = remember(scopedRecords) { monthly { it.amountPaid } }
    val wasteHistory       = remember(scopedRecords) { monthly { it.wasteCharge } }

    val rentIncomeLoss = remember(rentHistory) {
        if (rentHistory.size < 2) emptyList()
        else {
            rentHistory.zipWithNext().map { (prev, curr) ->
                RentDelta(
                    month = curr.first,
                    current = curr.second,
                    previous = prev.second,
                    delta = curr.second - prev.second
                )
            }
        }
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val thisMonth = remember { monthFormat.format(Date()) }
    val lastMonth = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        monthFormat.format(cal.time)
    }

    val thisMonthRecords = remember(scopedRecords, thisMonth) {
        scopedRecords.filter { it.billingMonth.equals(thisMonth, ignoreCase = true) }
    }
    val lastMonthRecords = remember(scopedRecords, lastMonth) {
        scopedRecords.filter { it.billingMonth.equals(lastMonth, ignoreCase = true) }
    }

    val thisMonthBilled = remember(thisMonthRecords) { thisMonthRecords.sumOf { it.totalAmount } }
    val thisMonthPaid   = remember(thisMonthRecords) { thisMonthRecords.sumOf { it.amountPaid } }
    val thisMonthDue    = remember(thisMonthRecords) { thisMonthRecords.sumOf { it.remainingAmount } }
    val lastMonthBilled = remember(lastMonthRecords) { lastMonthRecords.sumOf { it.totalAmount } }

    val collectionRate = if (thisMonthBilled > 0)
        (thisMonthPaid / thisMonthBilled).coerceIn(0.0, 1.0) else 0.0

    val pendingDues = remember(scopedRecords, allRooms) {
        scopedRecords.filter { it.remainingAmount > 0 }
            .groupBy { it.roomId }
            .map { (roomId, recs) ->
                val room = allRooms.firstOrNull { it.id == roomId }
                Triple(room, recs.sumOf { it.remainingAmount }, recs.size)
            }
            .filter { it.first != null }
            .sortedByDescending { it.second }
    }
    val totalOutstanding = remember(pendingDues) { pendingDues.sumOf { it.second } }

    val topElectricityRooms = remember(allRecords, allRooms) {
        allRooms.map { room ->
            room to allRecords.filter { it.roomId == room.id }.sumOf { it.electricityUnits }
        }.sortedByDescending { it.second }.take(3)
    }
    val topWaterRooms = remember(allRecords, allRooms) {
        allRooms.map { room ->
            room to allRecords.filter { it.roomId == room.id }.sumOf { it.waterUnits }
        }.sortedByDescending { it.second }.take(3)
    }

    val recentRecords = remember(scopedRecords, allRooms) {
        scopedRecords.sortedByDescending { it.updatedAt }.take(4)
            .mapNotNull { rec ->
                val room = allRooms.firstOrNull { it.id == rec.roomId }
                if (room != null) rec to room else null
            }
    }

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR).toString() }
    val yearRecords = remember(scopedRecords, currentYear) {
        scopedRecords.filter { it.billingMonth.endsWith(currentYear) }
    }
    val yearTotalBilled = remember(yearRecords) { yearRecords.sumOf { it.totalAmount } }
    val yearTotalPaid   = remember(yearRecords) { yearRecords.sumOf { it.amountPaid } }
    val yearTotalDue    = remember(yearRecords) { yearRecords.sumOf { it.remainingAmount } }

    val avgBill = remember(scopedRecords) {
        if (scopedRecords.isEmpty()) 0.0
        else scopedRecords.sumOf { it.totalAmount } / scopedRecords.size
    }
    val highestBill = remember(scopedRecords) {
        scopedRecords.maxByOrNull { it.totalAmount }?.totalAmount ?: 0.0
    }
    val lowestBill = remember(scopedRecords) {
        scopedRecords.minOfOrNull { it.totalAmount } ?: 0.0
    }

    val perRoomBreakdown = remember(allRecords, allRooms) {
        allRooms.map { room ->
            val rr = allRecords.filter { it.roomId == room.id }
            RoomBreakdown(
                room = room,
                recordCount = rr.size,
                totalBilled = rr.sumOf { it.totalAmount },
                totalPaid = rr.sumOf { it.amountPaid },
                totalDue = rr.sumOf { it.remainingAmount }
            )
        }.sortedByDescending { it.totalBilled }
    }
    val grandTotalBilled = remember(perRoomBreakdown) { perRoomBreakdown.sumOf { it.totalBilled } }

    val paidCount = remember(scopedRecords) { scopedRecords.count { it.remainingAmount <= 0 } }
    val unpaidCount = remember(scopedRecords) { scopedRecords.count { it.remainingAmount > 0 } }

    val propertyTypeStats = remember(allRooms, allRecords) {
        allRooms.groupBy { it.propertyType.ifBlank { "OTHER" } }
            .map { (type, rooms) ->
                val roomIds = rooms.map { it.id }.toSet()
                val typeRecords = allRecords.filter { it.roomId in roomIds }
                val occupied = rooms.count { room ->
                    allRecords.any { it.roomId == room.id }
                }
                PropertyTypeStat(
                    type = type,
                    total = rooms.size,
                    occupied = occupied,
                    vacant = rooms.size - occupied,
                    billed = typeRecords.sumOf { it.totalAmount },
                    collected = typeRecords.sumOf { it.amountPaid },
                    due = typeRecords.sumOf { it.remainingAmount },
                    share = 0.0
                )
            }
            .sortedByDescending { it.billed }
            .let { list ->
                val grand = list.sumOf { it.billed }
                if (grand > 0) list.map { it.copy(share = it.billed / grand) } else list
            }
    }

    val grandTypeTotal = remember(propertyTypeStats) {
        propertyTypeStats.sumOf { it.total }
    }

    fun redirectToRoomBills(room: RoomEntity) {
        selectedRoomId = room.id
        onSelectRoom(room)
        onViewHistory()
    }

    LaunchedEffect(pagerState, allRooms) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (!isProgrammaticScroll) {
                val newId = if (page == 0) null
                else allRooms.getOrNull(page - 1)?.id
                if (newId != selectedRoomId) {
                    selectedRoomId = newId
                }
            }
        }
    }

    LaunchedEffect(pagerState, allRooms) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page > 0) {
                allRooms.getOrNull(page - 1)?.let { room ->
                    if (room.id != activeRoom?.id) {
                        onSelectRoom(room)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedRoomId, allRooms.size) {
        val targetPage = if (selectedRoomId == null) 0
        else (allRooms.indexOfFirst { it.id == selectedRoomId }
            .coerceAtLeast(0) + 1)
        if (pagerState.settledPage != targetPage &&
            pagerState.targetPage != targetPage) {
            isProgrammaticScroll = true
            pagerState.animateScrollToPage(targetPage)
            isProgrammaticScroll = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // ═══════════════════════════════════════════════════════
        // TOP BAR
        // ═══════════════════════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Surface(
                    onClick = { dropdownOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isAllRoomsView) Icons.Default.Apartment
                            else propertyTypeIcon(selectedRoom?.propertyType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isAllRoomsView) "All Rooms"
                            else selectedRoom?.name ?: "All Rooms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowDropDown, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownOpen,
                    onDismissRequest = { dropdownOpen = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("All Rooms",
                                fontWeight = if (isAllRoomsView) FontWeight.Bold else FontWeight.Normal)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Apartment, null,
                                tint = if (isAllRoomsView) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        onClick = { selectedRoomId = null; dropdownOpen = false }
                    )
                    if (allRooms.isNotEmpty()) HorizontalDivider()
                    allRooms.forEach { room ->
                        val isSel = selectedRoomId == room.id
                        DropdownMenuItem(
                            text = {
                                Text(room.name,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            },
                            leadingIcon = {
                                Icon(propertyTypeIcon(room.propertyType), null,
                                    tint = if (isSel) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            onClick = {
                                selectedRoomId = room.id
                                onSelectRoom(room)
                                dropdownOpen = false
                            }
                        )
                    }
                }
            }

            Surface(
                onClick = onManageRooms,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Add Room",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // FULL-WIDTH ROOM PAGER
        // ═══════════════════════════════════════════════════════
        if (allRooms.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
                beyondViewportPageCount = 1,
                key = { page ->
                    if (page == 0) "all"
                    else allRooms.getOrNull(page - 1)?.id ?: page
                },
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    RoomSliderCard(
                        title = "All Rooms",
                        subtitle = "${allRooms.size} rooms · combined view",
                        billed = allRecords.sumOf { it.totalAmount },
                        collected = allRecords.sumOf { it.amountPaid },
                        due = allRecords.sumOf { it.remainingAmount },
                        currencySymbol = currencySymbol,
                        isActive = isAllRoomsView,
                        roomOccupancy = null,
                        propertyType = null,
                        onClick = { },
                        onInfoClick = null
                    )
                } else {
                    val room = allRooms[page - 1]
                    val rr = allRecords.filter { it.roomId == room.id }
                    val occupied = rr.isNotEmpty()
                    RoomSliderCard(
                        title = room.name,
                        subtitle = room.tenantName.ifBlank { room.address.ifBlank { "—" } },
                        billed = rr.sumOf { it.totalAmount },
                        collected = rr.sumOf { it.amountPaid },
                        due = rr.sumOf { it.remainingAmount },
                        currencySymbol = currencySymbol,
                        isActive = selectedRoomId == room.id,
                        roomOccupancy = occupied,
                        propertyType = room.propertyType,
                        onClick = { },
                        onInfoClick = { detailRoom = room }
                    )
                }
            }

            if (allRooms.size + 1 <= 12) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(allRooms.size + 1) { i ->
                        val isCurrent = i == pagerState.currentPage
                        val w by animateFloatAsState(
                            targetValue = if (isCurrent) 22f else 6f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = w.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        // ═══════════════════════════════════════════════════════
        // OCCUPANCY
        // ═══════════════════════════════════════════════════════
        if (isAllRoomsView && allRooms.isNotEmpty()) {
            SectionHeader(
                title = "Occupancy",
                subtitle = "Tap a card to filter rooms"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OccupancyStatClickable(
                    label = "Total",
                    value = allRooms.size.toString(),
                    icon = Icons.Default.Domain,
                    accent = MaterialTheme.colorScheme.primary,
                    selected = showRoomListExpanded && occupancyFilter == OccupancyFilter.ALL,
                    onClick = {
                        val wasShowing = showRoomListExpanded && occupancyFilter == OccupancyFilter.ALL
                        showRoomListExpanded = !wasShowing
                        occupancyFilter = OccupancyFilter.ALL
                    },
                    modifier = Modifier.weight(1f)
                )
                OccupancyStatClickable(
                    label = "Occupied",
                    value = occupiedCount.toString(),
                    icon = Icons.Default.Person,
                    accent = StatusPaid,
                    selected = occupancyFilter == OccupancyFilter.OCCUPIED && showRoomListExpanded,
                    onClick = {
                        val wasShowing = occupancyFilter == OccupancyFilter.OCCUPIED && showRoomListExpanded
                        showRoomListExpanded = !wasShowing
                        occupancyFilter = if (wasShowing) OccupancyFilter.ALL
                        else OccupancyFilter.OCCUPIED
                    },
                    modifier = Modifier.weight(1f)
                )
                OccupancyStatClickable(
                    label = "Vacant",
                    value = vacantCount.toString(),
                    icon = Icons.Default.MeetingRoom,
                    accent = StatusUnpaid,
                    selected = occupancyFilter == OccupancyFilter.VACANT && showRoomListExpanded,
                    onClick = {
                        val wasShowing = occupancyFilter == OccupancyFilter.VACANT && showRoomListExpanded
                        showRoomListExpanded = !wasShowing
                        occupancyFilter = if (wasShowing) OccupancyFilter.ALL
                        else OccupancyFilter.VACANT
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(visible = showRoomListExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredRooms.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Default.MeetingRoom,
                            message = "No ${occupancyFilter.label.lowercase()} rooms"
                        )
                    } else {
                        filteredRooms.forEach { room ->
                            val roomRecords = allRecords.filter { it.roomId == room.id }
                            RoomOccupancyCardFull(
                                room = room,
                                isActive = room.id == selectedRoomId,
                                isOccupied = roomRecords.isNotEmpty(),
                                currencySymbol = currencySymbol,
                                onClick = {
                                    selectedRoomId = room.id
                                    onSelectRoom(room)
                                    showRoomListExpanded = false
                                },
                                onInfoClick = { detailRoom = room }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        // ═══════════════════════════════════════════════════════
        // PROPERTY TYPE BREAKDOWN
        // ═══════════════════════════════════════════════════════
        if (isAllRoomsView && propertyTypeStats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Property Types",
                subtitle = "Rooms, occupancy & revenue by category"
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendChip("Occupied", StatusPaid)
                        LegendChip("Vacant", StatusUnpaid)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    propertyTypeStats.forEach { stat ->
                        TypeOccupancyRow(
                            stat = stat,
                            grandTotal = grandTypeTotal
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    propertyTypeStats.forEachIndexed { index, stat ->
                        PropertyTypeRow(
                            stat = stat,
                            currencySymbol = currencySymbol
                        )
                        if (index < propertyTypeStats.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // PER-ROOM BREAKDOWN
        // ═══════════════════════════════════════════════════════
        if (isAllRoomsView && perRoomBreakdown.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            SectionHeader(
                title = "Per-Room Breakdown",
                subtitle = "Tap a room to view its bills"
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    perRoomBreakdown.forEachIndexed { index, item ->
                        RoomBreakdownRow(
                            item = item,
                            grandTotal = grandTotalBilled,
                            currencySymbol = currencySymbol,
                            onClick = { redirectToRoomBills(item.room) }
                        )
                        if (index < perRoomBreakdown.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // ALERTS
        // ═══════════════════════════════════════════════════════
        if (totalOutstanding > 0 || thisMonthDue > 0) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (totalOutstanding > 0) {
                    AlertBanner(
                        icon = Icons.Default.Warning,
                        title = "Outstanding dues",
                        subtitle = "${pendingDues.size} room(s) · " +
                                FormatUtils.formatMoney(totalOutstanding, currencySymbol),
                        accent = StatusUnpaid,
                        onClick = onViewHistory
                    )
                }
                if (thisMonthDue > 0) {
                    AlertBanner(
                        icon = Icons.Default.Schedule,
                        title = "Unpaid this month",
                        subtitle = FormatUtils.formatMoney(thisMonthDue, currencySymbol) + " still due",
                        accent = StatusPartiallyPaid,
                        onClick = onViewHistory
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // QUICK ACTIONS
        // ═══════════════════════════════════════════════════════
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                icon = Icons.Default.Add, label = "New Bill",
                accent = MaterialTheme.colorScheme.primary,
                onClick = {
                    if (isAllRoomsView && allRooms.isNotEmpty()) {
                        pendingAction = PendingAction.NEW_BILL
                    } else {
                        onAddRecord()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.History, label = "History",
                accent = ElectricityAccent,
                onClick = {
                    if (isAllRoomsView && allRooms.isNotEmpty()) {
                        pendingAction = PendingAction.HISTORY
                    } else {
                        onViewHistory()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.PictureAsPdf, label = "Export",
                accent = WaterAccent,
                onClick = {
                    if (isAllRoomsView && allRooms.isNotEmpty()) {
                        showExportDialog = true
                    } else {
                        latestRecord?.let { onExportPdf(it) }
                    }
                },
                enabled = if (isAllRoomsView) allRecords.isNotEmpty()
                else latestRecord != null,
                modifier = Modifier.weight(1f)
            )
        }

        // ═══════════════════════════════════════════════════════
        // COLLECTION PROGRESS
        // ═══════════════════════════════════════════════════════
        if (thisMonthBilled > 0) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Collection Progress",
                subtitle = "This month · $thisMonth" + scopeSuffix(isAllRoomsView, selectedRoom)
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CollectionRing(
                        progress = collectionRate.toFloat(),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        CollectionRow("Billed",
                            FormatUtils.formatMoney(thisMonthBilled, currencySymbol),
                            MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        CollectionRow("Collected",
                            FormatUtils.formatMoney(thisMonthPaid, currencySymbol),
                            StatusPaid)
                        Spacer(modifier = Modifier.height(8.dp))
                        CollectionRow("Due",
                            FormatUtils.formatMoney(thisMonthDue, currencySymbol),
                            if (thisMonthDue > 0) StatusUnpaid else StatusPaid)
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // MONTH OVER MONTH
        // ═══════════════════════════════════════════════════════
        if (thisMonthBilled > 0 || lastMonthBilled > 0) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Month over Month",
                subtitle = "Compare with previous month")

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ComparisonStat(
                    label = lastMonth,
                    value = FormatUtils.formatMoney(lastMonthBilled, currencySymbol),
                    accent = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                ComparisonStat(
                    label = thisMonth,
                    value = FormatUtils.formatMoney(thisMonthBilled, currencySymbol),
                    accent = MaterialTheme.colorScheme.primary,
                    previousValue = lastMonthBilled,
                    currentValue = thisMonthBilled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ═══════════════════════════════════════════════════════
        // YEAR TO DATE
        // ═══════════════════════════════════════════════════════
        if (yearRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Year to Date",
                subtitle = "Totals for $currentYear" + scopeSuffix(isAllRoomsView, selectedRoom)
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YearStat("Billed",
                            FormatUtils.formatMoney(yearTotalBilled, currencySymbol),
                            MaterialTheme.colorScheme.primary,
                            Modifier.weight(1f))
                        YearStat("Collected",
                            FormatUtils.formatMoney(yearTotalPaid, currencySymbol),
                            StatusPaid,
                            Modifier.weight(1f))
                        YearStat("Due",
                            FormatUtils.formatMoney(yearTotalDue, currencySymbol),
                            StatusUnpaid,
                            Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${yearRecords.size} bills recorded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Avg ${FormatUtils.formatMoney(avgBill, currencySymbol)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // PENDING DUES
        // ═══════════════════════════════════════════════════════
        if (pendingDues.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Pending Dues",
                subtitle = when {
                    isAllRoomsView -> "Tap a room to open its bill payment page"
                    selectedRoom != null -> "Unpaid balances for ${selectedRoom.name}"
                    else -> "Rooms with unpaid balances"
                }
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    pendingDues.forEachIndexed { index, (room, due, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { room?.let { redirectToRoomBills(it) } }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                StatusUnpaid.copy(alpha = 0.22f),
                                                StatusUnpaid.copy(alpha = 0.08f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(propertyTypeIcon(room?.propertyType), null,
                                    tint = StatusUnpaid, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(room?.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Text("$count unpaid bill(s) · tap to open",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(FormatUtils.formatMoney(due, currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusUnpaid)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronRight, null,
                                tint = StatusUnpaid.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp))
                        }
                        if (index < pendingDues.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // RENT INCOME VS LOSS
        // ═══════════════════════════════════════════════════════
        if (rentIncomeLoss.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Rent Income vs Loss",
                subtitle = "Change in rent collected month over month"
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendChip("Income ↑", StatusPaid)
                        LegendChip("Loss ↓", StatusUnpaid)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    RentIncomeLossChart(
                        data = rentIncomeLoss,
                        currencySymbol = currencySymbol,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalGain = rentIncomeLoss.sumOf { if (it.delta > 0) it.delta else 0.0 }
                    val totalLoss = rentIncomeLoss.sumOf { if (it.delta < 0) -it.delta else 0.0 }
                    val netDelta = totalGain - totalLoss

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total gain",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+${FormatUtils.formatMoney(totalGain, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid)
                        }
                        Column {
                            Text("Total loss",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("−${FormatUtils.formatMoney(totalLoss, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusUnpaid)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net change",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = (if (netDelta >= 0) "+" else "−") +
                                        FormatUtils.formatMoney(kotlin.math.abs(netDelta), currencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netDelta >= 0) StatusPaid else StatusUnpaid
                            )
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // USAGE TRENDS
        // ═══════════════════════════════════════════════════════
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Usage Trends",
            subtitle = when {
                isAllRoomsView -> "Last 6 months · All rooms combined"
                selectedRoom != null -> "Last 6 months · ${selectedRoom.name}"
                else -> "Last 6 months at a glance"
            }
        )

        ChartCard(
            title = "Electricity",
            subtitle = "Monthly consumption",
            totalLabel = FormatUtils.formatUnits(
                scopedRecords.sumOf { it.electricityUnits }) + " units total",
            accent = ElectricityAccent,
            icon = Icons.Default.Bolt
        ) {
            SimpleBarChart(data = electricityHistory, barColor = ElectricityAccent, unitLabel = "units")
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            title = "Water",
            subtitle = "Monthly consumption",
            totalLabel = FormatUtils.formatUnits(
                scopedRecords.sumOf { it.waterUnits }) + " units total",
            accent = WaterAccent,
            icon = Icons.Default.WaterDrop
        ) {
            SimpleBarChart(data = waterHistory, barColor = WaterAccent, unitLabel = "units")
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            title = "Room Rent",
            subtitle = "Base rent per month",
            totalLabel = FormatUtils.formatMoney(
                scopedRecords.sumOf { it.roomRent }, currencySymbol) + " total",
            accent = RentAccent,
            icon = Icons.Default.Bed
        ) {
            SimpleBarChart(data = rentHistory, barColor = RentAccent, unitLabel = currencySymbol)
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            title = "Total Billed",
            subtitle = "Monthly total amounts",
            totalLabel = FormatUtils.formatMoney(
                scopedRecords.sumOf { it.totalAmount }, currencySymbol) + " total",
            accent = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Payments
        ) {
            SimpleBarChart(data = billHistory, barColor = MaterialTheme.colorScheme.primary,
                unitLabel = currencySymbol)
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChartCard(
            title = "Amount Collected",
            subtitle = "Monthly payments received",
            totalLabel = FormatUtils.formatMoney(
                scopedRecords.sumOf { it.amountPaid }, currencySymbol) + " total",
            accent = StatusPaid,
            icon = Icons.Default.CheckCircle
        ) {
            SimpleBarChart(data = paidHistory, barColor = StatusPaid, unitLabel = currencySymbol)
        }

        if (scopedRecords.any { it.wasteCharge > 0 }) {
            Spacer(modifier = Modifier.height(14.dp))

            ChartCard(
                title = "Waste Charges",
                subtitle = "Monthly waste / minimum fees",
                totalLabel = FormatUtils.formatMoney(
                    scopedRecords.sumOf { it.wasteCharge }, currencySymbol) + " total",
                accent = WasteAccent,
                icon = Icons.Default.Delete
            ) {
                SimpleBarChart(data = wasteHistory, barColor = WasteAccent, unitLabel = currencySymbol)
            }
        }

        // ═══════════════════════════════════════════════════════
        // PAYMENT STATUS DONUT
        // ═══════════════════════════════════════════════════════
        if (scopedRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Payment Status",
                subtitle = "Paid vs unpaid bills"
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(124.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PaymentStatusDonut(
                            paid = paidCount.toFloat(),
                            unpaid = unpaidCount.toFloat(),
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(scopedRecords.size.toString(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("bills",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.width(22.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LegendBar("Paid", paidCount.toString(), StatusPaid)
                        LegendBar("Unpaid", unpaidCount.toString(), StatusUnpaid)
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // BILL STATISTICS
        // ═══════════════════════════════════════════════════════
        if (scopedRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Bill Statistics",
                subtitle = "Average, highest, lowest"
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OccupancyStat("Avg Bill",
                    FormatUtils.formatMoney(avgBill, currencySymbol),
                    Icons.Default.TrendingUp,
                    MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f))
                OccupancyStat("Highest",
                    FormatUtils.formatMoney(highestBill, currencySymbol),
                    Icons.Default.ArrowUpward,
                    StatusUnpaid,
                    Modifier.weight(1f))
                OccupancyStat("Lowest",
                    FormatUtils.formatMoney(lowestBill, currencySymbol),
                    Icons.Default.ArrowDownward,
                    StatusPaid,
                    Modifier.weight(1f))
            }
        }

        // ═══════════════════════════════════════════════════════
        // TOP CONSUMERS
        // ═══════════════════════════════════════════════════════
        if (topElectricityRooms.any { it.second > 0 }) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Top Consumers",
                subtitle = "Highest cumulative usage (all rooms)"
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TopConsumerCard(
                    title = "Electricity",
                    icon = Icons.Default.Bolt,
                    accent = ElectricityAccent,
                    entries = topElectricityRooms,
                    onRoomClick = { redirectToRoomBills(it) },
                    modifier = Modifier.weight(1f)
                )
                TopConsumerCard(
                    title = "Water",
                    icon = Icons.Default.WaterDrop,
                    accent = WaterAccent,
                    entries = topWaterRooms,
                    onRoomClick = { redirectToRoomBills(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ═══════════════════════════════════════════════════════
        // RECENT ACTIVITY
        // ═══════════════════════════════════════════════════════
        if (recentRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Recent Activity",
                subtitle = when {
                    isAllRoomsView -> "Latest bills across all rooms"
                    selectedRoom != null -> "Latest bills for ${selectedRoom.name}"
                    else -> "Latest bills you've added"
                }
            )

            BeautifulCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    recentRecords.forEachIndexed { index, (rec, room) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isAllRoomsView) {
                                        selectedRoomId = room.id
                                        onSelectRoom(room)
                                    }
                                    onOpenRecordDetail(rec)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(room.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Text(rec.billingMonth,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(FormatUtils.formatMoney(rec.totalAmount, currencySymbol),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                PaymentStatusBadge(status = rec.paymentStatus)
                            }
                        }
                        if (index < recentRecords.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // LATEST BILL
        // ═══════════════════════════════════════════════════════
        if (latestRecord != null && !isAllRoomsView) {
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Latest Bill",
                subtitle = selectedRoom?.name ?: "Current room"
            )

            BeautifulCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenRecordDetail(latestRecord) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(latestRecord.billingMonth,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(FormatUtils.formatMoney(latestRecord.totalAmount, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        PaymentStatusBadge(status = latestRecord.paymentStatus)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (latestRecord.remainingAmount > 0)
                                "Due ${FormatUtils.formatMoney(latestRecord.remainingAmount, currencySymbol)}"
                            else "Paid in full",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (latestRecord.remainingAmount > 0) StatusUnpaid else StatusPaid,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ROOM DETAILS DIALOG
    // ═══════════════════════════════════════════════════════════
    detailRoom?.let { room ->
        RoomDetailsDialog(
            room = room,
            currencySymbol = currencySymbol,
            allRooms = allRooms,
            onSwitchTo = { picked ->
                onSelectRoom(picked)
                detailRoom = picked
            },
            onClose = { detailRoom = null },
            onEdit = {
                detailRoom = null
                editTargetRoom = room
                showEditDialog = true
            }
        )
    }

    // ═══════════════════════════════════════════════════════════
    // ★ INTERNAL MANAGE DIALOG — hosted here so Edit is direct
    // ═══════════════════════════════════════════════════════════
    if (showEditDialog) {
        ManageRoomsDialog(
            allRooms = allRooms,
            activeRoom = activeRoom,
            editingRoom = editTargetRoom,
            currencySymbol = currencySymbol,
            onSelectRoom = { room ->
                selectedRoomId = room.id
                onSelectRoom(room)
            },
            onSaveRoom = { room -> onSaveRoom(room) },
            onDeleteRoom = { room -> onDeleteRoom(room) },
            onClose = {
                showEditDialog = false
                editTargetRoom = null
            }
        )
    }

    // ═══════════════════════════════════════════════════════════
    // ROOM PICKER
    // ═══════════════════════════════════════════════════════════
    pendingAction?.let { action ->
        RoomPickerDialog(
            title = when (action) {
                PendingAction.NEW_BILL -> "New Bill for Which Room?"
                PendingAction.HISTORY -> "View History of Which Room?"
                PendingAction.EXPORT -> "Export Which Room?"
            },
            allRooms = allRooms,
            allRecords = allRecords,
            currencySymbol = currencySymbol,
            onDismiss = { pendingAction = null },
            onSelectRoom = { room ->
                selectedRoomId = room.id
                onSelectRoom(room)
                pendingAction = null

                when (action) {
                    PendingAction.NEW_BILL -> onAddRecord()
                    PendingAction.HISTORY -> onViewHistory()
                    PendingAction.EXPORT -> latestRecord?.let { onExportPdf(it) }
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════════
    // EXPORT DIALOG
    // ═══════════════════════════════════════════════════════════
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column {
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose a format to download your records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {

                    ExportOption(
                        title = "PDF Report",
                        subtitle = "Full formatted statement for printing",
                        icon = Icons.Default.PictureAsPdf,
                        baseColor = Color(0xFFF43F5E),
                        onClick = {
                            showExportDialog = false
                            onExportPdfAll()
                            Toast.makeText(
                                context,
                                "PDF exported successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExportOption(
                        title = "Excel Workbook",
                        subtitle = "Editable .xlsx file, one sheet per room",
                        icon = Icons.Default.TableChart,
                        baseColor = Color(0xFF10B981),
                        onClick = {
                            showExportDialog = false
                            onExportExcel()
                            Toast.makeText(
                                context,
                                "Excel exported successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════════════════════════ */
private fun scopeSuffix(isAllRooms: Boolean, room: RoomEntity?): String =
    when {
        isAllRooms -> " · All Rooms"
        room != null -> " · ${room.name}"
        else -> ""
    }

private data class RoomBreakdown(
    val room: RoomEntity,
    val recordCount: Int,
    val totalBilled: Double,
    val totalPaid: Double,
    val totalDue: Double
)

private data class RentDelta(
    val month: String,
    val current: Double,
    val previous: Double,
    val delta: Double
)

private data class PropertyTypeStat(
    val type: String,
    val total: Int,
    val occupied: Int,
    val vacant: Int,
    val billed: Double,
    val collected: Double,
    val due: Double,
    val share: Double
)

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    message: String
) {
    BeautifulCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ExportOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    baseColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = baseColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                baseColor,
                                baseColor.copy(alpha = 0.75f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = baseColor.copy(alpha = 0.95f))
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.ChevronRight, null,
                tint = baseColor.copy(alpha = 0.7f))
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   RENT INCOME vs LOSS CHART
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RentIncomeLossChart(
    data: List<RentDelta>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val gainColor = StatusPaid
    val lossColor = StatusUnpaid
    val zeroLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val padX = 24f
        val padTop = 16f
        val padBottom = 40f

        val chartTop = padTop
        val chartBottom = h - padBottom
        val chartHeight = chartBottom - chartTop
        val centerY = chartTop + chartHeight / 2f

        val maxAbs = data.maxOf { kotlin.math.abs(it.delta) }.coerceAtLeast(1.0)
        val n = data.size
        val slotW = (w - padX * 2f) / n
        val barW = slotW * 0.55f
        val maxBarHalf = (chartHeight / 2f) - 8f

        drawLine(
            color = zeroLineColor,
            start = Offset(padX, centerY),
            end = Offset(w - padX, centerY),
            strokeWidth = 1.5f
        )

        data.forEachIndexed { i, item ->
            val cx = padX + slotW * i + slotW / 2f
            val value = item.delta
            val barH = ((kotlin.math.abs(value) / maxAbs) * maxBarHalf).toFloat().coerceAtLeast(3f)
            val isGain = value >= 0

            val left = cx - barW / 2f
            val top = if (isGain) centerY - barH else centerY
            val bottom = if (isGain) centerY else centerY + barH

            val baseColor = if (isGain) gainColor else lossColor

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isGain) {
                        listOf(baseColor, baseColor.copy(alpha = 0.7f))
                    } else {
                        listOf(baseColor.copy(alpha = 0.7f), baseColor)
                    }
                ),
                topLeft = Offset(left, top),
                size = Size(barW, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   ROOM PICKER DIALOG
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RoomPickerDialog(
    title: String,
    allRooms: List<RoomEntity>,
    allRecords: List<MonthlyRecordEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSelectRoom: (RoomEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Apartment, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allRooms.forEach { room ->
                    val rr = allRecords.filter { it.roomId == room.id }
                    Surface(
                        onClick = { onSelectRoom(room) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(propertyTypeIcon(room.propertyType), null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(room.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if (rr.isNotEmpty())
                                        "${rr.size} bill(s) · ${room.tenantName.ifBlank { "—" }}"
                                    else "No bills yet",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/* ═══════════════════════════════════════════════════════════════
   ROOM SLIDER CARD
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RoomSliderCard(
    title: String,
    subtitle: String,
    billed: Double,
    collected: Double,
    due: Double,
    currencySymbol: String,
    isActive: Boolean,
    roomOccupancy: Boolean? = null,
    propertyType: String? = null,
    onClick: () -> Unit,
    onInfoClick: (() -> Unit)?
) {
    val accent = MaterialTheme.colorScheme.primary

    val statusColor = when (roomOccupancy) {
        true  -> StatusPaid
        false -> StatusUnpaid
        null  -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = when (roomOccupancy) {
        true  -> "Occupied"
        false -> "Vacant"
        null  -> "Combined"
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
            else     -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isActive) 8f else 2f,
        animationSpec = tween(300),
        label = "heroElev"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = accent.copy(alpha = 0.20f),
                spotColor = accent.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = if (isActive) 0.10f else 0.05f),
                        accent.copy(alpha = 0.02f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LeadingIconTile(
                    icon = propertyTypeIcon(propertyType),
                    accent = accent,
                    isActive = isActive
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            fontSize = 10.sp
                        )
                        if (subtitle.isNotBlank() && roomOccupancy != null) {
                            Text(
                                text = " · $subtitle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (onInfoClick != null) {
                    Surface(
                        onClick = onInfoClick,
                        shape = CircleShape,
                        color = accent.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Room Details",
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SliderStatCol(
                    "Billed", billed, currencySymbol,
                    MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f)
                )
                SliderStatCol(
                    "Collected", collected, currencySymbol,
                    StatusPaid,
                    Modifier.weight(1f)
                )
                SliderStatCol(
                    "Due", due, currencySymbol,
                    StatusUnpaid,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SliderStatCol(
    label: String,
    value: Double,
    currencySymbol: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = FormatUtils.formatMoney(value, currencySymbol),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   ROOM DETAILS DIALOG (compact, Luxe themed)
   ═══════════════════════════════════════════════════════════════ */


@Composable
private fun CompactSectionLabel(text: String, hint: String? = null) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 12.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(accent)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 1.sp,
            fontSize = 10.sp
        )
        if (hint != null) {
            Spacer(Modifier.weight(1f))
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun CompactDetail(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompactSpacer() {
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun CompactPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(accent, accent.copy(alpha = 0.85f))
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CompactSecondaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
        modifier = modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                color = accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   OCCUPANCY STAT
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun OccupancyStatClickable(
    label: String, value: String, icon: ImageVector,
    accent: Color, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        animationSpec = tween(250),
        label = "occBorder"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 0.14f else 0.04f,
        animationSpec = tween(250),
        label = "occBgAlpha"
    )
    val elevation by animateFloatAsState(
        targetValue = if (selected) 8f else 0f,
        animationSpec = tween(250),
        label = "occElev"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "occScale"
    )

    Box(
        modifier = modifier
            .height(116.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = accent.copy(alpha = 0.30f),
                spotColor = accent.copy(alpha = 0.30f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = bgAlpha),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.30f),
                                accent.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.9.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun OccupancyStat(
    label: String, value: String, icon: ImageVector,
    accent: Color, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(116.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.30f),
                                accent.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.9.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   FULL-WIDTH ROOM CARD
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RoomOccupancyCardFull(
    room: RoomEntity,
    isActive: Boolean,
    isOccupied: Boolean,
    currencySymbol: String,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val statusColor = if (isOccupied) StatusPaid else StatusUnpaid
    val statusLabel = if (isOccupied) "Occupied" else "Vacant"

    val borderColor = when {
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        isOccupied -> StatusPaid.copy(alpha = 0.35f)
        else -> StatusUnpaid.copy(alpha = 0.35f)
    }

    val elevation by animateFloatAsState(
        targetValue = if (isActive) 5f else 1f,
        animationSpec = tween(250),
        label = "occCardElev"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = statusColor.copy(alpha = 0.15f),
                spotColor = statusColor.copy(alpha = 0.18f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                statusColor.copy(alpha = 0.22f),
                                statusColor.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        statusColor.copy(alpha = 0.20f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(propertyTypeIcon(room.propertyType), null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(room.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(
                    text = room.tenantName.ifBlank {
                        room.address.ifBlank { "No tenant" }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp)
                    }
                }
            }

            Surface(
                onClick = onInfoClick,
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.10f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Info, contentDescription = "Info",
                        tint = statusColor,
                        modifier = Modifier.size(18.dp))
                }
            }

            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp))
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Room breakdown row
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RoomBreakdownRow(
    item: RoomBreakdown, grandTotal: Double,
    currencySymbol: String, onClick: () -> Unit
) {
    val share = if (grandTotal > 0) item.totalBilled / grandTotal else 0.0
    val sharePct = (share * 100).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(propertyTypeIcon(item.room.propertyType), null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.room.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Text("$sharePct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(share.toFloat().coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${item.recordCount} bill(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Due: ${FormatUtils.formatMoney(item.totalDue, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.totalDue > 0) StatusUnpaid else StatusPaid,
                    fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(FormatUtils.formatMoney(item.totalBilled, currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text("billed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Occupancy-per-type horizontal bar
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun TypeOccupancyRow(
    stat: PropertyTypeStat,
    grandTotal: Int
) {
    val occupiedFrac = if (stat.total > 0) stat.occupied.toFloat() / stat.total else 0f
    val vacantFrac = if (stat.total > 0) stat.vacant.toFloat() / stat.total else 0f
    val countFrac = if (grandTotal > 0) stat.total.toFloat() / grandTotal else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    propertyTypeIcon(stat.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    propertyTypeLabel(stat.type),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${stat.total} total · ${(countFrac * 100).toInt()}% of rooms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${stat.occupied}/${stat.total}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = StatusPaid
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (occupiedFrac > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(occupiedFrac)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        StatusPaid.copy(alpha = 0.85f),
                                        StatusPaid
                                    )
                                )
                            )
                    )
                }
                if (vacantFrac > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(
                                (vacantFrac / (occupiedFrac + vacantFrac).coerceAtLeast(0.001f))
                            )
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        StatusUnpaid.copy(alpha = 0.65f),
                                        StatusUnpaid.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Revenue row per property type
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun PropertyTypeRow(
    stat: PropertyTypeStat,
    currencySymbol: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    propertyTypeIcon(stat.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        propertyTypeLabel(stat.type),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${(stat.share * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(stat.share.toFloat().coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Collected: ${FormatUtils.formatMoney(stat.collected, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusPaid,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Due: ${FormatUtils.formatMoney(stat.due, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stat.due > 0) StatusUnpaid else StatusPaid,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    FormatUtils.formatMoney(stat.billed, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "billed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Quick action tile
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun QuickActionTile(
    icon: ImageVector, label: String, accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true
) {
    val effectiveAccent = if (enabled) accent else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 3.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = effectiveAccent.copy(alpha = 0.20f),
                spotColor = effectiveAccent.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        effectiveAccent.copy(alpha = if (enabled) 0.08f else 0.03f),
                        Color.Transparent
                    )
                )
            )
            .border(
                1.dp,
                if (enabled) effectiveAccent.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                effectiveAccent.copy(alpha = if (enabled) 0.22f else 0.10f),
                                effectiveAccent.copy(alpha = if (enabled) 0.08f else 0.04f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        effectiveAccent.copy(alpha = if (enabled) 0.22f else 0.10f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null,
                    tint = effectiveAccent,
                    modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(9.dp))
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.outline)
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Chart card wrapper
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ChartCard(
    title: String, subtitle: String, totalLabel: String,
    accent: Color, icon: ImageVector,
    content: @Composable () -> Unit
) {
    BeautifulCard(
        modifier = Modifier.padding(horizontal = 16.dp),
        accent = accent
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.25f),
                                    accent.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            accent.copy(alpha = 0.20f),
                            RoundedCornerShape(11.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accent.copy(alpha = 0.10f)
            ) {
                Text(totalLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Alert banner
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun AlertBanner(
    icon: ImageVector, title: String, subtitle: String,
    accent: Color, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = accent.copy(alpha = 0.22f),
                spotColor = accent.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.14f),
                        accent.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                1.dp,
                accent.copy(alpha = 0.30f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.28f),
                                accent.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = accent)
                Text(subtitle, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Collection ring
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun CollectionRing(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900),
        label = "ringProgress"
    )
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val progressColor = if (animated >= 0.99f) StatusPaid
    else MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.11f
            drawCircle(color = trackColor, style = Stroke(width = stroke, cap = StrokeCap.Round))

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        progressColor.copy(alpha = 0.75f),
                        progressColor,
                        progressColor.copy(alpha = 0.85f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = animated * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(animated * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text("collected", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CollectionRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = color)
    }
}

/* ═══════════════════════════════════════════════════════════════
   COMPARISON STAT
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun ComparisonStat(
    label: String, value: String, accent: Color,
    previousValue: Double = 0.0, currentValue: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val delta = when {
        previousValue == 0.0 || currentValue == 0.0 -> null
        else -> (currentValue - previousValue) / previousValue
    }
    Box(
        modifier = modifier
            .height(120.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = accent.copy(alpha = 0.15f),
                spotColor = accent.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(value, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = accent,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (delta != null) {
                val up = delta > 0
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (up) StatusUnpaid else StatusPaid).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (up) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null,
                            tint = if (up) StatusUnpaid else StatusPaid,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${if (up) "+" else ""}${(delta * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (up) StatusUnpaid else StatusPaid,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "vs prev",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            } else {
                Text(
                    "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun YearStat(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = accent,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TopConsumerCard(
    title: String, icon: ImageVector, accent: Color,
    entries: List<Pair<RoomEntity, Double>>,
    onRoomClick: (RoomEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BeautifulCard(modifier = modifier, accent = accent) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.25f),
                                    accent.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            val max = entries.maxOfOrNull { it.second } ?: 0.0
            entries.forEachIndexed { index, (room, value) ->
                if (value > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRoomClick(room) }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(room.name,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(if (max > 0) (value / max).toFloat() else 0f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    accent.copy(alpha = 0.75f),
                                                    accent
                                                )
                                            )
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(FormatUtils.formatUnits(value),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent)
                    }
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   PAYMENT STATUS DONUT
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun PaymentStatusDonut(
    paid: Float, unpaid: Float,
    modifier: Modifier = Modifier
) {
    val total = (paid + unpaid).coerceAtLeast(0.0001f)
    val paidSweep = (paid / total) * 360f

    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    Canvas(modifier = modifier) {
        val stroke = 22.dp.toPx()
        val radius = size.minDimension / 2f - stroke / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = trackColor, radius = radius, center = center,
            style = Stroke(width = stroke))

        if (paidSweep > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        StatusPaid.copy(alpha = 0.7f),
                        StatusPaid
                    )
                ),
                startAngle = -90f, sweepAngle = paidSweep - 3f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }
        if (paidSweep < 360f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        StatusUnpaid.copy(alpha = 0.7f),
                        StatusUnpaid
                    )
                ),
                startAngle = -90f + paidSweep,
                sweepAngle = (360f - paidSweep) - 3f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }
    }
}

@Composable
private fun LegendBar(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

/* ═══════════════════════════════════════════════════════════════
   LEADING ICON TILE
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun LeadingIconTile(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    corner: Dp = 14.dp
) {
    val isDark = isSystemInDarkTheme()
    val themedAccent = if (isDark) lerp(accent, Color.White, 0.18f) else accent

    val topAlpha by animateFloatAsState(
        targetValue = when {
            isActive -> 0.34f
            isDark  -> 0.24f
            else    -> 0.20f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileTopAlpha"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = when {
            isActive -> 0.14f
            isDark  -> 0.09f
            else    -> 0.06f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileBottomAlpha"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = when {
            isActive -> 0.45f
            isDark  -> 0.22f
            else    -> 0.16f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileRing"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tileScale"
    )

    val shape = RoundedCornerShape(corner)
    val plateColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Transparent

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isActive) 4.dp else 0.dp,
                shape = shape,
                ambientColor = themedAccent.copy(alpha = 0.35f),
                spotColor = themedAccent.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(plateColor)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        themedAccent.copy(alpha = topAlpha),
                        themedAccent.copy(alpha = bottomAlpha)
                    )
                )
            )
            .border(1.dp, themedAccent.copy(alpha = ringAlpha), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themedAccent,
            modifier = Modifier.size(iconSize)
        )
    }
}