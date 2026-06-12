package gl.joeppli.zueri.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import gl.joeppli.zueri.theme.BrandGreen
import gl.joeppli.zueri.theme.BrandBlue
import gl.joeppli.zueri.theme.BrandYellow
import gl.joeppli.zueri.theme.BrandRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val profile by RecyclingRepository.userProfile.collectAsState()
    val activeLang by RecyclingRepository.userLanguage.collectAsState()
    val strings = LocalJoeppliStrings.current
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

        // Header with avatar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = if (activeLang == "en") "Profile Settings" else "Profil & Adresse",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (activeLang == "en") "Your credentials for Jöppli collection" else "Dini Date für automatischi Abholige",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                    text = strings.profileContact,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.profileName) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.profilePhone) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.profileAddress) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        RecyclingRepository.updateProfile(name, phone, address, selectedPayment)
                        Toast.makeText(context, strings.profileSaveToast, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(strings.profileSave, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language settings card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.profileLangTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isDe = activeLang == "de"
                    FilterChip(
                        selected = isDe,
                        onClick = { RecyclingRepository.setLanguage("de") },
                        label = { Text("Deutsch (CH) 🇨🇭") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isDe,
                        onClick = { RecyclingRepository.setLanguage("en") },
                        label = { Text("English 🇬🇧") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme settings card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val currentTheme by RecyclingRepository.theme.collectAsState()
                Text(
                    text = if (activeLang == "en") "App Color Theme" else "App-Farbschema",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Green" else "Grü",
                        color = BrandGreen,
                        selected = currentTheme == "green",
                        onClick = { RecyclingRepository.setTheme("green") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Blue" else "Blau",
                        color = BrandBlue,
                        selected = currentTheme == "blue",
                        onClick = { RecyclingRepository.setTheme("blue") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Yellow" else "Gälb",
                        color = BrandYellow,
                        selected = currentTheme == "yellow",
                        onClick = { RecyclingRepository.setTheme("yellow") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Red" else "Rot",
                        color = BrandRed,
                        selected = currentTheme == "red",
                        onClick = { RecyclingRepository.setTheme("red") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment method
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.profilePayment,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
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
                        label = { Text(if (activeLang == "en") "Credit Card" else "Kreditkarte") },
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
                    text = strings.profileSupportTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = strings.profileSupportSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = supportMessage,
                    onValueChange = { supportMessage = it },
                    label = { Text(strings.profileSupportMsg) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (supportMessage.isNotBlank()) {
                            Toast.makeText(context, strings.profileSupportToast, Toast.LENGTH_SHORT).show()
                            supportMessage = ""
                        } else {
                            Toast.makeText(context, strings.profileSupportError, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.profileSupportSend, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                RecyclingRepository.logout()
                Toast.makeText(context, strings.profileLogoutToast, Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(strings.profileLogout, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) color else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(44.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
