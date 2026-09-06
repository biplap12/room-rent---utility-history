package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_records",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roomId"),
        Index("billingMonth")
    ]
)
data class MonthlyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val billingMonth: String,
    val previousElectricityReading: Double = 0.0,
    val currentElectricityReading: Double = 0.0,
    val electricityUnits: Double = 0.0,
    val electricityRate: Double = 15.0,
    val electricityCost: Double = 0.0,
    val previousWaterReading: Double = 0.0,
    val currentWaterReading: Double = 0.0,
    val waterUnits: Double = 0.0,
    val waterRate: Double = 0.0,
    val waterCost: Double = 0.0,
    val roomRent: Double = 0.0,
    val wasteCharge: Double = 0.0,
    val otherCharges: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentDate: String = "",
    val paymentStatus: String = "UNPAID",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
