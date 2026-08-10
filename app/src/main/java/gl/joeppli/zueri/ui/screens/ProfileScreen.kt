package gl.joeppli.zueri.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import gl.joeppli.zueri.data.AuthManager
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import gl.joeppli.zueri.ui.components.PageHeader
import kotlinx.coroutines.launch
import gl.joeppli.zueri.theme.Dimens
import gl.joeppli.zueri.theme.BrandGreen
import gl.joeppli.zueri.theme.BrandBlue
import gl.joeppli.zueri.theme.BrandYellow
import gl.joeppli.zueri.theme.BrandRed
import gl.joeppli.zueri.theme.NeutralDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val profile by RecyclingRepository.userProfile.collectAsState()
    val activeLang by RecyclingRepository.userLanguage.collectAsState()
    val strings = LocalJoeppliStrings.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var name by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var phone by rememberSaveable(profile.phone) { mutableStateOf(profile.phone) }
    var address by rememberSaveable(profile.homeAddress) { mutableStateOf(profile.homeAddress) }

    var supportMessage by rememberSaveable { mutableStateOf("") }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    // Payment method saves on tap, so only the three text fields can go stale.
    val isProfileDirty = name != profile.name ||
        phone != profile.phone ||
        address != profile.homeAddress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens.screenH)
            .padding(top = Dimens.screenTop, bottom = Dimens.screenBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.section)
    ) {
        PageHeader(
            title = if (activeLang == "en") "Profile Settings" else "Profil & Adresse",
            subtitle = if (activeLang == "en") "Your credentials for Jöppli collection" else "Dini Date für automatischi Abholige",
            icon = Icons.Outlined.Person
        )

        // Profile Form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dimens.card,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.gapLg), verticalArrangement = Arrangement.spacedBy(Dimens.gapLg)) {
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
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = Dimens.chip,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.profilePhone) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = Dimens.chip,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.profileAddress) },
                    singleLine = true,
                    shape = Dimens.chip,
                    modifier = Modifier.fillMaxWidth()
                )

                // Nothing else signals that edits are still only local, so the
                // save button stays inert until something actually differs.
                if (isProfileDirty) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (activeLang == "en") "You have unsaved changes"
                            else "Du hesch nöd gspeicherti Änderige",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Button(
                    onClick = {
                        RecyclingRepository.updateProfile(name, phone, address)
                        AuthManager.pushProfileToCloud()
                        Toast.makeText(context, strings.profileSaveToast, Toast.LENGTH_SHORT).show()
                    },
                    enabled = isProfileDirty,
                    shape = Dimens.ctaShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ctaHeight)
                ) {
                    Text(strings.profileSave, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Language settings card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dimens.card,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.gapLg)) {
                Text(
                    text = strings.profileLangTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Dimens.gapMd))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)) {
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

        // Theme settings card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dimens.card,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.gapLg)) {
                val currentTheme by RecyclingRepository.theme.collectAsState()
                Text(
                    text = if (activeLang == "en") "App Color Theme" else "App-Farbschema",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Dimens.gapMd))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                ) {
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Blue" else "Blau",
                        color = BrandBlue,
                        selected = currentTheme == "blue",
                        onClick = { RecyclingRepository.setTheme("blue") },
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                ) {
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Yellow" else "Gelb",
                        color = BrandYellow,
                        selected = currentTheme == "yellow",
                        onClick = { RecyclingRepository.setTheme("yellow") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = if (activeLang == "en") "Dark" else "Dunkel",
                        color = NeutralDark,
                        selected = currentTheme == "dark",
                        onClick = { RecyclingRepository.setTheme("dark") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ERZ Feedback/Contact support form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dimens.card,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.gapLg), verticalArrangement = Arrangement.spacedBy(Dimens.gapMd)) {
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
                    shape = Dimens.chip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (supportMessage.isBlank()) {
                            Toast.makeText(context, strings.profileSupportError, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@joeppli.gl"))
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                if (activeLang == "en") "Jöppli Support Request" else "Jöppli Support-Aafrog"
                            )
                            putExtra(Intent.EXTRA_TEXT, supportMessage)
                        }
                        try {
                            context.startActivity(
                                Intent.createChooser(
                                    emailIntent,
                                    if (activeLang == "en") "Send email" else "E-Mail sände"
                                )
                            )
                            supportMessage = ""
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                if (activeLang == "en") "No email app found" else "Kei E-Mail-App gfunde",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = Dimens.ctaShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ctaHeight)
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

        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = Dimens.ctaShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ctaHeight)
        ) {
            Text(strings.profileLogout, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onError)
        }
    }

    // Logging out ends the session and returns to the auth screen — confirm
    // first so a mis-tap under the support form can't drop the user out.
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(strings.profileLogout) },
            text = {
                Text(
                    if (activeLang == "en") "You'll be signed out and returned to the login screen."
                    else "Du wirsch abgmäldet und chunnsch zrugg zum Login."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    // Reset local state immediately (navigates to the auth screen),
                    // then end the Firebase session and clear the saved credential.
                    RecyclingRepository.logout()
                    scope.launch { AuthManager.signOut(context) }
                    Toast.makeText(context, strings.profileLogoutToast, Toast.LENGTH_SHORT).show()
                }) {
                    Text(strings.profileLogout, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (activeLang == "en") "Cancel" else "Abbräche")
                }
            }
        )
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
        shape = Dimens.chip,
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
