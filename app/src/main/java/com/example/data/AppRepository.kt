//package com.example.data
//
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.withContext
//
//class AppRepository(
//    private val database: AppDatabase,
//    val settings: AppSettings
//) {
//    private val roomDao = database.roomDao()
//    private val recordDao = database.monthlyRecordDao()
//
//    val allRooms: Flow<List<RoomEntity>> = roomDao.getAllRooms()
//
//    fun getRecordsForRoom(roomId: Long): Flow<List<MonthlyRecordEntity>> =
//        recordDao.getRecordsForRoom(roomId)
//
//    val allRecords: Flow<List<MonthlyRecordEntity>> = recordDao.getAllRecords()
//
//    fun getLatestRecordForRoom(roomId: Long): Flow<MonthlyRecordEntity?> =
//        recordDao.getLatestRecordForRoom(roomId)
//
//    suspend fun getLatestRecordForRoomSync(roomId: Long): MonthlyRecordEntity? =
//        withContext(Dispatchers.IO) {
//            recordDao.getLatestRecordForRoomSync(roomId)
//        }
//
//    suspend fun getRoomByIdSync(id: Long): RoomEntity? =
//        withContext(Dispatchers.IO) {
//            roomDao.getRoomByIdSync(id)
//        }
//
//    suspend fun insertRoom(room: RoomEntity): Long =
//        withContext(Dispatchers.IO) {
//            roomDao.insertRoom(room)
//        }
//
//    suspend fun updateRoom(room: RoomEntity) =
//        withContext(Dispatchers.IO) {
//            roomDao.updateRoom(room)
//        }
//
//    suspend fun deleteRoom(room: RoomEntity) =
//        withContext(Dispatchers.IO) {
//            roomDao.deleteRoom(room)
//        }
//
//    suspend fun deleteRoomById(roomId: Long) =
//        withContext(Dispatchers.IO) {
//            roomDao.deleteRoomById(roomId)
//        }
//
//    suspend fun insertRecord(record: MonthlyRecordEntity): Long =
//        withContext(Dispatchers.IO) {
//            recordDao.insertRecord(record)
//        }
//
//    suspend fun updateRecord(record: MonthlyRecordEntity) =
//        withContext(Dispatchers.IO) {
//            recordDao.updateRecord(record)
//        }
//
//    suspend fun deleteRecord(record: MonthlyRecordEntity) =
//        withContext(Dispatchers.IO) {
//            recordDao.deleteRecord(record)
//        }
//
//    suspend fun deleteRecordById(id: Long) =
//        withContext(Dispatchers.IO) {
//            recordDao.deleteRecordById(id)
//        }
//
//    suspend fun getRecordByIdSync(id: Long): MonthlyRecordEntity? =
//        withContext(Dispatchers.IO) {
//            recordDao.getRecordByIdSync(id)
//        }
//
//    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
//        if (roomDao.getRoomCount() == 0) {
//            val defaultRoomId = roomDao.insertRoom(
//                RoomEntity(
//                    name = "Room 101",
//                    address = "Apartment 2B, 2nd Floor",
//                    defaultRent = 10000.0,
//                    electricityRate = 15.0,
//                    waterRate = 30.0,
//                    wasteCharge = 100.0
//                )
//            )
//
//            // Add previous month (August 2026) - Paid
//            recordDao.insertRecord(
//                MonthlyRecordEntity(
//                    roomId = defaultRoomId,
//                    billingMonth = "August 2026",
//                    previousElectricityReading = 1165.0,
//                    currentElectricityReading = 1250.0,
//                    electricityUnits = 85.0,
//                    electricityRate = 15.0,
//                    electricityCost = 1275.0,
//                    previousWaterReading = 90.0,
//                    currentWaterReading = 100.0,
//                    waterUnits = 10.0,
//                    waterRate = 30.0,
//                    waterCost = 300.0,
//                    roomRent = 10000.0,
//                    wasteCharge = 100.0,
//                    otherCharges = 0.0,
//                    discount = 0.0,
//                    totalAmount = 11675.0,
//                    amountPaid = 11675.0,
//                    remainingAmount = 0.0,
//                    paymentDate = "2026-08-05",
//                    paymentStatus = "PAID",
//                    notes = "Paid on time via online transfer",
//                    createdAt = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
//                    updatedAt = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
//                )
//            )
//
//            // Add current month (September 2026) - Current Bill matching user example
//            recordDao.insertRecord(
//                MonthlyRecordEntity(
//                    roomId = defaultRoomId,
//                    billingMonth = "September 2026",
//                    previousElectricityReading = 1250.0,
//                    currentElectricityReading = 1335.0,
//                    electricityUnits = 85.0,
//                    electricityRate = 15.0,
//                    electricityCost = 1275.0,
//                    previousWaterReading = 100.0,
//                    currentWaterReading = 110.0,
//                    waterUnits = 10.0,
//                    waterRate = 30.0,
//                    waterCost = 300.0,
//                    roomRent = 10000.0,
//                    wasteCharge = 100.0,
//                    otherCharges = 0.0,
//                    discount = 0.0,
//                    totalAmount = 11675.0,
//                    amountPaid = 0.0,
//                    remainingAmount = 11675.0,
//                    paymentDate = "",
//                    paymentStatus = "UNPAID",
//                    notes = "Current month rent & utility due",
//                    createdAt = System.currentTimeMillis(),
//                    updatedAt = System.currentTimeMillis()
//                )
//            )
//
//            settings.setSelectedRoomId(defaultRoomId)
//        }
//    }
//}


package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val database: AppDatabase,
    val settings: AppSettings
) {
    private val roomDao = database.roomDao()
    private val recordDao = database.monthlyRecordDao()

    val allRooms: Flow<List<RoomEntity>> = roomDao.getAllRooms()

    fun getRecordsForRoom(roomId: Long): Flow<List<MonthlyRecordEntity>> =
        recordDao.getRecordsForRoom(roomId)

    val allRecords: Flow<List<MonthlyRecordEntity>> = recordDao.getAllRecords()

    fun getLatestRecordForRoom(roomId: Long): Flow<MonthlyRecordEntity?> =
        recordDao.getLatestRecordForRoom(roomId)

    suspend fun getLatestRecordForRoomSync(roomId: Long): MonthlyRecordEntity? =
        withContext(Dispatchers.IO) {
            recordDao.getLatestRecordForRoomSync(roomId)
        }

    suspend fun getRoomByIdSync(id: Long): RoomEntity? =
        withContext(Dispatchers.IO) {
            roomDao.getRoomByIdSync(id)
        }

    suspend fun insertRoom(room: RoomEntity): Long =
        withContext(Dispatchers.IO) {
            roomDao.insertRoom(room)
        }

    suspend fun updateRoom(room: RoomEntity) =
        withContext(Dispatchers.IO) {
            roomDao.updateRoom(room)
        }

    suspend fun deleteRoom(room: RoomEntity) =
        withContext(Dispatchers.IO) {
            roomDao.deleteRoom(room)
        }

    suspend fun deleteRoomById(roomId: Long) =
        withContext(Dispatchers.IO) {
            roomDao.deleteRoomById(roomId)
        }

    suspend fun insertRecord(record: MonthlyRecordEntity): Long =
        withContext(Dispatchers.IO) {
            recordDao.insertRecord(record)
        }

    suspend fun updateRecord(record: MonthlyRecordEntity) =
        withContext(Dispatchers.IO) {
            recordDao.updateRecord(record)
        }

    suspend fun deleteRecord(record: MonthlyRecordEntity) =
        withContext(Dispatchers.IO) {
            recordDao.deleteRecord(record)
        }

    suspend fun deleteRecordById(id: Long) =
        withContext(Dispatchers.IO) {
            recordDao.deleteRecordById(id)
        }

    suspend fun getRecordByIdSync(id: Long): MonthlyRecordEntity? =
        withContext(Dispatchers.IO) {
            recordDao.getRecordByIdSync(id)
        }
}