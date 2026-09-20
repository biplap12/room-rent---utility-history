package com.example.security

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun AppLockVerifyFlow(
    config: AppLockConfig,
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var biometricFailed by remember { mutableStateOf(false) }
    var shakeTrigger by remember { mutableIntStateOf(0) }

    val biometricAvailable = config.biometricEnabled && BiometricHelper.canUse(context)

    // Auto-focus input
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(160)
        runCatching { focusRequester.requestFocus() }
    }

    // ★ Auto-prompt biometric on entry if available
    LaunchedEffect(Unit) {
        if (biometricAvailable && !biometricFailed) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            BiometricHelper.prompt(
                activity = activity,
                title = title,
                subtitle = "Verify your identity to continue",
                onSuccess = onVerified,
                onFailure = { biometricFailed = true }
            )
        }
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            shake.animateTo(0f, keyframes {
                durationMillis = 400
                0f at 0; -10f at 60; 10f at 120
                -8f at 180; 8f at 240; -4f at 300; 0f at 400
            })
        }
    }

    fun verify() {
        if (AppLockPrefs.verify(input, config.credentialSalt, config.credentialHash)) {
            onVerified()
        } else {
            error = "Incorrect ${if (config.lockType == LockType.PIN) "PIN" else "password"}"
            input = ""
            shakeTrigger++
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
                accent = cs.primary,
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
                        icon = Icons.Default.VerifiedUser,
                        accent = cs.primary,
                        size = 72.dp,
                        iconSize = 34.dp,
                        corner = 22.dp
                    )

                    Spacer(Modifier.height(22.dp))

                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    )

                    Spacer(Modifier.height(26.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = shake.value.dp)
                    ) {
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
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            } else null,
                            visualTransformation =
                                if (config.lockType == LockType.PIN || !passwordVisible)
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
                    }

                    /* ── Biometric shortcut if it failed / was dismissed ── */
                    if (biometricAvailable && biometricFailed) {
                        Spacer(Modifier.height(16.dp))
                        LuxeSecondaryButton(
                            text = "Use biometric instead",
                            icon = Icons.Default.Fingerprint,
                            onClick = {
                                val activity = context as? FragmentActivity
                                    ?: return@LuxeSecondaryButton
                                BiometricHelper.prompt(
                                    activity = activity,
                                    title = title,
                                    subtitle = "Verify your identity",
                                    onSuccess = onVerified,
                                    onFailure = { error = "Biometric failed" }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(26.dp))

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
                            text = "Verify",
                            icon = Icons.Default.Check,
                            enabled = input.isNotBlank(),
                            onClick = ::verify,
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "You must verify before changing your credentials.",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}