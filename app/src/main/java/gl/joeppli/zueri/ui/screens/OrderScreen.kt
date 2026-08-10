package gl.joeppli.zueri.ui.screens

import android.Manifest
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.notify.OrderNotifications
import gl.joeppli.zueri.theme.TwintCyan
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderScreen(
    onNavigateHome: () -> Unit,
    prefillQuick: Boolean = false,
    startAtTracker: Boolean = false
) {
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    
    var currentStep by rememberSaveable { mutableStateOf(if (startAtTracker) 6 else if (prefillQuick) 2 else 1) }

    // Form states — saveable so an in-progress order survives rotation
    var address by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf("") }
    var selectedTimeSlot by rememberSaveable { mutableStateOf("") }
    val selectedMaterials = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf<String>() }
    var isExpress by rememberSaveable { mutableStateOf(prefillQuick) }

    val profile by RecyclingRepository.userProfile.collectAsState()
    val lastPickup by RecyclingRepository.lastPickup.collectAsState()

    // Initializer — when resuming the tracker, show the saved pickup's address;
    // otherwise prefill the user's registered home address.
    LaunchedEffect(profile, startAtTracker) {
        val resumeAddress = if (startAtTracker) lastPickup?.address else null
        when {
            resumeAddress != null -> address = resumeAddress
            profile.homeAddress.isNotEmpty() -> address = profile.homeAddress
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

    // Hardware Back mirrors the wizard's on-screen back arrow: step back
    // through 2..5, and leave to Home from the first step or the tracker.
    BackHandler {
        when (currentStep) {
            in 2..5 -> currentStep--
            else -> onNavigateHome()
        }
    }

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
                4 -> OrderStep4(address, selectedDate, selectedTimeSlot, selectedMaterials, price, isExpress, {
                    if (price == 0f) {
                        // Nothing to pay — complete the free order right away
                        // instead of showing a CHF 0.00 TWINT screen.
                        RecyclingRepository.addPickup(address, selectedDate, selectedTimeSlot, selectedMaterials.toList(), price, isExpress)
                        currentStep = 6
                    } else {
                        currentStep = 5
                    }
                })
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (lang == "en") "Back" else "Zurück")
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
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
    val quickAddresses = listOf(
        "Langstrasse 120, 8004 Zürich",
        "Badenerstrasse 350, 8003 Zürich",
        "Limmatquai 50, 8001 Zürich",
        "Schaffhauserstrasse 100, 8057 Zürich"
    )

    Column(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
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

        Text(
            text = strings.addressRegQuick,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        quickAddresses.forEach { addr ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onAddressChange(addr)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (address == addr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (address == addr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = if (address == addr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = addr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
        WizardCta(
            if (lang == "en") "Continue" else "Weiter",
            enabled = address.isNotBlank(),
            modifier = Modifier.padding(16.dp),
            onClick = {
                RecyclingRepository.registerAddress(address)
                onNext()
            }
        )
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

    Column(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
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

        Spacer(modifier = Modifier.height(16.dp))
        }
        WizardCta(
            if (lang == "en") "Continue" else "Weiter",
            enabled = selectedDate.isNotEmpty() && selectedTimeSlot.isNotEmpty(),
            modifier = Modifier.padding(16.dp),
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

    Column(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
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

        Spacer(modifier = Modifier.height(16.dp))
        }
        WizardCta(if (lang == "en") "Continue to Checkout" else "Weiter zur Kasse", enabled = selectedMaterials.isNotEmpty(), modifier = Modifier.padding(16.dp), onClick = onNext)
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
    Column(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
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

        Spacer(modifier = Modifier.height(16.dp))
        }
        val ctaText = if (price == 0f) {
            if (lang == "en") "Confirm Order" else "Bestellig bestätige"
        } else {
            if (lang == "en") "Pay with TWINT" else "Zahle mit TWINT"
        }
        WizardCta(ctaText, modifier = Modifier.padding(16.dp), onClick = onNext)
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

/** Great-circle distance in metres between two coordinates. */
private fun distanceMeters(a: LatLng, b: LatLng): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
    return results[0].toDouble()
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

    // Ask for notification permission right when there is an active order —
    // the one moment the value of notifications is obvious to the user.
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* declining just means no tray updates */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !OrderNotifications.hasPermission(context)) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 20000)
        )
    }

    // delay() keeps ticking while the app is backgrounded (unlike the frame-
    // clock animation above), so these notifications reach the tray even when
    // the user switches away mid-delivery.
    LaunchedEffect(Unit) {
        OrderNotifications.notifyStatus(context, lang, statusMessages[0])
        while (statusIndex < statusMessages.size - 1) {
            delay(2500)
            statusIndex++
            if (statusIndex == statusMessages.size - 2) {
                OrderNotifications.notifyStatus(context, lang, statusMessages[statusIndex])
            }
        }
        delay(2500)
        OrderNotifications.notifyStatus(
            context,
            lang,
            if (lang == "en") "I am here! Please place your recycling outside your door."
            else "Ich bi da! Bitte stell dis Recycling vor d'Tür.",
            arrived = true
        )
    }

    val isArrived = progress.value >= 1f

    // Geocode the saved pickup address so the final destination reflects
    // the user's real address; falls back to the Alt-Wiedikon centre
    // (Goldbrunnenplatz) when geocoding is unavailable.
    var geocodedDest by remember { mutableStateOf<LatLng?>(null) }
    LaunchedEffect(address) {
        geocodedDest = withContext(Dispatchers.IO) {
            runCatching {
                if (address.isNotBlank() && Geocoder.isPresent()) {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.GERMAN)
                        .getFromLocationName("$address, Zürich, Switzerland", 1)
                        ?.firstOrNull()
                        ?.let { LatLng(it.latitude, it.longitude) }
                } else null
            }.getOrNull()
        }
    }

    // Real route coordinates in Alt Wiedikon, Zurich:
    // Werkhof Hardau (Origin) -> Seebahnstrasse -> Schmiede Wiedikon -> Wiedikon Station -> real destination
    val routeCoords = remember(geocodedDest) {
        listOf(
            LatLng(47.3820, 8.5135), // Werkhof Hardau (Origin)
            LatLng(47.3795, 8.5144), // Seebahnstrasse North
            LatLng(47.3770, 8.5152), // Seebahnstrasse Mid
            LatLng(47.3735, 8.5165), // Schmiede Wiedikon area
            LatLng(47.3695, 8.5170), // Wiedikon Station area
            LatLng(47.3672, 8.5125)  // Alt Wiedikon junction (Birmensdorferstrasse)
        ) + (geocodedDest ?: LatLng(47.3662, 8.5134)) // real address, else Goldbrunnenplatz
    }

    // Interpolate the vehicle position based on animated progress (0.0 to 1.0)
    val truckPos = remember(progress.value, routeCoords) {
        val progressVal = progress.value.coerceIn(0f, 1f)
        if (progressVal >= 1f) {
            routeCoords.last()
        } else {
            val segmentCount = routeCoords.size - 1
            val exactIndex = progressVal * segmentCount
            val index = exactIndex.toInt()
            val fraction = exactIndex - index
            val startCoord = routeCoords[index]
            val endCoord = routeCoords[index + 1]
            val lat = startCoord.latitude + (endCoord.latitude - startCoord.latitude) * fraction
            val lng = startCoord.longitude + (endCoord.longitude - startCoord.longitude) * fraction
            LatLng(lat, lng)
        }
    }

    // Distance still to drive: rest of the current segment plus every segment
    // after it. ETA assumes ~18 km/h average for city driving with stops.
    val remainingMeters = remember(truckPos, routeCoords) {
        val progressVal = progress.value.coerceIn(0f, 1f)
        val segmentCount = routeCoords.size - 1
        val index = (progressVal * segmentCount).toInt().coerceAtMost(segmentCount - 1)
        var total = distanceMeters(truckPos, routeCoords[index + 1])
        for (i in (index + 1) until segmentCount) {
            total += distanceMeters(routeCoords[i], routeCoords[i + 1])
        }
        total
    }
    val etaMinutes = kotlin.math.ceil(remainingMeters / 300.0).toInt().coerceAtLeast(1)

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
            text = if (lang == "en") "Destination: $address" else "Ziel: $address",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live route map (depot → destination)
        val mapBg = MaterialTheme.colorScheme.surfaceContainerHigh
        val routeColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(mapBg)
        ) {
            val systemDark = isSystemInDarkTheme()
            val themeState by RecyclingRepository.theme.collectAsState()
            val isDarkTheme = themeState == "dark" || (themeState == "system" && systemDark)

            val mapProperties = remember(isDarkTheme) {
                MapProperties(
                    mapType = MapType.NORMAL,
                    mapStyleOptions = if (isDarkTheme) {
                        MapStyleOptions(
                            """
                            [
                              {
                                "elementType": "geometry",
                                "stylers": [
                                  {
                                    "color": "#242f3e"
                                  }
                                ]
                              },
                              {
                                "elementType": "labels.text.fill",
                                "stylers": [
                                  {
                                    "color": "#746855"
                                  }
                                ]
                              },
                              {
                                "elementType": "labels.text.stroke",
                                "stylers": [
                                  {
                                    "color": "#242f3e"
                                  }
                                ]
                              },
                              {
                                "featureType": "administrative.locality",
                                "elementType": "labels.text.fill",
                                "stylers": [
                                  {
                                    "color": "#d59563"
                                  }
                                ]
                              },
                              {
                                "featureType": "poi",
                                "elementType": "labels.text.fill",
                                "stylers": [
                                  {
                                    "color": "#d59563"
                                  }
                                ]
                              },
                              {
                                "featureType": "road",
                                "elementType": "geometry",
                                "stylers": [
                                  {
                                    "color": "#38414e"
                                  }
                                ]
                              },
                              {
                                "featureType": "road",
                                "elementType": "geometry.stroke",
                                "stylers": [
                                  {
                                    "color": "#212a37"
                                  }
                                ]
                              },
                              {
                                "featureType": "road",
                                "elementType": "labels.text.fill",
                                "stylers": [
                                  {
                                    "color": "#9ca5b3"
                                  }
                                ]
                              },
                              {
                                "featureType": "road.highway",
                                "elementType": "geometry",
                                "stylers": [
                                  {
                                    "color": "#746855"
                                  }
                                ]
                              },
                              {
                                "featureType": "water",
                                "elementType": "geometry",
                                "stylers": [
                                  {
                                    "color": "#17263c"
                                  }
                                ]
                              }
                            ]
                            """.trimIndent()
                        )
                    } else null
                )
            }

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(47.3740, 8.5150), 14.5f) // center Wiedikon
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                // Polyline representing the total route (faded)
                Polyline(
                    points = routeCoords,
                    color = routeColor.copy(alpha = 0.35f),
                    width = 10f
                )

                // Polyline representing the traveled portion
                val traveledCoords = remember(truckPos) {
                    val idx = routeCoords.indexOfFirst { it.latitude == truckPos.latitude && it.longitude == truckPos.longitude }
                    if (idx != -1) {
                        routeCoords.take(idx + 1)
                    } else {
                        val totalPoints = routeCoords.size
                        val segmentCount = totalPoints - 1
                        val progressVal = progress.value.coerceIn(0f, 1f)
                        val exactIndex = progressVal * segmentCount
                        val index = exactIndex.toInt()
                        routeCoords.take(index + 1) + truckPos
                    }
                }
                Polyline(
                    points = traveledCoords,
                    color = routeColor,
                    width = 12f
                )

                // Origin Depot Marker
                Marker(
                    state = MarkerState(position = routeCoords.first()),
                    title = if (lang == "en") "Werkhof Hardau" else "ERZ Werkhof Hardau",
                    snippet = if (lang == "en") "Start" else "Abfahrtsort",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                // Destination Home Marker
                Marker(
                    state = MarkerState(position = routeCoords.last()),
                    title = if (lang == "en") "Destination" else "Zieladresse",
                    snippet = address,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )

                // Pulse Effect / Radar Around the Moving Truck
                val pulseRadius = 60.0 * (1.0 + pulseProgress * 1.5)
                val radarAlpha = 1f - pulseProgress
                Circle(
                    center = truckPos,
                    radius = pulseRadius,
                    fillColor = routeColor.copy(alpha = radarAlpha * 0.25f),
                    strokeColor = routeColor.copy(alpha = radarAlpha * 0.45f),
                    strokeWidth = 2f
                )

                // Jöppli Vehicle Marker
                Marker(
                    state = MarkerState(position = truckPos),
                    title = if (lang == "en") "Jöppli vehicle" else "Jöppli-Fahrzeug",
                    snippet = if (isArrived) (if (lang == "en") "Arrived" else "Haustür erreicht") else (if (lang == "en") "Moving..." else "Unterwegs..."),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
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
                    // ETA + distance still to go — grounds the status prose in numbers
                    if (!isArrived) {
                        Text(
                            text = if (remainingMeters >= 1000)
                                String.format(Locale.ROOT, "%d min · %.1f km", etaMinutes, remainingMeters / 1000)
                            else
                                String.format(Locale.ROOT, "%d min · %d m", etaMinutes, remainingMeters.toInt()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
