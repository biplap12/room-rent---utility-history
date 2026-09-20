package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/* ═══════════════════════════════════════════════════════════════
   SET PIN DIALOG — first-time setup or change existing PIN
   ═══════════════════════════════════════════════════════════════ */
@Composable
fun SetPinDialog(
    isChanging: Boolean,
    onDismiss: () -> Unit,
    onSave: (newPin: String) -> Unit,
    verifyCurrentPin: (String) -> Boolean = { false }
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val accent = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accent.copy(alpha = 0.28f),
                                accent.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isChanging) Icons.Default.LockReset else Icons.Default.Lock,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                if (isChanging) "Change Delete PIN" else "Set Delete PIN",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "This PIN protects rooms and records from being deleted. " +
                            "Choose 4–6 digits. It is stored encrypted and cannot be recovered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                if (isChanging) {
                    PinField(
                        value = currentPin,
                        onValueChange = { if (it.length <= 6) currentPin = it.filter(Char::isDigit) },
                        label = "Current PIN",
                        showPin = showPin,
                        isError = errorText != null && currentPin.isNotBlank()
                    )
                    Spacer(Modifier.height(10.dp))
                }

                PinField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6) newPin = it.filter(Char::isDigit) },
                    label = "New PIN (4–6 digits)",
                    showPin = showPin,
                    isError = errorText != null && newPin.isNotBlank()
                )

                Spacer(Modifier.height(10.dp))

                PinField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it.filter(Char::isDigit) },
                    label = "Confirm PIN",
                    showPin = showPin,
                    isError = errorText != null && confirmPin.isNotBlank()
                )

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showPin,
                        onCheckedChange = { showPin = it }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Show PIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (errorText != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusUnpaid.copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, StatusUnpaid.copy(alpha = 0.30f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                tint = StatusUnpaid,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                errorText!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusUnpaid,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorText = when {
                        isChanging && !verifyCurrentPin(currentPin) ->
                            "Current PIN is incorrect"
                        newPin.length < 4 ->
                            "PIN must be at least 4 digits"
                        newPin != confirmPin ->
                            "PINs do not match"
                        newPin.all { it == newPin[0] } ->
                            "PIN is too simple (all same digit)"
                        else -> null
                    }
                    if (errorText == null) onSave(newPin)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save PIN", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/* ═══════════════════════════════════════════════════════════════
   VERIFY PIN DIALOG — required before destructive actions
   ═══════════════════════════════════════════════════════════════ */
@Composable
fun VerifyPinDialog(
    title: String = "Enter Delete PIN",
    message: String = "This action requires your delete PIN.",
    onDismiss: () -> Unit,
    onVerified: () -> Unit,
    verify: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var shakeTrigger by remember { mutableStateOf(0) }

    val accent = StatusUnpaid

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accent.copy(alpha = 0.24f),
                                accent.copy(alpha = 0.06f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock, null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                PinField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) {
                            pin = it.filter(Char::isDigit)
                            errorText = null
                        }
                    },
                    label = "PIN",
                    showPin = showPin,
                    isError = errorText != null,
                    shakeKey = shakeTrigger
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showPin, onCheckedChange = { showPin = it })
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Show PIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (errorText != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = StatusUnpaid,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            errorText!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusUnpaid,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length < 4) {
                        errorText = "PIN must be at least 4 digits"
                        shakeTrigger++
                    } else if (!verify(pin)) {
                        errorText = "Incorrect PIN"
                        shakeTrigger++
                        pin = ""
                    } else {
                        onVerified()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                enabled = pin.length >= 4
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Verify", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/* ═══════════════════════════════════════════════════════════════
   PIN INPUT FIELD — masked by default, shake on error
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showPin: Boolean,
    isError: Boolean,
    shakeKey: Int = 0
) {
    // Simple horizontal shake on error
    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(shakeKey) {
        if (shakeKey > 0) {
            val seq = listOf(-10f, 10f, -8f, 8f, -5f, 5f, 0f)
            for (v in seq) offsetX.animateTo(
                v,
                androidx.compose.animation.core.tween(30)
            )
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                Icons.Default.Password, null,
                tint = if (isError) StatusUnpaid
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        visualTransformation = if (showPin)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            letterSpacing = if (showPin) 0.sp else 8.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX.value.dp)
    )
}