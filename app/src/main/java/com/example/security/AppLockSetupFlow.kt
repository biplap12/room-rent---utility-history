//package com.example.security
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import com.example.ui.theme.*
//
///* Security questions the user picks from */
//private val QUESTION_POOL = listOf(
//    "What is your mother's maiden name?",
//    "What was your first pet's name?",
//    "What city were you born in?",
//    "What is your favourite teacher's name?",
//    "What was your first phone model?",
//    "What is your best friend's nickname?",
//    "What was the name of your first school?",
//    "What is your favourite book?",
//    "What was your childhood nickname?",
//    "What was your favourite childhood game?",
//    "What was your favourite cartoon as a child?",
//    "What was your favourite school subject?",
//    "What was your favourite school activity?",
//    "What was the name of your childhood friend?",
//    "What was your favourite childhood food?",
//    "What was your favourite fruit as a child?",
//    "What was your favourite colour as a child?",
//    "What was your favourite movie when you were younger?",
//    "What was your favourite TV show as a child?",
//    "What was your favourite sport?",
//    "What was your favourite musical instrument?",
//    "What was your favourite holiday destination?",
//    "What was the first book you remember reading?",
//    "What was the first movie you watched in a cinema?",
//    "What was your first username?",
//    "What was your first email provider?",
//    "What was the nickname of your childhood best friend?",
//    "What was your favourite subject in school?",
//    "What was your favourite school event?",
//    "What was the name of your favourite teacher?",
//    "What was your favourite place to visit as a child?",
//    "What was your favourite ice cream flavour?",
//    "What was your favourite snack growing up?",
//    "What was your favourite festival?",
//    "What was your favourite song when you were younger?",
//    "What was the first game you played on a computer?",
//    "What was the first game you played on a phone?",
//    "What was your favourite board game?",
//    "What was your favourite fictional character?",
//    "What was your favourite superhero?",
//    "What was your favourite animal?",
//    "What was your favourite outdoor activity?",
//    "What was your favourite indoor activity?",
//    "What was your favourite school memory?",
//    "What was your favourite place at school?",
//    "What was the first app you remember using?",
//    "What was your favourite website as a child?",
//    "What was the first device you used to access the internet?",
//    "What was your favourite ice cream shop?",
//    "What was your favourite restaurant growing up?"
//)
//
//
///**
// * First-time app lock setup wizard.
//
// *   Step 0 — Credentials  (PIN or password)
// *   Step 1 — Questions    (three fresh recovery questions)
// *   Step 2 — Biometric    (enable / disable + final save)
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AppLockSetupFlow(
//    existingConfig: AppLockConfig,
//    onDismiss: () -> Unit,
//    onComplete: (AppLockConfig) -> Unit,
//    isVerified: Boolean = false
//) {
//    val context = LocalContext.current
//    val cs = MaterialTheme.colorScheme
//    val isFirstSetup = !existingConfig.setupCompleted
//
//    val blocked = !isFirstSetup && !isVerified
//
//    LaunchedEffect(blocked) {
//        if (blocked) {
//            android.util.Log.w(
//                "AppLockSetupFlow",
//                "Blocked: existing credential cannot be changed without verification."
//            )
//            onDismiss()
//        }
//    }
//
//    if (blocked) return
//
//    /* ── Wizard state ── */
//    var step by remember { mutableIntStateOf(0) }
//    val totalSteps = 3
//
//    // Step 0 — credentials
//    var lockType by remember { mutableStateOf(LockType.PIN) }
//    var credential by remember { mutableStateOf("") }
//    var confirmCredential by remember { mutableStateOf("") }
//    var credentialVisible by remember { mutableStateOf(false) }
//
//    // Step 1 — security questions
//    var q1 by remember { mutableStateOf(QUESTION_POOL[0]) }
//    var q2 by remember { mutableStateOf(QUESTION_POOL[1]) }
//    var q3 by remember { mutableStateOf(QUESTION_POOL[2]) }
//    var a1 by remember { mutableStateOf("") }
//    var a2 by remember { mutableStateOf("") }
//    var a3 by remember { mutableStateOf("") }
//
//    // Step 2 — biometric
//    var biometricEnabled by remember { mutableStateOf(false) }
//
//    var error by remember { mutableStateOf<String?>(null) }
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 24.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            BeautifulCard(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.92f),
//                shape = RoundedCornerShape(LuxeTokens.DialogRadius),
//                elevation = 16.dp
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(22.dp)
//                ) {
//                    /* ── Header ── */
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        GradientIconBadge(
//                            icon = when (step) {
//                                0 -> Icons.Default.Security
//                                1 -> Icons.Default.HelpOutline
//                                else -> Icons.Default.Fingerprint
//                            },
//                            accent = cs.primary,
//                            size = 46.dp,
//                            iconSize = 22.dp,
//                            corner = 14.dp
//                        )
//                        Spacer(Modifier.width(14.dp))
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                when (step) {
//                                    0 -> "Set up app lock"
//                                    1 -> "Recovery questions"
//                                    else -> "Biometric unlock"
//                                },
//                                style = MaterialTheme.typography.titleLarge,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Text(
//                                "Step ${step + 1} of $totalSteps  ·  " +
//                                        when (step) {
//                                            0 -> "Credentials"
//                                            1 -> "Recovery"
//                                            else -> "Biometric"
//                                        },
//                                style = MaterialTheme.typography.labelSmall,
//                                color = cs.onSurfaceVariant
//                            )
//                        }
//                        IconButton(onClick = onDismiss) {
//                            Icon(Icons.Default.Close, "Close")
//                        }
//                    }
//
//                    Spacer(Modifier.height(10.dp))
//                    AccentProgressBar(
//                        fraction = (step + 1) / totalSteps.toFloat(),
//                        accent = cs.primary
//                    )
//
//                    Spacer(Modifier.height(20.dp))
//
//                    /* ── Body ── */
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .verticalScroll(rememberScrollState())
//                    ) {
//                        when (step) {
//
//                            /* ═════════════════════════════════════
//                               STEP 0 — CREDENTIALS
//                               ═════════════════════════════════════ */
//                            0 -> {
//                                LuxeSectionLabel(
//                                    text = "Credentials",
//                                    caption = "Choose how you want to protect the app"
//                                )
//
//                                Spacer(Modifier.height(16.dp))
//                                SingleChoiceSegmentedButtonRow(
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    SegmentedButton(
//                                        selected = lockType == LockType.PIN,
//                                        onClick = {
//                                            lockType = LockType.PIN
//                                            credential = ""
//                                            confirmCredential = ""
//                                            error = null
//                                        },
//                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
//                                        icon = {
//                                            Icon(
//                                                Icons.Default.Lock,
//                                                contentDescription = null,
//                                                modifier = Modifier.size(16.dp)
//                                            )
//                                        },
//                                        label = { Text("PIN") }
//                                    )
//                                    SegmentedButton(
//                                        selected = lockType == LockType.PASSWORD,
//                                        onClick = {
//                                            lockType = LockType.PASSWORD
//                                            credential = ""
//                                            confirmCredential = ""
//                                            error = null
//                                        },
//                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
//                                        icon = {
//                                            Icon(
//                                                Icons.Default.Password,
//                                                contentDescription = null,
//                                                modifier = Modifier.size(16.dp)
//                                            )
//                                        },
//                                        label = { Text("Password") }
//                                    )
//                                }
//
//                                Spacer(Modifier.height(18.dp))
//
//                                LuxeTextField(
//                                    value = credential,
//                                    onValueChange = {
//                                        error = null
//                                        credential = if (lockType == LockType.PIN)
//                                            it.filter { c -> c.isDigit() }.take(8)
//                                        else it.take(64)
//                                    },
//                                    label = if (lockType == LockType.PIN) "PIN" else "Password",
//                                    placeholder = if (lockType == LockType.PIN)
//                                        "4–8 digits" else "At least 6 characters",
//                                    leadingIcon = Icons.Default.Lock,
//                                    trailingIcon = {
//                                        IconButton(onClick = {
//                                            credentialVisible = !credentialVisible
//                                        }) {
//                                            Icon(
//                                                if (credentialVisible)
//                                                    Icons.Default.VisibilityOff
//                                                else Icons.Default.Visibility,
//                                                null,
//                                                modifier = Modifier.size(20.dp)
//                                            )
//                                        }
//                                    },
//                                    visualTransformation =
//                                        if (credentialVisible) VisualTransformation.None
//                                        else PasswordVisualTransformation(),
//                                    keyboardType = if (lockType == LockType.PIN)
//                                        KeyboardType.NumberPassword else KeyboardType.Password,
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//
//                                Spacer(Modifier.height(10.dp))
//
//                                LuxeTextField(
//                                    value = confirmCredential,
//                                    onValueChange = {
//                                        error = null
//                                        confirmCredential = it.take(64)
//                                    },
//                                    label = "Confirm",
//                                    leadingIcon = Icons.Default.CheckCircle,
//                                    trailingIcon = {
//                                        IconButton(onClick = {
//                                            credentialVisible = !credentialVisible
//                                        }) {
//                                            Icon(
//                                                if (credentialVisible)
//                                                    Icons.Default.VisibilityOff
//                                                else Icons.Default.Visibility,
//                                                null,
//                                                modifier = Modifier.size(20.dp)
//                                            )
//                                        }
//                                    },
//                                    visualTransformation =
//                                        if (credentialVisible) VisualTransformation.None
//                                        else PasswordVisualTransformation(),
//                                    keyboardType = if (lockType == LockType.PIN)
//                                        KeyboardType.NumberPassword else KeyboardType.Password,
//                                    isError = error != null,
//                                    supportingText = error,
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//                            }
//
//                            /* ═════════════════════════════════════
//                               STEP 1 — QUESTIONS
//                               ═════════════════════════════════════ */
//                            1 -> {
//                                LuxeSectionLabel(
//                                    text = "Recovery questions",
//                                    caption = "Answer all three — we'll ask these if you forget your PIN"
//                                )
//
//                                Spacer(Modifier.height(16.dp))
//
//                                QASlot(
//                                    label = "Question 1",
//                                    question = q1,
//                                    answer = a1,
//                                    onQuestionChange = { q1 = it },
//                                    onAnswerChange = { a1 = it; error = null }
//                                )
//                                Spacer(Modifier.height(16.dp))
//                                QASlot(
//                                    label = "Question 2",
//                                    question = q2,
//                                    answer = a2,
//                                    onQuestionChange = { q2 = it },
//                                    onAnswerChange = { a2 = it; error = null }
//                                )
//                                Spacer(Modifier.height(16.dp))
//                                QASlot(
//                                    label = "Question 3",
//                                    question = q3,
//                                    answer = a3,
//                                    onQuestionChange = { q3 = it },
//                                    onAnswerChange = { a3 = it; error = null }
//                                )
//
//                                if (error != null) {
//                                    Spacer(Modifier.height(12.dp))
//                                    Text(
//                                        error!!,
//                                        color = cs.error,
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                }
//                            }
//
//                            /* ═════════════════════════════════════
//                               STEP 2 — BIOMETRIC
//                               ═════════════════════════════════════ */
//                            else -> {
//                                LuxeSectionLabel(
//                                    text = "Biometric unlock",
//                                    caption = "Quickly unlock the app with your fingerprint or face"
//                                )
//
//                                Spacer(Modifier.height(16.dp))
//
//                                BeautifulCard(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    accent = cs.primary,
//                                    shape = RoundedCornerShape(LuxeTokens.CardRadius)
//                                ) {
//                                    LuxeToggleRow(
//                                        icon = Icons.Default.Fingerprint,
//                                        title = "Enable biometric unlock",
//                                        subtitle = if (BiometricHelper.canUse(context))
//                                            "Available on this device"
//                                        else "Not available on this device",
//                                        checked = biometricEnabled &&
//                                                BiometricHelper.canUse(context),
//                                        onCheckedChange = { biometricEnabled = it },
//                                        modifier = Modifier.padding(12.dp)
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    Spacer(Modifier.height(14.dp))
//
//                    /* ── Actions ── */
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(10.dp)
//                    ) {
//                        LuxeSecondaryButton(
//                            text = if (step == 0) "Cancel" else "Back",
//                            icon = if (step == 0) Icons.Default.Close
//                            else Icons.Default.ArrowBack,
//                            onClick = {
//                                error = null
//                                if (step == 0) onDismiss() else step--
//                            },
//                            modifier = Modifier.weight(1f)
//                        )
//
//                        LuxePrimaryButton(
//                            text = if (step == 2) "Enable app lock" else "Continue",
//                            icon = if (step == 2) Icons.Default.Check
//                            else Icons.Default.ArrowForward,
//                            enabled = when (step) {
//                                0 -> credential.isNotBlank()
//                                else -> true
//                            },
//                            onClick = {
//                                error = null
//                                when (step) {
//
//                                    /* ── STEP 0 → validate credentials ── */
//                                    0 -> {
//                                        val v = validateCredential(
//                                            lockType, credential, confirmCredential
//                                        )
//                                        if (v != null) {
//                                            error = v
//                                            return@LuxePrimaryButton
//                                        }
//                                        step = 1
//                                    }
//
//                                    /* ── STEP 1 → validate questions ── */
//                                    1 -> {
//                                        if (a1.isBlank() || a2.isBlank() || a3.isBlank()) {
//                                            error = "All answers are required"
//                                            return@LuxePrimaryButton
//                                        }
//                                        if (q1 == q2 || q2 == q3 || q1 == q3) {
//                                            error = "Questions must be different"
//                                            return@LuxePrimaryButton
//                                        }
//                                        step = 2
//                                    }
//
//                                    /* ── STEP 2 → save everything ── */
//                                    else -> {
//                                        val credSalt = AppLockPrefs.newSalt()
//                                        val s1 = AppLockPrefs.newSalt()
//                                        val s2 = AppLockPrefs.newSalt()
//                                        val s3 = AppLockPrefs.newSalt()
//
//                                        onComplete(
//                                            AppLockConfig(
//                                                enabled = true,
//                                                setupCompleted = true,
//                                                lockType = lockType,
//                                                credentialHash =
//                                                    AppLockPrefs.hash(credential, credSalt),
//                                                credentialSalt = credSalt,
//                                                credentialLength =
//                                                    if (lockType == LockType.PIN)
//                                                        credential.length
//                                                    else 0,
//                                                userName = existingConfig.userName,
//                                                userEmail = existingConfig.userEmail,
//                                                q1 = q1,
//                                                a1Hash = AppLockPrefs.hash(a1, s1),
//                                                a1Salt = s1,
//                                                q2 = q2,
//                                                a2Hash = AppLockPrefs.hash(a2, s2),
//                                                a2Salt = s2,
//                                                q3 = q3,
//                                                a3Hash = AppLockPrefs.hash(a3, s3),
//                                                a3Salt = s3,
//                                                biometricEnabled = biometricEnabled
//                                            )
//                                        )
//                                    }
//                                }
//                            },
//                            modifier = Modifier.weight(1.4f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
///* ═══════════════════════════════════════════════════════════════
//   Small helper: one question + answer pair
//   ═══════════════════════════════════════════════════════════════ */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun QASlot(
//    label: String,
//    question: String,
//    answer: String,
//    onQuestionChange: (String) -> Unit,
//    onAnswerChange: (String) -> Unit
//) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        LuxeDropdown(
//            label = label,
//            selected = question,
//            options = QUESTION_POOL,
//            optionLabel = { it },
//            onSelect = onQuestionChange,
//            leadingIcon = Icons.Default.HelpOutline
//        )
//        Spacer(Modifier.height(8.dp))
//        LuxeTextField(
//            value = answer,
//            onValueChange = onAnswerChange,
//            label = "Your answer",
//            leadingIcon = Icons.Default.Edit,
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//}
//
///* ═══════════════════════════════════════════════════════════════
//   Validation helper
//   ═══════════════════════════════════════════════════════════════ */
//private fun validateCredential(
//    type: LockType,
//    credential: String,
//    confirm: String
//): String? {
//    if (credential.isBlank()) return "Required"
//    if (type == LockType.PIN && credential.length < 4) return "PIN must be 4–8 digits"
//    if (type == LockType.PIN && credential.length > 8) return "PIN must be 4–8 digits"
//    if (type == LockType.PASSWORD && credential.length < 6)
//        return "Password must be at least 6 characters"
//    if (credential != confirm) return "Doesn't match"
//    return null
//}



package com.example.security

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/* Security questions the user picks from */
private val QUESTION_POOL = listOf(
    "Mother's maiden name",
    "First pet's name",
    "City you were born in",
    "Favourite teacher's name",
    "First phone model",
    "Best friend's nickname",
    "Name of your first school",
    "Favourite book"
)

/**
 * First-time app lock setup wizard.

 *   Step 0 — Your details  (name + email)
 *   Step 1 — Credentials   (PIN or password)
 *   Step 2 — Questions     (three recovery questions)
 *   Step 3 — Biometric     (enable / disable + final save)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSetupFlow(
    existingConfig: AppLockConfig,
    onDismiss: () -> Unit,
    onComplete: (AppLockConfig) -> Unit,
    isVerified: Boolean = false
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val isFirstSetup = !existingConfig.setupCompleted

    val blocked = !isFirstSetup && !isVerified

    LaunchedEffect(blocked) {
        if (blocked) {
            android.util.Log.w(
                "AppLockSetupFlow",
                "Blocked: existing credential cannot be changed without verification."
            )
            onDismiss()
        }
    }

    if (blocked) return

    /* ── Wizard state ── */
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Step 0 — profile details
    var userName by remember { mutableStateOf(existingConfig.userName) }
    var userEmail by remember { mutableStateOf(existingConfig.userEmail) }

    // Step 1 — credentials
    var lockType by remember { mutableStateOf(LockType.PIN) }
    var credential by remember { mutableStateOf("") }
    var confirmCredential by remember { mutableStateOf("") }
    var credentialVisible by remember { mutableStateOf(false) }

    // Step 2 — security questions
    var q1 by remember { mutableStateOf(QUESTION_POOL[0]) }
    var q2 by remember { mutableStateOf(QUESTION_POOL[1]) }
    var q3 by remember { mutableStateOf(QUESTION_POOL[2]) }
    var a1 by remember { mutableStateOf("") }
    var a2 by remember { mutableStateOf("") }
    var a3 by remember { mutableStateOf("") }

    // Step 3 — biometric
    var biometricEnabled by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(LuxeTokens.DialogRadius),
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    /* ── Header ── */
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GradientIconBadge(
                            icon = when (step) {
                                0 -> Icons.Default.Person
                                1 -> Icons.Default.Security
                                2 -> Icons.Default.HelpOutline
                                else -> Icons.Default.Fingerprint
                            },
                            accent = cs.primary,
                            size = 46.dp,
                            iconSize = 22.dp,
                            corner = 14.dp
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                when (step) {
                                    0 -> "Your details"
                                    1 -> "Set up app lock"
                                    2 -> "Recovery questions"
                                    else -> "Biometric unlock"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Step ${step + 1} of $totalSteps  ·  " +
                                        when (step) {
                                            0 -> "Profile"
                                            1 -> "Credentials"
                                            2 -> "Recovery"
                                            else -> "Biometric"
                                        },
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    AccentProgressBar(
                        fraction = (step + 1) / totalSteps.toFloat(),
                        accent = cs.primary
                    )

                    Spacer(Modifier.height(20.dp))

                    /* ── Body ── */
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (step) {

                            /* ═════════════════════════════════════
                               STEP 0 — YOUR DETAILS
                               ═════════════════════════════════════ */
                            0 -> {
                                LuxeSectionLabel(
                                    text = "Profile",
                                    caption = "Tell us a bit about you — stored only on this device"
                                )

                                Spacer(Modifier.height(16.dp))

                                LuxeTextField(
                                    value = userName,
                                    onValueChange = {
                                        error = null
                                        if (it.length <= 60) userName = it
                                    },
                                    label = "Full name",
                                    placeholder = "e.g. Ram Bahadur Thapa",
                                    leadingIcon = Icons.Default.Person,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                LuxeTextField(
                                    value = userEmail,
                                    onValueChange = {
                                        error = null
                                        if (it.length <= 120) userEmail = it
                                    },
                                    label = "Email",
                                    placeholder = "you@example.com",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    isError = error != null,
                                    supportingText = error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            /* ═════════════════════════════════════
                               STEP 1 — CREDENTIALS
                               ═════════════════════════════════════ */
                            1 -> {
                                LuxeSectionLabel(
                                    text = "Credentials",
                                    caption = "Choose how you want to protect the app"
                                )

                                Spacer(Modifier.height(16.dp))

                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SegmentedButton(
                                        selected = lockType == LockType.PIN,
                                        onClick = {
                                            lockType = LockType.PIN
                                            credential = ""
                                            confirmCredential = ""
                                            error = null
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = 0, count = 2
                                        ),
                                        icon = {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = { Text("PIN") }
                                    )
                                    SegmentedButton(
                                        selected = lockType == LockType.PASSWORD,
                                        onClick = {
                                            lockType = LockType.PASSWORD
                                            credential = ""
                                            confirmCredential = ""
                                            error = null
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = 1, count = 2
                                        ),
                                        icon = {
                                            Icon(
                                                Icons.Default.Password,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = { Text("Password") }
                                    )
                                }

                                Spacer(Modifier.height(18.dp))

                                LuxeTextField(
                                    value = credential,
                                    onValueChange = {
                                        error = null
                                        credential = if (lockType == LockType.PIN)
                                            it.filter { c -> c.isDigit() }.take(8)
                                        else it.take(64)
                                    },
                                    label = if (lockType == LockType.PIN) "PIN" else "Password",
                                    placeholder = if (lockType == LockType.PIN)
                                        "4–8 digits" else "At least 6 characters",
                                    leadingIcon = Icons.Default.Lock,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            credentialVisible = !credentialVisible
                                        }) {
                                            Icon(
                                                if (credentialVisible)
                                                    Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = if (credentialVisible) "Hide" else "Show",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    visualTransformation =
                                        if (credentialVisible) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    keyboardType = if (lockType == LockType.PIN)
                                        KeyboardType.NumberPassword else KeyboardType.Password,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                LuxeTextField(
                                    value = confirmCredential,
                                    onValueChange = {
                                        error = null
                                        confirmCredential = it.take(64)
                                    },
                                    label = "Confirm",
                                    leadingIcon = Icons.Default.CheckCircle,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            credentialVisible = !credentialVisible
                                        }) {
                                            Icon(
                                                if (credentialVisible)
                                                    Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = if (credentialVisible) "Hide" else "Show",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    visualTransformation =
                                        if (credentialVisible) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    keyboardType = if (lockType == LockType.PIN)
                                        KeyboardType.NumberPassword else KeyboardType.Password,
                                    isError = error != null,
                                    supportingText = error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            /* ═════════════════════════════════════
                               STEP 2 — QUESTIONS
                               ═════════════════════════════════════ */
                            2 -> {
                                LuxeSectionLabel(
                                    text = "Recovery questions",
                                    caption = "Answer all three — we'll ask these if you forget your PIN"
                                )

                                Spacer(Modifier.height(16.dp))

                                QASlot(
                                    label = "Question 1",
                                    question = q1,
                                    answer = a1,
                                    onQuestionChange = { q1 = it },
                                    onAnswerChange = { a1 = it; error = null }
                                )
                                Spacer(Modifier.height(16.dp))
                                QASlot(
                                    label = "Question 2",
                                    question = q2,
                                    answer = a2,
                                    onQuestionChange = { q2 = it },
                                    onAnswerChange = { a2 = it; error = null }
                                )
                                Spacer(Modifier.height(16.dp))
                                QASlot(
                                    label = "Question 3",
                                    question = q3,
                                    answer = a3,
                                    onQuestionChange = { q3 = it },
                                    onAnswerChange = { a3 = it; error = null }
                                )

                                if (error != null) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        error!!,
                                        color = cs.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            /* ═════════════════════════════════════
                               STEP 3 — BIOMETRIC
                               ═════════════════════════════════════ */
                            else -> {
                                LuxeSectionLabel(
                                    text = "Biometric unlock",
                                    caption = "Quickly unlock the app with your fingerprint or face"
                                )

                                Spacer(Modifier.height(16.dp))

                                BeautifulCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    accent = cs.primary,
                                    shape = RoundedCornerShape(LuxeTokens.CardRadius)
                                ) {
                                    LuxeToggleRow(
                                        icon = Icons.Default.Fingerprint,
                                        title = "Enable biometric unlock",
                                        subtitle = if (BiometricHelper.canUse(context))
                                            "Available on this device"
                                        else "Not available on this device",
                                        checked = biometricEnabled &&
                                                BiometricHelper.canUse(context),
                                        onCheckedChange = { biometricEnabled = it },
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    /* ── Actions ── */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LuxeSecondaryButton(
                            text = if (step == 0) "Cancel" else "Back",
                            icon = if (step == 0) Icons.Default.Close
                            else Icons.Default.ArrowBack,
                            onClick = {
                                error = null
                                if (step == 0) onDismiss() else step--
                            },
                            modifier = Modifier.weight(1f)
                        )

                        LuxePrimaryButton(
                            text = if (step == 3) "Enable app lock" else "Continue",
                            icon = if (step == 3) Icons.Default.Check
                            else Icons.Default.ArrowForward,
                            enabled = when (step) {
                                1 -> credential.isNotBlank()
                                else -> true
                            },
                            onClick = {
                                error = null
                                when (step) {

                                    /* ── STEP 0 → validate profile ── */
                                    0 -> {
                                        if (userName.isBlank()) {
                                            error = "Name required"
                                            return@LuxePrimaryButton
                                        }
                                        if (userName.trim().length < 2) {
                                            error = "Name must be at least 2 characters"
                                            return@LuxePrimaryButton
                                        }
                                        if (userEmail.isBlank()) {
                                            error = "Email required"
                                            return@LuxePrimaryButton
                                        }
                                        if (!userEmail.contains("@") ||
                                            !userEmail.contains(".")
                                        ) {
                                            error = "Enter a valid email"
                                            return@LuxePrimaryButton
                                        }
                                        step = 1
                                    }

                                    /* ── STEP 1 → validate credentials ── */
                                    1 -> {
                                        val v = validateCredential(
                                            lockType, credential, confirmCredential
                                        )
                                        if (v != null) {
                                            error = v
                                            return@LuxePrimaryButton
                                        }
                                        step = 2
                                    }

                                    /* ── STEP 2 → validate questions ── */
                                    2 -> {
                                        if (a1.isBlank() || a2.isBlank() || a3.isBlank()) {
                                            error = "All answers are required"
                                            return@LuxePrimaryButton
                                        }
                                        if (q1 == q2 || q2 == q3 || q1 == q3) {
                                            error = "Questions must be different"
                                            return@LuxePrimaryButton
                                        }
                                        step = 3
                                    }

                                    /* ── STEP 3 → save everything ── */
                                    else -> {
                                        val credSalt = AppLockPrefs.newSalt()
                                        val s1 = AppLockPrefs.newSalt()
                                        val s2 = AppLockPrefs.newSalt()
                                        val s3 = AppLockPrefs.newSalt()

                                        onComplete(
                                            AppLockConfig(
                                                enabled = true,
                                                setupCompleted = true,
                                                lockType = lockType,
                                                credentialHash =
                                                    AppLockPrefs.hash(credential, credSalt),
                                                credentialSalt = credSalt,
                                                credentialLength =
                                                    if (lockType == LockType.PIN)
                                                        credential.length
                                                    else 0,
                                                userName = userName.trim(),
                                                userEmail = userEmail.trim(),
                                                q1 = q1,
                                                a1Hash = AppLockPrefs.hash(a1, s1),
                                                a1Salt = s1,
                                                q2 = q2,
                                                a2Hash = AppLockPrefs.hash(a2, s2),
                                                a2Salt = s2,
                                                q3 = q3,
                                                a3Hash = AppLockPrefs.hash(a3, s3),
                                                a3Salt = s3,
                                                biometricEnabled = biometricEnabled
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Small helper: one question + answer pair
   ═══════════════════════════════════════════════════════════════ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QASlot(
    label: String,
    question: String,
    answer: String,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LuxeDropdown(
            label = label,
            selected = question,
            options = QUESTION_POOL,
            optionLabel = { it },
            onSelect = onQuestionChange,
            leadingIcon = Icons.Default.HelpOutline
        )
        Spacer(Modifier.height(8.dp))
        LuxeTextField(
            value = answer,
            onValueChange = onAnswerChange,
            label = "Your answer",
            leadingIcon = Icons.Default.Edit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   Validation helper
   ═══════════════════════════════════════════════════════════════ */
private fun validateCredential(
    type: LockType,
    credential: String,
    confirm: String
): String? {
    if (credential.isBlank()) return "Required"
    if (type == LockType.PIN && credential.length < 4) return "PIN must be 4–8 digits"
    if (type == LockType.PIN && credential.length > 8) return "PIN must be 4–8 digits"
    if (type == LockType.PASSWORD && credential.length < 6)
        return "Password must be at least 6 characters"
    if (credential != confirm) return "Doesn't match"
    return null
}