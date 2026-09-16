package com.example.viewmodel

import android.app.Application
import android.content.Context
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

    val currentRoomRecords: StateFlow<List<MonthlyRecordEntity>> = activeRoom.flatMapLatest { room ->
        if (room != null) repository.getRecordsForRoom(room.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<MonthlyRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestRecord: StateFlow<MonthlyRecordEntity?> = currentRoomRecords.combine(flowOf(Unit)) { records, _ ->
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
    val selectedRecordForDetail: StateFlow<MonthlyRecordEntity?> = _selectedRecordForDetail.asStateFlow()

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
            error = "Current electricity meter (${currElec}) cannot be lower than previous (${prevElec})."
        } else if (state.currentWaterReading.isNotBlank() && currWater < prevWater) {
            error = "Current water meter (${currWater}) cannot be lower than previous (${prevWater})."
        } else if (elecRate < 0 || waterRate < 0 || rent < 0 || waste < 0 || other < 0 || discount < 0 || paid < 0) {
            error = "Negative values are not allowed."
        } else if (state.billingMonth.isBlank()) {
            error = "Billing month cannot be empty."
        }

        // High usage warning
        var warning: String? = null
        val prevUsage = state.previousMonthElecUnits
        if (prevUsage != null && prevUsage > 0 && elecUnits > (prevUsage * 2.0)) {
            warning = "Electricity usage (${FormatUtils.formatUnits(elecUnits)} units) is more than 2x previous month (${FormatUtils.formatUnits(prevUsage)} units). Please verify meter reading."
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

    fun exportCsv(context: Context) {
        val room = activeRoom.value
        val records = currentRoomRecords.value
        val csv = CsvExporter.generateCsvContent(room, records, currencySymbol.value)
        CsvExporter.shareCsv(context, csv, "${room?.name ?: "Room"}_Bills.csv")
    }

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
}
