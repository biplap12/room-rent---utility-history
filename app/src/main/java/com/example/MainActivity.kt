//package com.example
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.viewModels
//import androidx.compose.material.icons.Icons
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.example.ui.screens.*
//import com.example.ui.theme.RoomRentTheme
//import com.example.viewmodel.MainViewModel
//import androidx.compose.foundation.background
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.draw.clip
//import androidx.compose.material.icons.filled.AccountCircle
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Apartment
//import androidx.compose.material.icons.filled.Settings
//import com.example.data.MonthlyRecordEntity
//import com.example.data.RoomEntity
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Handler
//import android.os.Looper
//import androidx.activity.result.contract.ActivityResultContracts
//import com.example.util.NotificationHelper
//import com.example.util.PaymentReminderScheduler
//import com.example.ui.components.ExitConfirmHandler
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.ui.draw.shadow
//import androidx.compose.animation.animateColor
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.animateDp
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.core.updateTransition
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.BarChart
//import androidx.compose.material.icons.filled.Dashboard
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.animation.core.animateFloatAsState
//
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.graphics.vector.ImageVector
//import com.example.security.AppLockPrefs
//
//
//class MainActivity : ComponentActivity() {
//    private val viewModel: MainViewModel by viewModels()
//    private val showSplashState = mutableStateOf(true)
//
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(null)
//        enableEdgeToEdge()
//        NotificationHelper.ensureChannels(this)
//        Handler(Looper.getMainLooper()).postDelayed({
//            showSplashState.value = false
//        }, 3200L)
//
//        // Request notification permission on Android 13+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val launcher = registerForActivityResult(
//                ActivityResultContracts.RequestPermission()
//            ) { /* granted -> */ }
//
//            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//
//        setContent {
//            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
//            val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
//            val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
//
//            val allRooms by viewModel.allRooms.collectAsStateWithLifecycle()
//            val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
//            val currentRoomRecords by viewModel.currentRoomRecords.collectAsStateWithLifecycle()
//            val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
//            val latestRecord by viewModel.latestRecord.collectAsStateWithLifecycle()
//
//            val formState by viewModel.formState.collectAsStateWithLifecycle()
//            val showRoomDialog by viewModel.showRoomDialog.collectAsStateWithLifecycle()
//            val editingRoom by viewModel.editingRoom.collectAsStateWithLifecycle()
//            val selectedRecordForDetail by viewModel.selectedRecordForDetail.collectAsStateWithLifecycle()
//
//            val context = LocalContext.current
//            val remindersEnabled by viewModel.paymentRemindersEnabled.collectAsStateWithLifecycle()
//            val deleteProtectionOn by viewModel.deleteProtectionEnabled.collectAsState()
//            val hasDeletePin by viewModel.hasDeletePin.collectAsState()
//
//            // app lock
//            var lockConfig by remember { mutableStateOf(AppLockPrefs.load(context)) }
//            var isLocked by remember { mutableStateOf(lockConfig.enabled) }
//            var showForgot by remember { mutableStateOf(false) }
//
//            // Re-check lock when config changes (e.g. from Settings)
//            LaunchedEffect(lockConfig.enabled) {
//                isLocked = lockConfig.enabled
//            }
//            LaunchedEffect(remindersEnabled) {
//                if (remindersEnabled) PaymentReminderScheduler.schedule(context)
//                else PaymentReminderScheduler.cancel(context)
//            }
//
//            var showProfileDialog by remember { mutableStateOf(false) }
//
//            RoomRentTheme(themePreference = themeMode) {
//                ExitConfirmHandler(
//                    currentTab = selectedTab,
//                    onBackToHome = { viewModel.selectTab(0) },
//                    onExit = {
//                        finishAffinity()
//                    }
//                )
//                if (showSplashState.value) {
//                    SplashScreen(onFinished = { showSplashState.value = false })
//                } else {
//                    Scaffold(
//                        modifier = Modifier.fillMaxSize(),
//                        topBar = {
//                            Column {
//                                TopAppBar(
//                                    title = {
//                                        Column {
//                                            Text(
//                                                text = "Room Rent & Utility",
//                                                fontWeight = FontWeight.Bold,
//                                                fontSize = 18.sp
//                                            )
//                                            Text(
//                                                text = "Offline Meter & Rent Notebook",
//                                                style = MaterialTheme.typography.labelSmall,
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                                            )
//                                        }
//                                    },
//                                    actions = {
//                                        IconButton(
//                                            onClick = { showProfileDialog = true },
//                                            modifier = Modifier.testTag("top_action_profile")
//                                        ) {
//                                            Icon(
//                                                Icons.Default.AccountCircle,
//                                                contentDescription = "Profile"
//                                            )
//                                        }
//                                    },
//                                    colors = TopAppBarDefaults.topAppBarColors(
//                                        containerColor = MaterialTheme.colorScheme.surface
//                                    )
//                                )
//                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
//                            }
//                        },
//                        bottomBar = {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .navigationBarsPadding()
//                                    .padding(
//                                        horizontal = 14.dp,
//                                        vertical = 10.dp
//                                    )
//                            ) {
//
//                                Surface(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(74.dp)
//                                        .shadow(
//                                            elevation = 12.dp,
//                                            shape = RoundedCornerShape(26.dp)
//                                        ),
//                                    shape = RoundedCornerShape(26.dp),
//                                    color = MaterialTheme.colorScheme.surface.copy(
//                                        alpha = 0.96f
//                                    ),
//                                    border = BorderStroke(
//                                        1.dp,
//                                        MaterialTheme.colorScheme.outline.copy(
//                                            alpha = 0.10f
//                                        )
//                                    )
//                                ) {
//
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .padding(horizontal = 5.dp),
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//
//                                        // HOME
//                                        FloatingNavItem(
//                                            selected = selectedTab == 0,
//                                            icon = Icons.Default.Dashboard,
//                                            label = "Home",
//                                            onClick = {
//                                                viewModel.selectTab(0)
//                                            },
//                                            testTag = "nav_dashboard",
//                                            modifier = Modifier.weight(1f)
//                                        )
//
//                                        // HISTORY
//                                        FloatingNavItem(
//                                            selected = selectedTab == 2,
//                                            icon = Icons.Default.History,
//                                            label = "History",
//                                            onClick = {
//                                                viewModel.selectTab(2)
//                                            },
//                                            testTag = "nav_history",
//                                            modifier = Modifier.weight(1f)
//                                        )
//
//                                        // ADD BUTTON
//                                        Box(
//                                            modifier = Modifier
//                                                .weight(1f)
//                                                .fillMaxHeight(),
//                                            contentAlignment = Alignment.Center
//                                        ) {
//
//                                            Box(
//                                                modifier = Modifier
//                                                    .size(56.dp)
//                                                    .shadow(
//                                                        elevation = 9.dp,
//                                                        shape = CircleShape
//                                                    )
//                                                    .clip(CircleShape)
//                                                    .background(
//                                                        MaterialTheme.colorScheme.primary
//                                                    )
//                                                    .clickable {
//                                                        viewModel.prepareAddRecord()
//                                                    }
//                                                    .testTag("nav_add"),
//                                                contentAlignment = Alignment.Center
//                                            ) {
//
//                                                Icon(
//                                                    imageVector = Icons.Default.Add,
//                                                    contentDescription = "Add",
//                                                    modifier = Modifier.size(29.dp),
//                                                    tint = MaterialTheme.colorScheme.onPrimary
//                                                )
//                                            }
//                                        }
//
//                                        // STATISTICS
//                                        FloatingNavItem(
//                                            selected = selectedTab == 3,
//                                            icon = Icons.Default.BarChart,
//                                            label = "Stats",
//                                            onClick = {
//                                                viewModel.selectTab(3)
//                                            },
//                                            testTag = "nav_statistics",
//                                            modifier = Modifier.weight(1f)
//                                        )
//
//                                        // SETTINGS
//                                        FloatingNavItem(
//                                            selected = selectedTab == 4,
//                                            icon = Icons.Default.Settings,
//                                            label = "Settings",
//                                            onClick = {
//                                                viewModel.selectTab(4)
//                                            },
//                                            testTag = "nav_settings",
//                                            modifier = Modifier.weight(1f)
//                                        )
//                                    }
//                                }
//                            }
//                        }) { innerPadding ->
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(innerPadding)
//                        ) {
//                            when (selectedTab) {
//                                0 -> DashboardScreen(
//                                    activeRoom = activeRoom,
//                                    allRooms = allRooms,
//                                    records = currentRoomRecords,
//                                    allRecords = allRecords,
//                                    latestRecord = latestRecord,
//                                    currencySymbol = currencySymbol,
//                                    onSelectRoom = { viewModel.selectRoom(it) },
//                                    onManageRooms = { viewModel.openNewRoomDialog() },
//                                    onAddRecord = { viewModel.prepareAddRecord() },
//                                    onViewHistory = { viewModel.selectTab(2) },
//                                    onOpenRecordDetail = { viewModel.openRecordDetail(it) },
//                                    onExportPdf = { viewModel.exportPdf(context, it) },
//                                    onExportExcel = { viewModel.exportExcelAll(context) },
//                                    onExportPdfAll = { viewModel.exportPdfAll(context) },
//                                )
//                                1 -> AddRecordScreen(
//                                    formState = formState,
//                                    activeRoom = activeRoom,
//                                    allRooms = allRooms,
//                                    currencySymbol = currencySymbol,
//                                    onUpdateField = { transform -> viewModel.updateFormField(transform) },
//                                    onSave = { onSaved -> viewModel.saveCurrentRecord(onSaved) },
//                                    onCancel = { viewModel.selectTab(0) },
//                                    onSelectRoom = { room ->
//                                        viewModel.selectRoom(room)
//                                    },
//                                    onDownloadPdf = { viewModel.exportPdf(context, it) },
//                                    onShare = { viewModel.exportPdf(context, it) },
//                                    onManageRooms = { viewModel.openNewRoomDialog() }
//                                )
//                                2 -> HistoryScreen(
//                                    activeRoom = activeRoom,
//                                    allRooms = allRooms,
//                                    records = currentRoomRecords,
//                                    currencySymbol = currencySymbol,
//                                    onSelectRoom = { viewModel.selectRoom(it) },
//                                    onManageRooms = { viewModel.openNewRoomDialog() },
//                                    onOpenRecordDetail = { viewModel.openRecordDetail(it) },
//                                    onEditRecord = { viewModel.prepareEditRecord(it) },
//                                    onDeleteRecord = { viewModel.deleteRecord(it) },
//                                    onExportPdf = { records -> viewModel.exportPdf(context, records) },
//                                    onExportExcel = { records -> viewModel.exportExcel(context, records)
//                                    },deleteProtectionEnabled = deleteProtectionOn,
//                                    hasDeletePin = hasDeletePin,
//                                    onVerifyDeletePin = { pin -> viewModel.verifyDeletePin(pin) }
//                                )
//                                3 -> StatisticsScreen(
//                                    activeRoom = activeRoom,
//                                    allRooms = allRooms,
//                                    records = currentRoomRecords,
//                                    currencySymbol = currencySymbol,
//                                    onSelectRoom = { viewModel.selectRoom(it) },
//                                    onManageRooms = { viewModel.openNewRoomDialog() }
//                                )
//                                4 -> SettingsScreen(
//                                    currentCurrency = currencySymbol,
//                                    themeMode = themeMode,
//                                    activeRoom = activeRoom,
//                                    allRooms = allRooms,
//                                    onSelectCurrency = { viewModel.setCurrency(it) },
//                                    onSelectTheme = { viewModel.setThemeMode(it) },
//                                    onManageRooms = { viewModel.openNewRoomDialog() },
//                                    onExportExcel = { viewModel.exportExcelAll(context) },
//                                    onExportPdfAll = { viewModel.exportPdfAll(context) },
//                                    onExportJson = { viewModel.exportAllJson() },
//                                    onRestoreJson = { viewModel.restoreFromJson(it) },
//                                    remindersEnabled = remindersEnabled,
//                                    onToggleReminders = { viewModel.setPaymentRemindersEnabled(it) },
//                                    deleteProtectionEnabled = deleteProtectionOn,
//                                    onToggleDeleteProtection = viewModel::setDeleteProtectionEnabled,
//                                    hasDeletePin = hasDeletePin,
//                                    onSetDeletePin = viewModel::setDeletePin,
//                                    onVerifyDeletePin = viewModel::verifyDeletePin,
//                                    onClearDeletePin = viewModel::clearDeletePin,
//                                )
//                            }
//                        }
//
//                        // ── Manage Rooms Modal Dialog ──
//                        if (showRoomDialog) {
//                            ManageRoomsDialog(
//                                allRooms = allRooms,
//                                activeRoom = activeRoom,
//                                editingRoom = editingRoom,
//                                currencySymbol = currencySymbol,
//                                // ✅ Uses the same PIN pattern as HistoryScreen / RecordDetailDialog.
//                                //    When no PIN is set, `hasDeletePin` is false and the dialog
//                                //    falls back to "type the room name" confirmation.
//                                deleteProtectionEnabled = deleteProtectionOn,
//                                hasDeletePin = hasDeletePin,
//                                onVerifyDeletePin = { pin -> viewModel.verifyDeletePin(pin) },
//                                onSelectRoom = {
//                                    viewModel.selectRoom(it)
//                                    viewModel.closeRoomDialog()
//                                },
//                                onSaveRoom = { room -> viewModel.saveRoom(room) },
//                                onDeleteRoom = { viewModel.deleteRoom(it) },
//                                onClose = { viewModel.closeRoomDialog() },
//                            )
//                        }
//
//                        if (showProfileDialog) {
//                            ProfileDialog(
//                                activeRoom = activeRoom,
//                                allRooms = allRooms,
//                                allRecords = allRecords,
//                                currencySymbol = currencySymbol,
//                                themeMode = themeMode,
//                                onClose = { showProfileDialog = false },
//                                onManageRooms = {
//                                    showProfileDialog = false
//                                    viewModel.openNewRoomDialog()
//                                },
//                                onOpenSettings = {
//                                    showProfileDialog = false
//                                    viewModel.selectTab(4)
//                                }
//                            )
//                        }
//
//                        // Record Details Statement Dialog
//                        if (selectedRecordForDetail != null) {
//                            val detailRecord = selectedRecordForDetail!!
//                            val roomForRecord = allRooms.find { it.id == detailRecord.roomId } ?: activeRoom
//                            RecordDetailDialog(
//                                record = detailRecord,
//                                room = roomForRecord,
//                                currencySymbol = currencySymbol,
//                                onEdit = {
//                                    viewModel.prepareEditRecord(it)
//                                },
//
//                                onDelete = {
//                                    viewModel.deleteRecord(it)
//                                },
//                                onExportPdf = {
//                                    viewModel.exportPdf(context, it)
//                                },
//                                onClose = { viewModel.closeRecordDetail() },
//                                deleteProtectionEnabled = deleteProtectionOn,
//                                hasDeletePin = hasDeletePin,
//                                onVerifyDeletePin = { pin -> viewModel.verifyDeletePin(pin) }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @Composable
//    private fun FloatingNavItem(
//        selected: Boolean,
//        icon: ImageVector,
//        label: String,
//        onClick: () -> Unit,
//        testTag: String,
//        modifier: Modifier = Modifier
//    ) {
//        val transition = updateTransition(
//            targetState = selected,
//            label = "nav_item_transition"
//        )
//
//        val iconScale by transition.animateFloat(
//            transitionSpec = {
//                spring(
//                    dampingRatio = Spring.DampingRatioMediumBouncy,
//                    stiffness = Spring.StiffnessMedium
//                )
//            },
//            label = "icon_scale"
//        ) {
//            if (it) 1.12f else 1f
//        }
//
//        val iconColor by transition.animateColor(
//            transitionSpec = {
//                tween(220)
//            },
//            label = "icon_color"
//        ) {
//            if (it) {
//                MaterialTheme.colorScheme.primary
//            } else {
//                MaterialTheme.colorScheme.onSurfaceVariant
//            }
//        }
//
//        val backgroundAlpha by transition.animateFloat(
//            transitionSpec = {
//                tween(220)
//            },
//            label = "background_alpha"
//        ) {
//            if (it) 1f else 0f
//        }
//
//        val dotScale by transition.animateFloat(
//            transitionSpec = {
//                spring(
//                    dampingRatio = Spring.DampingRatioMediumBouncy,
//                    stiffness = Spring.StiffnessMedium
//                )
//            },
//            label = "dot_scale"
//        ) {
//            if (it) 1f else 0f
//        }
//
//        Column(
//            modifier = modifier
//                .fillMaxHeight()
//                .clickable(
//                    indication = null,
//                    interactionSource = remember {
//                        MutableInteractionSource()
//                    }
//                ) {
//                    onClick()
//                }
//                .testTag(testTag),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//
//            // Active icon background
//            Box(
//                modifier = Modifier
//                    .size(42.dp)
//                    .clip(RoundedCornerShape(15.dp))
//                    .background(
//                        MaterialTheme.colorScheme.primary.copy(
//                            alpha = 0.10f * backgroundAlpha
//                        )
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = label,
//                    modifier = Modifier
//                        .size(23.dp)
//                        .graphicsLayer {
//                            scaleX = iconScale
//                            scaleY = iconScale
//                        },
//                    tint = iconColor
//                )
//            }
//
//            Spacer(
//                modifier = Modifier.height(2.dp)
//            )
//
//            Text(
//                text = label,
//                style = MaterialTheme.typography.labelSmall,
//                color = iconColor,
//                maxLines = 1
//            )
//
//            Spacer(
//                modifier = Modifier.height(3.dp)
//            )
//
//            // Small active indicator
//            Box(
//                modifier = Modifier
//                    .size(5.dp)
//                    .graphicsLayer {
//                        scaleX = dotScale
//                        scaleY = dotScale
//                    }
//                    .clip(CircleShape)
//                    .background(
//                        MaterialTheme.colorScheme.primary
//                    )
//            )
//        }
//    }
//
//    @Composable
//    fun ProfileDialog(
//        activeRoom: RoomEntity?,
//        allRooms: List<RoomEntity>,
//        allRecords: List<MonthlyRecordEntity>,
//        currencySymbol: String,
//        themeMode: String,
//        onClose: () -> Unit,
//        onManageRooms: () -> Unit,
//        onOpenSettings: () -> Unit
//    ) {
//        AlertDialog(
//            onDismissRequest = onClose,
//            shape = RoundedCornerShape(24.dp),
//            containerColor = MaterialTheme.colorScheme.surface,
//            title = null,
//            text = {
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // avatar
//                    Box(
//                        modifier = Modifier
//                            .size(80.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            Icons.Default.Person,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(44.dp)
//                        )
//                    }
//
//                    Spacer(Modifier.height(14.dp))
//
//                    Text(
//                        text = "Room Rent Manager",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(Modifier.height(4.dp))
//                    Text(
//                        text = "Offline Meter & Rent Notebook",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    Spacer(Modifier.height(18.dp))
//                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
//                    Spacer(Modifier.height(14.dp))
//
//                    // stat rows
//                    ProfileStatRow("Rooms", allRooms.size.toString())
//                    ProfileStatRow("Total Bills", allRecords.size.toString())
//                    ProfileStatRow("Active Room", activeRoom?.name ?: "—")
//                    ProfileStatRow("Currency", currencySymbol)
//                    ProfileStatRow(
//                        "Theme",
//                        themeMode.replaceFirstChar { it.uppercase() }
//                    )
//
//                    Spacer(Modifier.height(16.dp))
//
//                    // actions
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OutlinedButton(
//                            onClick = onManageRooms,
//                            modifier = Modifier.weight(1f),
//                            shape = RoundedCornerShape(10.dp)
//                        ) {
//                            Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp))
//                            Spacer(Modifier.width(6.dp))
//                            Text("Rooms", fontSize = 12.sp)
//                        }
//                        Button(
//                            onClick = onOpenSettings,
//                            modifier = Modifier.weight(1f),
//                            shape = RoundedCornerShape(10.dp)
//                        ) {
//                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
//                            Spacer(Modifier.width(6.dp))
//                            Text("Settings", fontSize = 12.sp)
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = onClose, shape = RoundedCornerShape(10.dp)) {
//                    Text("Close", fontWeight = FontWeight.SemiBold)
//                }
//            }
//        )
//    }
//
//    @Composable
//    private fun ProfileStatRow(label: String, value: String) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 6.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = label,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Text(
//                text = value,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}



package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.RoomRentTheme
import com.example.viewmodel.MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Settings
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import com.example.util.NotificationHelper
import com.example.util.PaymentReminderScheduler
import com.example.ui.components.ExitConfirmHandler
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector

// App lock
import com.example.security.AppLockConfig
import com.example.security.AppLockPrefs
import com.example.security.AppLockScreen
import com.example.security.AppLockForgotFlow
import com.example.ui.theme.LuxeDivider
import com.example.ui.theme.LuxePrimaryButton
import com.example.ui.theme.LuxeSecondaryButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.security.LockType
import com.example.ui.theme.*


class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val showSplashState = mutableStateOf(true)


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        enableEdgeToEdge()
        NotificationHelper.ensureChannels(this)
        Handler(Looper.getMainLooper()).postDelayed({
            showSplashState.value = false
        }, 3200L)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* granted -> */ }

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
            val remindersEnabled by viewModel.paymentRemindersEnabled.collectAsStateWithLifecycle()
            val deleteProtectionOn by viewModel.deleteProtectionEnabled.collectAsState()
            val hasDeletePin by viewModel.hasDeletePin.collectAsState()

            /* ── App lock state ──────────────────────────────────── */
            var lockConfig by remember { mutableStateOf(AppLockPrefs.load(context)) }
            var isLocked by remember { mutableStateOf(lockConfig.enabled) }
            var showForgot by remember { mutableStateOf(false) }

            // Sync isLocked whenever config changes (e.g. user just enabled it)
            LaunchedEffect(lockConfig.enabled) {
                isLocked = lockConfig.enabled
            }

            // Auto-lock when the app goes to background
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, lockConfig.enabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && lockConfig.enabled) {
                        isLocked = true
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(remindersEnabled) {
                if (remindersEnabled) PaymentReminderScheduler.schedule(context)
                else PaymentReminderScheduler.cancel(context)
            }

            var showProfileDialog by remember { mutableStateOf(false) }

            RoomRentTheme(themePreference = themeMode) {
                ExitConfirmHandler(
                    currentTab = selectedTab,
                    onBackToHome = { viewModel.selectTab(0) },
                    onExit = {
                        finishAffinity()
                    }
                )

                when {
                    /* ── Splash ── */
                    showSplashState.value -> {
                        SplashScreen(onFinished = { showSplashState.value = false })
                    }

                    /* ── Lock screen ── */
                    isLocked && lockConfig.enabled -> {
                        AppLockScreen(
                            config = lockConfig,
                            onUnlock = { isLocked = false },
                            onForgot = { showForgot = true }
                        )

                        if (showForgot) {

                            AppLockForgotFlow(
                                config = lockConfig,
                                onDismiss = { showForgot = false },
                                onResetCredential = { newCred ->
                                    val newSalt = AppLockPrefs.newSalt()
                                    val updated = lockConfig.copy(
                                        credentialHash = AppLockPrefs.hash(newCred, newSalt),
                                        credentialSalt = newSalt
                                    )
                                    AppLockPrefs.save(context, updated)
                                    lockConfig = updated
                                    showForgot = false
                                    isLocked = false
                                }
                            )
                        }
                    }

                    /* ── Main app ── */
                    else -> {
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
                                            IconButton(
                                                onClick = { showProfileDialog = true },
                                                modifier = Modifier.testTag("top_action_profile")
                                            ) {
                                                Icon(
                                                    Icons.Default.AccountCircle,
                                                    contentDescription = "Profile"
                                                )
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 10.dp
                                        )
                                ) {

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(74.dp)
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = RoundedCornerShape(26.dp)
                                            ),
                                        shape = RoundedCornerShape(26.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(
                                            alpha = 0.96f
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(
                                                alpha = 0.10f
                                            )
                                        )
                                    ) {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            // HOME
                                            FloatingNavItem(
                                                selected = selectedTab == 0,
                                                icon = Icons.Default.Dashboard,
                                                label = "Home",
                                                onClick = {
                                                    viewModel.selectTab(0)
                                                },
                                                testTag = "nav_dashboard",
                                                modifier = Modifier.weight(1f)
                                            )

                                            // HISTORY
                                            FloatingNavItem(
                                                selected = selectedTab == 2,
                                                icon = Icons.Default.History,
                                                label = "History",
                                                onClick = {
                                                    viewModel.selectTab(2)
                                                },
                                                testTag = "nav_history",
                                                modifier = Modifier.weight(1f)
                                            )

                                            // ADD BUTTON
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.Center
                                            ) {

                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .shadow(
                                                            elevation = 9.dp,
                                                            shape = CircleShape
                                                        )
                                                        .clip(CircleShape)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary
                                                        )
                                                        .clickable {
                                                            viewModel.prepareAddRecord()
                                                        }
                                                        .testTag("nav_add"),
                                                    contentAlignment = Alignment.Center
                                                ) {

                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add",
                                                        modifier = Modifier.size(29.dp),
                                                        tint = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }

                                            // STATISTICS
                                            FloatingNavItem(
                                                selected = selectedTab == 3,
                                                icon = Icons.Default.BarChart,
                                                label = "Stats",
                                                onClick = {
                                                    viewModel.selectTab(3)
                                                },
                                                testTag = "nav_statistics",
                                                modifier = Modifier.weight(1f)
                                            )

                                            // SETTINGS
                                            FloatingNavItem(
                                                selected = selectedTab == 4,
                                                icon = Icons.Default.Settings,
                                                label = "Settings",
                                                onClick = {
                                                    viewModel.selectTab(4)
                                                },
                                                testTag = "nav_settings",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
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
                                        allRecords = allRecords,
                                        latestRecord = latestRecord,
                                        currencySymbol = currencySymbol,
                                        onSelectRoom = { viewModel.selectRoom(it) },
                                        onManageRooms = { viewModel.openNewRoomDialog() },
                                        onAddRecord = { viewModel.prepareAddRecord() },
                                        onViewHistory = { viewModel.selectTab(2) },
                                        onOpenRecordDetail = { viewModel.openRecordDetail(it) },
                                        onExportPdf = { viewModel.exportPdf(context, it) },
                                        onExportExcel = { viewModel.exportExcelAll(context) },
                                        onExportPdfAll = { viewModel.exportPdfAll(context) },
                                    )
                                    1 -> AddRecordScreen(
                                        formState = formState,
                                        activeRoom = activeRoom,
                                        allRooms = allRooms,
                                        currencySymbol = currencySymbol,
                                        onUpdateField = { transform -> viewModel.updateFormField(transform) },
                                        onSave = { onSaved -> viewModel.saveCurrentRecord(onSaved) },
                                        onCancel = { viewModel.selectTab(0) },
                                        onSelectRoom = { room ->
                                            viewModel.selectRoom(room)
                                        },
                                        onDownloadPdf = { viewModel.exportPdf(context, it) },
                                        onShare = { viewModel.exportPdf(context, it) },
                                        onManageRooms = { viewModel.openNewRoomDialog() }
                                    )
                                    2 -> HistoryScreen(
                                        activeRoom = activeRoom,
                                        allRooms = allRooms,
                                        records = currentRoomRecords,
                                        currencySymbol = currencySymbol,
                                        onSelectRoom = { viewModel.selectRoom(it) },
                                        onManageRooms = { viewModel.openNewRoomDialog() },
                                        onOpenRecordDetail = { viewModel.openRecordDetail(it) },
                                        onEditRecord = { viewModel.prepareEditRecord(it) },
                                        onDeleteRecord = { viewModel.deleteRecord(it) },
                                        onExportPdf = { records -> viewModel.exportPdf(context, records) },
                                        onExportExcel = { records -> viewModel.exportExcel(context, records) },
                                        deleteProtectionEnabled = deleteProtectionOn,
                                        hasDeletePin = hasDeletePin,
                                        onVerifyDeletePin = { pin -> viewModel.verifyDeletePin(pin) }
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
                                        onExportExcel = { viewModel.exportExcelAll(context) },
                                        onExportPdfAll = { viewModel.exportPdfAll(context) },
                                        onExportJson = { viewModel.exportAllJson() },
                                        onRestoreJson = { viewModel.restoreFromJson(it) },
                                        remindersEnabled = remindersEnabled,
                                        onToggleReminders = { viewModel.setPaymentRemindersEnabled(it) },
                                        deleteProtectionEnabled = deleteProtectionOn,
                                        onToggleDeleteProtection = viewModel::setDeleteProtectionEnabled,
                                        hasDeletePin = hasDeletePin,
                                        onSetDeletePin = viewModel::setDeletePin,
                                        onVerifyDeletePin = viewModel::verifyDeletePin,
                                        onClearDeletePin = viewModel::clearDeletePin,
                                        // App lock
                                        appLockConfig = lockConfig,
                                        onAppLockConfigChanged = { lockConfig = it }
                                    )
                                }
                            }

                            // Manage Rooms Modal Dialog
                            if (showRoomDialog) {
                                ManageRoomsDialog(
                                    allRooms = allRooms,
                                    activeRoom = activeRoom,
                                    editingRoom = editingRoom,
                                    currencySymbol = currencySymbol,
                                    deleteProtectionEnabled = deleteProtectionOn,
                                    hasDeletePin = hasDeletePin,
                                    onVerifyDeletePin = { pin -> viewModel.verifyDeletePin(pin) },
                                    onSelectRoom = {
                                        viewModel.selectRoom(it)
                                        viewModel.closeRoomDialog()
                                    },
                                    onSaveRoom = { room -> viewModel.saveRoom(room) },
                                    onDeleteRoom = { viewModel.deleteRoom(it) },
                                    onClose = { viewModel.closeRoomDialog() },
                                )
                            }

                            if (showProfileDialog) {
                                ProfileDialog(
                                    allRooms = allRooms,
                                    allRecords = allRecords,
                                    currencySymbol = currencySymbol,
                                    themeMode = themeMode,
                                    appLockEnabled = lockConfig.enabled,
                                    appLockType = if (lockConfig.enabled)
                                        if (lockConfig.lockType == LockType.PIN) "PIN" else "Password"
                                    else "",
                                    biometricEnabled = lockConfig.biometricEnabled,
                                    userName = lockConfig.userName,          // ★ from AppLockConfig
                                    userEmail = lockConfig.userEmail,        // ★ from AppLockConfig
                                    onClose = { showProfileDialog = false },
                                    onManageRooms = {
                                        showProfileDialog = false
                                        viewModel.openNewRoomDialog()
                                    },
                                    onOpenSettings = {
                                        showProfileDialog = false
                                        viewModel.selectTab(4)
                                    }
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

                                    onDelete = {
                                        viewModel.deleteRecord(it)
                                    },
                                    onExportPdf = {
                                        viewModel.exportPdf(context, it)
                                    },
                                    onClose = { viewModel.closeRecordDetail() },
                                    deleteProtectionEnabled = deleteProtectionOn,
                                    hasDeletePin = hasDeletePin,
                                    onVerifyDeletePin = { pin -> viewModel.verifyDeletePin(pin) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FloatingNavItem(
        selected: Boolean,
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        testTag: String,
        modifier: Modifier = Modifier
    ) {
        val transition = updateTransition(
            targetState = selected,
            label = "nav_item_transition"
        )

        val iconScale by transition.animateFloat(
            transitionSpec = {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            },
            label = "icon_scale"
        ) {
            if (it) 1.12f else 1f
        }

        val iconColor by transition.animateColor(
            transitionSpec = {
                tween(220)
            },
            label = "icon_color"
        ) {
            if (it) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        }

        val backgroundAlpha by transition.animateFloat(
            transitionSpec = {
                tween(220)
            },
            label = "background_alpha"
        ) {
            if (it) 1f else 0f
        }

        val dotScale by transition.animateFloat(
            transitionSpec = {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            },
            label = "dot_scale"
        ) {
            if (it) 1f else 0f
        }

        Column(
            modifier = modifier
                .fillMaxHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
                ) {
                    onClick()
                }
                .testTag(testTag),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Active icon background
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.10f * backgroundAlpha
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(23.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                    tint = iconColor
                )
            }

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = iconColor,
                maxLines = 1
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            // Small active indicator
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer {
                        scaleX = dotScale
                        scaleY = dotScale
                    }
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
            )
        }
    }



    
}