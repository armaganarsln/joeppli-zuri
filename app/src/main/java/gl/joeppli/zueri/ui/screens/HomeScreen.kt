package gl.joeppli.zueri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.theme.Dimens
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import gl.joeppli.zueri.ui.components.PageHeader

@Composable
fun HomeScreen(
    onNavigateToTab: (String) -> Unit,
    onQuickPickupClick: () -> Unit,
    onTrackPickup: () -> Unit
) {
    val stats by RecyclingRepository.stats.collectAsState()
    val profile by RecyclingRepository.userProfile.collectAsState()
    val lastPickup by RecyclingRepository.lastPickup.collectAsState()
    val scrollState = rememberScrollState()
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()

    val firstName = if (profile.name.isBlank()) {
        if (lang == "en") "Recycler" else "Sammler"
    } else {
        profile.name.substringBefore(' ')
    }

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
            title = strings.greeting.format(firstName),
            subtitle = strings.readyToRecycle
        )

        // Mini Stats Dashboard Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapMd)
        ) {
            MiniStatCard(
                title = strings.statKarma,
                value = "${stats.karma}/100",
                icon = Icons.Filled.Star,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { onNavigateToTab("STATS") },
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                title = strings.statCo2,
                value = "-${stats.co2Saved.toInt()} kg",
                icon = Icons.Filled.Favorite,
                iconColor = Color(0xFFE11D48), // semantic CO2/heart accent, intentionally fixed across themes
                onClick = { onNavigateToTab("STATS") },
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Summon Jöppli Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = Dimens.cardHero,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onQuickPickupClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.section),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapLg),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = strings.quickPickup,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column {
                        Text(
                            text = strings.quickPickup,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = strings.quickPickupDesc,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Active / last pickup — lets the user return to the live tracker
        lastPickup?.let { pickup ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = Dimens.card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.gapLg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (lang == "en") "Your last pickup" else "Dini letschti Abholig",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapXs)) {
                        Text(
                            text = pickup.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${pickup.dateString} · ${pickup.timeSlot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Requested" else "Aagfrogt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Button(
                            onClick = onTrackPickup,
                            shape = Dimens.ctaShape
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Dimens.gapSm))
                            Text(if (lang == "en") "Track" else "Verfolge")
                        }
                    }
                }
            }
        }

        // Menu Options List
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.gapMd)
        ) {
            MenuRowCard(
                title = if (lang == "en") "Order Jöppli" else "Jöppli bestellen",
                subtitle = if (lang == "en") "Schedule autonomous collection" else "Autonomi Abholig planä",
                icon = Icons.Outlined.LocalShipping,
                onClick = { onNavigateToTab("ORDER") }
            )
            MenuRowCard(
                title = if (lang == "en") "What can I recycle?" else "Was chan ich recycle?",
                subtitle = if (lang == "en") "Disposal guide & scanner demo" else "Entsorgigs-Wegwiiser & Scanner-Demo",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                onClick = { onNavigateToTab("GUIDE") }
            )
            MenuRowCard(
                title = if (lang == "en") "My Profile" else "Mini Date",
                subtitle = if (lang == "en") "Address & TWINT" else "Adresse & TWINT",
                icon = Icons.Outlined.ManageAccounts,
                onClick = { onNavigateToTab("PROFILE") }
            )
        }
    }
}

@Composable
fun MiniStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = Dimens.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.gapLg)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapMd)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.12f), Dimens.chip),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun MenuRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = Dimens.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.gapLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapLg),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            Dimens.chip
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
