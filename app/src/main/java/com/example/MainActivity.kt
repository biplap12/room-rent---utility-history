package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.RoomRentTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
            val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

            val allRooms by viewModel.allRooms.collectAsStateWithLifecycle()
            val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
            val currentRoomRecords by viewModel.currentRoomRecords.collectAsStateWithLifecycle()
            val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
            val latestRecord by viewModel.latestRecord.collectAsStateWithLifecycle()

            val formState by viewModel.formState.collectAsStateWithLifecycle()
            val showRoomDialog by viewModel.showRoomDialog.collectAsStateWithLifecycle()
            val editingRoom by viewModel.editingRoom.collectAsStateWithLifecycle()
            val selectedRecordForDetail by viewModel.selectedRecordForDetail.collectAsStateWithLifecycle()

            val context = LocalContext.current

            RoomRentTheme(themePreference = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = "Room Rent & Utility",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            text = "Offline Meter & Rent Notebook",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                actions = {
                                    if (selectedTab == 0 && latestRecord != null) {
                                        IconButton(
                                            onClick = { viewModel.exportPdf(context, latestRecord!!) },
                                            modifier = Modifier.testTag("top_action_pdf")
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export Bill PDF")
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        }
                    },
                    bottomBar = {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            NavigationBar(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("bottom_navigation_bar"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { viewModel.selectTab(0) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard") },
                                    modifier = Modifier.testTag("nav_dashboard")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = {
                                        viewModel.prepareAddRecord()
                                    },
                                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add") },
                                    label = { Text("Add") },
                                    modifier = Modifier.testTag("nav_add")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { viewModel.selectTab(2) },
                                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                    label = { Text("History") },
                                    modifier = Modifier.testTag("nav_history")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { viewModel.selectTab(3) },
                                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
                                    label = { Text("Statistics") },
                                    modifier = Modifier.testTag("nav_statistics")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 4,
                                    onClick = { viewModel.selectTab(4) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") },
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                activeRoom = activeRoom,
                                allRooms = allRooms,
                                records = currentRoomRecords,
                                latestRecord = latestRecord,
                                currencySymbol = currencySymbol,
                                onSelectRoom = { viewModel.selectRoom(it) },
                                onManageRooms = { viewModel.openNewRoomDialog() },
                                onAddRecord = { viewModel.prepareAddRecord() },
                                onViewHistory = { viewModel.selectTab(2) },
                                onOpenRecordDetail = { viewModel.openRecordDetail(it) },
                                onExportPdf = { viewModel.exportPdf(context, it) }
                            )
                            1 -> AddRecordScreen(
                                formState = formState,
                                activeRoom = activeRoom,
                                currencySymbol = currencySymbol,
                                onUpdateField = { transform -> viewModel.updateFormField(transform) },
                                onSave = { viewModel.saveCurrentRecord() },
                                onCancel = { viewModel.selectTab(0) }
                            )
                            2 -> HistoryScreen(
                                activeRoom = activeRoom,
                                allRooms = allRooms,
                                records = currentRoomRecords,
                                currencySymbol = currencySymbol,
                                onSelectRoom = { viewModel.selectRoom(it) },
                                onManageRooms = { viewModel.openNewRoomDialog() },
                                onAddRecord = { viewModel.prepareAddRecord() },
                                onOpenRecordDetail = { viewModel.openRecordDetail(it) },
                                onEditRecord = { viewModel.prepareEditRecord(it) },
                                onDuplicateRecord = { viewModel.duplicateRecord(it) },
                                onDeleteRecord = { viewModel.deleteRecord(it) },
                                onExportCsv = { viewModel.exportCsv(context) }
                            )
                            3 -> StatisticsScreen(
                                activeRoom = activeRoom,
                                allRooms = allRooms,
                                records = currentRoomRecords,
                                currencySymbol = currencySymbol,
                                onSelectRoom = { viewModel.selectRoom(it) },
                                onManageRooms = { viewModel.openNewRoomDialog() }
                            )
                            4 -> SettingsScreen(
                                currentCurrency = currencySymbol,
                                themeMode = themeMode,
                                activeRoom = activeRoom,
                                allRooms = allRooms,
                                onSelectCurrency = { viewModel.setCurrency(it) },
                                onSelectTheme = { viewModel.setThemeMode(it) },
                                onManageRooms = { viewModel.openNewRoomDialog() },
                                onExportCsv = { viewModel.exportCsv(context) },
                                onExportJson = { viewModel.exportAllJson() },
                                onRestoreJson = { viewModel.restoreFromJson(it) }
                            )
                        }

                        // Manage Rooms Modal Dialog
                        if (showRoomDialog) {
                            ManageRoomsDialog(
                                allRooms = allRooms,
                                activeRoom = activeRoom,
                                editingRoom = editingRoom,
                                currencySymbol = currencySymbol,
                                onSelectRoom = {
                                    viewModel.selectRoom(it)
                                    viewModel.closeRoomDialog()
                                },
                                onSaveRoom = { name, address, rent, elecRate, waterRate, wasteCharge ->
                                    viewModel.saveRoom(name, address, rent, elecRate, waterRate, wasteCharge)
                                },
                                onDeleteRoom = { viewModel.deleteRoom(it) },
                                onClose = { viewModel.closeRoomDialog() }
                            )
                        }

                        // Record Details Statement Dialog
                        if (selectedRecordForDetail != null) {
                            val detailRecord = selectedRecordForDetail!!
                            val roomForRecord = allRooms.find { it.id == detailRecord.roomId } ?: activeRoom
                            RecordDetailDialog(
                                record = detailRecord,
                                room = roomForRecord,
                                currencySymbol = currencySymbol,
                                onEdit = {
                                    viewModel.prepareEditRecord(it)
                                },
                                onDuplicate = {
                                    viewModel.duplicateRecord(it)
                                },
                                onDelete = {
                                    viewModel.deleteRecord(it)
                                },
                                onExportPdf = {
                                    viewModel.exportPdf(context, it)
                                },
                                onClose = { viewModel.closeRecordDetail() }
                            )
                        }
                    }
                }
            }
        }
    }
}
