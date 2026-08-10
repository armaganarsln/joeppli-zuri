package gl.joeppli.zueri.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gl.joeppli.zueri.data.AuthManager
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.ui.JoeppliStrings
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Prototype OTP: no SMS is sent, so the demo verifies against this fixed code
// (shown on screen) instead of silently accepting any 6 digits.
private const val DEMO_OTP = "123456"

/** Maps an [AuthManager.AuthError] to a localized, user-facing message. */
private fun authErrorMessage(error: AuthManager.AuthError, strings: JoeppliStrings): String = when (error) {
    AuthManager.AuthError.EMAIL_IN_USE -> strings.authErrEmailInUse
    AuthManager.AuthError.INVALID_CREDENTIALS -> strings.authErrInvalidCreds
    AuthManager.AuthError.WEAK_PASSWORD -> strings.authErrWeakPassword
    AuthManager.AuthError.INVALID_EMAIL -> strings.authErrInvalidEmail
    AuthManager.AuthError.NETWORK -> strings.authErrNetwork
    AuthManager.AuthError.NO_GOOGLE_ACCOUNT -> strings.authErrNoGoogleAccount
    AuthManager.AuthError.INVALID_CODE -> strings.authErrInvalidCode
    AuthManager.AuthError.CANCELLED -> "" // user dismissed the picker; not shown
    AuthManager.AuthError.UNKNOWN -> strings.authErrUnknown
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
    val strings = LocalJoeppliStrings.current
    val activeLang by RecyclingRepository.userLanguage.collectAsState()

    var loginMethod by rememberSaveable { mutableStateOf<String?>(null) } // "GOOGLE", "EMAIL", "PHONE"
    var showGoogleAccounts by rememberSaveable { mutableStateOf(false) }
    var showOtpScreen by rememberSaveable { mutableStateOf(false) }
    // Transient: a simulated login in flight shouldn't survive rotation
    var isLoading by remember { mutableStateOf(false) }

    // Form fields — saveable so typed input survives rotation
    var nameInput by rememberSaveable { mutableStateOf("") }
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var phoneInput by rememberSaveable { mutableStateOf("") }
    var otpInput by rememberSaveable { mutableStateOf("") }

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    // Email card: true = create a new account, false = sign in to an existing one
    var isRegisterMode by rememberSaveable { mutableStateOf(true) }
    // Phone OTP: true when a real SMS send wasn't possible and we fall back to
    // the demo code path (keeps the app usable before the real Firebase project).
    var otpDemoMode by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    // Coroutine scope for the simulated login delays — cancels with the
    // screen, so a callback can't fire on a composable that's already gone.
    val scope = rememberCoroutineScope()

    // Inline field errors (transient; re-validated on submit)
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // 6-digit OTP focus coordinates
    val otpFocusRequester = remember { FocusRequester() }
    var isOtpFocused by remember { mutableStateOf(false) }

    // Back steps within the auth flow instead of exiting: OTP -> phone entry,
    // a chosen method -> the method picker. On the picker it's disabled so the
    // system handles back (exit).
    BackHandler(enabled = loginMethod != null) {
        if (showOtpScreen) {
            showOtpScreen = false
            otpInput = ""
        } else {
            loginMethod = null
            showGoogleAccounts = false
            isLoading = false
            otpInput = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language Selector Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeLang == "en") "Language: " else "Sprache: ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = { RecyclingRepository.setLanguage("de") },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (activeLang == "de") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text("DE", fontWeight = if (activeLang == "de") FontWeight.Black else FontWeight.Normal, fontSize = 13.sp)
            }
            Text("|", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), fontSize = 12.sp)
            TextButton(
                onClick = { RecyclingRepository.setLanguage("en") },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (activeLang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text("EN", fontWeight = if (activeLang == "en") FontWeight.Black else FontWeight.Normal, fontSize = 13.sp)
            }
        }

        // App Logo & Brand Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.appName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = if (activeLang == "en") "Autonomous Waste Collection & Recycling" else "Autonome Abholung & Recycling",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (loginMethod == null) {
            // Main auth gateway selection cards
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.authSelectTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Google login trigger button — neutral surface like the
                    // official sign-in button, not a brand color
                    Button(
                        onClick = {
                            loginMethod = "GOOGLE"
                            showGoogleAccounts = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Text(strings.authGoogleBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Email login button
                    Button(
                        onClick = { loginMethod = "EMAIL" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Text(strings.authEmailBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Phone login button
                    Button(
                        onClick = { loginMethod = "PHONE" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Text(strings.authPhoneBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Secondary login form inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (loginMethod) {
                                "GOOGLE" -> strings.authGoogleHeader
                                "EMAIL" -> strings.authEmailHeader
                                else -> strings.authPhoneHeader
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TextButton(onClick = {
                            loginMethod = null
                            showGoogleAccounts = false
                            showOtpScreen = false
                            isLoading = false
                            otpInput = ""
                        }) {
                            Text(strings.authBackBtn, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (loginMethod == "GOOGLE" && showGoogleAccounts) {
                        // Real Google sign-in: Credential Manager -> Google ID token -> Firebase
                        Button(
                            onClick = {
                                if (isLoading) return@Button
                                isLoading = true
                                scope.launch {
                                    val result = AuthManager.signInWithGoogle(context)
                                    isLoading = false
                                    if (result is AuthManager.AuthResult.Failure &&
                                        result.error != AuthManager.AuthError.CANCELLED
                                    ) {
                                        Toast.makeText(context, authErrorMessage(result.error, strings), Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(28.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                                    Text(strings.authGoogleBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = if (activeLang == "en") "or use a demo account (prototype):" else "oder es Demo-Konto bruche (Prototyp):",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Demo accounts list (prototype — no real Google auth)
                        val mockAccounts = listOf(
                            "ueli.maurer@gmail.com" to "Ueli Maurer",
                            "armag.business@gmail.com" to "Armag Arslan"
                        )

                        mockAccounts.forEach { (email, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isLoading) return@clickable
                                        isLoading = true
                                        scope.launch {
                                            delay(1000)
                                            isLoading = false
                                            RecyclingRepository.loginWithGoogle(name, email)
                                            Toast.makeText(context, if (activeLang == "en") "Successfully logged in as $name" else "Erfolgreich angemeldet als $name", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "DEMO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                        }
                    } else if (loginMethod == "EMAIL") {
                        // Shared by the CTA button and the keyboard's Done action
                        val submitEmailForm = fun() {
                            if (isLoading) return
                            nameError = if (isRegisterMode && nameInput.isBlank())
                                (if (activeLang == "en") "Please enter your name" else "Bitte gib din Name ih") else null
                            emailError = if (!Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches())
                                (if (activeLang == "en") "Enter a valid email address" else "Gib e gültigi E-Mail ih") else null
                            passwordError = if (passwordInput.length < 6)
                                (if (activeLang == "en") "Password must be at least 6 characters" else "Passwort mues mindestens 6 Zeiche ha") else null
                            if (nameError != null || emailError != null || passwordError != null) return
                            isLoading = true
                            scope.launch {
                                val result = if (isRegisterMode)
                                    AuthManager.registerWithEmail(nameInput, emailInput, passwordInput)
                                else
                                    AuthManager.signInWithEmail(emailInput, passwordInput)
                                isLoading = false
                                // Success flips isLoggedIn in the repository and the app
                                // navigates automatically; only failures need surfacing.
                                if (result is AuthManager.AuthResult.Failure) {
                                    Toast.makeText(context, authErrorMessage(result.error, strings), Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        // Sign in / Register segmented toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                false to strings.authEmailSignInTab,
                                true to strings.authEmailRegisterTab
                            ).forEach { (regMode, label) ->
                                val selected = isRegisterMode == regMode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                        .clickable {
                                            isRegisterMode = regMode
                                            nameError = null; emailError = null; passwordError = null
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Display name only needed when creating a new account
                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it; nameError = null },
                                label = { Text(strings.authEmailName) },
                                placeholder = { Text(strings.authEmailNamePlaceholder) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                isError = nameError != null,
                                supportingText = nameError?.let { { Text(it) } }
                            )
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; emailError = null },
                            label = { Text(strings.authEmailMail) },
                            placeholder = { Text("ueli@example.ch") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it) } }
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; passwordError = null },
                            label = { Text(strings.authEmailPassword) },
                            placeholder = { Text(strings.authEmailPasswordPlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                submitEmailForm()
                            }),
                            isError = passwordError != null,
                            supportingText = passwordError?.let { { Text(it) } },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) {
                                            if (activeLang == "en") "Hide password" else "Passwort verstecke"
                                        } else {
                                            if (activeLang == "en") "Show password" else "Passwort aazeige"
                                        }
                                    )
                                }
                            }
                        )

                        Button(
                            onClick = submitEmailForm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    if (isRegisterMode) strings.authEmailRegister else strings.authEmailSignInBtn,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (loginMethod == "PHONE") {
                        // Shared by the send button, the keyboard's Done action,
                        // and the resend option on the OTP screen.
                        val sendCodeAction = fun() {
                            if (isLoading) return
                            // Validate on digits only, so spaces / +41 formatting pass
                            phoneError = if (phoneInput.count { it.isDigit() } < 9)
                                (if (activeLang == "en") "Enter a valid phone number" else "Gib e gültigi Telefonnummere ih") else null
                            if (phoneError != null) return
                            isLoading = true
                            scope.launch {
                                val activity = context as android.app.Activity
                                when (AuthManager.sendPhoneCode(activity, phoneInput.trim())) {
                                    is AuthManager.PhoneSendResult.CodeSent -> {
                                        otpDemoMode = false
                                        otpInput = ""
                                        showOtpScreen = true
                                    }
                                    is AuthManager.PhoneSendResult.AutoVerified -> {
                                        // Instant verification signed the user in; app navigates away.
                                    }
                                    is AuthManager.PhoneSendResult.Failure -> {
                                        // Real SMS unavailable (e.g. backend not configured
                                        // yet) — fall back to the demo code path.
                                        otpDemoMode = true
                                        otpInput = ""
                                        showOtpScreen = true
                                    }
                                }
                                isLoading = false
                            }
                        }

                        if (!showOtpScreen) {
                            // Phone input field
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it; phoneError = null },
                                label = { Text(strings.authPhoneEnter) },
                                placeholder = { Text(strings.authPhoneEnterPlaceholder) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    sendCodeAction()
                                }),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = phoneError != null,
                                supportingText = phoneError?.let { { Text(it) } }
                            )

                            Button(
                                onClick = sendCodeAction,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                                } else {
                                    Text(strings.authPhoneSendOtp, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Premium 6-digit OTP simulation
                            Text(
                                text = strings.authPhoneOtpDesc.format(phoneInput),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                lineHeight = 16.sp
                            )
                            if (otpDemoMode) {
                                Text(
                                    text = if (activeLang == "en") "Demo code: $DEMO_OTP" else "Demo-Code: $DEMO_OTP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Hidden BasicTextField driving state
                            BasicTextField(
                                value = otpInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        otpInput = it
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .size(1.dp) // minimized size
                                    .focusRequester(otpFocusRequester)
                                    .onFocusChanged { isOtpFocused = it.isFocused }
                            )

                            // Visual 6-Digit Grid Boxes
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { otpFocusRequester.requestFocus() }
                                    .padding(vertical = 8.dp)
                            ) {
                                repeat(6) { index ->
                                    val char = otpInput.getOrNull(index)?.toString() ?: ""
                                    val isFocusedBox = index == otpInput.length && isOtpFocused

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp, 48.dp)
                                            .border(
                                                width = if (isFocusedBox) 2.dp else 1.dp,
                                                color = if (isFocusedBox) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .background(
                                                if (isFocusedBox) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.surface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (otpDemoMode) {
                                        if (otpInput != DEMO_OTP) {
                                            Toast.makeText(context, if (activeLang == "en") "Wrong code — enter the demo code $DEMO_OTP" else "Falsche Code — gib de Demo-Code $DEMO_OTP ih", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isLoading = true
                                        scope.launch {
                                            delay(1000)
                                            isLoading = false
                                            RecyclingRepository.loginWithPhone(phoneInput)
                                            Toast.makeText(context, strings.authPhoneOtpVerifyToast, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        if (otpInput.length < 6) {
                                            Toast.makeText(context, strings.authPhoneErrorOtp, Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isLoading = true
                                        scope.launch {
                                            val result = AuthManager.verifyPhoneCode(otpInput, phoneInput.trim())
                                            isLoading = false
                                            if (result is AuthManager.AuthResult.Failure) {
                                                Toast.makeText(context, authErrorMessage(result.error, strings), Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, strings.authPhoneOtpVerifyToast, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                                } else {
                                    Text(strings.authPhoneOtpVerify, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Escape hatch when the SMS never arrives. Meaningless
                            // in demo mode, where the code is shown on screen.
                            if (!otpDemoMode) {
                                TextButton(
                                    onClick = {
                                        otpInput = ""
                                        sendCodeAction()
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (activeLang == "en") "Didn't get a code? Send again"
                                        else "Kein Code becho? Nomal sände",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
