package gl.joeppli.zueri.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.theme.EcoGreen
import gl.joeppli.zueri.theme.ZurichBlue
import gl.joeppli.zueri.theme.TwintCyan
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderScreen(
    onNavigateHome: () -> Unit,
    prefillQuick: Boolean = false
) {
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    
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
        if (profile.homeAddress.isNotEmpty()) {
            address = profile.homeAddress
        }
    }

    if (prefillQuick && selectedMaterials.isEmpty()) {
        val standardPaper = if (lang == "en") "Paper / Cardboard" else "Papier/Karton"
        val standardGlass = if (lang == "en") "Glass" else "Altglas"
        val standardMetal = if (lang == "en") "Aluminum / Metal" else "Alu/Metall"
        val standardCompost = if (lang == "en") "Compost / Organic" else "Biogut/Kompost"
        selectedMaterials.addAll(listOf(standardPaper, standardGlass, standardMetal, standardCompost))
        
        val format = SimpleDateFormat("EEEE, dd. MMMM", if (lang == "en") Locale.US else Locale.GERMAN)
        selectedDate = format.format(Date())
        selectedTimeSlot = strings.orderSlotEvening
    }

    // Pricing calculation
    val freeCats = listOf("glass", "altglas", "paper", "cardboard", "karton", "compost", "bio", "aluminum", "tin", "alu", "papier", "organic", "grüngut", "biogut")
    val hasPaidMaterial = selectedMaterials.any { mat ->
        !freeCats.any { free -> mat.lowercase().contains(free) }
    }
    var price = 0f
    if (hasPaidMaterial) price += 8f
    if (isExpress) price += 4.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Wizard header with back navigation + progress (hidden on the tracker)
        if (currentStep <= 5) {
            WizardHeader(
                title = when (currentStep) {
                    1 -> strings.orderStepAddress
                    2 -> strings.orderStepSlot
                    3 -> strings.orderStepMaterials
                    4 -> strings.orderStepSummary
                    else -> strings.orderStepPayment
                },
                step = currentStep,
                totalSteps = 5,
                onBack = { if (currentStep > 1) currentStep-- else onNavigateHome() }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                1 -> OrderStep1(address, { address = it }, { currentStep = 2 })
                2 -> OrderStep2(selectedDate, { selectedDate = it }, selectedTimeSlot, { selectedTimeSlot = it }, isExpress, { isExpress = it }, { currentStep = 3 })
                3 -> OrderStep3(selectedMaterials, { currentStep = 4 })
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
}

@Composable
private fun WizardHeader(
    title: String,
    step: Int,
    totalSteps: Int,
    onBack: () -> Unit
) {
    val lang by RecyclingRepository.userLanguage.collectAsState()
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (lang == "en") "Step $step of $totalSteps" else "Schritt $step von $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LinearProgressIndicator(
            progress = { step / totalSteps.toFloat() },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun WizardCta(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun OrderStep1(
    address: String,
    onAddressChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (lang == "en") "Where should the Jöppli vehicle arrive?" else "Wo soll s'Jöppli-Fahrzeug vorfahre?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text(strings.profileAddress) },
            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        WizardCta(if (lang == "en") "Continue" else "Weiter", enabled = address.isNotBlank(), onClick = onNext)
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
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    
    val dateOptions = remember(lang) {
        val format = SimpleDateFormat("EEEE, dd. MMMM", if (lang == "en") Locale.US else Locale.GERMAN)
        val today = Date()
        val tomorrow = Date(today.time + 24 * 60 * 60 * 1000)
        val nextDay = Date(today.time + 2 * 24 * 60 * 60 * 1000)
        listOf(
            if (lang == "en") "Today (${format.format(today)})" else "Heute (${format.format(today)})",
            if (lang == "en") "Tomorrow (${format.format(tomorrow)})" else "Morgen (${format.format(tomorrow)})",
            format.format(nextDay)
        )
    }

    val timeSlots = listOf(
        strings.orderSlotMorning,
        strings.orderSlotAfternoon,
        strings.orderSlotEvening
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Date selection list
        Text(if (lang == "en") "Date" else "Datum", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        dateOptions.forEach { date ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDateChange(date) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedDate == date, onClick = { onDateChange(date) })
                Text(date, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Slot selection list
        Text(if (lang == "en") "Time Window" else "Zeitfenster", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        timeSlots.forEach { slot ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTimeSlotChange(slot) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedTimeSlot == slot, onClick = { onTimeSlotChange(slot) })
                Text(slot, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Express toggle
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        strings.orderExpressToggle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        strings.orderExpressDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isExpress, onCheckedChange = onExpressChange)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        WizardCta(
            if (lang == "en") "Continue" else "Weiter",
            enabled = selectedDate.isNotEmpty() && selectedTimeSlot.isNotEmpty(),
            onClick = onNext
        )
    }
}

@Composable
fun OrderStep3(
    selectedMaterials: MutableList<String>,
    onNext: () -> Unit
) {
    val lang by RecyclingRepository.userLanguage.collectAsState()
    val materials = listOf(
        Pair(if (lang == "en") "Paper / Cardboard" else "Papier/Karton", true),
        Pair(if (lang == "en") "Glass" else "Altglas", true),
        Pair(if (lang == "en") "Aluminum / Metal" else "Alu/Metall", true),
        Pair(if (lang == "en") "Compost / Organic" else "Biogut/Kompost", true),
        Pair(if (lang == "en") "Electronic Waste" else "Elektroschrott", false),
        Pair(if (lang == "en") "Bulky Waste (Furniture, Wood)" else "Sperrgut (Möbel, Holz)", false)
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (lang == "en") "Standard items are free. Bulky waste & E-waste costs CHF 8.00 flat fee." else "Standardgut isch gratis. Sperrgut & E-Schrott koschtet CHF 8.– pauschal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        materials.forEach { (name, isFree) ->
            val checked = selectedMaterials.contains(name)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (checked) selectedMaterials.remove(name) else selectedMaterials.add(name)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = {
                            if (checked) selectedMaterials.remove(name) else selectedMaterials.add(name)
                        }
                    )
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isFree) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFree) (if (lang == "en") "FREE" else "GRATIS") else "+ CHF 8.–",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFree) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        WizardCta(if (lang == "en") "Continue to Checkout" else "Weiter zur Kasse", enabled = selectedMaterials.isNotEmpty(), onClick = onNext)
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
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = onContainer)
                    Text(address, style = MaterialTheme.typography.bodyMedium, color = onContainer)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = onContainer)
                    Text("$dateString · $timeSlot", style = MaterialTheme.typography.bodyMedium, color = onContainer)
                }
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = null, tint = onContainer)
                    Column {
                        Text(if (lang == "en") "Recycle Items" else "Wertstoffe", style = MaterialTheme.typography.titleSmall, color = onContainer)
                        Text(
                            materials.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = onContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                if (isExpress) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = onContainer)
                        Text("${strings.orderExpressToggle} (+ CHF 4.50)", style = MaterialTheme.typography.bodyMedium, color = onContainer)
                    }
                }

                HorizontalDivider(color = onContainer.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (lang == "en") "Total Fee" else "Total Gebühr", style = MaterialTheme.typography.titleMedium, color = onContainer)
                    Text(
                        text = if (price == 0f) (if (lang == "en") "FREE" else "GRATIS") else String.format(Locale.ROOT, "CHF %.2f", price),
                        style = MaterialTheme.typography.headlineSmall,
                        color = onContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val ctaText = if (price == 0f) {
            if (lang == "en") "Confirm Order" else "Bestellig bestätige"
        } else {
            if (lang == "en") "Pay with TWINT" else "Zahle mit TWINT"
        }
        WizardCta(ctaText, onClick = onNext)
    }
}

@Composable
fun OrderStep5(
    price: Float,
    onPaymentSuccess: () -> Unit
) {
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    var loading by remember { mutableStateOf(false) }

    if (loading) {
        LaunchedEffect(Unit) {
            delay(2000)
            onPaymentSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                if (lang == "en") "Processing Payment..." else "Zahlig wird verarbeitet…",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(TwintCyan.copy(alpha = 0.12f), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhoneIphone,
                    contentDescription = null,
                    tint = TwintCyan,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                if (lang == "en") "TWINT Express Payment" else "TWINT Express-Zahlig",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = String.format(Locale.ROOT, "CHF %.2f", price),
                style = MaterialTheme.typography.displaySmall,
                color = TwintCyan,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(
                text = if (lang == "en") "Tap below to authorize the payment via TWINT sandbox." else "Klick unde, zum d'Zahlig über d'TWINT-Demo freigäh.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { loading = true },
                colors = ButtonDefaults.buttonColors(containerColor = TwintCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(if (lang == "en") "Authorize Payment" else "Zahlig freigäh", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun JöppliTrackerScreen(
    address: String,
    onGoHome: () -> Unit
) {
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    var statusIndex by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    val statusMessages = remember(lang) {
        if (lang == "en") {
            listOf(
                "Dispatched: Jöppli is leaving ERZ Werkhof Hardau...",
                "Calculating route through Kreis 4/5...",
                "Loading sensors calibrated: 100% green electricity charged...",
                "Crossing Hardbrücke towards Langstrasse...",
                "Slightly yielding to Tram 8 on Badenerstrasse...",
                "Turning into your neighborhood street...",
                "Jöppli is approaching your address...",
                "Safety sensors active: Ready to load..."
            )
        } else {
            listOf(
                "Dispositioniert: Jöppli verlaht de ERZ Werkhof Hardau…",
                "Routeplanig durch Kreis 4/5 wird grächnet…",
                "Ladesensore kalibriert: 100% Ökostrom glade…",
                "Überquert d'Hardbrugg Richtig Langstrass…",
                "Wiicht churz em Tram 8 uf de Badenerstrass uus…",
                "Biegt i dini Quartierstrass ii…",
                "Jöppli nähert sich dinere Adress…",
                "Sicherheits-Sensore aktiv: Parat zum Lade…"
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 20000)
        )
    }

    LaunchedEffect(Unit) {
        while (statusIndex < statusMessages.size - 1) {
            delay(2500)
            statusIndex++
        }
    }

    val isArrived = progress.value >= 1f

    // Radar pulsing transition animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (lang == "en") "Jöppli Live Tracking" else "Jöppli live verfolge",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            if (isArrived) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (lang == "en") "ARRIVED" else "AACHO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Ziel: $address",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stylized animated route map
        val mapBg = MaterialTheme.colorScheme.surfaceContainerHigh
        val roadColor = MaterialTheme.colorScheme.surface
        val routeTrack = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
        val routeColor = MaterialTheme.colorScheme.primary
        val depotColor = MaterialTheme.colorScheme.secondary
        val destColor = MaterialTheme.colorScheme.error
        val puckCenter = MaterialTheme.colorScheme.onPrimary

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(mapBg)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Zurich street grid mockup
                for (i in 1..6) {
                    drawLine(roadColor, Offset(w * i / 7f, 0f), Offset(w * i / 7f, h), strokeWidth = 8f)
                    drawLine(roadColor, Offset(0f, h * i / 7f), Offset(w, h * i / 7f), strokeWidth = 8f)
                }

                // Curved route: Werkhof Hardau (bottom left) → home (top right)
                val start = Offset(w * 0.15f, h * 0.82f)
                val end = Offset(w * 0.82f, h * 0.18f)
                val route = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(
                        w * 0.10f, h * 0.45f,
                        w * 0.50f, h * 0.70f,
                        w * 0.57f, h * 0.43f
                    )
                    cubicTo(
                        w * 0.63f, h * 0.22f,
                        w * 0.72f, h * 0.32f,
                        end.x, end.y
                    )
                }

                // Full route (faded)
                drawPath(route, color = routeTrack, style = Stroke(width = 10f, cap = StrokeCap.Round))

                // Travelled portion (Gradient trail, brand green to brand blue)
                val measure = PathMeasure()
                measure.setPath(route, false)
                val travelled = Path()
                measure.getSegment(0f, measure.length * progress.value, travelled, true)
                
                val trailBrush = Brush.linearGradient(
                    colors = listOf(
                        ZurichBlue,
                        EcoGreen
                    ),
                    start = start,
                    end = end
                )

                drawPath(
                    travelled,
                    brush = trailBrush,
                    style = Stroke(
                        width = 10f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )

                // Werkhof Hardau node
                drawCircle(color = depotColor, radius = 20f, center = start)
                drawCircle(color = roadColor, radius = 8f, center = start)

                // Home node
                drawCircle(color = destColor, radius = 20f, center = end)
                drawCircle(color = roadColor, radius = 8f, center = end)

                // Robot marker along the curve
                val robotPos = measure.getPosition(measure.length * progress.value.coerceIn(0f, 1f))
                
                // Pulsing radar ring
                val maxRadarRadius = 60f
                val currentRadarRadius = 24f + (maxRadarRadius - 24f) * pulseProgress
                val radarAlpha = 1f - pulseProgress
                drawCircle(
                    color = routeColor.copy(alpha = radarAlpha * 0.45f),
                    radius = currentRadarRadius,
                    center = robotPos
                )

                // Truck puck layers
                drawCircle(color = puckCenter, radius = 28f, center = robotPos)
                drawCircle(color = routeColor, radius = 24f, center = robotPos)
                drawCircle(color = puckCenter, radius = 10f, center = robotPos)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!isArrived) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (isArrived) (if (lang == "en") "Ready for Loading" else "Parat zum Iilade") else (if (lang == "en") "Jöppli Status" else "Jöppli Status"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArrived) (if (lang == "en") "I am here! Please place your recycling outside your door." else "Ich bi da! Bitte stell dis Recycling vor d'Tür.") else statusMessages[statusIndex],
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onGoHome,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isArrived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (isArrived) (if (lang == "en") "Return Home" else "Zrugg zum Start") else (if (lang == "en") "Go to Start Screen" else "Zrugg zum Startbildschirm"),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
