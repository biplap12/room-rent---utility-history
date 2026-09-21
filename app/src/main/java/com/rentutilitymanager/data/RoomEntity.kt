package com.rentutilitymanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String = "",
    val defaultRent: Double = 0.0,
    val electricityRate: Double = 0.0,
    val waterRate: Double = 0.0,
    val wasteCharge: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val tenantName: String = "",
    val tenantPhone: String = "",
    val tenantEmail: String = "",
    val tenantIdNumber: String = "",
    val emergencyContact: String = "",
    val moveInDate: String = "",
    val securityDeposit: Double = 0.0,
    val propertyType: String = "ROOM",
    val waterBillingMode: String = "UNIT",
    val directWaterAmount: Double = 0.0,
    val tenantNotes: String = "",
    val startElectricityUnit: Double = 0.0,
    val startWaterUnit: Double = 0.0,

    )
