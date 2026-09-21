package com.example.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.GradientIconBadge

import com.example.ui.theme.LuxePrimaryButton
import com.example.ui.theme.LuxeTextField
import kotlinx.coroutines.launch

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

    var biometricAttempted by remember {
        mutableStateOf(false)
    }

    var biometricFailed by remember {
        mutableStateOf(false)
    }

    var shakeTrigger by remember {
        mutableIntStateOf(0)
    }

    var fingerprintAnimationTrigger by remember {
        mutableIntStateOf(0)
    }

    val biometricEligible =
        config.biometricEnabled &&
                BiometricHelper.canUse(context)

    val pinLength =
        config.credentialLength.coerceIn(4, 8)

    /*
     * =========================================================
     * AUTO BIOMETRIC PROMPT
     * =========================================================
     */
    LaunchedEffect(
        config.biometricEnabled,
        config.enabled
    ) {
        if (
            biometricEligible &&
            !biometricAttempted
        ) {
            biometricAttempted = true

            val activity =
                context as? FragmentActivity
                    ?: return@LaunchedEffect

            BiometricHelper.prompt(
                activity = activity,
                title = "Unlock Room Rent",
                subtitle = "Verify your identity",
                onSuccess = {
                    onUnlock()
                },
                onFailure = {
                    biometricFailed = true
                }
            )
        }
    }

    /*
     * =========================================================
     * SHAKE ANIMATION
     * =========================================================
     */
    val shake = remember {
        Animatable(0f)
    }

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

    /*
     * =========================================================
     * FINGERPRINT ANIMATION
     * =========================================================
     *
     * Wrong password triggers:
     * - Scale pulse
     * - Small left/right rotation
     */
    val fingerprintScale = remember {
        Animatable(1f)
    }

    val fingerprintRotation = remember {
        Animatable(0f)
    }

    LaunchedEffect(
        fingerprintAnimationTrigger
    ) {
        if (fingerprintAnimationTrigger > 0) {

            fingerprintScale.snapTo(1f)
            fingerprintRotation.snapTo(0f)

            kotlinx.coroutines.coroutineScope {

                launch {
                    fingerprintScale.animateTo(
                        targetValue = 1f,
                        animationSpec = keyframes {

                            durationMillis = 520

                            1f at 0
                            1.25f at 130
                            0.92f at 260
                            1.15f at 390
                            1f at 520
                        }
                    )
                }

                launch {
                    fingerprintRotation.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {

                            durationMillis = 520

                            0f at 0
                            -10f at 100
                            10f at 200
                            -7f at 300
                            7f at 400
                            0f at 520
                        }
                    )
                }
            }
        }
    }

    /*
     * =========================================================
     * OPEN BIOMETRIC PROMPT
     * =========================================================
     */
    fun openBiometric() {

        val activity =
            context as? FragmentActivity
                ?: return

        biometricFailed = false

        BiometricHelper.prompt(
            activity = activity,
            title = "Unlock Room Rent",
            subtitle = "Verify your identity",
            onSuccess = {
                onUnlock()
            },
            onFailure = {
                biometricFailed = true
            }
        )
    }

    /*
     * =========================================================
     * TRY UNLOCK
     * =========================================================
     */
    fun tryUnlock() {

        val valid = AppLockPrefs.verify(
            input,
            config.credentialSalt,
            config.credentialHash
        )

        if (valid) {

            error = null
            successPulse = true

            android.os.Handler(
                android.os.Looper.getMainLooper()
            ).postDelayed(
                {
                    onUnlock()
                },
                180L
            )

        } else {

            /*
             * Wrong password / PIN
             */
            error =
                "Incorrect ${
                    if (
                        config.lockType ==
                        LockType.PIN
                    ) {
                        "PIN"
                    } else {
                        "password"
                    }
                }"

            /*
             * Reset input
             */
            input = ""

            /*
             * Shake input area
             */
            shakeTrigger++

            /*
             * Animate fingerprint
             */
            fingerprintAnimationTrigger++
        }
    }

    val topSolid =
        lerp(
            surface,
            cs.primary,
            0.10f
        )

    val middleSolid =
        lerp(
            surface,
            cs.primary,
            0.02f
        )

    /*
     * =========================================================
     * MAIN SCREEN
     * =========================================================
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        topSolid,
                        middleSolid,
                        surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(
                    top = 32.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                Modifier.weight(1f)
            )

            /*
             * =================================================
             * LOCK ICON
             * =================================================
             */
            val iconScale by animateFloatAsState(
                targetValue =
                    if (successPulse) {
                        1.12f
                    } else {
                        1f
                    },

                animationSpec = spring(
                    dampingRatio =
                        Spring.DampingRatioMediumBouncy,

                    stiffness =
                        Spring.StiffnessMedium
                ),

                label = "lock_icon_scale"
            )

            Box(
                modifier =
                    Modifier.scale(iconScale)
            ) {

                GradientIconBadge(
                    icon =
                        if (successPulse) {
                            Icons.Default.LockOpen
                        } else {
                            Icons.Default.Lock
                        },

                    accent =
                        cs.primary,

                    size =
                        108.dp,

                    iconSize =
                        52.dp,

                    corner =
                        32.dp,

                    topAmount =
                        0.30f,

                    bottomAmount =
                        0.12f,

                    ringAmount =
                        0.24f
                )
            }

            Spacer(
                Modifier.height(32.dp)
            )

            /*
             * =================================================
             * WELCOME TEXT
             * =================================================
             */
            Text(
                text = "Welcome back",

                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    cs.onSurface,

                fontSize =
                    34.sp
            )

            Spacer(
                Modifier.height(12.dp)
            )

            Text(
                text =
                    if (
                        config.lockType ==
                        LockType.PIN
                    ) {
                        "Enter your $pinLength-digit PIN to unlock"
                    } else {
                        "Enter your password to unlock"
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    cs.onSurfaceVariant,

                textAlign =
                    TextAlign.Center,

                fontSize =
                    16.sp
            )

            Spacer(
                Modifier.height(44.dp)
            )

            /*
             * =================================================
             * PIN MODE
             * =================================================
             */
            if (
                config.lockType ==
                LockType.PIN
            ) {

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    /*
                     * PIN DOTS
                     *
                     * No incorrect PIN message is shown.
                     */
                    Box(
                        modifier =
                            Modifier.offset(
                                x = shake.value.dp
                            )
                    ) {

                        PinDots(
                            input = input,
                            error = error,
                            length = pinLength,
                            success = successPulse
                        )
                    }

                    Spacer(
                        Modifier.height(36.dp)
                    )

                    /*
                     * PIN KEYPAD
                     */
                    PinKeypad(
                        onDigit = { digit ->

                            if (
                                input.length <
                                pinLength
                            ) {

                                /*
                                 * Clear error as soon
                                 * as user starts typing.
                                 */
                                if (error != null) {
                                    error = null
                                }

                                input += digit

                                /*
                                 * Automatically verify
                                 * after final digit.
                                 */
                                if (
                                    input.length ==
                                    pinLength
                                ) {
                                    tryUnlock()
                                }
                            }
                        },

                        onBackspace = {

                            if (
                                input.isNotEmpty()
                            ) {
                                input =
                                    input.dropLast(1)
                            }

                            if (error != null) {
                                error = null
                            }
                        },

                        onFingerprint = {
                            openBiometric()
                        }
                    )
                }

            } else {

                /*
                 * =================================================
                 * PASSWORD MODE
                 * =================================================
                 */

                /*
                 * Password field shakes when
                 * password is incorrect.
                 */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(
                            x = shake.value.dp
                        )
                ) {

                    LuxeTextField(

                        value =
                            input,

                        onValueChange = { value ->

                            /*
                             * Clear error when typing
                             * a new password.
                             */
                            if (error != null) {
                                error = null
                            }

                            input =
                                value.take(64)
                        },

                        label =
                            "Password",

                        placeholder =
                            "Enter your password",

                        leadingIcon =
                            Icons.Default.Password,

                        trailingIcon = {

                            IconButton(
                                onClick = {
                                    passwordVisible =
                                        !passwordVisible
                                }
                            ) {

                                Icon(

                                    imageVector =
                                        if (
                                            passwordVisible
                                        ) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },

                                    contentDescription =
                                        if (
                                            passwordVisible
                                        ) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        },

                                    tint =
                                        cs.onSurfaceVariant,

                                    modifier =
                                        Modifier.size(22.dp)
                                )
                            }
                        },

                        visualTransformation =
                            if (
                                passwordVisible
                            ) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },

                        keyboardType =
                            KeyboardType.Password,

                        isError =
                            error != null,

                        supportingText =
                            error,

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }

                Spacer(
                    Modifier.height(24.dp)
                )

                /*
                 * UNLOCK BUTTON
                 */
                LuxePrimaryButton(
                    text =
                        "Unlock",

                    icon =
                        Icons.Default.LockOpen,

                    onClick =
                        ::tryUnlock,

                    enabled =
                        input.isNotBlank(),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                /*
                 * =================================================
                 * PASSWORD MODE BIOMETRIC
                 * ICON ONLY
                 * =================================================
                 */
                if (biometricEligible) {

                    Spacer(
                        Modifier.height(28.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                lerp(
                                    surface,
                                    cs.primary,
                                    0.08f
                                )
                            )
                            .clickable(
                                onClick = {
                                    openBiometric()
                                }
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Fingerprint,

                            contentDescription =
                                "Biometric unlock",

                            tint =
                                cs.primary,

                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .scale(
                                        fingerprintScale.value
                                    )
                                    .rotate(
                                        fingerprintRotation.value
                                    )
                        )
                    }
                }
            }

            /*
             * =================================================
             * FORGOT BUTTON
             * =================================================
             */
            Spacer(
                Modifier.height(16.dp)
            )

            TextButton(
                onClick =
                    onForgot
            ) {

                Icon(
                    imageVector =
                        Icons.Default.HelpOutline,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(18.dp)
                )

                Spacer(
                    Modifier.width(8.dp)
                )

                Text(
                    text =
                        "Forgot ${
                            if (
                                config.lockType ==
                                LockType.PIN
                            ) {
                                "PIN"
                            } else {
                                "password"
                            }
                        }?",

                    fontWeight =
                        FontWeight.SemiBold,

                    fontSize =
                        15.sp
                )
            }

            Spacer(
                Modifier.weight(1.3f)
            )
        }
    }
}

/*
 * =============================================================
 * PIN DOTS
 * =============================================================
 */
@Composable
private fun PinDots(
    input: String,
    error: String?,
    length: Int,
    success: Boolean
) {

    val cs =
        MaterialTheme.colorScheme

    Row(
        horizontalArrangement =
            Arrangement.spacedBy(20.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        repeat(length) { index ->

            val filled =
                index < input.length

            val scale by
            animateFloatAsState(

                targetValue =
                    if (filled) {
                        1f
                    } else {
                        0.55f
                    },

                animationSpec =
                    spring(

                        dampingRatio =
                            Spring.DampingRatioMediumBouncy,

                        stiffness =
                            Spring.StiffnessMedium
                    ),

                label =
                    "pin_dot_$index"
            )

            val color =
                when {

                    success ->
                        cs.primary

                    error != null &&
                            !filled ->
                        cs.error

                    filled ->
                        cs.primary

                    else ->
                        cs.outline.copy(
                            alpha = 0.35f
                        )
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

/*
 * =============================================================
 * PIN KEYPAD
 * =============================================================
 *
 * 1   2   3
 * 4   5   6
 * 7   8   9
 * ⌫   0   fingerprint
 *
 * =============================================================
 */
@Composable
private fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onFingerprint: () -> Unit
) {

    val rows =
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(16.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        /*
         * 1 2 3
         * 4 5 6
         * 7 8 9
         */
        rows.forEach { row ->

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                row.forEach { key ->

                    AppKeypadButton(
                        label = key,

                        onClick = {
                            onDigit(key)
                        }
                    )
                }
            }
        }

        /*
         * Backspace | 0 | Fingerprint
         */
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            /*
             * Backspace
             */
            AppKeypadButton(
                icon =
                    Icons.Default.Backspace,

                iconSize =
                    32.dp,

                contentDescription =
                    "Backspace",

                onClick =
                    onBackspace
            )

            /*
             * Zero
             */
            AppKeypadButton(
                label =
                    "0",

                onClick = {
                    onDigit("0")
                }
            )

            /*
             * Fingerprint
             *
             * Larger icon.
             */
            AppKeypadButton(
                icon =
                    Icons.Default.Fingerprint,

                iconSize =
                    44.dp,

                iconTint =
                    MaterialTheme
                        .colorScheme
                        .primary,

                contentDescription =
                    "Biometric unlock",

                onClick =
                    onFingerprint
            )
        }
    }
}

/*
 * =============================================================
 * KEYPAD BUTTON
 * =============================================================
 */
@Composable
private fun AppKeypadButton(
    label: String = "",
    icon: ImageVector? = null,
    iconSize: Dp = 32.dp,
    iconTint: androidx.compose.ui.graphics.Color? = null,
    contentDescription: String? = null,
    onClick: () -> Unit
) {

    val cs =
        MaterialTheme.colorScheme

    val surface =
        cs.surface

    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interaction.collectIsPressedAsState()

    val backgroundColor by
    animateColorAsState(

        targetValue =
            if (pressed) {

                lerp(
                    surface,
                    cs.primary,
                    0.18f
                )

            } else {

                lerp(
                    surface,
                    cs.outline,
                    0.06f
                )
            },

        label =
            "keypad_background"
    )

    val buttonScale by
    animateFloatAsState(

        targetValue =
            if (pressed) {
                0.92f
            } else {
                1f
            },

        animationSpec =
            spring(
                stiffness =
                    Spring.StiffnessMediumLow
            ),

        label =
            "keypad_button_scale"
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource =
                    interaction,

                indication =
                    null,

                onClick =
                    onClick
            ),

        contentAlignment =
            Alignment.Center
    ) {

        if (icon != null) {

            Icon(
                imageVector =
                    icon,

                contentDescription =
                    contentDescription,

                tint =
                    iconTint
                        ?: cs.onSurfaceVariant,

                modifier =
                    Modifier.size(iconSize)
            )

        } else {

            Text(
                text =
                    label,

                fontSize =
                    34.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    cs.onSurface
            )
        }
    }
}