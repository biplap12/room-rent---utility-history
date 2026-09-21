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
    // Family & Childhood
    "What was your childhood nickname?",
    "What was your favorite childhood game?",
    "What was your favorite childhood toy?",
    "What was your favorite childhood snack?",
    "What was your favorite childhood cartoon?",
    "What was your favorite childhood movie?",
    "What was your favorite childhood story?",
    "What was your favorite childhood book?",
    "What was your favorite childhood song?",
    "What was your favorite childhood activity?",
    "What was the name of your childhood best friend?",
    "What was the name of your childhood neighbor?",
    "What was your favorite family tradition?",
    "What was your favorite family trip?",
    "What was your favorite family meal?",
    "What was the first gift you remember receiving?",
    "What was your favorite childhood memory?",
    "What was your favorite place to play as a child?",
    "What was the name of your childhood imaginary character?",
    "What was your favorite childhood costume?",

    // School
    "What was the name of your first school?",
    "What was the name of your first teacher?",
    "What was your favorite teacher's name?",
    "What was your favorite school subject?",
    "What was your least favorite school subject?",
    "What was your favorite school activity?",
    "What was your favorite school event?",
    "What was the name of your school mascot?",
    "What was your favorite school lunch?",
    "What was your favorite school game?",
    "What was your favorite school memory?",
    "What was the name of your first school friend?",
    "What was your favorite classroom subject?",
    "What was your favorite school trip?",
    "What was the name of your favorite teacher's subject?",
    "What was your favorite school uniform color?",
    "What was the name of your first school?",
    "What was the first award you received at school?",
    "What was your favorite school club?",
    "What was your favorite school celebration?",

    // Friends & Social
    "What was your best friend's nickname?",
    "What was the name of your first best friend?",
    "What nickname did your friends give you?",
    "What was your childhood friend's favorite game?",
    "What was the first nickname you gave someone?",
    "What was your favorite activity with friends?",
    "What was the name of your first group of friends?",
    "What was your favorite place to meet friends?",
    "What was the first game you played with your best friend?",
    "What was a memorable activity you did with friends?",

    // Places & Travel
    "What city were you born in?",
    "What was the name of the neighborhood where you grew up?",
    "What was the name of the street where you grew up?",
    "What was your first travel destination?",
    "What was your favorite place to visit as a child?",
    "What was your favorite holiday destination?",
    "What was your favorite family vacation location?",
    "What was the first city you visited?",
    "What was the first country you visited?",
    "What was your favorite travel memory?",
    "What was your favorite place in your hometown?",
    "What was your favorite park?",
    "What was your favorite local restaurant?",
    "What was your favorite place to relax?",

    // Technology
    "What was your first phone model?",
    "What was your first smartphone?",
    "What was your first computer?",
    "What was your first laptop brand?",
    "What was the first video game you played?",
    "What was your favorite video game?",
    "What was your first gaming console?",
    "What was your first social media platform?",
    "What was the first app you installed?",
    "What was the first website you remember using?",
    "What was your first email provider?",
    "What was your first operating system?",
    "What was your favorite computer game?",
    "What was your first electronic device?",
    "What was your favorite technology device?",

    // Food & Favorites
    "What is your favorite food?",
    "What is your favorite fruit?",
    "What is your favorite vegetable?",
    "What is your favorite dessert?",
    "What is your favorite ice cream flavor?",
    "What is your favorite drink?",
    "What is your favorite snack?",
    "What is your favorite restaurant?",
    "What is your favorite cuisine?",
    "What is your favorite breakfast?",
    "What is your favorite festival food?",
    "What is your favorite homemade meal?",
    "What is your favorite street food?",
    "What is your favorite pizza topping?",
    "What is your favorite chocolate?",

    // Entertainment
    "What is your favorite movie?",
    "What is your favorite TV show?",
    "What is your favorite book?",
    "Who is your favorite author?",
    "Who is your favorite singer?",
    "Who is your favorite actor?",
    "Who is your favorite fictional character?",
    "What is your favorite song?",
    "What is your favorite music genre?",
    "What is your favorite movie character?",
    "What is your favorite animated movie?",
    "What is your favorite TV character?",
    "What is your favorite superhero?",
    "What is your favorite comic book?",
    "What is your favorite fictional world?",

    // Hobbies & Interests
    "What is your favorite hobby?",
    "What is your favorite sport?",
    "What is your favorite team?",
    "What is your favorite outdoor activity?",
    "What is your favorite indoor activity?",
    "What is your favorite board game?",
    "What is your favorite card game?",
    "What is your favorite creative activity?",
    "What is your favorite musical instrument?",
    "What is your favorite exercise?",
    "What is your favorite weekend activity?",
    "What is your favorite way to relax?",
    "What is your favorite thing to collect?",
    "What is your favorite photography subject?",
    "What is your favorite type of art?",

    // Personal Memories
    "What was your first job?",
    "What was the name of your first workplace?",
    "What was your first major achievement?",
    "What was the first award you received?",
    "What was your first concert?",
    "What was your first flight?",
    "What was your first bicycle?",
    "What was your first car?",
    "What was your first musical instrument?",
    "What was your first pet's name?",
    "What was your first pet's nickname?",
    "What was the name of your favorite childhood pet?",
    "What was your first major purchase?",
    "What was your first memorable trip?",
    "What was your first memorable celebration?",

    // Preferences
    "What is your favorite color?",
    "What is your favorite season?",
    "What is your favorite flower?",
    "What is your favorite animal?",
    "What is your favorite number?",
    "What is your favorite day of the week?",
    "What is your favorite month?",
    "What is your favorite holiday?",
    "What is your favorite festival?",
    "What is your favorite time of day?",
    "What is your favorite weather?",
    "What is your favorite type of clothing?",
    "What is your favorite type of music?",
    "What is your favorite type of movie?",
    "What is your favorite type of book?",

    // More Personal / Custom
    "What word did you often use as a child?",
    "What was your childhood dream job?",
    "What was your favorite childhood hiding place?",
    "What was your favorite childhood playground?",
    "What was your favorite childhood celebration?",
    "What was your favorite childhood subject?",
    "What was your favorite childhood drink?",
    "What was your favorite childhood restaurant?",
    "What was your favorite childhood vacation?",
    "What was your favorite childhood tradition?",
    "What was your favorite childhood story?",
    "What was the first place you called home?",
    "What was the first thing you learned to cook?",
    "What was the first song you learned by heart?",
    "What was the first skill you were proud of?",
    "What was the first hobby you seriously enjoyed?",
    "What was the first thing you saved money to buy?",
    "What was the first memorable birthday gift you received?",
    "What was your favorite birthday celebration?",
    "What was the most memorable school event?"
)

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