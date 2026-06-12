package com.example.zuerijoeppli.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zuerijoeppli.data.RecyclingRepository
import com.example.zuerijoeppli.theme.EcoGreen
import com.example.zuerijoeppli.theme.ZurichBlue
import com.example.zuerijoeppli.ui.LocalJoeppliStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
    val strings = LocalJoeppliStrings.current
    val activeLang by RecyclingRepository.userLanguage.collectAsState()

    var loginMethod by remember { mutableStateOf<String?>(null) } // "GOOGLE", "EMAIL", "PHONE"
    var showGoogleAccounts by remember { mutableStateOf(false) }
    var showOtpScreen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Forms fields
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 6-digit OTP focus coordinates
    val otpFocusRequester = remember { FocusRequester() }
    var isOtpFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = { RecyclingRepository.setLanguage("de") },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (activeLang == "de") EcoGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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
                    contentColor = if (activeLang == "en") EcoGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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
                .background(EcoGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = EcoGreen,
                modifier = Modifier.size(44.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = strings.appName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ZurichBlue
        )
        Text(
            text = if (activeLang == "en") "Autonomous Waste Collection & Recycling" else "Autonome Abholung & Recycling",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
                        color = ZurichBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Google login trigger button
                    Button(
                        onClick = {
                            loginMethod = "GOOGLE"
                            showGoogleAccounts = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                            Text(strings.authGoogleBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Email login button
                    Button(
                        onClick = { loginMethod = "EMAIL" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZurichBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                            Text(strings.authEmailBtn, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Phone login button
                    Button(
                        onClick = { loginMethod = "PHONE" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
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
                            color = ZurichBlue
                        )
                        TextButton(onClick = {
                            loginMethod = null
                            showGoogleAccounts = false
                            showOtpScreen = false
                            isLoading = false
                            otpInput = ""
                        }) {
                            Text(strings.authBackBtn, color = EcoGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (loginMethod == "GOOGLE" && showGoogleAccounts) {
                        Text(
                            text = strings.authGoogleSelect,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        // Google mock accounts list
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
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        isLoading = true
                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                            isLoading = false
                                            RecyclingRepository.loginWithGoogle(name, email)
                                            Toast.makeText(context, if (activeLang == "en") "Successfully logged in as $name" else "Erfolgreich angemeldet als $name", Toast.LENGTH_SHORT).show()
                                        }, 1000)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ZurichBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(name.take(1), fontWeight = FontWeight.Bold, color = ZurichBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    } else if (loginMethod == "EMAIL") {
                        // Email + Password form
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(strings.authEmailName) },
                            placeholder = { Text(strings.authEmailNamePlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(strings.authEmailMail) },
                            placeholder = { Text("ueli@example.ch") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(strings.authEmailPassword) },
                            placeholder = { Text(strings.authEmailPasswordPlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Info else Icons.Default.Lock,
                                        contentDescription = "Passwort anzeigen"
                                    )
                                }
                            }
                        )

                        Button(
                            onClick = {
                                if (emailInput.isBlank() || nameInput.isBlank() || passwordInput.length < 6) {
                                    Toast.makeText(context, strings.authEmailErrorFill, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isLoading = true
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isLoading = false
                                    RecyclingRepository.loginWithEmail(nameInput, emailInput)
                                }, 1000)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZurichBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(strings.authEmailRegister, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (loginMethod == "PHONE") {
                        if (!showOtpScreen) {
                            // Phone input field
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text(strings.authPhoneEnter) },
                                placeholder = { Text(strings.authPhoneEnterPlaceholder) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (phoneInput.isBlank() || phoneInput.length < 9) {
                                        Toast.makeText(context, strings.authPhoneErrorValid, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isLoading = false
                                        showOtpScreen = true
                                    }, 1200)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
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
                                                color = if (isFocusedBox) EcoGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
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
                                            color = ZurichBlue,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (otpInput.length != 6) {
                                        Toast.makeText(context, strings.authPhoneErrorOtp, Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isLoading = false
                                        RecyclingRepository.loginWithPhone(phoneInput)
                                        Toast.makeText(context, strings.authPhoneOtpVerifyToast, Toast.LENGTH_SHORT).show()
                                    }, 1000)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text(strings.authPhoneOtpVerify, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
