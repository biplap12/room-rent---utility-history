package com.example.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.*

@Composable
fun AppLockScreen(
    config: AppLockConfig,
    onUnlock: () -> Unit,
    onForgot: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val surface = cs.surface

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var successPulse by remember { mutableStateOf(false) }
    var biometricAttempted by remember { mutableStateOf(false) }
    var biometricFailed by remember { mutableStateOf(false) }
    var shakeTrigger by remember { mutableIntStateOf(0) }

    val biometricEligible = config.biometricEnabled && BiometricHelper.canUse(context)
    val pinLength = config.credentialLength.coerceIn(4, 8)

    // Auto biometric prompt once
    LaunchedEffect(config.biometricEnabled, config.enabled) {
        if (biometricEligible && !biometricAttempted) {
            biometricAttempted = true
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            BiometricHelper.prompt(
                activity = activity,
                title = "Unlock Room Rent",
                subtitle = "Verify your identity",
                onSuccess = { onUnlock() },
                onFailure = { biometricFailed = true }
            )
        }
    }

    /* ★ Shake animation — fires on every wrong entry */
    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            shake.snapTo(0f)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 420
                    0f at 0
                    -14f at 60
                    14f at 120
                    -12f at 180
                    12f at 240
                    -8f at 300
                    8f at 360
                    0f at 420
                }
            )
        }
    }

    fun tryUnlock() {
        if (AppLockPrefs.verify(input, config.credentialSalt, config.credentialHash)) {
            successPulse = true
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onUnlock()
            }, 180L)
        } else {
            error = "Incorrect ${if (config.lockType == LockType.PIN) "PIN" else "password"}"
            input = ""
            shakeTrigger++          // ★ trigger the shake
        }
    }

    val topSolid    = lerp(surface, cs.primary, 0.10f)
    val middleSolid = lerp(surface, cs.primary, 0.02f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(topSolid, middleSolid, surface))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            val iconScale by animateFloatAsState(
                targetValue = if (successPulse) 1.12f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "icon_scale"
            )
            Box(modifier = Modifier.scale(iconScale)) {
                GradientIconBadge(
                    icon = if (successPulse) Icons.Default.LockOpen else Icons.Default.Lock,
                    accent = cs.primary,
                    size = 108.dp,
                    iconSize = 52.dp,
                    corner = 32.dp,
                    topAmount = 0.30f,
                    bottomAmount = 0.12f,
                    ringAmount = 0.24f
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Welcome back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface,
                fontSize = 34.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                if (config.lockType == LockType.PIN)
                    "Enter your $pinLength-digit PIN to unlock"
                else
                    "Enter your password to unlock",
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(44.dp))

            /* ★ Everything below shakes as one block when wrong */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = shake.value.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (config.lockType == LockType.PIN) {
                        PinDots(
                            input = input,
                            error = error,
                            length = pinLength,
                            success = successPulse
                        )

                        if (error != null) {
                            Spacer(Modifier.height(20.dp))
                            ErrorBanner(error!!)
                        }

                        Spacer(Modifier.height(36.dp))

                        PinKeypad(
                            onDigit = { d ->
                                if (input.length < pinLength) {
                                    /* ★ Clear error the moment user starts typing again */
                                    if (error != null) error = null
                                    input += d
                                    if (input.length == pinLength) tryUnlock()
                                }
                            },
                            onBackspace = {
                                if (input.isNotEmpty()) input = input.dropLast(1)
                                if (error != null) error = null
                            }
                        )
                    } else {
                        LuxeTextField(
                            value = input,
                            onValueChange = {
                                /* ★ Clear error as soon as the user types */
                                if (error != null) error = null
                                input = it.take(64)
                            },
                            label = "Password",
                            placeholder = "Enter your password",
                            leadingIcon = Icons.Default.Password,
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide" else "Show",
                                        tint = cs.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardType = KeyboardType.Password,
                            isError = error != null,
                            supportingText = error,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))

                        LuxePrimaryButton(
                            text = "Unlock",
                            icon = Icons.Default.LockOpen,
                            onClick = ::tryUnlock,
                            enabled = input.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    /* Biometric shortcut */
                    if (biometricEligible && (biometricFailed || !biometricAttempted)) {
                        Spacer(Modifier.height(24.dp))
                        LuxeSecondaryButton(
                            text = "Use biometric",
                            icon = Icons.Default.Fingerprint,
                            onClick = {
                                val activity = context as? FragmentActivity
                                    ?: return@LuxeSecondaryButton
                                BiometricHelper.prompt(
                                    activity = activity,
                                    title = "Unlock Room Rent",
                                    subtitle = "Verify your identity",
                                    onSuccess = { onUnlock() },
                                    onFailure = { error = "Biometric failed" }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    /* ★ Forgot link now sits right under the input block,
                       not pinned to the bottom of the screen. */
                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = onForgot) {
                        Icon(
                            Icons.Default.HelpOutline, null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Forgot ${if (config.lockType == LockType.PIN) "PIN" else "password"}?",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1.3f))
        }
    }
}

/* ── Animated PIN dots — turn red when error, back to primary when typing clears ── */
@Composable
private fun PinDots(
    input: String,
    error: String?,
    length: Int,
    success: Boolean
) {
    val cs = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { i ->
            val filled = i < input.length
            val scale by animateFloatAsState(
                targetValue = if (filled) 1f else 0.55f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "dot_$i"
            )
            /* ★ Color logic:
               - success: primary
               - error and the dot is empty (fresh attempt): error red
               - filled: primary
               - empty idle: outline */
            val color = when {
                success -> cs.primary
                error != null && !filled -> cs.error
                filled -> cs.primary
                else -> cs.outline.copy(alpha = 0.35f)
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/* ── PIN keypad ── */
@Composable
private fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(88.dp))
                    } else {
                        KeypadButton(key) {
                            if (key == "⌫") onBackspace() else onDigit(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val surface = cs.surface
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val bg by animateColorAsState(
        targetValue = if (pressed) lerp(surface, cs.primary, 0.18f)
        else lerp(surface, cs.outline, 0.06f),
        label = "keypad_bg"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "keypad_scale"
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(
                Icons.Default.Backspace, null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Text(
                label,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
                color = cs.onSurface
            )
        }
    }
}

/* ── Error banner ── */
@Composable
private fun ErrorBanner(message: String) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = cs.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ErrorOutline, null,
                tint = cs.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}