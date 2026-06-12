package com.example.zuerijoeppli.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            text = "Züri-Jöppli",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ZurichBlue
        )
        Text(
            text = "Autonome Abholung & Recycling",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

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
                        text = "Anmeldemethode wählen",
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
                            Text("Mit Google anmelden", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                            Text("Mit E-Mail anmelden", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                            Text("Mit Telefonnummer anmelden", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                                "GOOGLE" -> "Google Anmeldung"
                                "EMAIL" -> "E-Mail Login"
                                else -> "SMS-Verifikation"
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
                        }) {
                            Text("Zurück", color = EcoGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (loginMethod == "GOOGLE" && showGoogleAccounts) {
                        Text(
                            text = "Wähle ein Google-Konto aus:",
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
                                            Toast.makeText(context, "Erfolgreich angemeldet als $name", Toast.LENGTH_SHORT).show()
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
                            label = { Text("Display Name") },
                            placeholder = { Text("Ueli Maurer") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("E-Mail Adresse") },
                            placeholder = { Text("ueli@example.ch") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Passwort") },
                            placeholder = { Text("Mindestens 6 Zeichen") },
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
                                    Toast.makeText(context, "Bitte fülle alle Felder korrekt aus", Toast.LENGTH_SHORT).show()
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
                                Text("Registrieren & Anmelden", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (loginMethod == "PHONE") {
                        if (!showOtpScreen) {
                            // Phone input field
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Telefonnummer") },
                                placeholder = { Text("e.g. +41 79 123 45 67") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (phoneInput.isBlank() || phoneInput.length < 9) {
                                        Toast.makeText(context, "Bitte gib eine gültige Telefonnummer ein", Toast.LENGTH_SHORT).show()
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
                                    Text("Sende Verifikations-Code", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // 6-digit OTP Simulation input
                            Text(
                                text = "Wir haben einen 6-stelligen Code an $phoneInput gesendet. Trage ihn unten ein.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                lineHeight = 16.sp
                            )

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { if (it.length <= 6) otpInput = it },
                                label = { Text("SMS-Code (6 Ziffern)") },
                                placeholder = { Text("123456") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (otpInput.length != 6) {
                                        Toast.makeText(context, "Bitte gib den 6-stelligen Code ein", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isLoading = false
                                        RecyclingRepository.loginWithPhone(phoneInput)
                                        Toast.makeText(context, "Verifiziert!", Toast.LENGTH_SHORT).show()
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
                                    Text("Code verifizieren", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
