package com.example.zuerijoeppli.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zuerijoeppli.data.RecyclingRepository
import com.example.zuerijoeppli.theme.EcoGreen
import com.example.zuerijoeppli.theme.ZurichBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val profile by RecyclingRepository.userProfile.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember(profile.name) { mutableStateOf(profile.name) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var address by remember(profile.homeAddress) { mutableStateOf(profile.homeAddress) }
    
    var supportMessage by remember { mutableStateOf("") }
    var selectedPayment by remember(profile.defaultPaymentMethod) { mutableStateOf(profile.defaultPaymentMethod) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Profil & Adressen",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Hinterlege deine Daten für automatische Abholungen",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Wohnort & Kontakt",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZurichBlue
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vorname Nachname") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefonnummer") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Zürcher Wohnadresse") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        RecyclingRepository.updateProfile(name, phone, address, selectedPayment)
                        Toast.makeText(context, "Daten erfolgreich gespeichert", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Speichern")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Settle
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Standard-Zahlmittel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZurichBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isTwint = selectedPayment == "twint_demo"
                    FilterChip(
                        selected = isTwint,
                        onClick = { 
                            selectedPayment = "twint_demo"
                            RecyclingRepository.updateProfile(name, phone, address, "twint_demo")
                        },
                        label = { Text("TWINT (Standard)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isTwint,
                        onClick = { 
                            selectedPayment = "card"
                            RecyclingRepository.updateProfile(name, phone, address, "card")
                        },
                        label = { Text("Kreditkarte") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ERZ Feedback/Contact support form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ERZ Leitstelle kontaktieren",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZurichBlue
                )
                Text(
                    text = "Hast du Fragen zur Entsorgung oder deinem Jöppli-Fahrzeug?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                OutlinedTextField(
                    value = supportMessage,
                    onValueChange = { supportMessage = it },
                    label = { Text("Dine Mitteilung") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (supportMessage.isNotBlank()) {
                            Toast.makeText(context, "Meldung gesendet! ERZ wird antworten.", Toast.LENGTH_SHORT).show()
                            supportMessage = ""
                        } else {
                            Toast.makeText(context, "Bitte Nachricht eingeben", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZurichBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nachricht absenden")
                }
            }
        }
    }
}
