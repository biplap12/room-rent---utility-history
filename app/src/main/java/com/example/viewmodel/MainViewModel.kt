//package com.example.viewmodel
//
//import android.app.Application
//import android.content.Context
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.data.AppDatabase
//import com.example.data.AppRepository
//import com.example.data.AppSettings
//import com.example.data.MonthlyRecordEntity
//import com.example.data.RoomEntity
//import com.example.util.BackupRestoreHelper
//import com.example.util.CsvExporter
//import com.example.util.FormatUtils
//import com.example.util.PdfExporter
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.flatMapLatest
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//data class RecordFormState(
//    val editingRecordId: Long? = null,
//    val roomId: Long = 0L,
//    val billingMonth: String = "",
//    val previousElectricityReading: String = "",
//    val currentElectricityReading: String = "",
//    val electricityRate: String = "",
//    val previousWaterReading: String = "",
//    val currentWaterReading: String = "",
//    val waterRate: String = "",
//    val roomRent: String = "",
//    val wasteCharge: String = "",
//    val otherCharges: String = "",
//    val discount: String = "",
//    val amountPaid: String = "",
//    val paymentDate: String = "",
//    val notes: String = "",
//    // Calculated live
//    val electricityUnits: Double = 0.0,
//    val electricityCost: Double = 0.0,
//    val waterUnits: Double = 0.0,
//    val waterCost: Double = 0.0,
//    val totalAmount: Double = 0.0,
//    val remainingAmount: Double = 0.0,
//    val paymentStatus: String = "UNPAID",
//    val validationError: String? = null,
//    val highUsageWarning: String? = null,
//    val previousMonthElecUnits: Double? = null
//)
//
//class MainViewModel(application: Application) : AndroidViewModel(application) {
//    private val database = AppDatabase.getDatabase(application)
//    private val settings = AppSettings(application)
//    val repository = AppRepository(database, settings)
//
//    val currencySymbol: StateFlow<String> = settings.currencySymbol
//    val themeMode: StateFlow<String> = settings.themeMode
//
//    private val _selectedTab = MutableStateFlow(0)
//    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
//
//    fun selectTab(index: Int) {
//        _selectedTab.value = index
//    }
//
//    val allRooms: StateFlow<List<RoomEntity>> = repository.allRooms
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    val selectedRoomId: StateFlow<Long> = settings.selectedRoomId
//
//    val activeRoom: StateFlow<RoomEntity?> = combine(allRooms, selectedRoomId) { rooms, currentId ->
//        if (rooms.isEmpty()) null
//        else rooms.find { it.id == currentId } ?: rooms.firstOrNull()
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
//
//    val currentRoomRecords: StateFlow<List<MonthlyRecordEntity>> = activeRoom.flatMapLatest { room ->
//        if (room != null) repository.getRecordsForRoom(room.id)
//        else flowOf(emptyList())
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    val allRecords: StateFlow<List<MonthlyRecordEntity>> = repository.allRecords
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//
//    val latestRecord: StateFlow<MonthlyRecordEntity?> = currentRoomRecords.combine(flowOf(Unit)) { records, _ ->
//        records.firstOrNull()
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
//
//    // Form state
//    private val _formState = MutableStateFlow(RecordFormState())
//    val formState: StateFlow<RecordFormState> = _formState.asStateFlow()
//
//    // Room Dialog State
//    private val _editingRoom = MutableStateFlow<RoomEntity?>(null)
//    val editingRoom: StateFlow<RoomEntity?> = _editingRoom.asStateFlow()
//
//    private val _showRoomDialog = MutableStateFlow(false)
//    val showRoomDialog: StateFlow<Boolean> = _showRoomDialog.asStateFlow()
//
//    // Selected record for detail view
//    private val _selectedRecordForDetail = MutableStateFlow<MonthlyRecordEntity?>(null)
//    val selectedRecordForDetail: StateFlow<MonthlyRecordEntity?> = _selectedRecordForDetail.asStateFlow()
//
////    init {
////        viewModelScope.launch {
////            repository.checkAndSeedInitialData()
////        }
////    }
//
//    fun selectRoom(room: RoomEntity) {
//        settings.setSelectedRoomId(room.id)
//    }
//
//    fun setCurrency(symbol: String) {
//        settings.setCurrency(symbol)
//    }
//
//    fun setThemeMode(mode: String) {
//        settings.setThemeMode(mode)
//    }
//
//    fun openRecordDetail(record: MonthlyRecordEntity) {
//        _selectedRecordForDetail.value = record
//    }
//
//    fun closeRecordDetail() {
//        _selectedRecordForDetail.value = null
//    }
//
//    // Room operations
//    fun openNewRoomDialog() {
//        _editingRoom.value = null
//        _showRoomDialog.value = true
//    }
//
//    fun openEditRoomDialog(room: RoomEntity) {
//        _editingRoom.value = room
//        _showRoomDialog.value = true
//    }
//
//    fun closeRoomDialog() {
//        _showRoomDialog.value = false
//        _editingRoom.value = null
//    }
//
//    fun saveRoom(
//        name: String,
//        address: String,
//        rent: Double,
//        elecRate: Double,
//        waterRate: Double,
//        wasteCharge: Double
//    ) {
//        viewModelScope.launch {
//            val current = _editingRoom.value
//            if (current != null) {
//                repository.updateRoom(
//                    current.copy(
//                        name = name,
//                        address = address,
//                        defaultRent = rent,
//                        electricityRate = elecRate,
//                        waterRate = waterRate,
//                        wasteCharge = wasteCharge
//                    )
//                )
//            } else {
//                val newId = repository.insertRoom(
//                    RoomEntity(
//                        name = name,
//                        address = address,
//                        defaultRent = rent,
//                        electricityRate = elecRate,
//                        waterRate = waterRate,
//                        wasteCharge = wasteCharge
//                    )
//                )
//                settings.setSelectedRoomId(newId)
//            }
//            closeRoomDialog()
//        }
//    }
//
//    fun deleteRoom(room: RoomEntity) {
//        viewModelScope.launch {
//            repository.deleteRoom(room)
//            val remaining = allRooms.value.filter { it.id != room.id }
//            if (remaining.isNotEmpty()) {
//                settings.setSelectedRoomId(remaining.first().id)
//            } else {
//                settings.setSelectedRoomId(0L)
//            }
//        }
//    }
//
//    // Prepare Add Record (Section 17 requirement: auto-select active room, find latest record, copy latest meter readings into prev readings)
//    fun prepareAddRecord(targetRoomId: Long? = null) {
//        viewModelScope.launch {
//            val room = if (targetRoomId != null) {
//                repository.getRoomByIdSync(targetRoomId)
//            } else {
//                activeRoom.value ?: allRooms.value.firstOrNull()
//            } ?: return@launch
//
//            val latestRec = repository.getLatestRecordForRoomSync(room.id)
//
//            val nextMonthStr = getNextMonthName(latestRec?.billingMonth)
//
//            val prevElec = latestRec?.currentElectricityReading ?: 0.0
//            val prevWater = latestRec?.currentWaterReading ?: 0.0
//            val defaultRent = room.defaultRent
//            val elecRate = room.electricityRate
//            val waterRate = room.waterRate
//            val wasteCharge = room.wasteCharge
//
//            val initialForm = RecordFormState(
//                editingRecordId = null,
//                roomId = room.id,
//                billingMonth = nextMonthStr,
//                previousElectricityReading = if (prevElec > 0) prevElec.toString() else "0",
//                currentElectricityReading = "",
//                electricityRate = elecRate.toString(),
//                previousWaterReading = if (prevWater > 0) prevWater.toString() else "0",
//                currentWaterReading = "",
//                waterRate = waterRate.toString(),
//                roomRent = defaultRent.toString(),
//                wasteCharge = wasteCharge.toString(),
//                otherCharges = "0",
//                discount = "0",
//                amountPaid = "0",
//                paymentDate = "",
//                notes = "",
//                previousMonthElecUnits = latestRec?.electricityUnits
//            )
//
//            _formState.value = recalculate(initialForm)
//            _selectedTab.value = 1 // Add tab
//        }
//    }
//
//    fun prepareEditRecord(record: MonthlyRecordEntity) {
//        val form = RecordFormState(
//            editingRecordId = record.id,
//            roomId = record.roomId,
//            billingMonth = record.billingMonth,
//            previousElectricityReading = record.previousElectricityReading.toString(),
//            currentElectricityReading = record.currentElectricityReading.toString(),
//            electricityRate = record.electricityRate.toString(),
//            previousWaterReading = record.previousWaterReading.toString(),
//            currentWaterReading = record.currentWaterReading.toString(),
//            waterRate = record.waterRate.toString(),
//            roomRent = record.roomRent.toString(),
//            wasteCharge = record.wasteCharge.toString(),
//            otherCharges = record.otherCharges.toString(),
//            discount = record.discount.toString(),
//            amountPaid = record.amountPaid.toString(),
//            paymentDate = record.paymentDate,
//            notes = record.notes
//        )
//        _formState.value = recalculate(form)
//        _selectedTab.value = 1 // Add/Edit tab
//    }
//
//    fun duplicateRecord(record: MonthlyRecordEntity) {
//        viewModelScope.launch {
//            val nextMonthStr = getNextMonthName(record.billingMonth)
//            val form = RecordFormState(
//                editingRecordId = null,
//                roomId = record.roomId,
//                billingMonth = nextMonthStr,
//                previousElectricityReading = record.currentElectricityReading.toString(),
//                currentElectricityReading = "",
//                electricityRate = record.electricityRate.toString(),
//                previousWaterReading = record.currentWaterReading.toString(),
//                currentWaterReading = "",
//                waterRate = record.waterRate.toString(),
//                roomRent = record.roomRent.toString(),
//                wasteCharge = record.wasteCharge.toString(),
//                otherCharges = "0",
//                discount = "0",
//                amountPaid = "0",
//                paymentDate = "",
//                notes = "",
//                previousMonthElecUnits = record.electricityUnits
//            )
//            _formState.value = recalculate(form)
//            _selectedTab.value = 1
//        }
//    }
//
//    fun updateFormField(transform: (RecordFormState) -> RecordFormState) {
//        _formState.value = recalculate(transform(_formState.value))
//    }
//
//    private fun recalculate(state: RecordFormState): RecordFormState {
//        val prevElec = FormatUtils.parseDoubleOrZero(state.previousElectricityReading)
//        val currElec = FormatUtils.parseDoubleOrZero(state.currentElectricityReading)
//        val elecRate = FormatUtils.parseDoubleOrZero(state.electricityRate)
//
//        val prevWater = FormatUtils.parseDoubleOrZero(state.previousWaterReading)
//        val currWater = FormatUtils.parseDoubleOrZero(state.currentWaterReading)
//        val waterRate = FormatUtils.parseDoubleOrZero(state.waterRate)
//
//        val rent = FormatUtils.parseDoubleOrZero(state.roomRent)
//        val waste = FormatUtils.parseDoubleOrZero(state.wasteCharge)
//        val other = FormatUtils.parseDoubleOrZero(state.otherCharges)
//        val discount = FormatUtils.parseDoubleOrZero(state.discount)
//        val paid = FormatUtils.parseDoubleOrZero(state.amountPaid)
//
//        // Electricity units & cost
//        val elecUnits = if (currElec >= prevElec) currElec - prevElec else 0.0
//        val elecCost = elecUnits * elecRate
//
//        // Water units & cost
//        val waterUnits = if (currWater >= prevWater) currWater - prevWater else 0.0
//        val waterCost = waterUnits * waterRate
//
//        // Total bill
//        val total = rent + elecCost + waterCost + waste + other - discount
//        val safeTotal = if (total > 0) total else 0.0
//
//        // Remaining
//        val remaining = if (safeTotal > paid) safeTotal - paid else 0.0
//
//        // Payment status
//        val status = when {
//            paid >= safeTotal && safeTotal > 0 -> "PAID"
//            paid > 0 -> "PARTIALLY_PAID"
//            else -> "UNPAID"
//        }
//
//        // Validation error
//        var error: String? = null
//        if (state.currentElectricityReading.isNotBlank() && currElec < prevElec) {
//            error = "Current electricity meter (${currElec}) cannot be lower than previous (${prevElec})."
//        } else if (state.currentWaterReading.isNotBlank() && currWater < prevWater) {
//            error = "Current water meter (${currWater}) cannot be lower than previous (${prevWater})."
//        } else if (elecRate < 0 || waterRate < 0 || rent < 0 || waste < 0 || other < 0 || discount < 0 || paid < 0) {
//            error = "Negative values are not allowed."
//        } else if (state.billingMonth.isBlank()) {
//            error = "Billing month cannot be empty."
//        }
//
//        // High usage warning
//        var warning: String? = null
//        val prevUsage = state.previousMonthElecUnits
//        if (prevUsage != null && prevUsage > 0 && elecUnits > (prevUsage * 2.0)) {
//            warning = "Electricity usage (${FormatUtils.formatUnits(elecUnits)} units) is more than 2x previous month (${FormatUtils.formatUnits(prevUsage)} units). Please verify meter reading."
//        }
//
//        return state.copy(
//            electricityUnits = elecUnits,
//            electricityCost = elecCost,
//            waterUnits = waterUnits,
//            waterCost = waterCost,
//            totalAmount = safeTotal,
//            remainingAmount = remaining,
//            paymentStatus = status,
//            validationError = error,
//            highUsageWarning = warning
//        )
//    }
//
//    fun saveCurrentRecord(): Boolean {
//        val form = _formState.value
//        if (form.validationError != null) return false
//        if (form.billingMonth.isBlank()) return false
//
//        viewModelScope.launch {
//            val entity = MonthlyRecordEntity(
//                id = form.editingRecordId ?: 0L,
//                roomId = form.roomId,
//                billingMonth = form.billingMonth.trim(),
//                previousElectricityReading = FormatUtils.parseDoubleOrZero(form.previousElectricityReading),
//                currentElectricityReading = FormatUtils.parseDoubleOrZero(form.currentElectricityReading),
//                electricityUnits = form.electricityUnits,
//                electricityRate = FormatUtils.parseDoubleOrZero(form.electricityRate),
//                electricityCost = form.electricityCost,
//                previousWaterReading = FormatUtils.parseDoubleOrZero(form.previousWaterReading),
//                currentWaterReading = FormatUtils.parseDoubleOrZero(form.currentWaterReading),
//                waterUnits = form.waterUnits,
//                waterRate = FormatUtils.parseDoubleOrZero(form.waterRate),
//                waterCost = form.waterCost,
//                roomRent = FormatUtils.parseDoubleOrZero(form.roomRent),
//                wasteCharge = FormatUtils.parseDoubleOrZero(form.wasteCharge),
//                otherCharges = FormatUtils.parseDoubleOrZero(form.otherCharges),
//                discount = FormatUtils.parseDoubleOrZero(form.discount),
//                totalAmount = form.totalAmount,
//                amountPaid = FormatUtils.parseDoubleOrZero(form.amountPaid),
//                remainingAmount = form.remainingAmount,
//                paymentDate = form.paymentDate.trim(),
//                paymentStatus = form.paymentStatus,
//                notes = form.notes.trim(),
//                updatedAt = System.currentTimeMillis()
//            )
//
//            if (form.editingRecordId != null && form.editingRecordId > 0) {
//                repository.updateRecord(entity)
//            } else {
//                repository.insertRecord(entity)
//            }
//
//            // Switch to Dashboard or History
//            _selectedTab.value = 0
//            _selectedRecordForDetail.value = null
//        }
//        return true
//    }
//
//    fun deleteRecord(record: MonthlyRecordEntity) {
//        viewModelScope.launch {
//            repository.deleteRecord(record)
//            if (_selectedRecordForDetail.value?.id == record.id) {
//                _selectedRecordForDetail.value = null
//            }
//        }
//    }
//
//    fun exportCsv(context: Context) {
//        val room = activeRoom.value
//        val records = currentRoomRecords.value
//        val csv = CsvExporter.generateCsvContent(room, records, currencySymbol.value)
//        CsvExporter.shareCsv(context, csv, "${room?.name ?: "Room"}_Bills.csv")
//    }
//
//    fun exportPdf(context: Context, record: MonthlyRecordEntity) {
//        val room = allRooms.value.find { it.id == record.roomId } ?: activeRoom.value
//        PdfExporter.generateAndShareBillPdf(context, room, record, currencySymbol.value)
//    }
//
//    fun exportAllJson(): String {
//        return BackupRestoreHelper.exportToJson(allRooms.value, allRecords.value)
//    }
//
//    fun restoreFromJson(jsonString: String): Boolean {
//        return try {
//            val backup = BackupRestoreHelper.importFromJson(jsonString)
//            viewModelScope.launch {
//                for (room in backup.rooms) {
//                    repository.insertRoom(room.copy(id = 0))
//                }
//                for (rec in backup.records) {
//                    repository.insertRecord(rec.copy(id = 0))
//                }
//            }
//            true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    private fun getNextMonthName(lastBillingMonth: String?): String {
//        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
//        if (lastBillingMonth.isNullOrBlank()) {
//            return sdf.format(Date())
//        }
//        return try {
//            val date = sdf.parse(lastBillingMonth)
//            if (date != null) {
//                val cal = Calendar.getInstance().apply {
//                    time = date
//                    add(Calendar.MONTH, 1)
//                }
//                sdf.format(cal.time)
//            } else {
//                sdf.format(Date())
//            }
//        } catch (e: Exception) {
//            sdf.format(Date())
//        }
//    }
//}

package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.AppSettings
import com.example.data.MonthlyRecordEntity
import com.example.data.RoomEntity
import com.example.util.BackupRestoreHelper
import com.example.util.CsvExporter
import com.example.util.FormatUtils
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.RectF

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class RecordFormState(
    val editingRecordId: Long? = null,
    val roomId: Long = 0L,
    val billingMonth: String = "",
    val previousElectricityReading: String = "",
    val currentElectricityReading: String = "",
    val electricityRate: String = "",
    val previousWaterReading: String = "",
    val currentWaterReading: String = "",
    val waterRate: String = "",
    val roomRent: String = "",
    val wasteCharge: String = "",
    val otherCharges: String = "",
    val discount: String = "",
    val amountPaid: String = "",
    val paymentDate: String = "",
    val notes: String = "",
    // Calculated live
    val electricityUnits: Double = 0.0,
    val electricityCost: Double = 0.0,
    val waterUnits: Double = 0.0,
    val waterCost: Double = 0.0,
    val totalAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentStatus: String = "UNPAID",
    val validationError: String? = null,
    val highUsageWarning: String? = null,
    val previousMonthElecUnits: Double? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val settings = AppSettings(application)
    val repository = AppRepository(database, settings)

    val currencySymbol: StateFlow<String> = settings.currencySymbol
    val themeMode: StateFlow<String> = settings.themeMode

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    val allRooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedRoomId: StateFlow<Long> = settings.selectedRoomId

    val activeRoom: StateFlow<RoomEntity?> = combine(allRooms, selectedRoomId) { rooms, currentId ->
        if (rooms.isEmpty()) null
        else rooms.find { it.id == currentId } ?: rooms.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentRoomRecords: StateFlow<List<MonthlyRecordEntity>> =
        activeRoom.flatMapLatest { room ->
            if (room != null) repository.getRecordsForRoom(room.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<MonthlyRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestRecord: StateFlow<MonthlyRecordEntity?> =
        currentRoomRecords.combine(flowOf(Unit)) { records, _ ->
            records.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Form state
    private val _formState = MutableStateFlow(RecordFormState())
    val formState: StateFlow<RecordFormState> = _formState.asStateFlow()

    // Room Dialog State
    private val _editingRoom = MutableStateFlow<RoomEntity?>(null)
    val editingRoom: StateFlow<RoomEntity?> = _editingRoom.asStateFlow()

    private val _showRoomDialog = MutableStateFlow(false)
    val showRoomDialog: StateFlow<Boolean> = _showRoomDialog.asStateFlow()

    // Selected record for detail view
    private val _selectedRecordForDetail = MutableStateFlow<MonthlyRecordEntity?>(null)
    val selectedRecordForDetail: StateFlow<MonthlyRecordEntity?> =
        _selectedRecordForDetail.asStateFlow()

//    init {
//        viewModelScope.launch {
//            repository.checkAndSeedInitialData()
//        }
//    }

    fun selectRoom(room: RoomEntity) {
        settings.setSelectedRoomId(room.id)
    }

    fun setCurrency(symbol: String) {
        settings.setCurrency(symbol)
    }

    fun setThemeMode(mode: String) {
        settings.setThemeMode(mode)
    }

    fun openRecordDetail(record: MonthlyRecordEntity) {
        _selectedRecordForDetail.value = record
    }

    fun closeRecordDetail() {
        _selectedRecordForDetail.value = null
    }

    // Room operations
    fun openNewRoomDialog() {
        _editingRoom.value = null
        _showRoomDialog.value = true
    }

    fun openEditRoomDialog(room: RoomEntity) {
        _editingRoom.value = room
        _showRoomDialog.value = true
    }

    fun closeRoomDialog() {
        _showRoomDialog.value = false
        _editingRoom.value = null
    }

    fun saveRoom(
        name: String,
        address: String,
        rent: Double,
        elecRate: Double,
        waterRate: Double,
        wasteCharge: Double
    ) {
        viewModelScope.launch {
            val current = _editingRoom.value
            if (current != null) {
                repository.updateRoom(
                    current.copy(
                        name = name,
                        address = address,
                        defaultRent = rent,
                        electricityRate = elecRate,
                        waterRate = waterRate,
                        wasteCharge = wasteCharge
                    )
                )
            } else {
                val newId = repository.insertRoom(
                    RoomEntity(
                        name = name,
                        address = address,
                        defaultRent = rent,
                        electricityRate = elecRate,
                        waterRate = waterRate,
                        wasteCharge = wasteCharge
                    )
                )
                settings.setSelectedRoomId(newId)
            }
            closeRoomDialog()
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            repository.deleteRoom(room)
            val remaining = allRooms.value.filter { it.id != room.id }
            if (remaining.isNotEmpty()) {
                settings.setSelectedRoomId(remaining.first().id)
            } else {
                settings.setSelectedRoomId(0L)
            }
        }
    }

    // Prepare Add Record (Section 17 requirement: auto-select active room, find latest record, copy latest meter readings into prev readings)
    fun prepareAddRecord(targetRoomId: Long? = null) {
        viewModelScope.launch {
            val room = if (targetRoomId != null) {
                repository.getRoomByIdSync(targetRoomId)
            } else {
                activeRoom.value ?: allRooms.value.firstOrNull()
            } ?: return@launch

            val latestRec = repository.getLatestRecordForRoomSync(room.id)

            val nextMonthStr = getNextMonthName(latestRec?.billingMonth)

            val prevElec = latestRec?.currentElectricityReading ?: 0.0
            val prevWater = latestRec?.currentWaterReading ?: 0.0
            val defaultRent = room.defaultRent
            val elecRate = room.electricityRate
            val waterRate = room.waterRate
            val wasteCharge = room.wasteCharge

            val initialForm = RecordFormState(
                editingRecordId = null,
                roomId = room.id,
                billingMonth = nextMonthStr,
                previousElectricityReading = if (prevElec > 0) prevElec.toString() else "0",
                currentElectricityReading = "",
                electricityRate = elecRate.toString(),
                previousWaterReading = if (prevWater > 0) prevWater.toString() else "0",
                currentWaterReading = "",
                waterRate = waterRate.toString(),
                roomRent = defaultRent.toString(),
                wasteCharge = wasteCharge.toString(),
                otherCharges = "0",
                discount = "0",
                amountPaid = "0",
                paymentDate = "",
                notes = "",
                previousMonthElecUnits = latestRec?.electricityUnits
            )

            _formState.value = recalculate(initialForm)
            _selectedTab.value = 1 // Add tab
        }
    }

    fun prepareEditRecord(record: MonthlyRecordEntity) {
        val form = RecordFormState(
            editingRecordId = record.id,
            roomId = record.roomId,
            billingMonth = record.billingMonth,
            previousElectricityReading = record.previousElectricityReading.toString(),
            currentElectricityReading = record.currentElectricityReading.toString(),
            electricityRate = record.electricityRate.toString(),
            previousWaterReading = record.previousWaterReading.toString(),
            currentWaterReading = record.currentWaterReading.toString(),
            waterRate = record.waterRate.toString(),
            roomRent = record.roomRent.toString(),
            wasteCharge = record.wasteCharge.toString(),
            otherCharges = record.otherCharges.toString(),
            discount = record.discount.toString(),
            amountPaid = record.amountPaid.toString(),
            paymentDate = record.paymentDate,
            notes = record.notes
        )
        _formState.value = recalculate(form)
        _selectedTab.value = 1 // Add/Edit tab
    }

    fun duplicateRecord(record: MonthlyRecordEntity) {
        viewModelScope.launch {
            val nextMonthStr = getNextMonthName(record.billingMonth)
            val form = RecordFormState(
                editingRecordId = null,
                roomId = record.roomId,
                billingMonth = nextMonthStr,
                previousElectricityReading = record.currentElectricityReading.toString(),
                currentElectricityReading = "",
                electricityRate = record.electricityRate.toString(),
                previousWaterReading = record.currentWaterReading.toString(),
                currentWaterReading = "",
                waterRate = record.waterRate.toString(),
                roomRent = record.roomRent.toString(),
                wasteCharge = record.wasteCharge.toString(),
                otherCharges = "0",
                discount = "0",
                amountPaid = "0",
                paymentDate = "",
                notes = "",
                previousMonthElecUnits = record.electricityUnits
            )
            _formState.value = recalculate(form)
            _selectedTab.value = 1
        }
    }

    fun updateFormField(transform: (RecordFormState) -> RecordFormState) {
        _formState.value = recalculate(transform(_formState.value))
    }

    private fun recalculate(state: RecordFormState): RecordFormState {
        val prevElec = FormatUtils.parseDoubleOrZero(state.previousElectricityReading)
        val currElec = FormatUtils.parseDoubleOrZero(state.currentElectricityReading)
        val elecRate = FormatUtils.parseDoubleOrZero(state.electricityRate)

        val prevWater = FormatUtils.parseDoubleOrZero(state.previousWaterReading)
        val currWater = FormatUtils.parseDoubleOrZero(state.currentWaterReading)
        val waterRate = FormatUtils.parseDoubleOrZero(state.waterRate)

        val rent = FormatUtils.parseDoubleOrZero(state.roomRent)
        val waste = FormatUtils.parseDoubleOrZero(state.wasteCharge)
        val other = FormatUtils.parseDoubleOrZero(state.otherCharges)
        val discount = FormatUtils.parseDoubleOrZero(state.discount)
        val paid = FormatUtils.parseDoubleOrZero(state.amountPaid)

        // Electricity units & cost
        val elecUnits = if (currElec >= prevElec) currElec - prevElec else 0.0
        val elecCost = elecUnits * elecRate

        // Water units & cost
        val waterUnits = if (currWater >= prevWater) currWater - prevWater else 0.0
        val waterCost = waterUnits * waterRate

        // Total bill
        val total = rent + elecCost + waterCost + waste + other - discount
        val safeTotal = if (total > 0) total else 0.0

        // Remaining
        val remaining = if (safeTotal > paid) safeTotal - paid else 0.0

        // Payment status
        val status = when {
            paid >= safeTotal && safeTotal > 0 -> "PAID"
            paid > 0 -> "PARTIALLY_PAID"
            else -> "UNPAID"
        }

        // Validation error
        var error: String? = null
        if (state.currentElectricityReading.isNotBlank() && currElec < prevElec) {
            error =
                "Current electricity meter (${currElec}) cannot be lower than previous (${prevElec})."
        } else if (state.currentWaterReading.isNotBlank() && currWater < prevWater) {
            error =
                "Current water meter (${currWater}) cannot be lower than previous (${prevWater})."
        } else if (elecRate < 0 || waterRate < 0 || rent < 0 || waste < 0 || other < 0 || discount < 0 || paid < 0) {
            error = "Negative values are not allowed."
        } else if (state.billingMonth.isBlank()) {
            error = "Billing month cannot be empty."
        }

        // High usage warning
        var warning: String? = null
        val prevUsage = state.previousMonthElecUnits
        if (prevUsage != null && prevUsage > 0 && elecUnits > (prevUsage * 2.0)) {
            warning =
                "Electricity usage (${FormatUtils.formatUnits(elecUnits)} units) is more than 2x previous month (${
                    FormatUtils.formatUnits(prevUsage)
                } units). Please verify meter reading."
        }

        return state.copy(
            electricityUnits = elecUnits,
            electricityCost = elecCost,
            waterUnits = waterUnits,
            waterCost = waterCost,
            totalAmount = safeTotal,
            remainingAmount = remaining,
            paymentStatus = status,
            validationError = error,
            highUsageWarning = warning
        )
    }

    fun saveCurrentRecord(): Boolean {
        val form = _formState.value
        if (form.validationError != null) return false
        if (form.billingMonth.isBlank()) return false

        viewModelScope.launch {
            val entity = MonthlyRecordEntity(
                id = form.editingRecordId ?: 0L,
                roomId = form.roomId,
                billingMonth = form.billingMonth.trim(),
                previousElectricityReading = FormatUtils.parseDoubleOrZero(form.previousElectricityReading),
                currentElectricityReading = FormatUtils.parseDoubleOrZero(form.currentElectricityReading),
                electricityUnits = form.electricityUnits,
                electricityRate = FormatUtils.parseDoubleOrZero(form.electricityRate),
                electricityCost = form.electricityCost,
                previousWaterReading = FormatUtils.parseDoubleOrZero(form.previousWaterReading),
                currentWaterReading = FormatUtils.parseDoubleOrZero(form.currentWaterReading),
                waterUnits = form.waterUnits,
                waterRate = FormatUtils.parseDoubleOrZero(form.waterRate),
                waterCost = form.waterCost,
                roomRent = FormatUtils.parseDoubleOrZero(form.roomRent),
                wasteCharge = FormatUtils.parseDoubleOrZero(form.wasteCharge),
                otherCharges = FormatUtils.parseDoubleOrZero(form.otherCharges),
                discount = FormatUtils.parseDoubleOrZero(form.discount),
                totalAmount = form.totalAmount,
                amountPaid = FormatUtils.parseDoubleOrZero(form.amountPaid),
                remainingAmount = form.remainingAmount,
                paymentDate = form.paymentDate.trim(),
                paymentStatus = form.paymentStatus,
                notes = form.notes.trim(),
                updatedAt = System.currentTimeMillis()
            )

            if (form.editingRecordId != null && form.editingRecordId > 0) {
                repository.updateRecord(entity)
            } else {
                repository.insertRecord(entity)
            }

            // Switch to Dashboard or History
            _selectedTab.value = 0
            _selectedRecordForDetail.value = null
        }
        return true
    }

    fun deleteRecord(record: MonthlyRecordEntity) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            if (_selectedRecordForDetail.value?.id == record.id) {
                _selectedRecordForDetail.value = null
            }
        }
    }

//    fun exportCsv(context: Context) {
//        val room = activeRoom.value
//        val records = currentRoomRecords.value
//        val csv = CsvExporter.generateCsvContent(room, records, currencySymbol.value)
//        CsvExporter.shareCsv(context, csv, "${room?.name ?: "Room"}_Bills.csv")
//    }

    fun exportPdf(context: Context, record: MonthlyRecordEntity) {
        val room = allRooms.value.find { it.id == record.roomId } ?: activeRoom.value
        PdfExporter.generateAndShareBillPdf(context, room, record, currencySymbol.value)
    }

    fun exportAllJson(): String {
        return BackupRestoreHelper.exportToJson(allRooms.value, allRecords.value)
    }

    fun restoreFromJson(jsonString: String): Boolean {
        return try {
            val backup = BackupRestoreHelper.importFromJson(jsonString)
            viewModelScope.launch {
                for (room in backup.rooms) {
                    repository.insertRoom(room.copy(id = 0))
                }
                for (rec in backup.records) {
                    repository.insertRecord(rec.copy(id = 0))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getNextMonthName(lastBillingMonth: String?): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        if (lastBillingMonth.isNullOrBlank()) {
            return sdf.format(Date())
        }
        return try {
            val date = sdf.parse(lastBillingMonth)
            if (date != null) {
                val cal = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MONTH, 1)
                }
                sdf.format(cal.time)
            } else {
                sdf.format(Date())
            }
        } catch (e: Exception) {
            sdf.format(Date())
        }
    }


    // ─────────────────────────────────────────────────────────────────
//  PDF  – multi-page history report with summary + totals + description
// ─────────────────────────────────────────────────────────────────
    fun exportPdf(context: Context, records: List<MonthlyRecordEntity>) {
        if (records.isEmpty()) return

        val room = activeRoom.value
        val symbol = currencySymbol.value
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val tableWidth = pageWidth - 2 * margin

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f; color = Color.rgb(107, 114, 128)
        }
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(37, 99, 235)
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f; isFakeBoldText = true; color = Color.WHITE
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = Color.rgb(31, 41, 55)
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240); strokeWidth = 0.5f
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = Color.rgb(148, 163, 184)
        }
        val paidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.rgb(22, 163, 74)
        }
        val partialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.rgb(217, 119, 6)
        }
        val unpaidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.rgb(220, 38, 38)
        }

        // ── columns (Description added, note renamed label) ───────────
        val columns = listOf(
            "Month" to 0.13f, "Status" to 0.10f,
            "Rent" to 0.085f, "Elec" to 0.085f, "Water" to 0.085f, "Waste" to 0.075f,
            "Total" to 0.095f, "Paid" to 0.095f, "Due" to 0.095f,
            "Description" to 0.155f
        )
        val colWidths = columns.map { it.second * tableWidth }
        val colX = FloatArray(columns.size)
        run {
            var acc = margin
            for (i in columns.indices) {
                colX[i] = acc; acc += colWidths[i]
            }
        }
        val rightAligned = setOf(2, 3, 4, 5, 6, 7, 8)

        fun money(v: Double) = FormatUtils.formatMoney(v, symbol)

        val totalBilled = records.sumOf { it.totalAmount }
        val totalPaid = records.sumOf { it.amountPaid }
        val totalDue = records.sumOf { it.remainingAmount }

        var pageNumber = 1

        fun newPage(): PdfDocument.Page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )

        var page = newPage()
        var canvas = page.canvas
        var y = margin

        canvas.drawText("Rent History Report", margin, y + 16f, titlePaint)
        y += 22f

        val roomLine = buildString {
            append(room?.name ?: "All Rooms")
            if (!room?.address.isNullOrBlank()) append("  •  ${room?.address}")
        }
        canvas.drawText(roomLine, margin, y + 10f, subtitlePaint)
        y += 15f
        canvas.drawText(
            "Generated: $generatedAt  •  ${records.size} record(s)",
            margin,
            y + 10f,
            subtitlePaint
        )
        y += 20f
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        y += 16f

        // summary cards
        val gap = 8f
        val boxW = (tableWidth - 2 * gap) / 3f
        val boxH = 44f
        drawSummaryCard(
            canvas, margin, y, boxW, boxH,
            "Total Billed", money(totalBilled), Color.rgb(37, 99, 235)
        )
        drawSummaryCard(
            canvas, margin + boxW + gap, y, boxW, boxH,
            "Total Paid", money(totalPaid), Color.rgb(22, 163, 74)
        )
        drawSummaryCard(
            canvas, margin + 2 * (boxW + gap), y, boxW, boxH,
            "Total Due", money(totalDue), Color.rgb(220, 38, 38)
        )
        y += boxH + 16f

        val headerH = 22f
        val rowH = 20f

        fun drawTableHeader() {
            canvas.drawRect(margin, y, pageWidth - margin, y + headerH, headerBgPaint)
            columns.forEachIndexed { i, (label, _) ->
                val x = if (i in rightAligned)
                    colX[i] + colWidths[i] - 6f - headerTextPaint.measureText(label)
                else colX[i] + 6f
                canvas.drawText(label, x, y + 15f, headerTextPaint)
            }
            y += headerH
        }
        drawTableHeader()

        fun drawFooter() {
            val fy = pageHeight - 20f
            canvas.drawText("Room Rent & Utility ‣ By Biplap Neupane.", margin, fy, footerPaint)
            val label = "Page $pageNumber"
            canvas.drawText(
                label,
                pageWidth - margin - footerPaint.measureText(label),
                fy,
                footerPaint
            )
        }

        var rowIndex = 0
        for (r in records) {
            if (y + rowH > pageHeight - 30f) {
                drawFooter()
                document.finishPage(page)
                pageNumber++
                page = newPage()
                canvas = page.canvas
                y = margin
                drawTableHeader()
                rowIndex = 0
            }
            if (rowIndex % 2 == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + rowH, rowAltPaint)
            }
            canvas.drawLine(margin, y + rowH, pageWidth - margin, y + rowH, borderPaint)

            val textY = y + 14f
            columns.forEachIndexed { i, _ ->
                val (text, paint) = when (i) {
                    0 -> r.billingMonth.take(12) to bodyBoldPaint
                    1 -> when (r.paymentStatus.uppercase()) {
                        "PAID" -> "Paid" to paidPaint
                        "PARTIALLY_PAID" -> "Partial" to partialPaint
                        else -> "Unpaid" to unpaidPaint
                    }

                    2 -> money(r.roomRent) to bodyPaint
                    3 -> money(r.electricityCost) to bodyPaint
                    4 -> money(r.waterCost) to bodyPaint
                    5 -> money(r.wasteCharge) to bodyPaint
                    6 -> money(r.totalAmount) to bodyBoldPaint
                    7 -> money(r.amountPaid) to bodyPaint
                    8 -> money(r.remainingAmount) to (if (r.remainingAmount > 0) unpaidPaint else paidPaint)
                    9 -> r.notes.ifBlank { "—" }.take(30) to bodyPaint   // Description
                    else -> "" to bodyPaint
                }
                val x = if (i in rightAligned)
                    colX[i] + colWidths[i] - 6f - paint.measureText(text)
                else colX[i] + 6f
                canvas.drawText(text, x, textY, paint)
            }
            y += rowH
            rowIndex++
        }

        // totals row
        if (y + 24f > pageHeight - 30f) {
            drawFooter()
            document.finishPage(page)
            pageNumber++
            page = newPage()
            canvas = page.canvas
            y = margin
            drawTableHeader()
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        val tY = y + 15f
        canvas.drawText("TOTALS", margin + 6f, tY, bodyBoldPaint)
        listOf(
            6 to totalBilled,
            7 to totalPaid,
            8 to totalDue
        ).forEach { (idx, value) ->
            val text = money(value)
            val paint = if (idx == 8 && totalDue > 0) unpaidPaint else bodyBoldPaint
            val x = colX[idx] + colWidths[idx] - 6f - paint.measureText(text)
            canvas.drawText(text, x, tY, paint)
        }

        drawFooter()
        document.finishPage(page)

        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_$timestamp.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export PDF"))
    }

    private fun drawSummaryCard(
        canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
        label: String, value: String, accent: Int
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(248, 250, 252) }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; color = Color.rgb(100, 116, 139)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f; isFakeBoldText = true; color = Color.rgb(17, 24, 39)
        }
        canvas.drawRect(x, y, x + w, y + h, bg)
        canvas.drawRect(x, y, x + 3f, y + h, accentPaint)
        canvas.drawText(label, x + 10f, y + 16f, labelPaint)
        canvas.drawText(value, x + 10f, y + 34f, valuePaint)
    }

    // ─────────────────────────────────────────────────────────────────
//  EXCEL  – real .xlsx written via ZipOutputStream (no external deps)
// ─────────────────────────────────────────────────────────────────
    fun exportExcel(context: Context, records: List<MonthlyRecordEntity>) {
        if (records.isEmpty()) return

        val room = activeRoom.value
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val headers = listOf(
            "Billing Month", "Payment Date", "Status",
            "Room Rent", "Elec. Units", "Elec. Cost",
            "Water Units", "Water Cost", "Waste Charge",
            "Total Amount", "Amount Paid", "Remaining Due",
            "Description"
        )

        val sheet = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            append("<cols>")
            append("""<col min="1" max="1" width="18" customWidth="1"/>""")
            append("""<col min="2" max="2" width="14" customWidth="1"/>""")
            append("""<col min="3" max="3" width="14" customWidth="1"/>""")
            for (i in 4..12) append("""<col min="$i" max="$i" width="14" customWidth="1"/>""")
            append("""<col min="13" max="13" width="36" customWidth="1"/>""") // Description
            append("</cols><sheetData>")

            append(
                """<row r="1" ht="24" customHeight="1"><c r="A1" t="inlineStr" s="1"><is><t>Rent History Report — ${
                    xml(
                        room?.name ?: "All Rooms"
                    )
                }</t></is></c></row>"""
            )
            append("""<row r="2" ht="16" customHeight="1"><c r="A2" t="inlineStr" s="2"><is><t>Generated: $generatedAt  •  ${records.size} record(s)</t></is></c></row>""")
            append("""<row r="3"/>""")

            append("""<row r="4" ht="22" customHeight="1">""")
            headers.forEachIndexed { i, h ->
                append("""<c r="${col(i + 1)}4" t="inlineStr" s="3"><is><t>${xml(h)}</t></is></c>""")
            }
            append("</row>")

            var rn = 5
            for (r in records) {
                append("""<row r="$rn" ht="18" customHeight="1">""")
                val cells: List<Any> = listOf(
                    r.billingMonth,
                    r.paymentDate.ifBlank { "—" },
                    r.paymentStatus.replace("_", " "),
                    r.roomRent,
                    r.electricityUnits,
                    r.electricityCost,
                    r.waterUnits,
                    r.waterCost,
                    r.wasteCharge,
                    r.totalAmount,
                    r.amountPaid,
                    r.remainingAmount,
                    r.notes.ifBlank { "" }   // Description
                )
                cells.forEachIndexed { i, v ->
                    val c = col(i + 1)
                    if (v is Number) {
                        append("""<c r="$c$rn" s="5"><v>${v.toDouble()}</v></c>""")
                    } else {
                        append("""<c r="$c$rn" t="inlineStr" s="4"><is><t>${xml(v.toString())}</t></is></c>""")
                    }
                }
                append("</row>")
                rn++
            }

            append("""<row r="$rn" ht="22" customHeight="1">""")
            append("""<c r="A$rn" t="inlineStr" s="6"><is><t>TOTALS</t></is></c>""")
            append("""<c r="B$rn" s="6"/><c r="C$rn" s="6"/>""")
            listOf(
                4 to records.sumOf { it.roomRent },
                5 to records.sumOf { it.electricityUnits },
                6 to records.sumOf { it.electricityCost },
                7 to records.sumOf { it.waterUnits },
                8 to records.sumOf { it.waterCost },
                9 to records.sumOf { it.wasteCharge },
                10 to records.sumOf { it.totalAmount },
                11 to records.sumOf { it.amountPaid },
                12 to records.sumOf { it.remainingAmount }
            ).forEach { (idx, v) ->
                append("""<c r="${col(idx)}$rn" s="6"><v>$v</v></c>""")
            }
            append("""<c r="M$rn" s="6"/>""")
            append("</row></sheetData></worksheet>")
        }

        val styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="#,##0.00"/></numFmts>
<fonts count="6">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="16"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><sz val="10"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
<font><sz val="11"/><color rgb="FF1F2937"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF111827"/><name val="Calibri"/></font>
</fonts>
<fills count="5">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF2563EB"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFE2E8F0"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="2">
<border><left/><right/><top/><bottom/><diagonal/></border>
<border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FFCBD5E1"/></top><bottom style="thin"><color rgb="FFCBD5E1"/></bottom><diagonal/></border>
</borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="7">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
<xf numFmtId="0" fontId="4" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
<xf numFmtId="164" fontId="4" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="5" fillId="4" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
</cellXfs>
</styleSheet>"""

        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

        val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Rent History" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

        val workbookRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_$timestamp.xlsx")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            fun entry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", contentTypes)
            entry("_rels/.rels", rootRels)
            entry("xl/workbook.xml", workbook)
            entry("xl/_rels/workbook.xml.rels", workbookRels)
            entry("xl/styles.xml", styles)
            entry("xl/worksheets/sheet1.xml", sheet)
        }

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Excel"))
    }

    // ─────────────────────────────────────────────────────────────────
//  helpers
// ─────────────────────────────────────────────────────────────────
    private fun col(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i > 0) {
            val rem = (i - 1) % 26
            sb.insert(0, ('A' + rem))
            i = (i - 1) / 26
        }
        return sb.toString()
    }

    private fun xml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")


    // all room
    fun exportExcelAll(context: Context) {
        val rooms = allRooms.value
        if (rooms.isEmpty()) return

        val recordsByRoom = allRecords.value.groupBy { it.roomId }
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val headers = listOf(
            "Billing Month", "Payment Date", "Status",
            "Room Rent", "Elec. Units", "Elec. Cost",
            "Water Units", "Water Cost", "Waste Charge",
            "Total Amount", "Amount Paid", "Remaining Due",
            "Description"
        )

        // ── unique, sanitized sheet names ─────────────────────────────
        val usedNames = mutableSetOf<String>()
        val sheetNames = rooms.map { room ->
            val base = sanitizeSheetName(room.name)
            var candidate = base
            var suffix = 2
            while (candidate.lowercase() in usedNames) {
                candidate = base.take(28) + " " + suffix
                suffix++
            }
            usedNames.add(candidate.lowercase())
            candidate
        }

        // ── one sheet per room ────────────────────────────────────────
        val sheetsXml = rooms.mapIndexed { idx, room ->
            val roomRecords = recordsByRoom[room.id].orEmpty()
                .sortedByDescending { it.billingMonth }
            buildRoomSheet(room, roomRecords, generatedAt, headers)
        }

        // ── [Content_Types].xml ──────────────────────────────────────
        val contentTypes = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
            append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
            append("""<Default Extension="xml" ContentType="application/xml"/>""")
            append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
            for (i in 1..rooms.size) {
                append("""<Override PartName="/xl/worksheets/sheet$i.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
            }
            append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
            append("</Types>")
        }

        // ── workbook.xml ─────────────────────────────────────────────
        val workbook = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
            append("<sheets>")
            rooms.forEachIndexed { idx, _ ->
                append("""<sheet name="${xml(sheetNames[idx])}" sheetId="${idx + 1}" r:id="rId${idx + 1}"/>""")
            }
            append("</sheets></workbook>")
        }

        // ── workbook.xml.rels ────────────────────────────────────────
        val workbookRels = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
            for (i in 1..rooms.size) {
                append("""<Relationship Id="rId$i" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$i.xml"/>""")
            }
            append("""<Relationship Id="rId${rooms.size + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
            append("</Relationships>")
        }

        // ── root rels ────────────────────────────────────────────────
        val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        // ── shared styles ────────────────────────────────────────────
        val styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="#,##0.00"/></numFmts>
<fonts count="6">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="16"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><sz val="10"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
<font><sz val="11"/><color rgb="FF1F2937"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF111827"/><name val="Calibri"/></font>
</fonts>
<fills count="5">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF2563EB"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFE2E8F0"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="2">
<border><left/><right/><top/><bottom/><diagonal/></border>
<border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FFCBD5E1"/></top><bottom style="thin"><color rgb="FFCBD5E1"/></bottom><diagonal/></border>
</borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="7">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
<xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
<xf numFmtId="0" fontId="4" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
<xf numFmtId="164" fontId="4" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
<xf numFmtId="164" fontId="5" fillId="4" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center"/></xf>
</cellXfs>
</styleSheet>"""

        // ── write zip ─────────────────────────────────────────────────
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_all_rooms_$timestamp.xlsx")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            fun entry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry("[Content_Types].xml", contentTypes)
            entry("_rels/.rels", rootRels)
            entry("xl/workbook.xml", workbook)
            entry("xl/_rels/workbook.xml.rels", workbookRels)
            entry("xl/styles.xml", styles)
            sheetsXml.forEachIndexed { idx, sheet ->
                entry("xl/worksheets/sheet${idx + 1}.xml", sheet)
            }
        }

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Excel"))
    }

    private fun buildRoomSheet(
        room: RoomEntity,
        records: List<MonthlyRecordEntity>,
        generatedAt: String,
        headers: List<String>
    ): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("<cols>")
        append("""<col min="1" max="1" width="18" customWidth="1"/>""")
        append("""<col min="2" max="2" width="14" customWidth="1"/>""")
        append("""<col min="3" max="3" width="14" customWidth="1"/>""")
        for (i in 4..12) append("""<col min="$i" max="$i" width="14" customWidth="1"/>""")
        append("""<col min="13" max="13" width="36" customWidth="1"/>""")
        append("</cols><sheetData>")

        // Title row
        append(
            """<row r="1" ht="24" customHeight="1"><c r="A1" t="inlineStr" s="1"><is><t>${
                xml(
                    room.name
                )
            } — Rent History</t></is></c></row>"""
        )

        // Subtitle row (address • generated • count)
        val sub = buildString {
            if (room.address.isNotBlank()) append(room.address).append("  •  ")
            append("Generated: $generatedAt  •  ${records.size} record(s)")
        }
        append("""<row r="2" ht="16" customHeight="1"><c r="A2" t="inlineStr" s="2"><is><t>${xml(sub)}</t></is></c></row>""")
        append("""<row r="3"/>""")

        // Header row
        append("""<row r="4" ht="22" customHeight="1">""")
        headers.forEachIndexed { i, h ->
            append("""<c r="${col(i + 1)}4" t="inlineStr" s="3"><is><t>${xml(h)}</t></is></c>""")
        }
        append("</row>")

        // Data rows
        var rn = 5
        for (r in records) {
            append("""<row r="$rn" ht="18" customHeight="1">""")
            val cells: List<Any> = listOf(
                r.billingMonth,
                r.paymentDate.ifBlank { "—" },
                r.paymentStatus.replace("_", " "),
                r.roomRent,
                r.electricityUnits,
                r.electricityCost,
                r.waterUnits,
                r.waterCost,
                r.wasteCharge,
                r.totalAmount,
                r.amountPaid,
                r.remainingAmount,
                r.notes.ifBlank { "" }
            )
            cells.forEachIndexed { i, v ->
                val c = col(i + 1)
                if (v is Number) {
                    append("""<c r="$c$rn" s="5"><v>${v.toDouble()}</v></c>""")
                } else {
                    append("""<c r="$c$rn" t="inlineStr" s="4"><is><t>${xml(v.toString())}</t></is></c>""")
                }
            }
            append("</row>")
            rn++
        }

        // Totals row (or "no records" note)
        if (records.isNotEmpty()) {
            append("""<row r="$rn" ht="22" customHeight="1">""")
            append("""<c r="A$rn" t="inlineStr" s="6"><is><t>TOTALS</t></is></c>""")
            append("""<c r="B$rn" s="6"/><c r="C$rn" s="6"/>""")
            listOf(
                4 to records.sumOf { it.roomRent },
                5 to records.sumOf { it.electricityUnits },
                6 to records.sumOf { it.electricityCost },
                7 to records.sumOf { it.waterUnits },
                8 to records.sumOf { it.waterCost },
                9 to records.sumOf { it.wasteCharge },
                10 to records.sumOf { it.totalAmount },
                11 to records.sumOf { it.amountPaid },
                12 to records.sumOf { it.remainingAmount }
            ).forEach { (idx, v) ->
                append("""<c r="${col(idx)}$rn" s="6"><v>$v</v></c>""")
            }
            append("""<c r="M$rn" s="6"/>""")
            append("</row>")
        } else {
            append("""<row r="$rn" ht="18" customHeight="1"><c r="A$rn" t="inlineStr" s="4"><is><t>No records for this room yet.</t></is></c></row>""")
        }

        append("</sheetData></worksheet>")
    }

    private fun sanitizeSheetName(name: String): String {
        val cleaned = name.replace(Regex("""[\\/*?\[\]:]"""), "_").trim()
        return cleaned.ifBlank { "Room" }.take(31)
    }

    // ─────────────────────────────────────────────────────────────────
//  PDF  – all rooms, with summary cover page + per-room + grand totals
// ─────────────────────────────────────────────────────────────────
    fun exportPdfAll(context: Context) {
        val rooms = allRooms.value
        if (rooms.isEmpty()) return

        val recordsByRoom = allRecords.value.groupBy { it.roomId }
        val symbol = currencySymbol.value
        val generatedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 32f
        val tableWidth = pageWidth - 2 * margin

        // ── palette ──────────────────────────────────────────────────
        val ink          = Color.rgb(15, 23, 42)
        val inkSoft      = Color.rgb(71, 85, 105)
        val inkMuted     = Color.rgb(148, 163, 184)
        val indigo       = Color.rgb(79, 70, 229)
        val indigoSoft   = Color.rgb(238, 242, 255)
        val emerald      = Color.rgb(16, 185, 129)
        val emeraldSoft  = Color.rgb(236, 253, 245)
        val amber        = Color.rgb(245, 158, 11)
        val amberSoft    = Color.rgb(255, 251, 235)
        val rose         = Color.rgb(244, 63, 94)
        val roseSoft     = Color.rgb(255, 241, 242)
        val line         = Color.rgb(226, 232, 240)
        val rowAlt       = Color.rgb(248, 250, 252)

        // ── paints ───────────────────────────────────────────────────
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f; isFakeBoldText = true; color = ink
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9.5f; color = inkSoft
        }
        val sectionLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f; isFakeBoldText = true; color = indigo
            letterSpacing = 0.15f
        }
        val roomTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 15f; isFakeBoldText = true; color = ink
        }
        val roomSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = inkSoft
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = inkSoft
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = ink
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = ink
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = line; strokeWidth = 0.6f
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = inkMuted
        }
        val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = rowAlt }

        val fillIndigo     = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = indigo }
        val fillIndigoSoft = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = indigoSoft }
        val fillEmeraldSoft= Paint(Paint.ANTI_ALIAS_FLAG).apply { color = emeraldSoft }
        val fillAmberSoft  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = amberSoft }
        val fillRoseSoft   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = roseSoft }

        val emeraldText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = emerald
        }
        val amberText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = amber
        }
        val roseText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = rose
        }

        // ── columns ──────────────────────────────────────────────────
        val columns = listOf(
            "MONTH" to 0.15f, "STATUS" to 0.11f,
            "RENT" to 0.09f, "ELEC" to 0.09f, "WATER" to 0.09f, "WASTE" to 0.08f,
            "TOTAL" to 0.10f, "PAID" to 0.09f, "DUE" to 0.10f,
            "DESCRIPTION" to 0.20f
        )
        val colWidths = columns.map { it.second * tableWidth }
        val colX = FloatArray(columns.size)
        run {
            var acc = margin
            for (i in columns.indices) { colX[i] = acc; acc += colWidths[i] }
        }
        val rightAligned = setOf(2, 3, 4, 5, 6, 7, 8)

        fun money(v: Double) = FormatUtils.formatMoney(v, symbol)

        var pageNumber = 1
        fun newPage(): PdfDocument.Page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )

        var page = newPage()
        var canvas = page.canvas
        var y = margin

        fun drawFooter() {
            val fy = pageHeight - 22f
            canvas.drawLine(margin, fy - 10f, pageWidth - margin, fy - 10f, borderPaint)
            canvas.drawText("Room Rent & Utility — All Rooms Report ‣ By Biplap Neupane.", margin, fy, footerPaint)
            val label = "Page $pageNumber"
            canvas.drawText(label, pageWidth - margin - footerPaint.measureText(label), fy, footerPaint)
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > pageHeight - 34f) {
                drawFooter(); document.finishPage(page)
                pageNumber++; page = newPage(); canvas = page.canvas; y = margin
            }
        }

        fun roundedRect(x: Float, top: Float, w: Float, h: Float, r: Float, paint: Paint) {
            canvas.drawRoundRect(RectF(x, top, x + w, top + h), r, r, paint)
        }

        // ── aggregates ───────────────────────────────────────────────
        data class RoomSummary(
            val room: RoomEntity,
            val records: List<MonthlyRecordEntity>,
            val billed: Double, val paid: Double, val due: Double, val count: Int
        )
        val summaries = rooms.map { room ->
            val recs = recordsByRoom[room.id].orEmpty().sortedByDescending { it.billingMonth }
            RoomSummary(room, recs,
                recs.sumOf { it.totalAmount },
                recs.sumOf { it.amountPaid },
                recs.sumOf { it.remainingAmount },
                recs.size)
        }
        val grandBilled = summaries.sumOf { it.billed }
        val grandPaid   = summaries.sumOf { it.paid }
        val grandDue    = summaries.sumOf { it.due }
        val grandCount  = summaries.sumOf { it.count }

        // ═══════════════════════════════════════════════════════════════
        // COVER — title only, no numbers, no summary
        // ═══════════════════════════════════════════════════════════════
        canvas.drawRect(margin, y, margin + 4f, y + 46f, fillIndigo)
        canvas.drawText("All Rooms Report", margin + 16f, y + 26f, titlePaint)
        canvas.drawText(
            "Generated $generatedAt  ·  ${rooms.size} rooms  ·  $grandCount records",
            margin + 16f, y + 42f, subtitlePaint
        )
        y += 74f

        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        y += 20f

        // ═══════════════════════════════════════════════════════════════
        // DETAIL SECTIONS — one room per block, generous spacing
        // ═══════════════════════════════════════════════════════════════
        val rowH = 24f   // taller rows for breathing room

        fun drawDetailHeader() {
            roundedRect(margin, y, tableWidth, 22f, 6f, fillIndigoSoft)
            columns.forEachIndexed { i, (label, _) ->
                val paint = Paint(headerTextPaint).apply { color = indigo }
                val x = if (i in rightAligned)
                    colX[i] + colWidths[i] - 8f - paint.measureText(label)
                else colX[i] + 10f
                canvas.drawText(label, x, y + 15f, paint)
            }
            y += 22f
        }

        for ((idx, s) in summaries.withIndex()) {
            // generous pre-section spacing
            ensureSpace(150f)

            // divider between rooms
            if (idx > 0) {
                y += 6f
                canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
                y += 22f
            }

            // room header block
            canvas.drawRect(margin, y, margin + 3f, y + 40f, fillIndigo)
            canvas.drawText("ROOM ${idx + 1} OF ${rooms.size}", margin + 14f, y + 14f, sectionLabelPaint)
            canvas.drawText(s.room.name, margin + 14f, y + 32f, roomTitlePaint)
            y += 46f

            val sub = buildString {
                if (s.room.address.isNotBlank()) append(s.room.address).append("  ·  ")
                append("${s.count} record(s)")
            }
            canvas.drawText(sub, margin + 14f, y + 9f, roomSubPaint)
            y += 22f

            // mini stats
            val mGap = 8f
            val mW = (tableWidth - 2 * mGap) / 3f
            val mH = 36f
            drawMiniStat(canvas, margin, y, mW, mH, "BILLED", money(s.billed), indigo, fillIndigoSoft, ink)
            drawMiniStat(canvas, margin + mW + mGap, y, mW, mH, "PAID", money(s.paid), emerald, fillEmeraldSoft, ink)
            drawMiniStat(canvas, margin + 2 * (mW + mGap), y, mW, mH, "DUE", money(s.due), rose, fillRoseSoft, ink)
            y += mH + 22f

            if (s.records.isEmpty()) {
                canvas.drawText("No records for this room yet.", margin + 14f, y + 12f, bodyPaint)
                y += 30f
                continue
            }

            drawDetailHeader()

            var rowIndex = 0
            for (r in s.records) {
                if (y + rowH > pageHeight - 34f) {
                    drawFooter(); document.finishPage(page)
                    pageNumber++; page = newPage(); canvas = page.canvas; y = margin
                    canvas.drawText("${s.room.name} (continued)", margin, y + 12f, roomTitlePaint)
                    y += 26f
                    drawDetailHeader()
                    rowIndex = 0
                }
                if (rowIndex % 2 == 1) {
                    canvas.drawRect(margin, y, pageWidth - margin, y + rowH, rowAltPaint)
                }
                canvas.drawLine(margin, y + rowH, pageWidth - margin, y + rowH, borderPaint)

                val textY = y + 16f
                canvas.drawText(r.billingMonth.take(13), colX[0] + 10f, textY, bodyBoldPaint)

                drawStatusPill(canvas, r.paymentStatus, colX[1] + 8f, y + 6f,
                    fillEmeraldSoft, fillAmberSoft, fillRoseSoft,
                    emeraldText, amberText, roseText)

                val values = listOf(
                    money(r.roomRent), money(r.electricityCost), money(r.waterCost),
                    money(r.wasteCharge), money(r.totalAmount), money(r.amountPaid),
                    money(r.remainingAmount)
                )
                values.forEachIndexed { j, text ->
                    val colIdx = j + 2
                    val paint = when (colIdx) {
                        6 -> bodyBoldPaint
                        8 -> if (r.remainingAmount > 0) roseText else emeraldText
                        else -> bodyPaint
                    }
                    val x = colX[colIdx] + colWidths[colIdx] - 8f - paint.measureText(text)
                    canvas.drawText(text, x, textY, paint)
                }

                val desc = r.notes.ifBlank { "—" }.take(34)
                canvas.drawText(desc, colX[9] + 10f, textY, bodyPaint)

                y += rowH
                rowIndex++
            }

            // subtotal
            ensureSpace(34f)
            y += 8f
            canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
            roundedRect(margin, y, tableWidth, 26f, 0f, fillIndigoSoft)
            val subY = y + 17f
            canvas.drawText("SUBTOTAL — ${s.room.name.take(22)}", margin + 10f, subY, bodyBoldPaint)
            listOf(
                6 to s.billed, 7 to s.paid, 8 to s.due
            ).forEach { (i, v) ->
                val text = money(v)
                val paint = when (i) {
                    7 -> emeraldText
                    8 -> if (s.due > 0) roseText else emeraldText
                    else -> bodyBoldPaint
                }
                val x = colX[i] + colWidths[i] - 8f - paint.measureText(text)
                canvas.drawText(text, x, subY, paint)
            }
            y += 26f
        }

        // ═══════════════════════════════════════════════════════════════
        // FINAL — room summary table + grand total (ONCE, at the end)
        // ═══════════════════════════════════════════════════════════════
        ensureSpace(320f)
        y += 14f
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        y += 24f

        canvas.drawText("ROOM SUMMARY", margin, y + 10f, sectionLabelPaint)
        y += 24f

        val sumCols = listOf(
            "ROOM" to 0.36f, "RECORDS" to 0.10f,
            "BILLED" to 0.18f, "PAID" to 0.18f, "DUE" to 0.18f
        )
        val sumWidths = sumCols.map { it.second * tableWidth }
        val sumX = FloatArray(sumCols.size)
        run {
            var acc = margin
            for (i in sumCols.indices) { sumX[i] = acc; acc += sumWidths[i] }
        }
        val sumRight = setOf(1, 2, 3, 4)

        roundedRect(margin, y, tableWidth, 24f, 6f, fillIndigoSoft)
        sumCols.forEachIndexed { i, (label, _) ->
            val paint = Paint(headerTextPaint).apply { color = indigo }
            val x = if (i in sumRight)
                sumX[i] + sumWidths[i] - 8f - paint.measureText(label)
            else sumX[i] + 10f
            canvas.drawText(label, x, y + 16f, paint)
        }
        y += 24f

        summaries.forEachIndexed { idx, s ->
            ensureSpace(26f)
            if (y == margin) {
                roundedRect(margin, y, tableWidth, 24f, 6f, fillIndigoSoft)
                y += 24f
            }
            if (idx % 2 == 1) canvas.drawRect(margin, y, pageWidth - margin, y + 24f, rowAltPaint)
            canvas.drawLine(margin, y + 24f, pageWidth - margin, y + 24f, borderPaint)

            val textY = y + 16f
            canvas.drawText(s.room.name.take(34), sumX[0] + 10f, textY, bodyBoldPaint)

            val recText = s.count.toString()
            canvas.drawText(recText, sumX[1] + sumWidths[1] - 8f - bodyPaint.measureText(recText), textY, bodyPaint)

            val billedText = money(s.billed)
            canvas.drawText(billedText, sumX[2] + sumWidths[2] - 8f - bodyPaint.measureText(billedText), textY, bodyPaint)

            val paidText = money(s.paid)
            canvas.drawText(paidText, sumX[3] + sumWidths[3] - 8f - emeraldText.measureText(paidText), textY, emeraldText)

            val dueText = money(s.due)
            val duePaint = if (s.due > 0) roseText else emeraldText
            canvas.drawText(dueText, sumX[4] + sumWidths[4] - 8f - duePaint.measureText(dueText), textY, duePaint)

            y += 24f
        }

        // grand total row inside the summary table
        ensureSpace(30f)
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        roundedRect(margin, y, tableWidth, 28f, 0f, fillIndigoSoft)
        canvas.drawRect(margin, y, margin + 3f, y + 28f, fillIndigo)
        val gTotY = y + 18f
        canvas.drawText("GRAND TOTAL", margin + 10f, gTotY, bodyBoldPaint)
        listOf(
            1 to grandCount.toString(),
            2 to money(grandBilled),
            3 to money(grandPaid),
            4 to money(grandDue)
        ).forEach { (i, text) ->
            val paint = when (i) {
                3 -> emeraldText
                4 -> if (grandDue > 0) roseText else emeraldText
                else -> bodyBoldPaint
            }
            val x = sumX[i] + sumWidths[i] - 8f - paint.measureText(text)
            canvas.drawText(text, x, gTotY, paint)
        }
        y += 44f

        // light grand total band
        ensureSpace(110f)
        val bandH = 88f

        val bandBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = indigoSoft }   // indigo-50
        roundedRect(margin, y, tableWidth, bandH, 14f, bandBg)
        canvas.drawRect(margin, y, margin + 4f, y + bandH, fillIndigo)            // accent bar

        val lbl = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f; isFakeBoldText = true; color = indigo
            letterSpacing = 0.12f
        }
        canvas.drawText("GRAND TOTAL ACROSS ALL ROOMS", margin + 20f, y + 28f, lbl)

        val valPaintBig = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f; isFakeBoldText = true; color = ink
        }
        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; isFakeBoldText = true; color = inkSoft
            letterSpacing = 0.08f
        }
        val seg = (tableWidth - 40f) / 3f
        val segY = y + 54f
        listOf(
            Triple("BILLED", grandBilled, indigo),
            Triple("PAID",   grandPaid,   emerald),
            Triple("DUE",    grandDue,    if (grandDue > 0) rose else emerald)
        ).forEachIndexed { i, (cap, value, color) ->
            val cx = margin + 20f + i * seg
            val cp = Paint(valPaintBig).apply { this.color = color }
            canvas.drawText(cap, cx, segY, capPaint)
            canvas.drawText(money(value), cx, segY + 22f, cp)
        }

        y += bandH + 18f
        canvas.drawText(
            "Report covers ${rooms.size} room(s) and $grandCount record(s).",
            margin, y + 8f, subtitlePaint
        )

        drawFooter()
        document.finishPage(page)

        // ── write & share ─────────────────────────────────────────────
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "rent_history_all_rooms_$timestamp.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export All Rooms PDF"))
    }    private fun drawMetricCard(
        canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
        label: String, value: String,
        accent: Int, fillColor: Paint, valueColor: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 10f, 10f, fillColor)
        canvas.drawRect(x, y, x + 3f, y + h, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent })

        val cap = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f; isFakeBoldText = true; color = accent; letterSpacing = 0.1f
        }
        val valP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 15f; isFakeBoldText = true; color = valueColor
        }
        canvas.drawText(label, x + 14f, y + 20f, cap)
        canvas.drawText(value, x + 14f, y + 42f, valP)

        val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = Color.rgb(148, 163, 184)
        }
        canvas.drawText("across all rooms", x + 14f, y + 56f, sub)
    }

    private fun drawMiniStat(
        canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
        label: String, value: String,
        accent: Int, fillColor: Paint, valueColor: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 8f, 8f, fillColor)
        canvas.drawRect(x, y, x + 3f, y + h, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent })

        val cap = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 7.5f; isFakeBoldText = true; color = accent; letterSpacing = 0.08f
        }
        val valP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f; isFakeBoldText = true; color = valueColor
        }
        canvas.drawText(label, x + 12f, y + 12f, cap)
        canvas.drawText(value, x + 12f, y + 25f, valP)
    }

    private fun drawStatusPill(
        canvas: Canvas, status: String, x: Float, y: Float,
        fillPaid: Paint, fillPartial: Paint, fillUnpaid: Paint,
        textPaid: Paint, textPartial: Paint, textUnpaid: Paint
    ) {
        val (label, fill, text) = when (status.uppercase()) {
            "PAID" -> Triple("Paid", fillPaid, textPaid)
            "PARTIALLY_PAID" -> Triple("Partial", fillPartial, textPartial)
            else -> Triple("Unpaid", fillUnpaid, textUnpaid)
        }
        val tw = text.measureText(label)
        val pillW = tw + 16f
        val pillH = 14f
        val rect = RectF(x, y, x + pillW, y + pillH)
        canvas.drawRoundRect(rect, 7f, 7f, fill)
        canvas.drawText(label, x + 8f, y + 10f, text)
    }
}