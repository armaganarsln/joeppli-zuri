package com.example.zuerijoeppli.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zuerijoeppli.data.RecyclingRepository
import com.example.zuerijoeppli.theme.EcoGreen
import com.example.zuerijoeppli.theme.ZurichBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderScreen(
    onNavigateHome: () -> Unit,
    prefillQuick: Boolean = false
) {
    var currentStep by remember { mutableStateOf(if (prefillQuick) 2 else 1) }
    
    // Form states
    var address by remember { mutableStateOf("Langstrasse 120, 8004 Zürich") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf("") }
    val selectedMaterials = remember { mutableStateListOf<String>() }
    var isExpress by remember { mutableStateOf(prefillQuick) }
    
    val profile by RecyclingRepository.userProfile.collectAsState()
    
    // Initializer
    LaunchedEffect(profile) {
        address = profile.homeAddress
    }
    
    if (prefillQuick && selectedMaterials.isEmpty()) {
        selectedMaterials.addAll(listOf("Papier/Karton", "Altglas", "Alu/Metall", "Biogut/Kompost"))
        selectedDate = SimpleDateFormat("EEEE, dd. MMMM", Locale.GERMAN).format(Date())
        selectedTimeSlot = "Feierabend (18:00 - 21:00)"
    }

    // Pricing calculation
    val freeCats = listOf("glass", "altglas", "paper", "cardboard", "karton", "compost", "bio", "aluminum", "tin", "alu", "papier", "organic", "grüngut", "biogut")
    val hasPaidMaterial = selectedMaterials.any { mat ->
        !freeCats.any { free -> mat.lowercase().contains(free) }
    }
    var price = 0f
    if (hasPaidMaterial) price += 8f
    if (isExpress) price += 5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentStep) {
            1 -> OrderStep1(address, { address = it }, { currentStep = 2 })
            2 -> OrderStep2(selectedDate, { selectedDate = it }, selectedTimeSlot, { selectedTimeSlot = it }, isExpress, { isExpress = it }, { currentStep = 3 })
            3 -> OrderStep3(selectedMaterials, { currentStep = 4 }, hasPaid = hasPaidMaterial)
            4 -> OrderStep4(address, selectedDate, selectedTimeSlot, selectedMaterials, price, isExpress, { currentStep = 5 })
            5 -> OrderStep5(price, {
                // Complete order and update stats
                RecyclingRepository.addPickup(address, selectedDate, selectedTimeSlot, selectedMaterials.toList(), price, isExpress)
                currentStep = 6
            })
            6 -> JöppliTrackerScreen(address, onNavigateHome)
        }
    }
}

@Composable
fun OrderStep1(
    address: String,
    onAddressChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Abholort wählen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        Text("Wo soll der Jöppli-Fahrzeug vorfahren?", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Zürcher Adresse") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Weiter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrderStep2(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    selectedTimeSlot: String,
    onTimeSlotChange: (String) -> Unit,
    isExpress: Boolean,
    onExpressChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    val dateOptions = remember {
        val format = SimpleDateFormat("EEEE, dd. MMMM", Locale.GERMAN)
        val today = Date()
        val tomorrow = Date(today.time + 24 * 60 * 60 * 1000)
        val nextDay = Date(today.time + 2 * 24 * 60 * 60 * 1000)
        listOf("Heute (${format.format(today)})", "Morgen (${format.format(tomorrow)})", format.format(nextDay))
    }

    val timeSlots = listOf(
        "Vormittag (08:00 - 12:00)",
        "Nachmittag (13:00 - 17:00)",
        "Feierabend (18:00 - 21:00)"
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Abholzeit wählen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        Spacer(modifier = Modifier.height(20.dp))

        // Date selection list
        Text("Datum", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        Spacer(modifier = Modifier.height(8.dp))
        dateOptions.forEach { date ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateChange(date) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedDate == date, onClick = { onDateChange(date) }, colors = RadioButtonDefaults.colors(selectedColor = EcoGreen))
                Text(date, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Slot selection list
        Text("Zeitfenster", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        Spacer(modifier = Modifier.height(8.dp))
        timeSlots.forEach { slot ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTimeSlotChange(slot) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedTimeSlot == slot, onClick = { onTimeSlotChange(slot) }, colors = RadioButtonDefaults.colors(selectedColor = EcoGreen))
                Text(slot, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Express toggle
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sofort (Express Abholung)", fontWeight = FontWeight.Bold, color = ZurichBlue)
                    Text("Jöppli startet sofort zu dir (+ CHF 5.00)", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(checked = isExpress, onCheckedChange = onExpressChange, colors = SwitchDefaults.colors(checkedThumbColor = EcoGreen))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            enabled = selectedDate.isNotEmpty() && selectedTimeSlot.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Weiter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrderStep3(
    selectedMaterials: MutableList<String>,
    onNext: () -> Unit,
    hasPaid: Boolean
) {
    val materials = listOf(
        Pair("Papier/Karton", true),
        Pair("Altglas", true),
        Pair("Alu/Metall", true),
        Pair("Biogut/Kompost", true),
        Pair("Elektroschrott", false),
        Pair("Sperrgut (Möbel, Holz)", false)
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Wertstoffe sortieren", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        Text("Standardgut ist gratis. Bulky/E-waste kostet CHF 8.- flat.", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        materials.forEach { (name, isFree) ->
            val checked = selectedMaterials.contains(name)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (checked) selectedMaterials.remove(name) else selectedMaterials.add(name)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = {
                            if (checked) selectedMaterials.remove(name) else selectedMaterials.add(name)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = EcoGreen)
                    )
                    Text(name, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isFree) EcoGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFree) "GRATIS" else "+ CHF 8.-",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFree) EcoGreen else ZurichBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            enabled = selectedMaterials.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Weiter zur Kasse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrderStep4(
    address: String,
    dateString: String,
    timeSlot: String,
    materials: List<String>,
    price: Float,
    isExpress: Boolean,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Zusammenfassung", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBEF)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EcoGreen.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcoGreen)
                    Text(address, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = EcoGreen)
                    Text("$dateString | $timeSlot", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.List, contentDescription = null, tint = EcoGreen)
                    Column {
                        Text("Wertstoffe:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(materials.joinToString(", "), fontSize = 12.sp, color = Color.Gray)
                    }
                }
                if (isExpress) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = EcoGreen)
                        Text("Express Abholung (+ CHF 5.00)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Divider(color = EcoGreen.copy(alpha = 0.1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Gebühr", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ZurichBlue)
                    Text(
                        text = if (price == 0f) "GRATIS" else String.format("CHF %.2f", price),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = EcoGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(if (price == 0f) "Bestellung bestätigen" else "Zahlen mit TWINT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrderStep5(
    price: Float,
    onPaymentSuccess: () -> Unit
) {
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(color = EcoGreen, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Zahlung wird verarbeitet...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF00B5E2).copy(alpha = 0.1f), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF00B5E2),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("TWINT Express-Zahlung", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
            Text(
                text = String.format("CHF %.2f", price),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00B5E2),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(
                text = "Klicke unten, um die Zahlung über die TWINT-Demo freizugeben.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    loading = true
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loading = false
                        onPaymentSuccess()
                    }, 2000)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B5E2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Zahlung freigeben", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun JöppliTrackerScreen(
    address: String,
    onGoHome: () -> Unit
) {
    var statusIndex by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    val statusMessages = remember {
        listOf(
            "Dispositioniert: Jöppli verlässt den ERZ Werkhof Hardau...",
            "Routenplanung durch Kreis 4/5 wird berechnet...",
            "Ladesensoren kalibriert: 100% Ökostrom geladen...",
            "Überquert die Hardbrücke Richtung Langstrasse...",
            "Weicht vorübergehend dem Tram 8 auf der Badenerstrasse aus...",
            "Biegt in Ihre Quartierstrasse ein...",
            "Jöppli nähert sich Ihrer Adresse...",
            "Sicherheits-Sensoren aktiv: Bereit zur Beladung..."
        )
    }

    LaunchedEffect(Unit) {
        // Animate truck movement progress over 20 seconds
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 20000)
        )
    }

    LaunchedEffect(Unit) {
        // Increment status message index every 2.5 seconds
        while (statusIndex < statusMessages.size - 1) {
            delay(2500)
            statusIndex++
        }
    }

    val isArrived = progress.value >= 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Jöppli live verfolgen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ZurichBlue)
            if (isArrived) {
                Box(
                    modifier = Modifier
                        .background(EcoGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("ANGEKOMMEN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreen)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Ziel: $address", fontSize = 12.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(20.dp))

        // Custom canvas animated route tracker representation
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE5E7EB))
                .border(2.dp, Color.White, RoundedCornerShape(24.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Zurich Grid mockup background
                val linePaintColor = Color.White
                for (i in 1..6) {
                    // Vertical roads
                    drawLine(linePaintColor, Offset(w * i / 7f, 0f), Offset(w * i / 7f, h), strokeWidth = 8f)
                    // Horizontal roads
                    drawLine(linePaintColor, Offset(0f, h * i / 7f), Offset(w, h * i / 7f), strokeWidth = 8f)
                }

                // Dotted Path from Werkhof Hardau to Home
                // Werkhof: w * 0.2f, h * 0.8f
                // Home: w * 0.8f, h * 0.2f
                val startX = w * 0.2f
                val startY = h * 0.8f
                val endX = w * 0.8f
                val endY = h * 0.2f

                // Draw route line
                drawLine(
                    color = ZurichBlue.copy(alpha = 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 10f
                )

                // Dotted actual track path
                drawLine(
                    color = EcoGreen,
                    start = Offset(startX, startY),
                    end = Offset(
                        startX + (endX - startX) * progress.value,
                        startY + (endY - startY) * progress.value
                    ),
                    strokeWidth = 10f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )

                // Draw Werkhof Hardau Node
                drawCircle(color = ZurichBlue, radius = 20f, center = Offset(startX, startY))
                drawCircle(color = Color.White, radius = 8f, center = Offset(startX, startY))

                // Draw Home Node
                drawCircle(color = Color(0xFFDC2626), radius = 20f, center = Offset(endX, endY))
                drawCircle(color = Color.White, radius = 8f, center = Offset(endX, endY))

                // Draw Robot marker position
                val currentRobotX = startX + (endX - startX) * progress.value
                val currentRobotY = startY + (endY - startY) * progress.value
                drawCircle(color = EcoGreen, radius = 24f, center = Offset(currentRobotX, currentRobotY))
                drawCircle(color = Color.White, radius = 10f, center = Offset(currentRobotX, currentRobotY))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!isArrived) {
                        CircularProgressIndicator(color = EcoGreen, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = if (isArrived) "Bereit zum Einladen" else "Jöppli Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZurichBlue
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessages[statusIndex],
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onGoHome,
            colors = ButtonDefaults.buttonColors(containerColor = if (isArrived) EcoGreen else ZurichBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(if (isArrived) "Fertig & Zurück" else "Schliessen & Später verfolgen", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
