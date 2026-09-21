package com.rentutilitymanager.data

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