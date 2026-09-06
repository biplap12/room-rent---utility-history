package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoomEntity
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.*
import com.example.util.FormatUtils
import com.example.viewmodel.RecordFormState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    formState: RecordFormState,
    activeRoom: RoomEntity?,
    currencySymbol: String,
    onUpdateField: ((RecordFormState) -> RecordFormState) -> Unit,
    onSave: () -> Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (formState.editingRecordId != null) "Edit Monthly Record" else "Add New Record",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Room: ${activeRoom?.name ?: "Selected Room"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (formState.editingRecordId != null) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                ) {
                    Text("Cancel")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LIVE BILL PREVIEW SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CALCULATED TOTAL BILL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatMoney(formState.totalAmount, currencySymbol),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    PaymentStatusBadge(status = formState.paymentStatus)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Elec: ${FormatUtils.formatUnits(formState.electricityUnits)} u (${FormatUtils.formatMoney(formState.electricityCost, currencySymbol)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricityAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Water: ${FormatUtils.formatUnits(formState.waterUnits)} u (${FormatUtils.formatMoney(formState.waterCost, currencySymbol)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = WaterAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Due: ${FormatUtils.formatMoney(formState.remainingAmount, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (formState.remainingAmount > 0) StatusUnpaid else StatusPaid,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Validation Error banner
        AnimatedVisibility(visible = formState.validationError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusUnpaidContainer),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StatusUnpaidBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = StatusUnpaid, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formState.validationError ?: "",
                        color = StatusUnpaid,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // High Usage Warning banner
        AnimatedVisibility(visible = formState.highUsageWarning != null && formState.validationError == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusPartialContainer),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StatusPartialBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusPartial, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formState.highUsageWarning ?: "",
                        color = StatusPartial,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: BILLING PERIOD & RENT
        Text(
            text = "1. Billing Period & Rent",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.billingMonth,
            onValueChange = { value -> onUpdateField { it.copy(billingMonth = value) } },
            label = { Text("Billing Month / Period") },
            placeholder = { Text("e.g. October 2026") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("billing_month_input"),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = formState.roomRent,
            onValueChange = { value -> onUpdateField { it.copy(roomRent = value) } },
            label = { Text("Room Rent ($currencySymbol)") },
            leadingIcon = { Icon(Icons.Default.Bed, contentDescription = null, tint = RentAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("room_rent_input"),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: ELECTRICITY USAGE
        Text(
            text = "2. Electricity Meter & Units",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ElectricityAccent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Previous meter is automatically filled. Enter current meter reading.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = formState.previousElectricityReading,
                onValueChange = { value -> onUpdateField { it.copy(previousElectricityReading = value) } },
                label = { Text("Prev Meter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("prev_electricity_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.currentElectricityReading,
                onValueChange = { value -> onUpdateField { it.copy(currentElectricityReading = value) } },
                label = { Text("Current Meter *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("current_electricity_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = formState.electricityRate,
                onValueChange = { value -> onUpdateField { it.copy(electricityRate = value) } },
                label = { Text("Rate / Unit ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("electricity_rate_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Electricity Cost",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${FormatUtils.formatUnits(formState.electricityUnits)} u = ${FormatUtils.formatMoney(formState.electricityCost, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricityAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: WATER USAGE
        Text(
            text = "3. Water Meter & Units",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = WaterAccent
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = formState.previousWaterReading,
                onValueChange = { value -> onUpdateField { it.copy(previousWaterReading = value) } },
                label = { Text("Prev Meter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("prev_water_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.currentWaterReading,
                onValueChange = { value -> onUpdateField { it.copy(currentWaterReading = value) } },
                label = { Text("Current Meter") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("current_water_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = formState.waterRate,
                onValueChange = { value -> onUpdateField { it.copy(waterRate = value) } },
                label = { Text("Water Rate ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("water_rate_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Water Cost",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${FormatUtils.formatUnits(formState.waterUnits)} u = ${FormatUtils.formatMoney(formState.waterCost, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaterAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: OTHER CHARGES & DISCOUNT
        Text(
            text = "4. Additional Charges & Discount",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = formState.wasteCharge,
                onValueChange = { value -> onUpdateField { it.copy(wasteCharge = value) } },
                label = { Text("Waste / Minimum ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("waste_charge_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.otherCharges,
                onValueChange = { value -> onUpdateField { it.copy(otherCharges = value) } },
                label = { Text("Other Charges ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("other_charges_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = formState.discount,
            onValueChange = { value -> onUpdateField { it.copy(discount = value) } },
            label = { Text("Discount, if any ($currencySymbol)") },
            leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = StatusPaid) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("discount_input"),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: PAYMENT DETAILS
        Text(
            text = "5. Payment Tracking",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = formState.amountPaid,
                onValueChange = { value -> onUpdateField { it.copy(amountPaid = value) } },
                label = { Text("Amount Paid ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("amount_paid_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    onUpdateField { it.copy(amountPaid = it.totalAmount.toString()) }
                },
                modifier = Modifier
                    .weight(0.8f)
                    .align(Alignment.CenterVertically),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Pay Full", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = formState.paymentDate,
                onValueChange = { value -> onUpdateField { it.copy(paymentDate = value) } },
                label = { Text("Payment Date") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("payment_date_input"),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            OutlinedButton(
                onClick = {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    onUpdateField { it.copy(paymentDate = sdf.format(Date())) }
                },
                modifier = Modifier.weight(0.8f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
            ) {
                Text("Today", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = formState.notes,
            onValueChange = { value -> onUpdateField { it.copy(notes = value) } },
            label = { Text("Notes / Remarks (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_input"),
            shape = RoundedCornerShape(8.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PRIMARY ACTION SAVE BUTTON
        Button(
            onClick = { onSave() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_record_button"),
            shape = RoundedCornerShape(8.dp),
            enabled = formState.validationError == null && formState.billingMonth.isNotBlank()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (formState.editingRecordId != null) "Update Monthly Record" else "Save Monthly Record",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
