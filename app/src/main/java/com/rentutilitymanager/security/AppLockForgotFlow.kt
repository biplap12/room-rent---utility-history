package com.rentutilitymanager.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity

private enum class RecoveryStep { ANSWERS, BIOMETRIC, RESET }

@Composable
fun AppLockForgotFlow(
    config: AppLockConfig,
    onDismiss: () -> Unit,
    onResetCredential: (String) -> Unit
) {
    val context = LocalContext.current

    var step by remember { mutableStateOf(RecoveryStep.ANSWERS) }
    var a1 by remember { mutableStateOf("") }
    var a2 by remember { mutableStateOf("") }
    var a3 by remember { mutableStateOf("") }
    var newCredential by remember { mutableStateOf("") }
    var confirmNew by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var biometricChecked by remember { mutableStateOf(false) }

    // Auto-launch biometric once we reach that step
    LaunchedEffect(step) {
        if (step == RecoveryStep.BIOMETRIC && !biometricChecked) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            BiometricHelper.prompt(
                activity = activity,
                title = "Verify identity",
                subtitle = "Confirm with biometrics to continue",
                onSuccess = { biometricChecked = true; step = RecoveryStep.RESET },
                onFailure = { error = "Biometric verification failed" }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Account recovery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    when (step) {
                        RecoveryStep.ANSWERS -> "Answer all three security questions"
                        RecoveryStep.BIOMETRIC -> "Verify with biometrics"
                        RecoveryStep.RESET -> "Set a new ${if (config.lockType == LockType.PIN) "PIN" else "password"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                when (step) {
                    RecoveryStep.ANSWERS -> {
                        RecoveryAnswerField(config.q1, a1, { a1 = it; error = null })
                        Spacer(Modifier.height(10.dp))
                        RecoveryAnswerField(config.q2, a2, { a2 = it; error = null })
                        Spacer(Modifier.height(10.dp))
                        RecoveryAnswerField(config.q3, a3, { a3 = it; error = null })
                    }
                    RecoveryStep.BIOMETRIC -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Fingerprint, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Verifying…", fontWeight = FontWeight.Medium)
                        }
                    }
                    RecoveryStep.RESET -> {
                        OutlinedTextField(
                            value = newCredential,
                            onValueChange = {
                                newCredential = if (config.lockType == LockType.PIN)
                                    it.filter { c -> c.isDigit() }.take(8)
                                else it.take(64)
                                error = null
                            },
                            label = { Text("New ${if (config.lockType == LockType.PIN) "PIN" else "password"}") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (config.lockType == LockType.PIN)
                                    KeyboardType.NumberPassword else KeyboardType.Password
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmNew,
                            onValueChange = { confirmNew = it; error = null },
                            label = { Text("Confirm") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (config.lockType == LockType.PIN)
                                    KeyboardType.NumberPassword else KeyboardType.Password
                            ),
                            isError = error != null,
                            supportingText = error?.let { { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (error != null && step != RecoveryStep.RESET) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            error = null
                            when (step) {
                                RecoveryStep.ANSWERS -> {
                                    if (a1.isBlank() || a2.isBlank() || a3.isBlank()) {
                                        error = "All answers required"; return@Button
                                    }
                                    val ok1 = AppLockPrefs.verify(a1, config.a1Salt, config.a1Hash)
                                    val ok2 = AppLockPrefs.verify(a2, config.a2Salt, config.a2Hash)
                                    val ok3 = AppLockPrefs.verify(a3, config.a3Salt, config.a3Hash)
                                    if (ok1 && ok2 && ok3) {
                                        step = RecoveryStep.BIOMETRIC
                                    } else {
                                        error = "One or more answers are incorrect"
                                    }
                                }
                                RecoveryStep.BIOMETRIC -> {
                                    // retry biometric
                                    biometricChecked = false
                                }
                                RecoveryStep.RESET -> {
                                    if (newCredential.isBlank()) { error = "Required"; return@Button }
                                    if (config.lockType == LockType.PIN && newCredential.length < 4) {
                                        error = "PIN must be 4–8 digits"; return@Button
                                    }
                                    if (config.lockType == LockType.PASSWORD && newCredential.length < 6) {
                                        error = "Password must be at least 6 characters"; return@Button
                                    }
                                    if (newCredential != confirmNew) {
                                        error = "Doesn't match"; return@Button
                                    }
                                    onResetCredential(newCredential)
                                }
                            }
                        },
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            when (step) {
                                RecoveryStep.ANSWERS -> "Verify"
                                RecoveryStep.BIOMETRIC -> "Retry"
                                RecoveryStep.RESET -> "Save"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryAnswerField(
    question: String,
    value: String,
    onChange: (String) -> Unit
) {
    Column {
        Text(
            question,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text("Answer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}