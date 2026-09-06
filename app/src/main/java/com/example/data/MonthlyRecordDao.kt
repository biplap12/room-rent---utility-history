package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyRecordDao {
    @Query("SELECT * FROM monthly_records WHERE roomId = :roomId ORDER BY createdAt DESC, id DESC")
    fun getRecordsForRoom(roomId: Long): Flow<List<MonthlyRecordEntity>>

    @Query("SELECT * FROM monthly_records ORDER BY createdAt DESC, id DESC")
    fun getAllRecords(): Flow<List<MonthlyRecordEntity>>

    @Query("SELECT * FROM monthly_records WHERE roomId = :roomId ORDER BY createdAt DESC, id DESC LIMIT 1")
    fun getLatestRecordForRoom(roomId: Long): Flow<MonthlyRecordEntity?>

    @Query("SELECT * FROM monthly_records WHERE roomId = :roomId ORDER BY createdAt DESC, id DESC LIMIT 1")
    suspend fun getLatestRecordForRoomSync(roomId: Long): MonthlyRecordEntity?

    @Query("SELECT * FROM monthly_records WHERE id = :id LIMIT 1")
    fun getRecordById(id: Long): Flow<MonthlyRecordEntity?>

    @Query("SELECT * FROM monthly_records WHERE id = :id LIMIT 1")
    suspend fun getRecordByIdSync(id: Long): MonthlyRecordEntity?

    @Query("SELECT COUNT(*) FROM monthly_records")
    suspend fun getRecordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MonthlyRecordEntity): Long

    @Update
    suspend fun updateRecord(record: MonthlyRecordEntity)

    @Delete
    suspend fun deleteRecord(record: MonthlyRecordEntity)

    @Query("DELETE FROM monthly_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
}
