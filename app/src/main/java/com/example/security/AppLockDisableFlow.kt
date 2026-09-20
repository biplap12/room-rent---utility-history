package com.example.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AppLockDisableFlow(
    config: AppLockConfig,
    onDismiss: () -> Unit,
    onConfirmDisable: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var biometricFailed by remember { mutableStateOf(false) }

    val biometricAvailable = config.biometricEnabled && BiometricHelper.canUse(context)

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(160)
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(Unit) {
        if (biometricAvailable && !biometricFailed) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            BiometricHelper.prompt(
                activity = activity,
                title = "Remove app lock",
                subtitle = "Verify to continue",
                onSuccess = onConfirmDisable,
                onFailure = { biometricFailed = true }
            )
        }
    }

    fun verifyAndDisable() {
        if (AppLockPrefs.verify(input, config.credentialSalt, config.credentialHash)) {
            onConfirmDisable()
        } else {
            error = "Incorrect ${if (config.lockType == LockType.PIN) "PIN" else "password"}"
            input = ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier.fillMaxWidth(),
                accent = StatusUnpaid,
                shape = RoundedCornerShape(LuxeTokens.DialogRadius),
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientIconBadge(
                        icon = Icons.Default.Warning,
                        accent = StatusUnpaid,
                        size = 64.dp,
                        iconSize = 30.dp,
                        corner = 20.dp
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "Remove app lock?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Enter your current ${if (config.lockType == LockType.PIN) "PIN" else "password"} to confirm. This will disable all protection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    LuxeTextField(
                        value = input,
                        onValueChange = {
                            input = if (config.lockType == LockType.PIN)
                                it.filter { c -> c.isDigit() }.take(8)
                            else it.take(64)
                            error = null
                        },
                        label = if (config.lockType == LockType.PIN)
                            "Current PIN" else "Current password",
                        placeholder = if (config.lockType == LockType.PIN)
                            "Enter your PIN" else "Enter your password",
                        leadingIcon = if (config.lockType == LockType.PIN)
                            Icons.Default.Lock else Icons.Default.Password,
                        trailingIcon = if (config.lockType == LockType.PASSWORD) {
                            {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        null,
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        } else null,
                        visualTransformation = if (config.lockType == LockType.PIN || !passwordVisible)
                            PasswordVisualTransformation()
                        else VisualTransformation.None,
                        keyboardType = if (config.lockType == LockType.PIN)
                            KeyboardType.NumberPassword else KeyboardType.Password,
                        isError = error != null,
                        supportingText = error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    if (biometricAvailable && biometricFailed) {
                        Spacer(Modifier.height(14.dp))
                        LuxeSecondaryButton(
                            text = "Use biometric instead",
                            icon = Icons.Default.Fingerprint,
                            onClick = {
                                val activity = context as? FragmentActivity
                                    ?: return@LuxeSecondaryButton
                                BiometricHelper.prompt(
                                    activity = activity,
                                    title = "Remove app lock",
                                    subtitle = "Verify to continue",
                                    onSuccess = onConfirmDisable,
                                    onFailure = { error = "Biometric failed" }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LuxeSecondaryButton(
                            text = "Cancel",
                            icon = Icons.Default.Close,
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        LuxePrimaryButton(
                            text = "Remove lock",
                            icon = Icons.Default.LockOpen,
                            accent = StatusUnpaid,
                            enabled = input.isNotBlank(),
                            onClick = ::verifyAndDisable,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                }
            }
        }
    }
}